package com.frostwire.gui.library;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.limewire.util.FilenameUtils;

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
                        LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Downloading ") + currentFD.title);
                    }
                });

                URL url = new URL(device.getDownloadURL(currentFD));

                InputStream is = null;
                FileOutputStream fos = null;

                try {
                    is = url.openStream();

                    File file = buildFile(savePath, FilenameUtils.getName(currentFD.filePath));

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

                        if (getProgress() % 5 == 0) {
                            GUIMediator.safeInvokeLater(new Runnable() {
                                public void run() {
                                    LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Downloading ") + currentFD.title + " " + getProgress() + "%");
                                }
                            });
                        }
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
