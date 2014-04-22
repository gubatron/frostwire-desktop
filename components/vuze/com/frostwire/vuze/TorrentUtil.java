/*
 * Created on 9 Jul 2007
 * Created by Allan Crooks
 * Copyright (C) 2007 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
package com.frostwire.vuze;

import org.gudy.azureus2.core3.category.Category;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.download.Download;

import com.aelitis.azureus.ui.selectedcontent.SelectedContent;

/**
 * @author Allan Crooks
 *
 */
@SuppressWarnings({"deprecation", "unused"})
class TorrentUtil {

    public static boolean shouldStopGroup(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        DiskManagerFileInfo[] dmfi = toDMFI(datasources);
        if (dms.length == 0 && dmfi.length == 0) {
            return true;
        }
        for (DownloadManager dm : dms) {
            int state = dm.getState();
            boolean stopped = state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING;
            if (!stopped) {
                return true;
            }
        }

        for (DiskManagerFileInfo fileInfo : dmfi) {
            if (!fileInfo.isSkipped()) {
                return true;
            }
        }
        return false;
    }

    public static void stopOrStartDataSources(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        DiskManagerFileInfo[] dmfi = toDMFI(datasources);
        if (dms.length == 0 && dmfi.length == 0) {
            return;
        }
        boolean doStop = shouldStopGroup(datasources);
        if (doStop) {
            stopDataSources(datasources);
        } else {
            queueDataSources(datasources, true);
        }
    }

    public static void stopDataSources(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        for (DownloadManager dm : dms) {
            ManagerUtils.stop(dm);
        }
        //		DiskManagerFileInfo[] fileInfos = toDMFI(datasources);
        //		if (fileInfos.length > 0) {
        //			FilesViewMenuUtil.changePriority(FilesViewMenuUtil.PRIORITY_SKIPPED,
        //					fileInfos);
        //		}
    }

    public static void pauseDataSources(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        for (DownloadManager dm : dms) {
            ManagerUtils.pause(dm);
        }
    }

    public static void queueDataSources(Object[] datasources, boolean startStoppedParents) {
        DownloadManager[] dms = toDMS(datasources);
        for (DownloadManager dm : dms) {
            ManagerUtils.queue(dm);
        }
        //		DiskManagerFileInfo[] fileInfos = toDMFI(datasources);
        //		if (fileInfos.length > 0) {
        //			FilesViewMenuUtil.changePriority(FilesViewMenuUtil.PRIORITY_NORMAL,
        //					fileInfos);
        //
        //			if (startStoppedParents) {
        //				for (DiskManagerFileInfo fileInfo : fileInfos) {
        //					if (fileInfo.getDownloadManager().getState() == DownloadManager.STATE_STOPPED) {
        //						ManagerUtils.queue(fileInfo.getDownloadManager(), null);
        //					}
        //				}
        //			}
        //		}
    }

    /** Queue torrents so they can be automatically restarted when connection comes back.*/
    public static void queueTorrents(Object[] download_managers) {
        DMTask task = new DMTask(toDMS(download_managers)) {
            public void run(DownloadManager dm) {
                //don't mark queued those that were stopped by the user.
                if (dm.getState() != DownloadManager.STATE_STOPPED) {
                    ManagerUtils.stop(dm);
                    ManagerUtils.queue(dm); //this won't work unless it's already stopped.
                }
            }
        };
        task.go();        
    }

    
    public static void resumeTorrents(Object[] download_managers) {
        DMTask task = new DMTask(toDMS(download_managers)) {
            public void run(DownloadManager dm) {
                //queued == same as STATE_STOPPED, except it can be restarted automatically.
                if (dm.getState() == DownloadManager.STATE_QUEUED) {
                    ManagerUtils.start(dm);
                }
            }
        };
        task.go();
    }

    // Category Stuff
    public static void assignToCategory(Object[] download_managers, final Category category) {
        DMTask task = new DMTask(toDMS(download_managers)) {
            public void run(DownloadManager dm) {
                dm.getDownloadState().setCategory(category);
            }
        };
        task.go();
    }

    private static DownloadManager[] toDMS(Object[] objects) {
        int count = 0;
        DownloadManager[] result = new DownloadManager[objects.length];
        for (Object object : objects) {
            if (object instanceof DownloadManager) {
                DownloadManager dm = (DownloadManager) object;
                result[count++] = dm;
            } else if (object instanceof SelectedContent) {
                SelectedContent sc = (SelectedContent) object;
                if (sc.getFileIndex() == -1 && sc.getDownloadManager() != null) {
                    result[count++] = sc.getDownloadManager();
                }
            }
        }
        DownloadManager[] resultTrim = new DownloadManager[count];
        System.arraycopy(result, 0, resultTrim, 0, count);
        return resultTrim;
    }

