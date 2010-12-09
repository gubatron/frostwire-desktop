package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.frostwire.gnutella.gui.HintTextField;
import com.frostwire.gnutella.gui.GraphicPanel;
import com.frostwire.gnutella.gui.SlideshowPanel;
import com.frostwire.gnutella.gui.android.Task.OnChangedListener;

public class DeviceExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716798921645948528L;
	
	private static final String SLIDESHOW_JSON_URL = "http://localhost/~gubatron/slides.php";
	
	private static final String DEVICE = "device";
	private static final String NO_DEVICE = "no-device";
	
	private FileDescriptorListModel _model;
	private Device _device;
	private int _selectedFileType;
	
	private JPanel _panelDevice;
	private JPanel _panelNoDevice;
	private JList _list;
	private JScrollPane _scrollPane;
	private BrowseFileTypeButton _buttonApplications;
	private BrowseFileTypeButton _buttonDocuments;
	private BrowseFileTypeButton _buttonPictures;
	private BrowseFileTypeButton _buttonVideos;
	private BrowseFileTypeButton _buttonRingtones;
	private BrowseFileTypeButton _buttonAudio;
	private JRadioButton _invisibleRadioButton;
	private ButtonGroup _buttonGroup;	
	private HintTextField _textFilter;
	private JLabel _labelLoading;
	
	private Thread _searchThread;

	public DeviceExplorer() {
		_model = new FileDescriptorListModel();
		setupUI();		
		setPanelDevice(false);
	}
	
	public Device getDevice() {
		return _device;
	}

	public void setDevice(Device device) {
		_device = device;
		_model.clear();
		_invisibleRadioButton.setSelected(true);
		setPanelDevice(device != null ? true : false);
	}
	
	public void setPanelDevice(boolean device) {
		CardLayout cl = (CardLayout) getLayout();
		cl.show(this, device ? DEVICE : NO_DEVICE);
		
		if (device) {
    		refreshHeader();
		}
	}
	
	public void refreshHeader() {
	    
	    if (_device == null) {
	        return;
	    }
	    
	    Finger finger = _device.getFinger();
	    refreshBrowseButton(_buttonApplications, finger.numSharedApplicationFiles);
	    refreshBrowseButton(_buttonDocuments, finger.numSharedDocumentFiles);
	    refreshBrowseButton(_buttonPictures, finger.numSharedPictureFiles);
	    refreshBrowseButton(_buttonVideos, finger.numSharedVideoFiles);
	    refreshBrowseButton(_buttonRingtones, finger.numSharedRingtoneFiles);
	    refreshBrowseButton(_buttonAudio, finger.numSharedAudioFiles);
	}
	
	public FileDescriptorListModel getModel() {
	    return _model;
	}
	
	public List<FileDescriptor> getSelectedFileDescriptors() {
        Object[] selectedValues = _list.getSelectedValues();
        
        if (selectedValues == null) {
            return new ArrayList<FileDescriptor>();
        }
        
        ArrayList<FileDescriptor> selectedFileDescriptors = new ArrayList<FileDescriptor>(selectedValues.length);
        for (int i = 0; i < selectedValues.length; i++) {
            selectedFileDescriptors.add((FileDescriptor) selectedValues[i]);
        }
        
        return selectedFileDescriptors;
    }
	
	public int getSelectedFileType() {
	    return _selectedFileType;
	}
	
	protected void setupUI() {
        setLayout(new CardLayout());
        
        _panelDevice = setupPanelDevice();
        _panelNoDevice = new SlideshowPanel(SLIDESHOW_JSON_URL);
        
        add(_panelDevice, DEVICE);
        add(_panelNoDevice, NO_DEVICE);
    }
	
	protected void textFilter_keyTyped(KeyEvent e) {
	    if (_searchThread != null && !_searchThread.isInterrupted()) {
	        _searchThread.interrupt();
	    }
	    
	    String tempStr = _textFilter.getText();
	    
	    char ch = e.getKeyChar();	    
	    if (Character.isLetterOrDigit(ch)) {
	        tempStr += ch;
	    }
	    
	    final String text = tempStr;
	    _searchThread = new Thread(new Runnable() {
            public void run() {
                _model.filter(text);
            }
        });
	    _searchThread.setDaemon(true);
	    _searchThread.start();
    }
	
	private JPanel setupPanelDevice() {
		JPanel panel = new JPanel(new BorderLayout());
		
		GraphicPanel header = new GraphicPanel();
		header.setImage(new UITool().loadImage("device_explorer_background.jpg"));
		header.setLayout(new GridBagLayout());
		
		GridBagConstraints c;
		
		_buttonApplications = setupButtonType(DeviceConstants.FILE_TYPE_APPLICATIONS);
		c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 0, 5);
        header.add(_buttonApplications, c);
        
		_buttonDocuments = setupButtonType(DeviceConstants.FILE_TYPE_DOCUMENTS);
		c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonDocuments, c);
        
		_buttonPictures = setupButtonType(DeviceConstants.FILE_TYPE_PICTURES);
		c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonPictures, c);
        
		_buttonVideos = setupButtonType(DeviceConstants.FILE_TYPE_VIDEOS);
		c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonVideos, c);
        
		_buttonRingtones = setupButtonType(DeviceConstants.FILE_TYPE_RINGTONES);
		c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonRingtones, c);
        
		_buttonAudio = setupButtonType(DeviceConstants.FILE_TYPE_AUDIO);
		c = new GridBagConstraints();
        c.gridx = 5;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonAudio, c);
        
        _buttonGroup = new ButtonGroup();
        _buttonGroup.add(_buttonApplications);
        _buttonGroup.add(_buttonDocuments);
        _buttonGroup.add(_buttonPictures);
        _buttonGroup.add(_buttonVideos);
        _buttonGroup.add(_buttonRingtones);
        _buttonGroup.add(_buttonAudio);
        _buttonGroup.add(_invisibleRadioButton = new JRadioButton());
        
        
        
        _textFilter = new HintTextField("Type here to search");
        _textFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                textFilter_keyTyped(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = 6;
        header.add(_textFilter, c);
        
		panel.add(header, BorderLayout.PAGE_START);
		
		_list = new JList(_model);
		_list.setCellRenderer(new FileDescriptorRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
		_list.setLayoutOrientation(JList.VERTICAL);
		_list.setDragEnabled(true);
		_list.setTransferHandler(new DeviceListTransferHandler());
		_list.setPrototypeCellValue(new FileDescriptor(0, DeviceConstants.FILE_TYPE_AUDIO, "", "", "", "", "", 0));
		_list.setVisibleRowCount(-1);
		
		_scrollPane = new JScrollPane(_list);		
		
		panel.add(_scrollPane, BorderLayout.CENTER);
		
		_labelLoading = new JLabel();
		_labelLoading.setSize(100, 100);
        _labelLoading.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images" + File.separator + "loading.gif"))));
        _labelLoading.setVisible(false);
        _list.add(_labelLoading, BorderLayout.CENTER);
		
		return panel;
	}
	
	private BrowseFileTypeButton setupButtonType(final int type) {	    
	    UITool imageTool = new UITool();
	    BrowseFileTypeButton button = new BrowseFileTypeButton();
		button.setIcon(new ImageIcon(imageTool.loadImage(imageTool.getImageNameByFileType(type))));
		button.setPressedIcon(new ImageIcon(imageTool.loadImage(imageTool.getImageNameByFileType(type) + "_checked")));
		button.setSelectedIcon(new ImageIcon(imageTool.loadImage(imageTool.getImageNameByFileType(type) + "_checked")));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			    buttonType_mouseClicked(e, type);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			    String fileType = "Application";
		    	switch(type) {
		    		case DeviceConstants.FILE_TYPE_AUDIO:
		    			fileType="Audio"; break;
		    		case DeviceConstants.FILE_TYPE_DOCUMENTS:
		    			fileType="Document"; break;
		    		case DeviceConstants.FILE_TYPE_PICTURES:
		    			fileType="Picture"; break;
		    		case DeviceConstants.FILE_TYPE_RINGTONES:
		    			fileType="Ringtone"; break;
		    		case DeviceConstants.FILE_TYPE_VIDEOS:
		    			fileType="Video"; break;
		    	}
			    	
			    _textFilter.setHint("Type here to filter "+ fileType +" files");
			    _textFilter.focusLost(null);
			    _textFilter.clear();
			}
		});
		
		Font font = button.getFont();
		button.setFont(new Font(font.getName(), font.getStyle() | Font.BOLD, font.getSize() + 4));
		
		return button;
	}
	
	private void buttonType_mouseClicked(MouseEvent e, int type) {
	    _selectedFileType = type;
        _textFilter.clear();
        _model.clear();
        
        int x = (_scrollPane.getWidth() - _labelLoading.getWidth()) / 2;
        int y = (_scrollPane.getHeight() - _labelLoading.getHeight()) / 2 - 10;
        _labelLoading.setLocation(x, y);
        _labelLoading.setVisible(true);
        
        BrowseTask browseTask = new BrowseTask(_device, _model, type);
        browseTask.addOnChangedListener(new OnChangedListener() {
            public void onChanged(Task task) {
                if (!task.isRunning()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _labelLoading.setVisible(false);
                        }
                    });
                }
            }
        });
        
        AndroidMediator.addTask(browseTask);
    }

    private void refreshBrowseButton(BrowseFileTypeButton button, int numShared) {
	    button.setText(String.valueOf(numShared));
	    if (numShared == 0 && button.isSelected() && _model.getSize() > 0) {
	        _model.clear();
	    }
	}
}
