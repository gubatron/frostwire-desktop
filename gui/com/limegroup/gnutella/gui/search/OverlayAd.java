package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.UISettings;
import com.limegroup.gnutella.settings.UISettings.ImageInfo;
import com.limegroup.gnutella.util.LimeWireUtils;

/**
 * A JPanel designed to be used as an overlay in the default
 * search window.
 */
public class OverlayAd extends JPanel implements ThemeObserver {
    /**
	 * 
	 */
	private static final long serialVersionUID = -617582500794644377L;

	public final int NOT_READY_YET = -1000;
    
    /** The icon to close the overlay. */
    private final Icon CLOSER;
    
    /** The background image. */
    private ImageIcon _image = null;
    
    /** Whether or not this is still the 'Getting Started'. */
    private boolean _searchDone;
    
    /** Future for loading intro. */
    private Future<Void> introFuture;
    
    /** The runner that has access to the image we're loading */
    private LoadImageThenRun introRunner;
    
    private ImageInfo introInfo = UISettings.INTRO_IMAGE_INFO;
    private ImageInfo afterSearchInfo = UISettings.AFTER_SEARCH_IMAGE_INFO;
    
    private JPanel lastIntroPanel;
    private JPanel introPanel;
    private JPanel afterSearchPanel;
   
    /**
     * Constructs a new OverlayAd, starting with the 'Getting Started'
     * image/text.
     */
    public OverlayAd() {
        super();
        setLayout(new OverlayLayout(this));
        CLOSER = GUIMediator.getThemeImage("kill_on");
        loadOverlay();
    }
    
    public void loadOverlay() {
    	_searchDone = false; //reloads intro promo
        introInfo = UISettings.INTRO_IMAGE_INFO;
        afterSearchInfo = UISettings.AFTER_SEARCH_IMAGE_INFO;

        LateImageRunner lateRunner = new LateImageRunner() {
            public void runWithImage(ImageIcon img, boolean usingBackup) {
                _image = img;
                Dimension preferredSize =
                    new Dimension(_image.getIconWidth(), _image.getIconHeight());
                setMaximumSize(preferredSize);
                setPreferredSize(preferredSize);
                
                try {
                    if(usingBackup) {
                        add(createTextPanel(introInfo));
                    }
                    else if(!afterSearchInfo.canProShowPic() && LimeWireUtils.isPro()) {
                        add(createCloserPanel());
                    }

                    
                    removeLastIntroPanel();
                    introPanel = createImagePanel(getUrl(introInfo, usingBackup),
                            introInfo.getTorrentUrl());
                    lastIntroPanel = introPanel;
                    
                    add(introPanel);
                    if (afterSearchPanel!=null && !_searchDone) {
                    	afterSearchPanel.setVisible(false);
                    }
                    GUIUtils.setOpaque(false, OverlayAd.this);
                } catch(NullPointerException npe) {
                    // internal error w/ swing
                    setVisible(false); //posible cause for flickering
                    _searchDone = true;
                }
            }
        };
        
        introRunner = new LoadImageThenRun(introInfo,
                                           lateRunner,
                                           "intro");
        introFuture = introRunner.run();
    }
    
    public void removeLastIntroPanel() {
    	if (lastIntroPanel != null) {
    		lastIntroPanel.setVisible(false);
    		remove(lastIntroPanel);
    	}
    }

    //debug purposes, to see how the image loading is doing
    public int getIntroFutureRunnerIconImageLoadStatus() {
        if (introRunner == null) {
            return NOT_READY_YET;
        }
        
        return introRunner.getImageIconLoadStatus();
    }
    
    public ImageIcon getIntroImageIcon() {
        if (introRunner == null)
            return null;
        
        return introRunner.getImageIcon();
    }
    
    public ImageInfo getImageInfo() {
        return introInfo;
    }
    
    public void updateIntroInfo(ImageInfo newIntro) {
        UISettings.INTRO_IMAGE_INFO = newIntro;
        UISettings.instance().save();

        introInfo = UISettings.INTRO_IMAGE_INFO;
    }

    public void updateAfterSearchInfo(ImageInfo newAfterSearchInfo) {
    	UISettings.AFTER_SEARCH_IMAGE_INFO = newAfterSearchInfo;
    	UISettings.instance().save();
    	
        afterSearchInfo = UISettings.AFTER_SEARCH_IMAGE_INFO;
    }
    
    /**
     * Resets everything to be opaque.
     */
    public void updateTheme() {
        GUIUtils.setOpaque(false, this);
    }
    
