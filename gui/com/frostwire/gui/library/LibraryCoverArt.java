package com.frostwire.gui.library;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

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
                setPrivateImage(coverArtImage, true);
                //System.out.println("componentResized:" + getWidth()+","+getHeight());
            }
        });
    }

    public void setPlaylistItem(final PlaylistItem playlistItem) {
        setPrivateImage(null,false);
        new Thread(new Runnable() {
            public void run() {
                setPrivateImage(retrieveImage(playlistItem), false);
            }
        }).start();
    }
    
    @Override
    public void paint(Graphics g) {
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
    

	public Image getScaledImageFast(Image img, int w, int h) {
		
		int type = BufferedImage.TYPE_INT_RGB;
		BufferedImage tmp = new BufferedImage(w, h, type);
		Graphics2D g2 = tmp.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		img.setAccelerationPriority(1);
		g2.drawImage(img, 0, 0, w, h, null);
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img, 0);
		g2.dispose();
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return tmp;
	}
    
    private void setPrivateImage(Image image, boolean fast) {
        coverArtImage = image;
        
        if (coverArtImage != null) {
        	
        	if (fast) {
        	scaledImage = getScaledImageFast(coverArtImage,
                    getWidth(),
                    getHeight());
        	} else {
        		coverArtImage.setAccelerationPriority(1);
        		scaledImage = coverArtImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        		MediaTracker mt = new MediaTracker(this);
        		mt.addImage(scaledImage, 0);
        		try {
					mt.waitForAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        } else {
            scaledImage = null;
        }
        
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
