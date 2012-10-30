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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.frostwire.content.ContentResolver;
import com.frostwire.content.Context;
import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.core.providers.BaseColumns;
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

    public Finger finger(boolean local) {
        Finger finger = new Finger();

        finger.uuid = ConfigurationManager.instance().getUUIDString();
        finger.nickname = ConfigurationManager.instance().getNickname();
        finger.frostwireVersion = Constants.FROSTWIRE_VERSION_STRING;
        finger.totalShared = getNumFiles();

        DeviceInfo di = new DeviceInfo();
        finger.deviceVersion = di.getVersion();
        finger.deviceModel = di.getModel();
        finger.deviceProduct = di.getProduct();
        finger.deviceName = di.getName();
        finger.deviceManufacturer = di.getManufacturer();
        finger.deviceBrand = di.getBrand();
        finger.deviceScreen = di.getScreenMetrics();

        finger.numSharedAudioFiles = getNumFiles(Constants.FILE_TYPE_AUDIO, true);
        finger.numSharedVideoFiles = getNumFiles(Constants.FILE_TYPE_VIDEOS, true);
        finger.numSharedPictureFiles = getNumFiles(Constants.FILE_TYPE_PICTURES, true);
        finger.numSharedDocumentFiles = getNumFiles(Constants.FILE_TYPE_DOCUMENTS, true);
        finger.numSharedApplicationFiles = getNumFiles(Constants.FILE_TYPE_APPLICATIONS, true);
        finger.numSharedRingtoneFiles = getNumFiles(Constants.FILE_TYPE_RINGTONES, true);

        if (local) {
            finger.numTotalAudioFiles = getNumFiles(Constants.FILE_TYPE_AUDIO, false);
            finger.numTotalVideoFiles = getNumFiles(Constants.FILE_TYPE_VIDEOS, false);
            finger.numTotalPictureFiles = getNumFiles(Constants.FILE_TYPE_PICTURES, false);
            finger.numTotalDocumentFiles = getNumFiles(Constants.FILE_TYPE_DOCUMENTS, false);
            finger.numTotalApplicationFiles = getNumFiles(Constants.FILE_TYPE_APPLICATIONS, false);
            finger.numTotalRingtoneFiles = getNumFiles(Constants.FILE_TYPE_RINGTONES, false);
        } else {
            finger.numTotalAudioFiles = finger.numSharedAudioFiles;
            finger.numTotalVideoFiles = finger.numSharedVideoFiles;
            finger.numTotalPictureFiles = finger.numSharedPictureFiles;
            finger.numTotalDocumentFiles = finger.numSharedDocumentFiles;
            finger.numTotalApplicationFiles = finger.numSharedApplicationFiles;
            finger.numTotalRingtoneFiles = finger.numSharedRingtoneFiles;
        }

        return finger;
    }

    public int getNumFiles() {
        int result = 0;

        for (byte i = 0; i < 6; i++) {
            //update numbers if you have to.
            //if (!cache[i].cacheValid(true)) {
            //    cache[i].updateShared(getNumFiles(i, true));
            //}

            result += getNumFiles(i, true);//cache[i].shared;
        }

        return result < 0 ? 0 : result;
    }

    /**
     * 
     * @param fileType
     * @param onlyShared - If false, forces getting all files, shared or unshared. 
     * @return
     */
    public int getNumFiles(byte fileType, boolean onlyShared) {
        TableFetcher fetcher = TableFetchers.getFetcher(fileType);

        //        if (cache[fileType].cacheValid(onlyShared)) {
        //            return cache[fileType].getCount(onlyShared);
        //        }

        Cursor c = null;

        int result = 0;
        int numFiles = 0;

        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(fetcher.getContentUri(), new String[] { BaseColumns._ID }, null, null, null);
            numFiles = c != null ? c.getCount() : 0;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get num of files", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        result = numFiles;// onlyShared ? (getSharedFiles(fileType).size()) : numFiles;

        //updateCacheNumFiles(fileType, result, onlyShared);

        return result;
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
}
