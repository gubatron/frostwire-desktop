package com.frostwire.gui.library.android;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.limewire.util.FilenameUtils;

import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class DownloadTask extends DeviceTask {

    private final File savePath;
    private final DeviceFileDescriptor[] dfds;

    private DeviceFileDescriptor currentDeviceFD;

    public DownloadTask(File savePath, DeviceFileDescriptor[] dfds) {
        this.savePath = savePath;
        this.dfds = dfds;
    }

    public DeviceFileDescriptor getCurrentDeviceFD() {
        return currentDeviceFD;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }

        try {
            setProgress(0);

            if (!savePath.exists()) {
                savePath.mkdirs();
            }

            long totalBytes = getTotalBytes();
            long totalWritten = 0;

            for (int i = 0; i < dfds.length; i++) {
                if (!isRunning()) {
                    return;
                }

                currentDeviceFD = dfds[i];
                
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Downloading ") + currentDeviceFD.getFD().title);
                    }
                });

                URL url = new URL(currentDeviceFD.getDevice().getDownloadURL(currentDeviceFD.getFD()));

                InputStream is = null;
                FileOutputStream fos = null;

                try {
                    is = url.openStream();

                    File file = new File(savePath, FilenameUtils.getName(currentDeviceFD.getFD().filePath));

                    fos = new FileOutputStream(file);

                    byte[] buffer = new byte[4 * 1024];
                    int n = 0;

                    while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                        if (!isRunning()) {
                            return;
                        }

                        fos.write(buffer, 0, n);
                        totalWritten += n;
                        setProgress((int) ((totalWritten * 100) / totalBytes));
                        
                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                                LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Downloading ") + currentDeviceFD.getFD().title + " " + getProgress() + "%");
                            }
                        });
                    }
                } finally {
                    close(fos);
                    close(is);
                }
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
        for (DeviceFileDescriptor dfd : dfds) {
            total += dfd.getFD().fileSize;
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
