/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.limegroup.gnutella.gui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.util.CommonUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.frostwire.gui.bittorrent.TorrentUtil;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesImportSettings;
import com.limegroup.gnutella.settings.iTunesSettings;

/**
 * Handles sending completed downloads into iTunes.
 */
public final class iTunesMediator {

    private static final Log LOG = LogFactory.getLog(iTunesMediator.class);

    private static final String JS_IMPORT_SCRIPT_NAME = "itunes_import.js";

    private static final String JS_REMOVE_PLAYLIST_SCRIPT_NAME = "itunes_remove_playlist.js";

    private static iTunesMediator INSTANCE;

    /**
     * The queue that will process the tunes to add.
     */
    private final ExecutorService QUEUE = ExecutorsHelper.newProcessingQueue("iTunesAdderThread");

    /**
     * Returns the sole instance of this class.
     */
    public static synchronized iTunesMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new iTunesMediator();
        }
        return INSTANCE;
    }

    /**
     * Initializes iTunesMediator with the script file.
     */
    private iTunesMediator() {
        if (OSUtils.isWindows()) {
            createiTunesJavaScript(JS_IMPORT_SCRIPT_NAME);
            createiTunesJavaScript(JS_REMOVE_PLAYLIST_SCRIPT_NAME);
        }
    }

    /**
     * If running on OSX, iTunes integration is enabled and the downloaded file
     * is a supported type, send it to iTunes.
     */
    private void addSongsITunes(String playlist, File file) {

        // Make sure we convert any uppercase to lowercase or vice versa.
        try {
            file = FileUtils.getCanonicalFile(file);
        } catch (IOException ignored) {
        }

        // Verify that we're adding a real file.
        if (!file.exists()) {
            LOG.warn("File: '" + file + "' does not exist");
            return;
        }

        File[] files;
        if (file.isDirectory()) {
            files = FileUtils.getFilesRecursive(file, iTunesSettings.ITUNES_SUPPORTED_FILE_TYPES.getValue());
        } else if (file.isFile() && isSupported(FileUtils.getFileExtension(file))) {
            files = new File[] { file };
        } else {
            return;
        }

        if (files.length == 0) {
            return;
        }

        addSongsiTunes(playlist, files);
    }

    public void addSongsiTunes(String playlist, File[] files) {
        //remove incomplete files from files.
        Set<File> incompleteFiles = TorrentUtil.getIncompleteFiles();
        incompleteFiles.addAll(TorrentUtil.getSkipedFiles());

        List<File> completeFiles = new ArrayList<File>(files.length);
        for (File f : files) {
            if (incompleteFiles.contains(f)) {
                continue;
            }

            if (f.exists() && f.isFile() && isSupported(FileUtils.getFileExtension(f))) {
                completeFiles.add(f);
            }
        }

        files = completeFiles.toArray(new File[0]);

        if (files.length == 0) {
            return;
        }

        if (OSUtils.isMacOSX()) {
            QUEUE.execute(new ExecOSAScriptCommand(playlist, files));
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Will add '" + files.length + " files" + "' to Playlist");
            }
            QUEUE.execute(new ExecWSHScriptCommand(playlist, files));
        }
    }

    /**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupported(String extension) {
        if (extension == null) {
            return false;
        }

        String[] types = iTunesSettings.ITUNES_SUPPORTED_FILE_TYPES.getValue();
        for (int i = 0; i < types.length; i++) {
            if (extension.equalsIgnoreCase(types[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs and returns a osascript command.
     */
    private static String[] createOSAScriptCommand(String playlist, File[] files) {
        List<String> command = new ArrayList<String>();
        command.add("osascript");
        command.add("-e");
        command.add("tell application \"Finder\"");

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            String path = f.getAbsolutePath();
            command.add("-e");
            command.add("set hfsFile" + i + " to (POSIX file \"" + path + "\")");
        }

        command.add("-e");
        command.add("set thePlaylist to \"" + playlist + "\"");
        command.add("-e");
        command.add("tell application \"iTunes\"");
        command.add("-e");
        command.add("launch");
        command.add("-e");
        command.add("if not (exists playlist thePlaylist) then");
        command.add("-e");
        command.add("set thisPlaylist to make new playlist");
        command.add("-e");
        command.add("set name of thisPlaylist to thePlaylist");
        command.add("-e");
        command.add("end if");

        for (int i = 0; i < files.length; i++) {
            command.add("-e");
            command.add("add hfsFile" + i + " to playlist thePlaylist");
        }

        command.add("-e");
        command.add("end tell");
        command.add("-e");
        command.add("end tell");

        return command.toArray(new String[0]);
    }

    private static String[] createWSHScriptCommand(String playlist, File[] files) {
        ArrayList<String> command = new ArrayList<String>();
        command.add("wscript");
        command.add("//B");
        command.add("//NoLogo");
        command.add(new File(CommonUtils.getUserSettingsDir(), JS_IMPORT_SCRIPT_NAME).getAbsolutePath());
        command.add(playlist);
        for (File file : files) {
            command.add(file.getAbsolutePath());
        }

        return command.toArray(new String[0]);
    }

    /**
     * Executes the osascript CLI command
     */
    private class ExecOSAScriptCommand implements Runnable {

        private final String playlist;

        /**
         * The file to add.
         */
        private final File[] files;

        /**
         * Constructs a new ExecOSAScriptCommand for the specified file.
         */
        public ExecOSAScriptCommand(String playlist, File[] files) {
            this.playlist = playlist;
            this.files = files;
        }

        /**
         * Runs the osascript command
         */
        public void run() {
            try {
                Runtime.getRuntime().exec(createOSAScriptCommand(playlist, files));
            } catch (IOException e) {
                LOG.debug(e);
            }
        }
    }

    private class ExecWSHScriptCommand implements Runnable {

        private final String playlist;

        /**
         * The file to add.
         */
        private final File[] files;

        /**
         * Constructs a new ExecOSAScriptCommand for the specified file.
         */
        public ExecWSHScriptCommand(String playlist, File[] files) {
            this.playlist = playlist;
            this.files = files;
        }

        /**
         * Runs the osascript command
         */
        public void run() {
            try {
                Runtime.getRuntime().exec(createWSHScriptCommand(playlist, files));
            } catch (IOException e) {
                LOG.debug(e);
            }
        }
    }

    public void scanForSongs(File file) {
        scanForSongs(iTunesSettings.ITUNES_PLAYLIST.getValue(), file);
    }

    private void scanForSongs(String playlist, File file) {
        iTunesImportSettings.IMPORT_FILES.add(file);
        if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
            addSongsITunes(playlist, file);
        } else if (OSUtils.isUbuntu()) {
            //System.out.println("Import in Banshee: " + file);
        }
    }

    public boolean isScanned(File file) {
        return iTunesImportSettings.IMPORT_FILES.contains(file);
    }

    public void removeFromScanned(File file) {
        iTunesImportSettings.IMPORT_FILES.remove(file);
    }

    private static void createiTunesJavaScript(String scriptName) {
        File fileJS = new File(CommonUtils.getUserSettingsDir(), scriptName);
        if (fileJS.exists()) {
            return;
        }

        URL url = ResourceManager.getURLResource(scriptName);

        InputStream is = null;
        OutputStream out = null;

        try {
            if (url != null) {
                is = new BufferedInputStream(url.openStream());
                out = new FileOutputStream(fileJS);
                IOUtils.copy(is, out);
            }
        } catch (IOException e) {
            LOG.error("Error creating iTunes javascript", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }

    public void deleteFrostWirePlaylist() {
        String playlistName = iTunesSettings.ITUNES_PLAYLIST.getValue();

        try {
            if (OSUtils.isMacOSX()) {
                String[] command = new String[] { "osascript", "-e", "tell application \"iTunes\"", "-e", "delete playlist \"" + playlistName + "\"", "-e", "end tell" };

                Runtime.getRuntime().exec(command);
            } else if (OSUtils.isWindows()) {
                ArrayList<String> command = new ArrayList<String>();
                command.add("wscript");
                command.add("//B");
                command.add("//NoLogo");
                command.add(new File(CommonUtils.getUserSettingsDir(), JS_REMOVE_PLAYLIST_SCRIPT_NAME).getAbsolutePath());
                command.add(playlistName);
                
                Runtime.getRuntime().exec(command.toArray(new String[0]));
            }
        } catch (IOException e) {
            LOG.error("Error executing itunes command", e);
        }
    }

    public void resetFrostWirePlaylist() {
        deleteFrostWirePlaylist();

        QUEUE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                iTunesMediator.instance().scanForSongs(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue());
            }
        });
    }
}
