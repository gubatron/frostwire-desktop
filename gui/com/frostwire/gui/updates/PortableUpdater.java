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

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.frostwire.util.ZipUtils;
import com.frostwire.util.ZipUtils.ZipListener;
import com.limegroup.gnutella.gui.GUIMediator;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class PortableUpdater {

    private static final String[] EXE_PATHS = { "MacOS", "Contents/Home/bin" };

    private final File zipFile;
    private final File tempDir;

    public PortableUpdater(File zipFile) {
        this.zipFile = zipFile;
        this.tempDir = new File("/Volumes/FW/temp_zip");
    }

    public void update() {
        ProgressMonitor progressMonitor = new ProgressMonitor(GUIMediator.getAppFrame(), "test", "note", 0, 100);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);
        progressMonitor.setProgress(0);
        UncompressTask task = new UncompressTask(progressMonitor);
        task.execute();
    }

    private void fixPermissions(File newFile) {
        for (String path : EXE_PATHS) {
            if (newFile.getPath().contains(path)) {
                newFile.setExecutable(true);
            }
        }
    }

    private class UncompressTask extends SwingWorker<Void, Void> {

        private final ProgressMonitor progressMonitor;

        public UncompressTask(ProgressMonitor progressMonitor) {
            this.progressMonitor = progressMonitor;
        }

        @Override
        public Void doInBackground() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressMonitor.setProgress(0);
                }
            });

            ZipUtils.unzip(zipFile, tempDir, new ZipListener() {

                @Override
                public void onUnzipping(final File file, final int progress) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressMonitor.setNote(file.getAbsolutePath());
                            progressMonitor.setProgress(progress);
                        }
                    });
                }

                @Override
                public boolean isCanceled() {
                    return progressMonitor.isCanceled();
                }
            });

            return null;
        }

        @Override
        public void done() {
            progressMonitor.close();
            fixPermissions(tempDir);
        }
    }
}
