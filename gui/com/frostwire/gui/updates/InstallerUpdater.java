/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.updates;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerPiece;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerListener;
import org.gudy.azureus2.core3.download.DownloadManagerStats;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.limewire.util.CommonUtils;
import org.limewire.util.FilenameUtils;
import org.limewire.util.OSUtils;

import com.frostwire.AzureusStarter;
import com.frostwire.HttpFetcher;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.UpdateSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class InstallerUpdater implements Runnable, DownloadManagerListener {

    private static final Log LOG = LogFactory.getLog(InstallerUpdater.class);

    private DownloadManager _manager = null;
    private UpdateMessage _updateMessage;
    private File _executableFile;

    public InstallerUpdater(UpdateMessage updateMessage) {
        _updateMessage = updateMessage;
    }

    public void start() {
        new Thread(this, "InstallerUpdater").start();
    }

    public void run() {
        if (!UpdateSettings.AUTOMATIC_INSTALLER_DOWNLOAD.getValue()) {
            return;
        }

        if (checkIfDownloaded()) {
            showUpdateMessage();
        } else {
            if (_updateMessage.getTorrent() != null) {
                handleTorrentDownload();
            } else if (_updateMessage.getInstallerUrl() != null) {
                handleHttpDownload();
            }
        }
    }

    private void handleTorrentDownload() {
        File torrentFileLocation = downloadDotTorrent();

        try {

            if (torrentFileLocation != null && torrentFileLocation.exists()) {
                _manager = startTorrentDownload(torrentFileLocation.getAbsolutePath(), UpdateSettings.UPDATES_DIR.getAbsolutePath(), this);
            }

        } catch (Throwable e) {
            LOG.error("Error starting update torrent download", e);
        }
    }

    private void handleHttpDownload() {
        File updateFolder = UpdateSettings.UPDATES_DIR;

        int index = _updateMessage.getInstallerUrl().lastIndexOf('/');
        File installerFileLocation = new File(updateFolder, _updateMessage.getInstallerUrl().substring(index + 1));

        if (!updateFolder.exists()) {
            updateFolder.mkdir();
            updateFolder.setWritable(true);
        }
        try {
            new HttpFetcher(new URI(_updateMessage.getInstallerUrl())).save(installerFileLocation);
            saveMetaData();
            cleanupOldUpdates();

            if (checkIfDownloaded()) {
                showUpdateMessage();
            }
        } catch (Throwable e) {
            LOG.error("Failed to download installer: " + _updateMessage.getInstallerUrl(), e);
        }
    }

    private final DownloadManager startTorrentDownload(String torrentFile, String saveDataPath, DownloadManagerListener listener) throws Exception {

        DownloadManager manager = AzureusStarter.getAzureusCore().getGlobalManager().addDownloadManager(torrentFile, saveDataPath);
        manager.addListener(listener);

        manager.initialize();

        return manager;
    }

    private void showUpdateMessage() {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                if (_executableFile == null) {
                    return;
                }

                int result = JOptionPane.showConfirmDialog(null, _updateMessage.getMessageInstallerReady(), I18n.tr("Update"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    try {
                        if (OSUtils.isWindows()) {
                            String[] commands = new String[] { "CMD.EXE", "/C", _executableFile.getAbsolutePath() };

                            ProcessBuilder pbuilder = new ProcessBuilder(commands);
                            pbuilder.start();
                        } else if (OSUtils.isLinux() && OSUtils.isUbuntu()) {
                            String[] commands = new String[] { "gdebi-gtk", _executableFile.getAbsolutePath() };

                            ProcessBuilder pbuilder = new ProcessBuilder(commands);
                            pbuilder.start();
                        } else if (OSUtils.isMacOSX()) {
                            String[] mountCommand = new String[] { "hdiutil", "attach", _executableFile.getAbsolutePath() };

                            String[] finderShowCommand = new String[] { "open", "/Volumes/" + FilenameUtils.getBaseName(_executableFile.getName()) };

                            ProcessBuilder pbuilder = new ProcessBuilder(mountCommand);
                            Process mountingProcess = pbuilder.start();

                            mountingProcess.waitFor();

                            pbuilder = new ProcessBuilder(finderShowCommand);
                            pbuilder.start();
                        }

                        GUIMediator.shutdown();
                    } catch (Throwable e) {
                        LOG.error("Unable to launch new installer", e);
                    }
                }
            }
        });
    }

    private File downloadDotTorrent() {

        File appSpecialShareFolder = UpdateSettings.UPDATES_DIR;

        int index = _updateMessage.getTorrent().lastIndexOf('/');
        File torrentFileLocation = new File(appSpecialShareFolder, _updateMessage.getTorrent().substring(index + 1));

        if (!appSpecialShareFolder.exists()) {
            appSpecialShareFolder.mkdir();
            appSpecialShareFolder.setWritable(true);
        }

        //We always re-download the torrent just in case.
        try {
            downloadTorrentFile(_updateMessage.getTorrent(), torrentFileLocation);
        } catch (Throwable e) {
            LOG.error("Error downloading update torrent file", e);
        }

        return torrentFileLocation;
    }

    private final InstallerMetaData getLastInstallerMetaData() {
        InstallerMetaData result = null;
        try {
            File installerDatFile = new File(getInstallerDatPath());

            if (!installerDatFile.exists()) {
                return null;
            }

            FileInputStream fis = new FileInputStream(installerDatFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            result = (InstallerMetaData) ois.readObject();

            if (result == null) {
                return null;
            }

            fis.close();

            return result;

        } catch (Throwable e) {
            // processMessage will deal with us returning null
            LOG.info("Can't read installer meta data");
            return null;
        }
    }

    private boolean checkIfDownloaded() {

        InstallerMetaData md = getLastInstallerMetaData();

        if (md == null) {
            return false;
        }

        if (!md.frostwireVersion.equals(_updateMessage.getVersion())) {
            return false;
        }

        String installerFilename = null;

        if (_updateMessage.getTorrent() != null) {
            int indx1 = _updateMessage.getTorrent().lastIndexOf('/') + 1;
            int indx2 = _updateMessage.getTorrent().lastIndexOf(".torrent");

            installerFilename = _updateMessage.getTorrent().substring(indx1, indx2);
        } else if (_updateMessage.getInstallerUrl() != null) {
            int indx1 = _updateMessage.getInstallerUrl().lastIndexOf('/') + 1;

            installerFilename = _updateMessage.getInstallerUrl().substring(indx1);
        }

        File f = new File(UpdateSettings.UPDATES_DIR, installerFilename);

        if (installerFilename == null || !f.exists()) {
            return false;
        }

        _executableFile = f;

        try {
            return checkMD5(f, _updateMessage.getRemoteMD5());
        } catch (Throwable e) {
            LOG.error("Error checking update MD5", e);
            return false;
        }
    }

    @Override
    public void completionChanged(DownloadManager manager, boolean bCompleted) {
    }

    @Override
    public void filePriorityChanged(DownloadManager download, DiskManagerFileInfo file) {
    }

    @Override
    public void positionChanged(DownloadManager download, int oldPosition, int newPosition) {
    }

    @Override
    public void stateChanged(DownloadManager manager, int state) {
        if (_manager == null && manager != null) {
            _manager = manager;
        }

        printDiskManagerPieces(manager.getDiskManager());
        printDownloadManagerStatus(manager);

        if (torrentDataDownloadedToDisk()) {
            return;
        }

        System.out.println("InstallerUpdater.stateChanged() - " + state + " completed: " + manager.isDownloadComplete(false));
        if (state == DownloadManager.STATE_SEEDING) {
            System.out.println("InstallerUpdater.stateChanged() - SEEDING!");
            return;
        }

        if (state == DownloadManager.STATE_ERROR) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(_manager.getErrorDetails());
            System.out.println("InstallerUpdater: ERROR - stopIt, startDownload!");
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //try to restart the download. delete torrent and data
            //manager.stopIt(DownloadManager.STATE_READY, false, true);
            try {
                AzureusStarter.getAzureusCore().getGlobalManager().removeDownloadManager(manager, false, true);
                //processMessage(_updateMessage);
            } catch (GlobalManagerDownloadRemovalVetoException e) {
                LOG.error("Error removing download manager on error", e);
            }

        } else if (state == DownloadManager.STATE_DOWNLOADING) {
            System.out.println("stateChanged(STATE_DOWNLOADING)");
        } else if (state == DownloadManager.STATE_READY) {
            System.out.println("stateChanged(STATE_READY)");
            manager.startDownload();
        }
    }

    @Override
    public void downloadComplete(DownloadManager manager) {
        System.out.println("InstallerUpdater.downloadComplete()!!!!");
        printDownloadManagerStatus(_manager);

        saveMetaData();
        cleanupOldUpdates();

        if (checkIfDownloaded()) {
            showUpdateMessage();
        }
    }

    private void cleanupOldUpdates() {

        final Pattern p = Pattern.compile("^frostwire-([0-9]+[0-9]?\\.[0-9]+[0-9]?\\.[0-9]+[0-9]?)(.*?)(\\.torrent)?$");

        for (File f : UpdateSettings.UPDATES_DIR.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

                Matcher m = p.matcher(name);

                if (m.matches()) {
                    return !m.group(1).equals(_updateMessage.getVersion());
                }

                return false;
            }
        })) {

            f.delete();
        }
    }

    private final String getInstallerDatPath() {
        return CommonUtils.getUserSettingsDir().getAbsolutePath() + File.separator + "installer.dat";
    }

    private void saveMetaData() {
        try {
            String installerPath = getInstallerDatPath();

            InstallerMetaData md = new InstallerMetaData();
            md.frostwireVersion = _updateMessage.getVersion();

            File f = new File(installerPath);

            if (f.exists()) {
                f.delete();
            }

            f.createNewFile();

            FileOutputStream fos = new FileOutputStream(installerPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject((InstallerMetaData) md);

            fos.close();

        } catch (Throwable e) {
            LOG.info("Can't save update meta data");
        }
    }

    private boolean torrentDataDownloadedToDisk() {
        if (_manager == null || _manager.getDiskManager() == null) {
            return false;
        }

        String saveLocation = UpdateSettings.UPDATES_DIR.getAbsolutePath();
        File f = new File(saveLocation);

        DiskManager dm = _manager.getDiskManager();
        //boolean filesExist = dm.filesExist();		
        int percentDone = dm.getPercentDone();
        long totalLength = dm.getTotalLength();
        int rechecking = dm.getCompleteRecheckStatus();

        return f.exists() && f.length() == totalLength && percentDone == 1000 && rechecking == -1;
    }

    public static void printDiskManagerPieces(DiskManager dm) {
        if (dm == null) {
            return;
        }
        DiskManagerPiece[] pieces = dm.getPieces();
        for (DiskManagerPiece piece : pieces) {
            System.out.print(piece.isDone() ? "1" : "0");
        }

        System.out.println();
    }

    public static void printDownloadManagerStatus(DownloadManager manager) {
        if (manager == null) {
            return;
        }

        StringBuffer buf = new StringBuffer();
        buf.append(" Completed:");

        DownloadManagerStats stats = manager.getStats();

        int completed = stats.getCompleted();
        buf.append(completed / 10);
        buf.append('.');
        buf.append(completed % 10);
        buf.append('%');
        buf.append(" Seeds:");
        buf.append(manager.getNbSeeds());
        buf.append(" Peers:");
        buf.append(manager.getNbPeers());
        buf.append(" Downloaded:");
        buf.append(DisplayFormatters.formatDownloaded(stats));
        buf.append(" Uploaded:");
        buf.append(DisplayFormatters.formatByteCountToKiBEtc(stats.getTotalDataBytesSent()));
        buf.append(" DSpeed:");
        buf.append(DisplayFormatters.formatByteCountToKiBEtcPerSec(stats.getDataReceiveRate()));
        buf.append(" USpeed:");
        buf.append(DisplayFormatters.formatByteCountToKiBEtcPerSec(stats.getDataSendRate()));
        buf.append(" TrackerStatus:");
        buf.append(manager.getTrackerStatus());
        while (buf.length() < 80) {
            buf.append(' ');
        }

        buf.append(" TO:");
        buf.append(manager.getSaveLocation().getAbsolutePath());

        System.out.println(buf.toString());
    }

    /**
     * Returns true if the MD5 of the file corresponds to the given MD5 string.
     * It works with lowercase or uppercase, you don't need to worry about that.
     * 
     * @param f
     * @param expectedMD5
     * @return
     * @throws Exception
     */
    public final static boolean checkMD5(File f, String expectedMD5) throws Exception {
        if (expectedMD5 == null) {
            throw new Exception("Expected MD5 is null");
        }

        if (expectedMD5.length() != 32) {
            throw new Exception("Invalid Expected MD5, not 32 chars long");
        }

        return getMD5(f).trim().equalsIgnoreCase(expectedMD5.trim());
    }

    public final static String getMD5(File f) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");

        //We read the file in buffers so we don't
        //eat all the memory in case we have a huge plugin.
        byte[] buf = new byte[65536];
        int num_read;

        InputStream in = new BufferedInputStream(new FileInputStream(f));

        while ((num_read = in.read(buf)) != -1) {
            m.update(buf, 0, num_read);
        }

        in.close();

        String result = new BigInteger(1, m.digest()).toString(16);

        //pad with zeros if until it's 32 chars long.
        if (result.length() < 32) {
            int paddingSize = 32 - result.length();
            for (int i = 0; i < paddingSize; i++) {
                result = "0" + result;
            }
        }

        System.out.println("MD5: " + result);
        return result;
    }

    public final static void downloadTorrentFile(String torrentURL, File saveLocation) throws IOException, URISyntaxException {
        byte[] contents = new HttpFetcher(new URI(torrentURL)).fetch();

        // save the torrent locally if you have to
        if (saveLocation != null && contents != null && contents.length > 0) {

            if (saveLocation.exists()) {
                saveLocation.delete();
            }

            //Create all the route necessary to save the .torrent file if it does not exit.
            saveLocation.getParentFile().mkdirs();
            saveLocation.createNewFile();
            saveLocation.setWritable(true);

            FileOutputStream fos = new FileOutputStream(saveLocation, false);
            fos.write(contents);
            fos.flush();
            fos.close();
        }
    } //downloadTorrentFile

    public static class InstallerMetaData implements Serializable {

        private static final long serialVersionUID = -2309399378691373445L;

        /**
         * Version coming from the update message
         */
        public String frostwireVersion;
    }
}
