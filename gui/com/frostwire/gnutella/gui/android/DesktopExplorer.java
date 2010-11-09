package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.pushingpixels.flamingo.api.bcb.BreadcrumbItem;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathEvent;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathListener;
import org.pushingpixels.flamingo.api.bcb.core.BreadcrumbFileSelector;

public class DesktopExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362861227107918643L;
	
	private LocalFileListModel _model;

	private BreadcrumbFileSelector _breadcrumb;
	private JList _list;
	
	public DesktopExplorer() {
		
		_model = new LocalFileListModel();
		
		setupUI();
	}
	
	public LocalFile getSelectedFolder() {
		return _model.getRoot();
	}
	
	public void setSelectedFolder(LocalFile selectedFolder) {
		_breadcrumb.setPath(selectedFolder.getFile());
		_model.setRoot(selectedFolder);
	}
	
	protected void breadcrumb_pathEvent(BreadcrumbPathEvent event) {
		List<BreadcrumbItem<File>> items = _breadcrumb.getModel().getItems();
		
		if (items.size() > 0) {
			File file = items.get(items.size() - 1).getData();
			_model.setRoot(new LocalFile(file, _model));
		}
	}

	private void setupUI() {
		setLayout(new BorderLayout());
		
		_breadcrumb = new BreadcrumbFileSelector();
		_breadcrumb.getModel().addPathListener(new BreadcrumbPathListener() {
			public void breadcrumbPathEvent(BreadcrumbPathEvent event) {
				breadcrumb_pathEvent(event);
			}
		});
		add(_breadcrumb, BorderLayout.PAGE_START);
		
		_list = new JList(_model);
		_list.setCellRenderer(new LocalFileRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
		_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		_list.setDragEnabled(true);
		_list.setTransferHandler(new DesktopListTransferHandler());
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);

		add(scrollPane, BorderLayout.CENTER);
		
		LocalFile initFolder = new LocalFile(new File("C:\\Users\\Alden\\Downloads\\FW"), _model);
		setSelectedFolder(initFolder);
	}
}
