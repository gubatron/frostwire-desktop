package com.frostwire.gui.library;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.frostwire.jpeg.JPEGImageIO;
import com.frostwire.mp3.ID3v2;
import com.frostwire.mp3.Mp3File;
import com.limegroup.gnutella.gui.GUIMediator;

public class LibraryCoverArt extends JPanel {

    private static final long serialVersionUID = 4302859512245078593L;

    private final BufferedImage background;
    private final Image defaultCoverArt;

    private Image coverArtImage;
    private File file;

    public LibraryCoverArt() {
        background = new BufferedImage(350, 350, BufferedImage.TYPE_INT_RGB);
        defaultCoverArt = GUIMediator.getThemeImage("default_cover_art").getImage();
        setFile(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setPrivateImage(coverArtImage);
            }
        });
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
        new Thread(new Runnable() {
            public void run() {
                setPrivateImage(retrieveImage(file));
            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
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
            return null;
        } catch (Exception e) {
            // ignore
            return null;
        }
    }
}
