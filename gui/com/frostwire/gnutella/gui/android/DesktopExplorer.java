package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import org.pushingpixels.flamingo.api.bcb.BreadcrumbItem;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathEvent;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathListener;
import org.pushingpixels.flamingo.api.bcb.core.BreadcrumbFileSelector;

import com.frostwire.gnutella.gui.android.LocalFileListModel.OnRootListener;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class DesktopExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362861227107918643L;

	private JToolBar _toolBar;
	private JButton _buttonUp;
	private JButton _buttonNew;
	private JButton _buttonViewThumbnail;
	private JButton _buttonViewList;
	private JButton _buttonFavoriteApplications;
	private JButton _buttonFavoriteDocuments;
	private JButton _buttonFavoritePictures;
	private JButton _buttonFavoriteVideo;
	private JButton _buttonFavoriteRingtones;
	private JButton _buttonFavoriteAudio;
	private BreadcrumbFileSelector _breadcrumb;
	private JList _list;
	private JScrollPane _scrollPane;
	
	private LocalFileListModel _model;
	
	public DesktopExplorer() {
		
		_model = new LocalFileListModel();
		_model.setOnRootListener(new OnRootListener() {
			public void onRoot(LocalFileListModel localFileListModel, File path) {
				_breadcrumb.setPath(path);
			}
		});
		
		setupUI();
		
		setSelectedFolder(SharingSettings.getDeviceFilesDirectory()); // guarantee the creation of files
	}
	
	public File getSelectedFolder() {
		return _model.getRoot();
	}
	
	public void setSelectedFolder(File path) {
		_model.setRoot(path);
	}
	
	public void refresh() {
		_model.refresh();
	}
	
	protected void setupUI() {
		setLayout(new GridBagLayout());
		
		setupTop();
		setupList();
	}
	
	protected void buttonUp_mousePressed(MouseEvent e) {
        File path = _model.getRoot().getParentFile();
        if (path != null) {
            setSelectedFolder(path);
        }
    }
    
    protected void buttonNew_mousePressed(MouseEvent e) {
        LocalFile localFile = _model.createNewFolder();
        if (localFile != null) {
            _list.setSelectedValue(localFile, true);
        }
    }
    
    protected void buttonViewThumbnail_mousePressed(MouseEvent e) {
        _list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    }
    
    protected void buttonViewList_mousePressed(MouseEvent e) {
        _list.setLayoutOrientation(JList.VERTICAL);
    }
	
	private void setupTop() {
		
		GridBagConstraints c;
		
		_toolBar = new JToolBar();
		_toolBar.setFloatable(false);
		_toolBar.setRollover(true);
		
		Dimension toolBarButtonSize = new Dimension(28, 28);
		
		_buttonUp = new JButton();
		_buttonUp.setIcon(new ImageIcon(new ImageTool().load("folder_up")));
		_buttonUp.setPreferredSize(toolBarButtonSize);
		_buttonUp.setMinimumSize(toolBarButtonSize);
		_buttonUp.setMaximumSize(toolBarButtonSize);
		_buttonUp.setSize(toolBarButtonSize);
		_buttonUp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				buttonUp_mousePressed(e);
			}
		});
		_toolBar.add(_buttonUp);
		
		_buttonNew = new JButton();
		_buttonNew.setIcon(new ImageIcon(new ImageTool().load("folder_new")));
		_buttonNew.setPreferredSize(toolBarButtonSize);
		_buttonNew.setMinimumSize(toolBarButtonSize);
		_buttonNew.setMaximumSize(toolBarButtonSize);
		_buttonNew.setSize(toolBarButtonSize);
		_buttonNew.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonNew_mousePressed(e);
            }
        });
        _toolBar.add(_buttonNew);
        
        _toolBar.addSeparator();
        
        _buttonViewThumbnail = new JButton();
        _buttonViewThumbnail.setIcon(new ImageIcon(new ImageTool().load("view_thumbnail")));
        _buttonViewThumbnail.setPreferredSize(toolBarButtonSize);
        _buttonViewThumbnail.setMinimumSize(toolBarButtonSize);
        _buttonViewThumbnail.setMaximumSize(toolBarButtonSize);
        _buttonViewThumbnail.setSize(toolBarButtonSize);
        _buttonViewThumbnail.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonViewThumbnail_mousePressed(e);
            }
        });
        _toolBar.add(_buttonViewThumbnail);
        
        _buttonViewList = new JButton();
        _buttonViewList.setIcon(new ImageIcon(new ImageTool().load("view_list")));
        _buttonViewList.setPreferredSize(toolBarButtonSize);
        _buttonViewList.setMinimumSize(toolBarButtonSize);
        _buttonViewList.setMaximumSize(toolBarButtonSize);
        _buttonViewList.setSize(toolBarButtonSize);
        _buttonViewList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonViewList_mousePressed(e);
            }
        });
        _toolBar.add(_buttonViewList);
        
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(_toolBar, c);
		
		_buttonFavoriteApplications = setupButtonFavorite(I18n.tr("Applications"), SharingSettings.DEVICE_APPLICATIONS_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(_buttonFavoriteApplications, c);
		
		_buttonFavoriteDocuments = setupButtonFavorite(I18n.tr("Documents"), SharingSettings.DEVICE_DOCUMENTS_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		add(_buttonFavoriteDocuments, c);
		
		_buttonFavoritePictures = setupButtonFavorite(I18n.tr("Pictures"), SharingSettings.DEVICE_PICTURES_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		add(_buttonFavoritePictures, c);
		
		_buttonFavoriteVideo = setupButtonFavorite(I18n.tr("Video"), SharingSettings.DEVICE_VIDEO_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		add(_buttonFavoriteVideo, c);
		
		_buttonFavoriteRingtones = setupButtonFavorite(I18n.tr("Ringtones"), SharingSettings.DEVICE_RINGTONES_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 5;
		c.gridy = 0;
		add(_buttonFavoriteRingtones, c);
		
		_buttonFavoriteAudio = setupButtonFavorite(I18n.tr("Audio"), SharingSettings.DEVICE_AUDIO_FILES_DIR);
		c = new GridBagConstraints();
		c.gridx = 6;
		c.gridy = 0;
		add(_buttonFavoriteAudio, c);
		
		_breadcrumb = new BreadcrumbFileSelector();
		_breadcrumb.getModel().addPathListener(new BreadcrumbPathListener() {
			public void breadcrumbPathEvent(BreadcrumbPathEvent event) {
				breadcrumb_pathEvent(event);
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 8; // this put a extra column and perform a nice fill at the end in the top row
		add(_breadcrumb, c);
	}

    private void setupList() {
		
		GridBagConstraints c;
		
		_list = new JList(_model);
		_list.setCellRenderer(new LocalFileRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
		_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		_list.setDragEnabled(true);
		_list.setTransferHandler(new DesktopListTransferHandler());
		_list.setVisibleRowCount(-1);
		
		_scrollPane = new JScrollPane(_list);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 8;

		add(_scrollPane, c);
	}

	private JButton setupButtonFavorite(String text, final File path) {
		JButton button = new JButton(text);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				setSelectedFolder(path);
			}
		});
		return button;
	}
	
	private void breadcrumb_pathEvent(BreadcrumbPathEvent event) {
		List<BreadcrumbItem<File>> items = _breadcrumb.getModel().getItems();
		
		if (items.size() > 0) {
			File path = items.get(items.size() - 1).getData();
			OnRootListener listener = _model.getOnRootListener();
			_model.setOnRootListener(null); // avoid infinite recursion
			_model.setRoot(path);
			_model.setOnRootListener(listener);
		}
	}
}
