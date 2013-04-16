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

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.limewire.util.FilenameUtils;

import com.frostwire.core.FileDescriptor;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class DownloadTask extends DeviceTask {

    private final File savePath;
    private final Device device;
    private final FileDescriptor[] fds;

    private FileDescriptor currentFD;

    public DownloadTask(File savePath, Device device, FileDescriptor[] fds) {
        this.savePath = savePath;
        this.device = device;
        this.fds = fds;
    }

    public FileDescriptor getCurrentFD() {
        return currentFD;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }

        File lastFile = null;

        try {
            setProgress(0);

            if (!savePath.exists()) {
                savePath.mkdirs();
            }

            long totalBytes = getTotalBytes();
            long totalWritten = 0;

            for (int i = 0; i < fds.length; i++) {
                if (!isRunning()) {
                    return;
                }

                currentFD = fds[i];

                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        String status = String.format("%s from %s - %s", I18n.tr("Downloading"), device.getName(), currentFD.title);
                        LibraryMediator.instance().getLibrarySearch().pushStatus(status);
                    }
                });

                URL url = new URL(device.getDownloadURL(currentFD));

                InputStream is = null;
                OutputStream fos = null;

                try {
                    is = url.openStream();

                    String filename = FilenameUtils.getName(currentFD.filePath);
                    File file = buildFile(savePath, filename);
                    Path incompleteFile = buildIncompleteFile(file).toPath();
                    lastFile = file.getAbsoluteFile();

                    fos = Files.newOutputStream(incompleteFile, StandardOpenOption.CREATE);// new FileOutputStream(incompleteFile);

                    byte[] buffer = new byte[4 * 1024];
                    int n = 0;

                    while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                        if (!isRunning()) {
                            return;
                        }

                        fos.write(buffer, 0, n);
                        fos.flush();
                        totalWritten += n;
                        setProgress((int) ((totalWritten * 100) / totalBytes));

                        if (getProgress() % 5 == 0) {
                            GUIMediator.safeInvokeLater(new Runnable() {
                                public void run() {
                                    String status = String.format("%d%% %s from %s - %s", getProgress(), I18n.tr("Downloading"), device.getName(), currentFD.title);
                                    LibraryMediator.instance().getLibrarySearch().pushStatus(status);
                                }
                            });
                        }

                        //System.out.println("Progress: " + getProgress() + " Total Written: " + totalWritten + " Total Bytes: " + totalBytes);
                    }

                    close(fos);
                    Files.move(incompleteFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    close(is);
                    close(fos);
                }
            }

            setProgress(100);
        } catch (Throwable e) {
            e.printStackTrace();
            onError(e);

            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Wi-Fi download error. Please try again."));
                }
            });
        } finally {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryMediator.instance().getLibrarySearch().revertStatus();
                }
            });

            if (lastFile != null) {
                GUIMediator.launchExplorer(lastFile);
            }
        }

        stop();
    }

    private File buildFile(File savePath, String name) {
        String baseName = FilenameUtils.getBaseName(name);
        String ext = FilenameUtils.getExtension(name);

        File f = new File(savePath, name);
        int i = 1;
        while (f.exists() && i < 100) {
            f = new File(savePath, baseName + " (" + i + ")." + ext);
            i++;
        }
        return f;
    }

    private File buildIncompleteFile(File file) {
        String prefix = FilenameUtils.removeExtension(file.getAbsolutePath());
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        return new File(prefix + ".Incomplete." + ext);
    }

    private long getTotalBytes() {
        long total = 0;
        for (FileDescriptor fd : fds) {
            total += fd.fileSize;
        }
        return total;
    }

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (Throwable e) {
            }
        }
    }
}
