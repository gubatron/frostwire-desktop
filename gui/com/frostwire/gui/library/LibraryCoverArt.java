package com.frostwire.gui.library;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.frostwire.alexandria.PlaylistItem;
import com.limegroup.gnutella.gui.GUIMediator;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

public class LibraryCoverArt extends JPanel {

    private static final long serialVersionUID = 4302859512245078593L;
    
    private Image coverArtImage;
    private Image scaledImage;
    
    public LibraryCoverArt() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setPrivateImage(coverArtImage);
            }
        });
    }

    public void setPlaylistItem(final PlaylistItem playlistItem) {
        setPrivateImage(null);
        new Thread(new Runnable() {
            public void run() {
                setPrivateImage(retrieveImage(playlistItem));
            }
        }).start();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (scaledImage != null) {
            g.drawImage(scaledImage, 0, 0, null);
        }
    }
    
    private Image retrieveImage(PlaylistItem playlistItem) {
        Image image = null;
        if (playlistItem.getFileExtension().toLowerCase().equals("mp3")) {
            image = retrieveImageFromMP3(playlistItem.getFilePath());
        }
        
        return image;
    }
    
    private void setPrivateImage(Image image) {
        coverArtImage = image;
        
        if (coverArtImage != null) {
            scaledImage = coverArtImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        } else {
            scaledImage = null;
        }
        
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });
    }

    private Image retrieveImageFromMP3(String filename) {
         try {
            Mp3File mp3 = new Mp3File(filename);
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                return Toolkit.getDefaultToolkit().createImage(tag.getAlbumImage());
            }
            return null;
        } catch (Exception e) {
            // ignore
            return null;
        }
    }
}
