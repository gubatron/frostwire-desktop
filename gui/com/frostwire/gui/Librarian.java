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

package com.frostwire.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.frostwire.content.ContentResolver;
import com.frostwire.content.Context;
import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.core.providers.ShareFilesDB;
import com.frostwire.core.providers.ShareFilesDB.Columns;
import com.frostwire.core.providers.TableFetcher;
import com.frostwire.core.providers.TableFetchers;
import com.frostwire.database.Cursor;
import com.frostwire.gui.bittorrent.TorrentUtil;
import com.frostwire.gui.library.Finger;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class Librarian {

    private static final Logger LOG = Logger.getLogger(Librarian.class.getName());

    public static final int FILE_STATE_UNSHARED = 0;
    public static final int FILE_STATE_SHARING = 1;
    public static final int FILE_STATE_SHARED = 2;

    private final Context context;

    private static final Librarian instance = new Librarian();

    public static Librarian instance() {
        return instance;
    }

    private Librarian() {
        context = new Context();
    }

    public Finger finger() {
        Finger finger = new Finger();

        finger.uuid = ConfigurationManager.instance().getUUIDString();
        finger.nickname = ConfigurationManager.instance().getNickname();
        finger.frostwireVersion = Constants.FROSTWIRE_VERSION_STRING;
        finger.totalShared = getNumSharedFiles();

        DeviceInfo di = new DeviceInfo();
        finger.deviceVersion = di.getVersion();
        finger.deviceModel = di.getModel();
        finger.deviceProduct = di.getProduct();
        finger.deviceName = di.getName();
        finger.deviceManufacturer = di.getManufacturer();
        finger.deviceBrand = di.getBrand();
        finger.deviceScreen = di.getScreenMetrics();

        finger.numSharedAudioFiles = getNumSharedFiles(Constants.FILE_TYPE_AUDIO);
        finger.numSharedVideoFiles = getNumSharedFiles(Constants.FILE_TYPE_VIDEOS);
        finger.numSharedPictureFiles = getNumSharedFiles(Constants.FILE_TYPE_PICTURES);
        finger.numSharedDocumentFiles = getNumSharedFiles(Constants.FILE_TYPE_DOCUMENTS);
        finger.numSharedApplicationFiles = getNumSharedFiles(Constants.FILE_TYPE_APPLICATIONS);
        finger.numSharedRingtoneFiles = getNumSharedFiles(Constants.FILE_TYPE_RINGTONES);

        return finger;
    }

    public int getNumSharedFiles() {
        int result = 0;

        for (byte i = 0; i < 6; i++) {
            result += getNumSharedFiles(i);
        }

        return result;
    }

    /**
     * 
     * @param fileType
     * @param onlyShared - If false, forces getting all files, shared or unshared. 
     * @return
     */
    public int getNumSharedFiles(byte fileType) {
        Cursor c = null;

        int numFiles = 0;

        try {
            ShareFilesDB db = ShareFilesDB.intance();

            String[] columns = new String[] { Columns.ID, Columns.FILE_PATH };
            String where = Columns.FILE_TYPE + " = ?";
            String[] whereArgs = new String[] { String.valueOf(fileType) };

            c = db.query(columns, where, whereArgs, null);

            List<FileDescriptor> fds = filteredOutBadRows(c);

            numFiles = fds.size();

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get num of shared files", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return numFiles;
    }

    private List<FileDescriptor> filteredOutBadRows(Cursor c) {
        int filePathCol = c.getColumnIndex(Columns.FILE_PATH);

        if (filePathCol == -1) {
            throw new IllegalArgumentException("Can't perform filtering without file path column in cursor");
        }

        List<FileDescriptor> fds = new LinkedList<FileDescriptor>();

        int num = c.getCount();

        for (int i = 0; i < num; i++) {
            String filePath = c.getString(filePathCol);

            if (!(new File(filePath)).exists()) {

            }
            
            FileDescriptor fd = new FileDescriptor();
            
            
            fds.add(fd);
        }

        return fds;
    }

    public List<FileDescriptor> getFiles(byte fileType, int offset, int pageSize, boolean sharedOnly) {
        return getFiles(offset, pageSize, TableFetchers.getFetcher(fileType), sharedOnly);
    }

    public void scan(File file) {
        scan(file, TorrentUtil.getIgnorableFiles());
    }

    public int getFileShareState(String path) {
        return FILE_STATE_UNSHARED;
    }

    private void scan(File file, Set<File> ignorableFiles) {
        if (ignorableFiles.contains(file)) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (child.isDirectory() || child.isFile()) {
                    scan(child);
                }
            }
        } else if (file.isFile()) {
            new UniversalScanner(context).scan(file.getAbsolutePath());
        }
    }

    private List<FileDescriptor> getFiles(int offset, int pageSize, TableFetcher fetcher, boolean sharedOnly) {
        return getFiles(offset, pageSize, fetcher, null, null, sharedOnly);
    }

    private List<FileDescriptor> getFiles(int offset, int pageSize, TableFetcher fetcher, String where, String[] whereArgs, boolean sharedOnly) {
        List<FileDescriptor> result = new ArrayList<FileDescriptor>();

        Cursor c = null;
        //Set<Integer> sharedIds = getSharedFiles(fetcher.getFileType());

        try {

            ContentResolver cr = context.getContentResolver();

            String[] columns = fetcher.getColumns();
            String sort = fetcher.getSortByExpression();

            c = cr.query(fetcher.getContentUri(), columns, where, whereArgs, sort);

            if (c == null || !c.moveToPosition(offset)) {
                return result;
            }

            fetcher.prepare(c);

            int count = 1;

            do {
                FileDescriptor fd = fetcher.fetch(c);

                fd.shared = true;//sharedIds.contains(fd.id);

                if (sharedOnly && !fd.shared) {
                    continue;
                }

                result.add(fd);

            } while (c.moveToNext() && count++ < pageSize);

        } catch (Throwable e) {
            LOG.log(Level.WARNING, "General failure getting files", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    public void shareFile(String path, boolean share) {
        // TODO Auto-generated method stub

    }
}
