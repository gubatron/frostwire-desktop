package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import com.frostwire.gnutella.gui.GraphicPanel;

public class LocalFileRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8723371611297478100L;
	
	private static final Color FILL_COLOR = new Color(0x8DB2ED);
	private static final Color INNER_BORDER_COLOR = new Color(0x98BFFF);
	private static final Color OUTER_BORDER_COLOR = new Color(0x516688);
	
	private static final Color EXT_SHADOW_COLOR = new Color(0x333333);
	private static final Color EXT_COLOR = new Color(0xFFFFFF);
	
	private static final int EXT_POS_X = 14;
	private static final int EXT_POS_Y = 14;
	private static final int EXT_SHADOW_DELTA = 10;
	
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa");
	
	private static Map<String, BufferedImage> IMAGE_TYPES = new HashMap<String, BufferedImage>();
	private static FileSystemView FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();
	private static UITool UI_TOOL = new UITool();
	
	private LocalFile _localFile;
	private int _layoutOrientation;
	private boolean _selected;
	
	private GraphicPanel _imagePanel;
	private MultilineLabel _multilineLabelName;
	private JLabel _labelName;
	private JLabel _labelDateModified;
	private JLabel _labelSize;
	
	public LocalFileRenderer() {
	    _layoutOrientation = -1;
	    setupUI();
	}

    @Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
        _localFile = (LocalFile) value;     
        _selected = isSelected;
        
        if (_layoutOrientation != list.getLayoutOrientation()) {
            _layoutOrientation = list.getLayoutOrientation();
            relayout();
        }		
		
        File file = _localFile.getFile();
        
		setImagePanelThumbnail(_localFile);		
		if (_layoutOrientation == JList.HORIZONTAL_WRAP) {
		    _multilineLabelName.setText(FILE_SYSTEM_VIEW.getSystemDisplayName(file));
		} else {
		    _labelName.setText(FILE_SYSTEM_VIEW.getSystemDisplayName(file));
		    _labelDateModified.setText(DATE_FORMAT.format(new Date(file.lastModified())));
		    _labelSize.setText(UI_TOOL.getBytesInHuman(file.length()));
		}		
		setToolTip(file);
		
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
    
    public Dimension getLabelNameSize() {
        return _labelName.getSize();
    }
    
    public Point getLabelNameLocation() {
        return _labelName.getLocation();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        
        if (_selected) {
            g.setColor(FILL_COLOR);
            g.fillRoundRect(2, 2, w - 5, h - 5, 5, 5);
            g.setColor(INNER_BORDER_COLOR);
            g.drawRoundRect(3, 3, w - 7, h - 7, 5, 5);
            g.setColor(OUTER_BORDER_COLOR);
            g.drawRoundRect(2, 2, w - 5, h - 5, 5, 5);
        }
    }
	
	protected void setupUI() {
	    
	    _imagePanel = new GraphicPanel();
	    
	    _multilineLabelName = new MultilineLabel();
        
        _labelName = new JLabel();
        
        _labelDateModified = new JLabel();
        
        _labelSize = new JLabel();
        Dimension labelSizeSize = new Dimension(70, 26);
        _labelSize.setPreferredSize(labelSizeSize);
        _labelSize.setMinimumSize(labelSizeSize);
        _labelSize.setHorizontalAlignment(SwingConstants.RIGHT);
	}
	
	private void relayout() {
	    
	    IMAGE_TYPES.remove(_localFile.getExt());
	    
	    remove(_imagePanel);
	    remove(_multilineLabelName);
	    remove(_labelName);
	    remove(_labelDateModified);
	    remove(_labelSize);
	    
	    if (_layoutOrientation == JList.HORIZONTAL_WRAP) {
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
        _imagePanel.setPreferredSize(thumbnailSize);
        _imagePanel.setMinimumSize(thumbnailSize);
        _imagePanel.setMaximumSize(thumbnailSize);
        _imagePanel.setSize(thumbnailSize);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 38, 0, 38);
        add(_imagePanel, c);
        
        Dimension labelNameSize = new Dimension(118, 30);
        _multilineLabelName.setPreferredSize(labelNameSize);
        _multilineLabelName.setMinimumSize(labelNameSize);
        _multilineLabelName.setMaximumSize(labelNameSize);
        _multilineLabelName.setSize(labelNameSize);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 12, 0, 10);
        add(_multilineLabelName, c);
    }

   private void layoutList() {
        setLayout(new GridBagLayout());
        setPreferredSize(null);
        
        GridBagConstraints c;
        
        Dimension size = new Dimension(32, 32);
        _imagePanel.setPreferredSize(size);
        _imagePanel.setMinimumSize(size);
        _imagePanel.setMaximumSize(size);
        _imagePanel.setSize(size);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 0, 5);
        add(_imagePanel, c);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(_labelName, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        add(_labelDateModified, c);
        
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 6);
        add(_labelSize, c);
    }

    private void setImagePanelThumbnail(LocalFile localFile) {
        if (localFile == null)
        	return;

    	Image image = null;
        if (localFile.getFile().isDirectory()) {
            if (_layoutOrientation == JList.HORIZONTAL_WRAP) {
                image = UI_TOOL.loadImage("folder_64");
            } else {
                image = UI_TOOL.loadImage("folder_32");
            }
        } else {
            String ext = localFile.getExt();
            if (IMAGE_TYPES.containsKey(ext)) {
                image = IMAGE_TYPES.get(ext);
            } else {
                BufferedImage imageFileType = UI_TOOL.loadImage(UI_TOOL.getImageNameByFileType(localFile.getFileType()));
                if (ext != null) {
                    imageFileType = composeImage(imageFileType, ext);
                    IMAGE_TYPES.put(ext, imageFileType);
                }
                
                image = imageFileType;
            }
            
            if (_layoutOrientation != JList.HORIZONTAL_WRAP) {
                image = image.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            }
        }
        
        _imagePanel.setImage(image);
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
            if (ext.length() <= 3) {
                graphics.drawImage(image2, EXT_POS_X, EXT_POS_Y, w2, h2, null);
            } else {
                graphics.drawImage(image2, EXT_POS_X - EXT_SHADOW_DELTA, EXT_POS_Y, w2, h2, null);
            }
            
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return image;
    }
    
    private BufferedImage buildTextImage(String text) {
        
        if (text.length() > 3) {
            text = text.substring(0, 4);
        }
        
        Font font = new Font("Courier", Font.ITALIC | Font.BOLD, 16);

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
            graphics1.setPaint(EXT_SHADOW_COLOR);
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
            graphics2.setPaint(EXT_COLOR);
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
    
    private void setToolTip(File file) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html>");
        sb.append(FILE_SYSTEM_VIEW.getSystemDisplayName(file) + "<br/>");
        sb.append(DATE_FORMAT.format(new Date(file.lastModified())) + "<br/>");
        sb.append(UI_TOOL.getBytesInHuman(file.length()));
        sb.append("</html>");
        
        setToolTipText(sb.toString());
    }
}
