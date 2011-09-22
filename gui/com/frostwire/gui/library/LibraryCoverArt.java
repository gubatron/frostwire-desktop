package com.frostwire.gui.library;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
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

    private Image coverArtImage;
    private Image scaledImage;
    
    private File file;

    public LibraryCoverArt() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setPrivateImage(coverArtImage, true);
                //System.out.println("componentResized:" + getWidth()+","+getHeight());
            }
        });
    }

    /**
     * Async
     * @param file
     */
    public void setFile(final File file) {
        //System.out.println(file);
        if (this.file != null && file != null && this.file.equals(file)) {
            return;
        }
        this.file = file;
        setPrivateImage(null, false);
        if (file == null) {
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                setPrivateImage(retrieveImage(file), false);
            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        if (scaledImage != null) {
            g.drawImage(scaledImage, 0, 0, null);
        }
    }

    /**
     * Synchronous.
     * @param file
     * @return
     */
    private Image retrieveImage(File file) {
    	String path = file.getAbsolutePath();
        Image image = null;
        if (path.toLowerCase().endsWith(".mp3")) {
            image = retrieveImageFromMP3(path);
        }

        return image;
    }

    public Image getScaledImageFast(Image img, int w, int h) {

        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage tmp = new BufferedImage(w, h, type);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();

        waitForImageToLoad(img);

        return tmp;
    }

    private void setPrivateImage(Image image, boolean fast) {
        coverArtImage = image;

        if (coverArtImage != null) {

            if (fast) {
                scaledImage = getScaledImageFast(coverArtImage, getWidth(), getHeight());
            } else {
                scaledImage = coverArtImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            }
        } else {
            scaledImage = LibraryMediator.instance().getDefaultCoverArt().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);//getScaledImageFast(LibraryMediator.instance().getDefaultCoverArt(), getWidth(), getHeight());
        }

        waitForImageToLoad(scaledImage);

        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });

        //Hack. Otherwise the cover art is not painted unless we mouse over the splitpane on the library.
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                LibraryMediator.instance().replaintSplitPane();
            }
        });

    }

    private void waitForImageToLoad(Image image) {
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
