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
package com.frostwire.gui.library;

import java.io.File;

import org.limewire.util.FilenameUtils;

import com.frostwire.gui.library.ProgressFileEntity.ProgressFileEntityListener;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class DeviceUploadTask extends DeviceTask {

    private final Device device;
    private final FileDescriptor[] fds;
    private final String token;

    private FileDescriptor currentFD;
    private long totalBytes;
    private long totalWritten;

    public DeviceUploadTask(Device device, FileDescriptor[] fds, String token) {
        this.device = device;
        this.fds = fds;
        this.token = token;
    }

    public FileDescriptor getCurrentFD() {
        return currentFD;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }

        try {
            setProgress(0);

            totalBytes = getTotalBytes();
            totalWritten = 0;

            for (int i = 0; i < fds.length; i++) {
                if (!isRunning()) {
                    return;
                }

                currentFD = fds[i];
                final String name = FilenameUtils.getName(currentFD.filePath);

                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        LibraryMediator.instance().getLibrarySearch().pushStatus(String.format("%s to %s - %s", I18n.tr("Uploading"), device.getName(), name));
                    }
                });

                device.upload(new File(currentFD.filePath), token, new ProgressFileEntityListener() {
                    public void onWrite(ProgressFileEntity progressFileEntity, int written) {
                        totalWritten += written;
                        setProgress((int) ((totalWritten * 100) / totalBytes));

                        if (getProgress() % 5 == 0) {
                            GUIMediator.safeInvokeLater(new Runnable() {
                                public void run() {
                                    String status = String.format("%d%% %s to %s - %s", getProgress(), I18n.tr("Uploading"), device.getName(), name);
                                    LibraryMediator.instance().getLibrarySearch().pushStatus(status);
                                }
                            });
                        }
                    }

                    public boolean isRunning() {
                        return DeviceUploadTask.this.isRunning();
                    }
                });
            }

            setProgress(100);
        } catch (Throwable e) {
            onError(e);
        } finally {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryMediator.instance().getLibrarySearch().revertStatus();
                }
            });
        }

        stop();
    }

    private long getTotalBytes() {
        long total = 0;
        for (FileDescriptor fd : fds) {
            total += fd.fileSize;
        }
        return total;
    }
}
