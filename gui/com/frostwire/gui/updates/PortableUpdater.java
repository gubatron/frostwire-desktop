/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2013, FrostWire(R). All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class PortableUpdater {

    private static final int CLOSED_TEST_ATTEMPTS = 20;
    private static final int CLOSED_TEST_WAIT_MILLIS = 2000;
    private static final int TIMEOUT = 10000;
    private static final int UNZIP_WAIT_MILLIS = 0;

    private static final String[] EXE_PATHS = { "MacOS", "Contents/Home/bin" };

    private static final InetSocketAddress ADDRESS_V1 = new InetSocketAddress("127.0.0.1", 45099);

    private final InetSocketAddress[] addresses;

    public PortableUpdater() {
        this.addresses = new InetSocketAddress[] { ADDRESS_V1 };
    }

    public boolean waitFrostWireClosed() {
        boolean closed = false;

        for (int i = 0; !closed && i < CLOSED_TEST_ATTEMPTS; i++) {
            if (isFrostWireRunning()) {
                log("FrostWire is running, waiting...");
                wait(CLOSED_TEST_WAIT_MILLIS);
            } else {
                closed = true;
            }
        }

        return closed;
    }

    public boolean unzip(String zipFile, File outputDir) {
        boolean result = false;

        try {

            FileUtils.deleteDirectory(outputDir);

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            try {
                unzipEntries(outputDir, zis, System.currentTimeMillis());
            } finally {
                zis.close();
            }

            result = true;

        } catch (IOException ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }

    private boolean isFrostWireRunning() {
        for (InetSocketAddress address : addresses) {
            if (isFrostWireRunning(address)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFrostWireRunning(InetSocketAddress address) {
        boolean running = false;

        Socket s = new Socket();

        try {
            s.setSoTimeout(TIMEOUT);
            s.connect(address);
            running = true;
        } catch (Throwable e) {
            running = false;
        } finally {
            try {
                s.close();
            } catch (Throwable e) {
                // ignore
            }
        }

        return running;
    }

    private void unzipEntries(File folder, ZipInputStream zis, long time) throws IOException, FileNotFoundException {
        ZipEntry ze = null;

        while ((ze = zis.getNextEntry()) != null) {

            String fileName = ze.getName();
            File newFile = new File(folder, fileName);

            log("unzip: " + newFile.getAbsoluteFile());

            if (ze.isDirectory()) {
                if (!newFile.mkdirs()) {
                    break;
                }
                continue;
            }

            FileOutputStream fos = new FileOutputStream(newFile);

            try {
                int n;
                byte[] buffer = new byte[1024];
                while ((n = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, n);
                    wait(UNZIP_WAIT_MILLIS);
                }
            } finally {
                fos.close();
                zis.closeEntry();
            }

            newFile.setLastModified(time);

            fixPermissions(newFile);
        }
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void fixPermissions(File newFile) throws IOException {
        for (String path : EXE_PATHS) {
            if (newFile.getPath().contains(path)) {
                newFile.setExecutable(true);
            }
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }
}
