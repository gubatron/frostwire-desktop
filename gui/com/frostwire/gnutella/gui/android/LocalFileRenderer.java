package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import com.frostwire.gnutella.gui.ImagePanel;
import com.limegroup.gnutella.gui.I18n;

public class LocalFileRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8723371611297478100L;
	
	public static final int VIEW_THUMBNAIL = 0;
	public static final int VIEW_LIST = 1;
	
	private static Map<String, BufferedImage> IMAGE_TYPES = new HashMap<String, BufferedImage>();
	private static FileSystemView FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();
	private static ImageTool IMAGE_TOOL = new ImageTool();
	
	private LocalFile _localFile;
	private int _viewType;
	
	private ImagePanel _imagePanelThumbnail;
	private MultilineLabel _labelName;
	
	public LocalFileRenderer() {
	    _viewType = VIEW_THUMBNAIL;
	    setupUI();
	}

    @Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		_localFile = (LocalFile) value;
		
		if (isSelected) {
            setBackground(Color.LIGHT_GRAY);
        }
        else {
            setBackground(Color.WHITE);
        }
		
		setImagePanelThumbnail(_localFile);
		
		_labelName.setText(FILE_SYSTEM_VIEW.getSystemDisplayName(_localFile.getFile()));
		
		return this;
	}

    public Component getComponentAt(int x, int y) {
		for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i).getBounds().contains(x, y)) {
                return getComponent(i);
            }
		}
        
        if (getBounds().contains(x, y)) {
            return this;
        }
        
        return null;
    }
	
	protected void setupUI() {
	    
	    _imagePanelThumbnail = new ImagePanel();
	    _imagePanelThumbnail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && !e.isConsumed()) {
                    _localFile.open();
                }
            }
        });
	    
	    _labelName = new MultilineLabel();
        _labelName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && !e.isConsumed()) {
                    _localFile.open();
                }
            }
        });
        
        relayout();
	}
	
	private void relayout() {
	    remove(_imagePanelThumbnail);
	    remove(_labelName);
	    
	    if (_viewType == VIEW_THUMBNAIL) {
	        layoutThumbnail();
	    } else {
	        layoutList();
	    }
	}

    private void layoutThumbnail() {
        setLayout(new GridBagLayout());
        Dimension size = new Dimension(140, 100);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        
        GridBagConstraints c;
        
        Dimension thumbnailSize = new Dimension(64, 64);
        _imagePanelThumbnail.setPreferredSize(thumbnailSize);
        _imagePanelThumbnail.setMinimumSize(thumbnailSize);
        _imagePanelThumbnail.setMaximumSize(thumbnailSize);
        _imagePanelThumbnail.setSize(thumbnailSize);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 38, 0, 38);
        add(_imagePanelThumbnail, c);
        
        Dimension labelNameSize = new Dimension(118, 30);
        _labelName.setPreferredSize(labelNameSize);
        _labelName.setMinimumSize(labelNameSize);
        _labelName.setMaximumSize(labelNameSize);
        _labelName.setSize(labelNameSize);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 12, 0, 10);
        add(_labelName, c);
    }
    
    private void layoutList() {
        // TODO Auto-generated method stub
        
    }

    private void setImagePanelThumbnail(LocalFile localFile) {
        BufferedImage image = null;
        if (localFile.getFile().isDirectory()) {
            if (_viewType == VIEW_THUMBNAIL) {
                image = IMAGE_TOOL.load("folder_64");
            } else {
                image = IMAGE_TOOL.load("folder");
            }
        } else {
            String ext = localFile.getExt();
            if (IMAGE_TYPES.containsKey(ext)) {
                image = IMAGE_TYPES.get(ext);
            } else {
                image = IMAGE_TOOL.load(IMAGE_TOOL.getImageNameByFileType(localFile.getFileType()));
                if (ext != null) {
                    image = composeImage(image, ext);
                    IMAGE_TYPES.put(ext, image);
                }
            }
        }
        
        _imagePanelThumbnail.setImage(image);
    }
    
    private BufferedImage composeImage(BufferedImage imageInput, String ext) {
        
        BufferedImage image1 = imageInput;
        BufferedImage image2 = buildTextImage(ext.toUpperCase());
        
        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image2.getWidth();
        int h2 = image2.getHeight();
        
        int w = w1;
        int h = h1;
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D graphics = image.createGraphics();
        
        try {
            
            graphics.drawImage(image1,  0,  0, w1, h1, null);
            graphics.drawImage(image2, 14, 45, w2, h2, null);
            
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return image;
    }
    
    private BufferedImage buildTextImage(String text) {
        
        Font font = new Font("Curier", Font.ITALIC | Font.BOLD, 16);

        Graphics2D graphicsDummy = null;
        Graphics2D graphics1 = null;
        Graphics2D graphics2 = null;
        
        BufferedImage image = null;

        try {

            BufferedImage imageDummy = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            graphicsDummy = imageDummy.createGraphics();
            graphicsDummy.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphicsDummy.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            FontMetrics metrics = graphicsDummy.getFontMetrics(font);
            int w = metrics.stringWidth(text) + 20;
            int h = metrics.getHeight() + 10;

            BufferedImage image1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            graphics1 = image1.createGraphics();
            graphics1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics1.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            //draw "shadow" text: to be blurred next
            TextLayout textLayout = new TextLayout(text, font, graphics1.getFontRenderContext());
            graphics1.setPaint(Color.BLUE);
            textLayout.draw(graphics1, 11, 14);
            graphics1.dispose();

            //blur the shadow: result is sorted in image2
            float ninth = 3.0f / 9.0f;
            float[] kernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
            ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
            BufferedImage image2 = op.filter(image1, null);

            //write "original" text on top of shadow
            graphics2 = image2.createGraphics();
            graphics2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics2.setPaint(Color.YELLOW);
            textLayout.draw(graphics2, 10, 13);
            
            image = image2;
            
        } finally {
            if (graphicsDummy != null) {
                graphicsDummy.dispose();
            }
            if (graphicsDummy != null) {
                graphics1.dispose();
            }
            if (graphicsDummy != null) {
                graphics2.dispose();
            }
        }

        return image;
    }
}
