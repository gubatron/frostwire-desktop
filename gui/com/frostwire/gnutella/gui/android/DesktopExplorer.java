package com.frostwire.gnutella.gui.android;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.pushingpixels.flamingo.api.bcb.BreadcrumbItem;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathEvent;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathListener;
import org.pushingpixels.flamingo.api.bcb.core.BreadcrumbFileSelector;

import com.frostwire.gnutella.gui.android.LocalFileListModel.OnRootListener;
import com.limegroup.gnutella.gui.I18n;

public class DesktopExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362861227107918643L;

	private JButton _buttonUp;
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
		
		setSelectedFolder(new File("C:\\Users\\Alden\\Downloads\\FW"));
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
	
	private void setupTop() {
		
		GridBagConstraints c;
		
		_buttonUp = new JButton("UP");
		_buttonUp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonUp_mouseClicked(e);
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(_buttonUp, c);
		
		_buttonFavoriteApplications = setupButtonFavorite(I18n.tr("Applications"), new File("C:\\"));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(_buttonFavoriteApplications, c);
		
		_buttonFavoriteDocuments = setupButtonFavorite(I18n.tr("Documents"), new File("C:\\"));
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		add(_buttonFavoriteDocuments, c);
		
		_buttonFavoritePictures = setupButtonFavorite(I18n.tr("Pictures"), new File("C:\\"));
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		add(_buttonFavoritePictures, c);
		
		_buttonFavoriteVideo = setupButtonFavorite(I18n.tr("Video"), new File("C:\\"));
		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		add(_buttonFavoriteVideo, c);
		
		_buttonFavoriteRingtones = setupButtonFavorite(I18n.tr("Ringtones"), new File("C:\\"));
		c = new GridBagConstraints();
		c.gridx = 5;
		c.gridy = 0;
		add(_buttonFavoriteRingtones, c);
		
		_buttonFavoriteAudio = setupButtonFavorite(I18n.tr("Audio"), new File("C:\\"));
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
			public void mouseClicked(MouseEvent e) {
				setSelectedFolder(path);
			}
		});
		return button;
	}

	private void buttonUp_mouseClicked(MouseEvent e) {
		File path = _model.getRoot().getParentFile();
		if (path != null) {
			setSelectedFolder(path);
		}
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