    /**
     * Changes the overlay after a search is done.
     */
    void searchPerformed() {
    	//only does this once
    	if(_searchDone) {
    		System.out.println("OverlayAd.searchPerformed() - already did it sorry");
            return;
    	}
        
        //_searchDone = true;
        
        LoadImageThenRun runner = new LoadImageThenRun(afterSearchInfo,
            new LateImageRunner() {
                public void runWithImage(ImageIcon img, boolean usingBackup) {
                    introFuture.cancel(false);
                    _image = img;

                    Dimension preferredSize =
                        new Dimension(_image.getIconWidth(), _image.getIconHeight());
                    setMaximumSize(preferredSize);
                    setPreferredSize(preferredSize);

                    try {
                        removeAll();
                        if(usingBackup)
                            add(createTextPanel(afterSearchInfo));
                        
                        afterSearchPanel = createImagePanel(getUrl(afterSearchInfo, usingBackup),
            					afterSearchInfo.getTorrentUrl()); 
                        add(afterSearchPanel);
                        
                        if (introPanel != null) {
                        	introPanel.setVisible(false);
                        	afterSearchPanel.setVisible(true);
                        }
                        GUIUtils.setOpaque(false, OverlayAd.this);
                    } catch(NullPointerException npe) {
                        // internal error w/ swing
                        setVisible(false); //possible cause for flickering
                    } finally {
                      _searchDone = true;
                    }
                }
            }
        , "afterSearch");
        runner.run();
    }

    private String getUrl(ImageInfo info, boolean backup) {
        if(!info.canLink()) {
            return null;
        } else if(backup) {
            return info.getLocalLinkUrl();
        } else {
            return info.getNetworkLinkUrl();
        }
    }
    
    /**
     * Creates the background image panel.
     */
    private JPanel createImagePanel(String url,String torrentUrl) {
        JPanel panel = new JPanel(new BorderLayout());
        try {
        	java.awt.MediaTracker mt = new java.awt.MediaTracker(panel);
        	mt.addImage(_image.getImage(), 0);
        	mt.waitForID(0);
        } catch (Exception e) {}
        panel.add(new JLabel(_image), BorderLayout.CENTER);
        panel.setMaximumSize(getMaximumSize());
        panel.setPreferredSize(getPreferredSize());
        panel.addMouseListener(new Launcher(url,torrentUrl));
        panel.validate();
        return panel;
    }
    
    /**
     * Creates the text panel, with either 'go pro' text or 'getting started'
     * text.
     * @param goPro whether or not the text is for going pro or getting started
     */
    private JPanel createTextPanel(ImageInfo info) {
        JPanel panel = new JPanel(new BorderLayout());
        
        if(LimeWireUtils.isPro() || !info.isIntro())
            panel.add(createNorthPanel(LimeWireUtils.isPro()), BorderLayout.NORTH);
        panel.add(Box.createHorizontalStrut(18), BorderLayout.WEST);
        JPanel center = !info.isIntro() ? 
                    createGoProCenter() : createGettingStartedCenter();
        panel.add(center, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(18), BorderLayout.EAST);

        panel.setMaximumSize(getMaximumSize());
        panel.setPreferredSize(getPreferredSize());
        return panel;
    }
    
    private JPanel createCloserPanel() {
        JPanel panel = new JPanel(new BorderLayout());            
        panel.add(createNorthPanel(false), BorderLayout.NORTH); //true shows the closer button
        return panel;
    }
    
