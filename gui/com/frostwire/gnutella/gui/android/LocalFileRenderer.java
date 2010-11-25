package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.IconUIResource;

import com.apple.laf.AquaIcon;
import com.frostwire.gnutella.gui.ImagePanel;

public class LocalFileRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8723371611297478100L;
	
	public static final int VIEW_THUMBNAIL = 0;
	public static final int VIEW_LIST = 1;
	
	private static Map<Integer, Image> IMAGE_TYPES = new HashMap<Integer, Image>();
	
	private LocalFile _localFile;
	private int _viewType;
	
	private ImagePanel _imagePanelThumbnail;
	private JLabel _labelName;
	
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
		
		setImagePanelThumbnail(_localFile.getFile());
		_labelName.setText(_localFile.getName());
		
		return this;
	}

    public Component getComponentAt(int x, int y) {
		for (int i = 0; i < getComponentCount(); i++)
            if (getComponent(i).getBounds().contains(x, y))
                return getComponent(i);
        
        return null;
    }
	
	protected void setupUI() {
	    
	    _imagePanelThumbnail = new ImagePanel();
	    
	    _labelName = new JLabel();
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
        setLayout(new GridLayout(2, 1));
        Dimension size = new Dimension(100, 100);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        add(_imagePanelThumbnail);
        add(_labelName);
    }
    
    private void layoutList() {
        // TODO Auto-generated method stub
        
    }

    private void setImagePanelThumbnail(File file) {
        Image image = new ImageTool().load("audio");
        _imagePanelThumbnail.setImage(image);
    }
}
