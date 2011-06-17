package com.frostwire.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import com.frostwire.gui.components.GraphicPanel;
import com.limegroup.gnutella.gui.I18n;

public class TaskRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3202019053091139910L;
	
	private static final Color COLOR_SELECTED = new Color(0x8DB2ED);
	private static final Color COLOR_EVEN_ROW = new Color(0xFFFFFF);
	private static final Color COLOR_ODD_ROW = new Color(0xCFE3EA);
	private static final Color COLOR_PROGRESS_LIGHT = new Color(0xFFFFCF);
	private static final Color COLOR_PROGRESS = new Color(0xFCE314);
	
	private static UITool UI_TOOL = new UITool();
	private static Map<Integer, Image> TO_DEVICE_IMAGE_TYPES = new HashMap<Integer, Image>();
	private static Map<Integer, Image> TO_DESKTOP_IMAGE_TYPES = new HashMap<Integer, Image>();
	private static Image IMAGE_STOP;
	
	private Task _task;
	private boolean _selected;
	private int _index;
	
	private GraphicPanel _imagePanel;
	private JLabel _labelText;
	private JLabel _labelPercent;
	private JRadioButton _buttonStop;
	
	public TaskRenderer() {
		setupUI();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		_task = (Task) value;
		_selected = isSelected;
		_index = index;
		
		if (_task instanceof CopyToDesktopTask) {
		    renderDownload((CopyToDesktopTask) _task);
		} else if (_task instanceof CopyToDeviceTask) {
		    renderUpload((CopyToDeviceTask) _task);
		}

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
	    setLayout(new GridBagLayout());
	    
	    GridBagConstraints c;

	    _imagePanel = new GraphicPanel();
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
        
        _labelText = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(_labelText, c);
        
        _labelPercent = new JLabel();
        _labelPercent.setForeground(Color.LIGHT_GRAY);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0, 5, 0, 5);
        add(_labelPercent, c);
        
        _buttonStop = new JRadioButton();
        _buttonStop.setBorder(null);
        _buttonStop.setBackground(null);
        _buttonStop.setFocusable(false);
        _buttonStop.setFocusPainted(false);
        _buttonStop.setContentAreaFilled(false);
        _buttonStop.setIcon(new ImageIcon(loadImageStop()));
        _buttonStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                buttonStop_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 6);
        add(_buttonStop, c);
	}
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        if (_selected) {
            g.setColor(COLOR_SELECTED);
            g.fillRect(0, 0, w, h);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
        }
        
        if (_index % 2 == 0) {
            g.setColor(COLOR_EVEN_ROW);
            g.fillRoundRect(2, 2, w - 4, h - 4, 5, 5);
        } else {
            g.setColor(COLOR_ODD_ROW);
            g.fillRoundRect(2, 2, w - 4, h - 4, 5, 5);
        }
        
        if (_task.getProgress() == 0) {
            
        } else if (_task.getProgress() == 100) {
            
        } else if (_task.isCanceled()) {
            
        } else if (_task.isFailed()) {
            
        } else {
            g.setColor(COLOR_PROGRESS_LIGHT);
            g.fillRoundRect(2, 2, w - 4, h - 4, 5, 5);
            int pw = (w * _task.getProgress()) / 100;
            if (pw > w - 4) {
                pw = w - 4;
            }
            g.setColor(COLOR_PROGRESS);
            g.fillRoundRect(2, 2, pw, h - 4, 5, 5);
        }
    }

	protected void buttonStop_mouseReleased(MouseEvent e) {
        _task.cancel();
        _buttonStop.setVisible(false);
    }
	
	private void renderDownload(CopyToDesktopTask task) {
        Image image = null;
        if (TO_DESKTOP_IMAGE_TYPES.containsKey(task.getFileType())) {
            image = TO_DESKTOP_IMAGE_TYPES.get(task.getFileType());
        } else {
            BufferedImage imageCopyDevice = UI_TOOL.loadImage("copy_device");
            BufferedImage imageFileType = UI_TOOL.loadImage(UI_TOOL.getImageNameByFileType(task.getFileType()));

            image = composeImage(imageCopyDevice, imageFileType, false);
            TO_DESKTOP_IMAGE_TYPES.put(task.getFileType(), image);
        }
        
        _imagePanel.setImage(image);
        
        if (task.getProgress() == 0 && !(task.isCanceled() || task.isFailed())) {
            _labelText.setText(I18n.tr("Download") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("from") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("pending"));
            _buttonStop.setVisible(true);
        } else if (task.getProgress() == 100) {
            _labelText.setText(I18n.tr("Downloaded") +  " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("from") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("done"));
            _buttonStop.setVisible(false);
        } else if (task.isCanceled()) {
            _labelText.setText(I18n.tr("Download") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("from") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("canceled"));
            _buttonStop.setVisible(false);
        } else if (task.isFailed()) {
            _labelText.setText(I18n.tr("Download") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("from") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("error"));
            _buttonStop.setVisible(false);
        } else {
            _labelText.setText(I18n.tr("Downloading") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : (task.getCurrentIndex() + 1) + " " + I18n.tr("out of") + " " + task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("from") + " " + task.getDevice().getName());
            _labelPercent.setText(task.getProgress() + "%");
            _buttonStop.setVisible(true);
        }
    }
	
	private void renderUpload(CopyToDeviceTask task) {
	    
	    Image image = null;
        if (TO_DEVICE_IMAGE_TYPES.containsKey(task.getFileType())) {
            image = TO_DEVICE_IMAGE_TYPES.get(task.getFileType());
        } else {
            BufferedImage imageCopyDevice = UI_TOOL.loadImage("copy_desktop");
            //BufferedImage imageFileType = UI_TOOL.loadImage(UI_TOOL.getImageNameByFileType(task.getFileType()));

            //image = composeImage(imageCopyDevice, imageFileType, true);
            image = imageCopyDevice.getScaledInstance(26, 26, Image.SCALE_SMOOTH);
            TO_DEVICE_IMAGE_TYPES.put(task.getFileType(), image);
        }
        
        _imagePanel.setImage(image);
	    
	    if (task.getProgress() == 0 && !(task.isCanceled() || task.isFailed())) {
	    	if (task.isWaitingForAuthorization()) {
		        _labelText.setText(I18n.tr("Waiting for authorization from ") + " " + task.getDevice().getName());
	            _labelPercent.setText("0%");
	    	} else {
		        _labelText.setText(I18n.tr("Upload") + " " +
		                (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
		                " " + I18n.tr("to") + " " + task.getDevice().getName());
		        _labelPercent.setText(I18n.tr("pending"));
		        _buttonStop.setVisible(true);
	    	}
	    } else if (task.isCanceled()) {
	        _labelText.setText(I18n.tr("Upload") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("to") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("canceled"));
            _buttonStop.setVisible(false);
	    } else if (task.isFailed()) {
	        _labelText.setText(I18n.tr("Upload") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("to") + " " + task.getDevice().getName());
            _labelPercent.setText(I18n.tr("error"));
            _buttonStop.setVisible(false);
	    } else if (task.getProgress() == 100) {
	        _labelText.setText(I18n.tr("Uploaded") +  " " +
	                (task.getTotalItems() == 1 ? I18n.tr("one file") : task.getTotalItems() + " " + I18n.tr("files")) +
	                " " + I18n.tr("to") + " " + task.getDevice().getName());
	        _labelPercent.setText(I18n.tr("done"));
	        _buttonStop.setVisible(false);
	    } else {
	        _labelText.setText(I18n.tr("Uploading") + " " +
                    (task.getTotalItems() == 1 ? I18n.tr("one file") : (task.getCurrentIndex() + 1) + " " + I18n.tr("out of") + " " + task.getTotalItems() + " " + I18n.tr("files")) +
                    " " + I18n.tr("to") + " " + task.getDevice().getName());
            _labelPercent.setText(task.getProgress() + "%");
            _buttonStop.setVisible(true);
	    }
    }
	
	private Image loadImageStop() {
        if (IMAGE_STOP != null) {
            return IMAGE_STOP;
        }
        
        return IMAGE_STOP = UI_TOOL.loadImage("stop").getScaledInstance(24, 24, Image.SCALE_SMOOTH);
    }
	
	private Image composeImage(BufferedImage imageCopyDevice, BufferedImage imageFileType, boolean left) {
        
        BufferedImage image1 = imageCopyDevice;
        Image image2 = imageFileType.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        
        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image2.getWidth(null);
        int h2 = image2.getHeight(null);
        
        int w = 64;
        int h = 64;
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D graphics = image.createGraphics();
        
        try {
            
            graphics.drawImage(image1,  0,  4, w1, h1, null);
            
            if (left) {
                graphics.drawImage(image2, 0, 20, w2, h2, null);
            } else {
                graphics.drawImage(image2, 32, 20, w2, h2, null);
            }
            
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
    }
}