    /**
     * Creates the north panel, with the closer icon.
     */
    private JPanel createNorthPanel(boolean useCloser) {	
        JPanel box = new BoxPanel(BoxPanel.X_AXIS);
        box.add(Box.createHorizontalGlue());
        if(useCloser) {
            JLabel closer = new JLabel(CLOSER);
            closer.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    OverlayAd.this.setVisible(false);
                }
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
            box.add(closer);
        } else {
            box.add(Box.createVerticalStrut(CLOSER.getIconHeight()));
        }
        return box;
    }
    
    /** Creates the backup getting started text overlay. */
    private JPanel createGettingStartedCenter() {
        JLabel title = new JLabel(
            I18n.tr("Getting Started"));
        title.setFont(new Font("Dialog", Font.PLAIN, 24));
        title.setForeground(new Color(0x5A, 0x76, 0x94));
        
        JTextArea text = new JTextArea(
            I18n.tr("To start using FrostWire, find the text field on the left, type in what you are looking for and click the \"Search\" button. If you are looking for only one type of content (music, video, etc...), you can narrow your search results by using the buttons above the text field."));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setForeground(new Color(0x00, 0x00, 0x00));
        text.setFont(new Font("Dialog", Font.PLAIN, 14));
        
        JScrollPane pane = new JScrollPane(text);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel box = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(40, 0, 5, 0);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.NORTHWEST;
        box.add(title, c);
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        box.add(pane, c);
        
        String url = getUrl(introInfo, true);
        if(url != null && !url.equals("")) {
            MouseListener launcher = new Launcher(url,null);
            box.addMouseListener(launcher);
            text.addMouseListener(launcher);
            title.addMouseListener(launcher);
            addMouseListener(launcher);
        }
        
        return box;
    }
    
    /** Creates the backup Go Pro text overlay. */
    private JPanel createGoProCenter() {
    	return null;
    }
    
    private static class Launcher implements MouseListener {
        private final String url;
        private final String torrentUrl;
        
        Launcher(String url, String torrentUrl) {
            this.url = url;
            this.torrentUrl = torrentUrl;
        }
        
        public void mouseClicked(MouseEvent e) {
            if(!e.isConsumed()) {
                e.consume();
                //byte[] guid = GuiCoreMediator.getApplicationServices().getMyGUID();
                //String finalUrl = LimeWireUtils.addLWInfoToUrl(url, guid);

                //System.out.println("URL is " + url);
                //System.out.println("TORRENT is " + torrentUrl);
                
                if (url != null && !url.equals(""))
                	GUIMediator.openURL(url);
                
                if (this.torrentUrl != null && !this.torrentUrl.equals("")) {
                	openTorrent(this.torrentUrl);
                }
            }
        }
        
    	/**
    	 * Starts a torrent download
    	 * @param uriStr
    	 */
    	public void openTorrent(String uriStr) {
    		if (uriStr == null || uriStr.equals(""))
    			return;
    		
    		try {
    			URI uri = new URI(uriStr);
    			
    			String scheme = uri.getScheme();
    			if(scheme == null || !scheme.equalsIgnoreCase("http")) {
    				//System.out.println("Not a torrent URL");
    				return;
    			}
    			
    			String authority = uri.getAuthority();
    			if(authority == null || authority.equals("") || authority.indexOf(' ') != -1) {
    				return;
    			}
    			
    			GUIMediator.instance().openTorrentURI(uri);
    		} catch (URISyntaxException e) {
    			System.out.println(e);
    		}
    	}        

        
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {
            ((JComponent)e.getComponent()).getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        public void mouseExited(MouseEvent e) {
            ((JComponent)e.getComponent()).getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());
        }
    };    
    
    private static class LoadImageThenRun {
        private ImageInfo imageInfo;
        private LateImageRunner runner;
        private String backupImage;
        private ImageIcon imageIcon;
        
        public LoadImageThenRun(ImageInfo imageInfo, 
                                LateImageRunner runner, 
                                String backupImage) {
            this.imageInfo = imageInfo;
            this.runner = runner;
            this.backupImage = backupImage;
        }
        
        /**
         * For debug purposes
         *  MediaTracker.ABORTED - 2 
         *  MediaTracker.ERRORED - 4 
         *  MediaTracker.COMPLETE - 8
         *  MediaTracker.LOADING - 1
         * @return
         */
        public int getImageIconLoadStatus() {
            if (imageIcon == null) {
                return -1000;
            }
            return imageIcon.getImageLoadStatus();
        }
        
        public ImageIcon getImageIcon() {
            return imageIcon;
        }

        private ImageIcon image() {
            String url = imageInfo.getImageUrl();
            if(!imageInfo.useNetworkImage() || url == null || url.length() == 0) {
                System.out.println("LoadImageThenRun.image() - Returning simple new ImageIcon()");
                imageIcon = new ImageIcon();
                return imageIcon;
            }

            try {
                System.out.println("**OverlayAd.image() - OVERLAY AD, im going to load:" + url);

                imageIcon = new ImageIcon(new URL(url));
                return imageIcon;
            } catch(MalformedURLException murl) {
            	System.out.println("OverlayAd.image() - MalformedURLException fail: " + murl.getMessage());
                return new ImageIcon();
            } catch (Exception someException) {
            	System.out.println("OverlayAd.image() - Coudn't load the image...");
            	someException.printStackTrace();
            	return new ImageIcon();
            }
        }
        
        Future<Void> run() {
            return BackgroundExecutorService.submit(new Callable<Void>() {
                public Void call() {
                    ImageIcon img = image();
                    imageIcon = img;
                    final boolean usingBackup;
                    if(img.getIconHeight() <= 0 || img.getIconHeight() <= 0) {
                        usingBackup = true;
                        img = GUIMediator.getThemeImage(backupImage);
                    } else {
                        usingBackup = false;
                    }
                    final ImageIcon finalImg = img;
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            runner.runWithImage(finalImg, usingBackup);
                        }
                    });
                    return null;
                }
            });
        }
    }
    
    private interface LateImageRunner {
        void runWithImage(ImageIcon img, boolean usingBackup);
    }
    
}