    private static DiskManagerFileInfo[] toDMFI(Object[] objects) {
        int count = 0;
        DiskManagerFileInfo[] result = new DiskManagerFileInfo[objects.length];
        for (Object object : objects) {
            if (object instanceof DiskManagerFileInfo) {
                DiskManagerFileInfo fileInfo = (DiskManagerFileInfo) object;
                result[count++] = fileInfo;
            } else if (object instanceof SelectedContent) {
                SelectedContent sc = (SelectedContent) object;
                int fileIndex = sc.getFileIndex();
                if (fileIndex >= 0 && sc.getDownloadManager() != null) {
                    DownloadManager dm = sc.getDownloadManager();
                    if (dm != null) {
                        DiskManagerFileInfo[] infos = dm.getDiskManagerFileInfo();
                        if (fileIndex < infos.length) {
                            result[count++] = infos[fileIndex];
                        }
                    }
                }
            }
        }
        DiskManagerFileInfo[] resultTrim = new DiskManagerFileInfo[count];
        System.arraycopy(result, 0, resultTrim, 0, count);
        return resultTrim;
    }

    private abstract static class DMTask {
        private DownloadManager[] dms;

        private boolean ascending;
        private boolean async;

        public DMTask(DownloadManager[] dms) {
            this(dms, true);
        }

        public DMTask(DownloadManager[] dms, boolean ascending) {
            this.dms = dms;
            this.ascending = ascending;
        }

        public DMTask(DownloadManager[] dms, boolean ascending, boolean async) {
            this.dms = dms;
            this.ascending = ascending;
            this.async = async;
        }

        // One of the following methods should be overridden.
        public void run(DownloadManager dm) {
        }

        public void run(DownloadManager[] dm) {
        }

        public void go() {
            try {
                DownloadManager dm = null;
                for (int i = 0; i < dms.length; i++) {
                    dm = dms[ascending ? i : (dms.length - 1) - i];
                    if (dm == null) {
                        continue;
                    }
                    this.run(dm);
                }
                this.run(dms);
            } catch (Exception e) {
                Debug.printStackTrace(e);
            }
        }
    }

    public static void removeDownloads(DownloadManager[] dms, AERunnable deleteFailed) {
        removeDownloads(dms, deleteFailed, false);
    }

    public static void removeDownloads(final DownloadManager[] dms, final AERunnable deleteFailed, final boolean deleteData) {
        if (dms == null) {
            return;
        }

        // confusing code:
        // for loop goes through erasing published and low noise torrents until
        // it reaches a normal one.  We then prompt the user, and stop the loop.
        // When the user finally chooses an option, we act on it.  If the user
        // chose to act on all, we do immediately all and quit.  
        // If the user chose an action just for the one torrent, we do that action, 
        // remove that item from the array (by nulling it), and then call 
        // removeDownloads again so we can prompt again (or erase more published/low noise torrents)
        for (int i = 0; i < dms.length; i++) {
            DownloadManager dm = dms[i];
            if (dm == null) {
                continue;
            }

            boolean deleteTorrent = COConfigurationManager.getBooleanParameter("def.deletetorrent");

            removeDownloadsPrompterClosed(dms, i, deleteFailed, deleteData ? 1 : 2, true, deleteTorrent);
        }
    }

    private static void removeDownloadsPrompterClosed(DownloadManager[] dms, int index, AERunnable deleteFailed, int result, boolean doAll, boolean deleteTorrent) {
        if (result == -1) {
            // user pressed ESC (as opposed to clicked Cancel), cancel whole
            // list
            return;
        }
        if (doAll) {
            if (result == 1 || result == 2) {

                for (int i = index; i < dms.length; i++) {
                    DownloadManager dm = dms[i];
                    boolean deleteData = result == 2 ? false : !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);
                    ManagerUtils.asyncStopDelete(dm, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, deleteFailed);
                }
            } //else cancel
        } else { // not remembered
            if (result == 1 || result == 2) {
                DownloadManager dm = dms[index];
                boolean deleteData = result == 2 ? false : !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

                ManagerUtils.asyncStopDelete(dm, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
            }
            // remove the one we just did and go through loop again
            dms[index] = null;
            if (index != dms.length - 1) {
                removeDownloads(dms, deleteFailed, true);
            }
        }
    }
}
