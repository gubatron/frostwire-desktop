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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.jpeg.JPEGImageIO;
import com.frostwire.mp3.ID3v2;
import com.frostwire.mp3.Mp3File;
import com.frostwire.mp4.IsoFile;
import com.frostwire.mp4.Path;
import com.frostwire.mp4.boxes.apple.AppleDataBox;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class LibraryCoverArt extends JPanel implements ThemeObserver {

    private static final long serialVersionUID = 4302859512245078593L;

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(LibraryCoverArt.class);

    private final BufferedImage background;
    private final Image defaultCoverArt;

    private Image coverArtImage;
    private File file;

    public LibraryCoverArt() {
        background = new BufferedImage(350, 350, BufferedImage.TYPE_INT_ARGB);
        defaultCoverArt = GUIMediator.getThemeImage("default_cover_art").getImage();
        setFile(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTheme();
            }
        });
        ThemeMediator.addThemeObserver(this);
    }

    /**
     * Async
     * @param file
     */
    public void setFile(final File file) {
        if (this.file != null && file != null && this.file.equals(file)) {
            return;
        }
        this.file = file;
        Thread t = new Thread(new Runnable() {
            public void run() {
                Image image = retrieveImage(file);
                if (file != null && file.equals(LibraryCoverArt.this.file)) {
                    setPrivateImage(image);
                }
            }
        }, "Cover Art extract");
        t.setDaemon(true);
        t.start();
    }

    public void setDefault() {
        this.file = null;
        new Thread(new Runnable() {
            public void run() {
                Image image = retrieveImage(file);
                setPrivateImage(image);
            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(background, 0, 0, null);
    }

    /**
     * Synchronous.
     * @param file
     * @return
     */
    private Image retrieveImage(File file) {
        if (file == null) {
            return defaultCoverArt;
        }
        String path = file.getAbsolutePath();
        Image image = null;
        if (path.toLowerCase().endsWith(".mp3")) {
            image = retrieveImageFromMP3(path);
        } else if (path.toLowerCase().endsWith(".m4a")) {
            image = retrieveImageFromM4A(path);
        }

        return image;
    }

    private void setPrivateImage(Image image) {
        coverArtImage = image;

        if (coverArtImage == null) {
            coverArtImage = defaultCoverArt;
        }

        Graphics2D g2 = background.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2.setBackground(new Color(255, 255, 255, 0));
        g2.clearRect(0, 0, getWidth(), getHeight());

        g2.drawImage(coverArtImage, 0, 0, getWidth(), getHeight(), null);
        g2.dispose();

        repaint();
        getToolkit().sync();
    }

    private Image retrieveImageFromMP3(String filename) {
        try {
            Mp3File mp3 = new Mp3File(filename);
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                byte[] imageBytes = tag.getAlbumImage();
                try {
                    return ImageIO.read(new ByteArrayInputStream(imageBytes, 0, imageBytes.length));
                } catch (IIOException e) {
                    return JPEGImageIO.read(new ByteArrayInputStream(imageBytes, 0, imageBytes.length));
                }
            }
        } catch (Throwable e) {
            //LOG.error("Unable to read cover art from mp3");
        }

        return null;
    }

    private Image retrieveImageFromM4A(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            FileChannel inFC = fis.getChannel();
            IsoFile iso = new IsoFile(inFC);

            Path p = new Path(iso);

            AppleDataBox data = (AppleDataBox) p.getPath("/moov/udta/meta/ilst/covr/data");
            if (data != null) {
                if ((data.getFlags() & 0x1) == 0x1) { // jpg
                    byte[] imageBytes = data.getData();
                    try {
                        return ImageIO.read(new ByteArrayInputStream(imageBytes, 0, imageBytes.length));
                    } catch (IIOException e) {
                        return JPEGImageIO.read(new ByteArrayInputStream(imageBytes, 0, imageBytes.length));
                    }
                } else if ((data.getFlags() & 0x2) == 0x2) { // png
                    byte[] imageBytes = data.getData();
                    try {
                        return ImageIO.read(new ByteArrayInputStream(imageBytes, 0, imageBytes.length));
                    } catch (IIOException e) {
                        return null;
                    }
                }
            }

            fis.close();

        } catch (Throwable e) {
            //LOG.error("Unable to read cover art from m4a");
        }

        return null;
    }

    @Override
    public void updateTheme() {
        setPrivateImage(coverArtImage);
    }
}