package com.limegroup.gnutella.gui.library;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.limewire.collection.Tuple;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.frostwire.components.TorrentSaveFolderComponent;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileDetails;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.downloader.CantResumeException;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.CheckBoxList;
import com.limegroup.gnutella.gui.CheckBoxListPanel;
import com.limegroup.gnutella.gui.FileDescProvider;
import com.limegroup.gnutella.gui.FileDetailsProvider;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.LicenseWindow;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.MultiLineLabel;
import com.limegroup.gnutella.gui.actions.ActionUtils;
import com.limegroup.gnutella.gui.actions.CopyMagnetLinkToClipboardAction;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.playlist.PlaylistMediator;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.util.CoreExceptionHandler;
import com.limegroup.gnutella.gui.util.GUILauncher;
import com.limegroup.gnutella.gui.util.GUILauncher.LaunchableProvider;
import com.limegroup.gnutella.library.SharingUtils;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.licenses.VerificationListener;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.QueryUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/**
 * This class wraps the JTable that displays files in the library,
 * controlling access to the table and the various table properties.
 * It is the Mediator to the Table part of the Library display.
 */
final class LibraryTableMediator extends AbstractTableMediator<LibraryTableModel, LibraryTableDataLine, File>
	implements VerificationListener, FileDetailsProvider {
	
	/**
     * Variables so the PopupMenu & ButtonRow can have the same listeners
     */
    public static Action LAUNCH_ACTION;
    public static Action OPEN_IN_FOLDER_ACTION;
    public static Action ENQUEUE_ACTION;
	public static Action DELETE_ACTION;
    public static Action RENAME_ACTION;
	
    private Action MAGNET_LOOKUP_ACTION;
	private Action COPY_MAGNET_TO_CLIPBOARD_ACTION;

    /**
     * instance, for singelton access
     */
    private static LibraryTableMediator INSTANCE;

    public static LibraryTableMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryTableMediator();
        }
        return INSTANCE;
    }
    
    /**
     * Build some extra listeners
     */
    protected void buildListeners() {
        super.buildListeners();

        LAUNCH_ACTION = new LaunchAction();
        OPEN_IN_FOLDER_ACTION = new OpenInFolderAction();
        ENQUEUE_ACTION = new EnqueueAction();
		DELETE_ACTION = new RemoveAction();
        RENAME_ACTION = new RenameAction();
        MAGNET_LOOKUP_ACTION = new MagnetLookupAction();
		COPY_MAGNET_TO_CLIPBOARD_ACTION = new CopyMagnetLinkToClipboardAction(this);
    }

    /**
     * Set up the constants
     */
    protected void setupConstants() {
		MAIN_PANEL = null;
		DATA_MODEL = new LibraryTableModel();
		TABLE = new LimeJTable(DATA_MODEL);
		DATA_MODEL.setTable(TABLE);
		Action[] aa = new Action[] { 
				LAUNCH_ACTION,
				ENQUEUE_ACTION,
				DELETE_ACTION
		};
		
		BUTTON_ROW = new ButtonRow(aa, ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
    }

    // inherit doc comment
    protected JPopupMenu createPopupMenu() {
		if (TABLE.getSelectionModel().isSelectionEmpty())
			return null;
        
        JPopupMenu menu = new SkinPopupMenu();
        
		menu.add(new SkinMenuItem(LAUNCH_ACTION));
		if (hasExploreAction()) {
		    menu.add(new SkinMenuItem(OPEN_IN_FOLDER_ACTION));
		}
		menu.addSeparator();
		menu.add(new SkinMenuItem(DELETE_ACTION));
		menu.add(new SkinMenuItem(RENAME_ACTION));
		menu.addSeparator();
		
        int[] rows = TABLE.getSelectedRows();
		boolean dirSelected = false;
		boolean fileSelected = false;

		for (int i = 0; i < rows.length; i++) {
			File f = DATA_MODEL.get(rows[i]).getFile();
			if (f.isDirectory()) {
				dirSelected = true;
//				if (IncompleteFileManager.isTorrentFolder(f))
//					torrentSelected = true;
			} else
				fileSelected = true;
			
			if (dirSelected && fileSelected)
				break;
		}
		if (dirSelected) {
	        if (GUIMediator.isPlaylistVisible())
	            ENQUEUE_ACTION.setEnabled(false);
	        DELETE_ACTION.setEnabled(true);
	        RENAME_ACTION.setEnabled(false);
		} else {
	        if (GUIMediator.isPlaylistVisible() && PlaylistMediator.isPlayableFile(DATA_MODEL.getFile(rows[0])) )
	            ENQUEUE_ACTION.setEnabled(true);
	        DELETE_ACTION.setEnabled(true);
	        // only allow single selection for renames
	        RENAME_ACTION.setEnabled(LibraryMediator.isRenameEnabled() && rows.length == 1);
		}
		menu.addSeparator();
		
        LibraryTableDataLine line = DATA_MODEL.get(rows[0]);
		menu.add(createSearchSubMenu(line));
		menu.add(createAdvancedMenu(line));

		return menu;
    }

	private JMenu createAdvancedMenu(LibraryTableDataLine dl) {
		JMenu menu = new SkinMenu(I18n.tr("Advanced"));
		if (dl != null) {
			menu.add(new SkinMenuItem(MAGNET_LOOKUP_ACTION));
			menu.add(new SkinMenuItem(COPY_MAGNET_TO_CLIPBOARD_ACTION));
			File file = getFile(TABLE.getSelectedRow());
			menu.setEnabled(GuiCoreMediator.getFileManager().isFileShared(file)); 
		}
		
        if (menu.getItemCount() == 0)
            menu.setEnabled(false);

		return menu;
	}
	

	private JMenu createSearchSubMenu(LibraryTableDataLine dl) {
		JMenu menu = new SkinMenu(I18n.tr("Search"));
        
        if(dl != null) {
            File f = dl.getInitializeObject();
    		String keywords = QueryUtils.createQueryString(f.getName());
            if (keywords.length() > 2)
    			menu.add(new SkinMenuItem(new SearchAction(keywords)));
    		
    		LimeXMLDocument doc = dl.getXMLDocument();
    		if(doc != null) {
                Action[] actions = ActionUtils.createSearchActions(doc);
        		for (int i = 0; i < actions.length; i++)
        			menu.add(new SkinMenuItem(actions[i]));
            }
        }
        
        if(menu.getItemCount() == 0)
            menu.setEnabled(false);
            
        return menu;
	}

	/**
     * Upgrade getScrolledTablePane to public access.
     */
    public JComponent getScrolledTablePane() {
        return super.getScrolledTablePane();
    }

    /* Don't display anything for this.  The LibraryMediator will do it. */
	protected void updateSplashScreen() {}

    /**
     * Note: This is set up for this to work.
     * Polling is not needed though, because updates
     * already generate update events.
     */
    private LibraryTableMediator() {
        super("LIBRARY_TABLE");
        ThemeMediator.addThemeObserver(this);
    }
    
    /**
     * Sets up drag & drop for the table.
     */
    protected void setupDragAndDrop() {
    	TABLE.setDragEnabled(true);
    	TABLE.setTransferHandler(new LibraryTableTransferHandler());
    }

	/**
	 * there is no actual component that holds all of this table.
	 * The LibraryMediator is real the holder.
	 */
	public JComponent getComponent() {
		return null;
	}
	
	/**
	 * Allows annotation once XML is set up
	 *
	 * @param enabled whether or not annotation is allowed
	 */
	public void setAnnotateEnabled(boolean enabled) {
		
	    LibraryTableDataLine.setXMLEnabled(enabled);
	    DATA_MODEL.refresh();
		
	    handleSelection(-1);
	}
	
    /**
     * Sets the default editors.
     */
    protected void setDefaultEditors() {
        TableColumnModel model = TABLE.getColumnModel();
        TableColumn tc = model.getColumn(LibraryTableDataLine.NAME_IDX);
        tc.setCellEditor(new LibraryTableCellEditor(this));
    }


	/**
	 * Cancels all editing of fields in the tree and table.
	 */
	void cancelEditing() {
		if(TABLE.isEditing()) {
			TableCellEditor editor = TABLE.getCellEditor();
			editor.cancelCellEditing();
		}	    
	}

	/**
	 * Adds the mouse listeners to the wrapped <tt>JTable</tt>.
	 *
	 * @param listener the <tt>MouseInputListener</tt> that handles mouse events
	 *                 for the library
	 */
	void addMouseInputListener(final MouseInputListener listener) {
        TABLE.addMouseListener(listener);
        TABLE.addMouseMotionListener(listener);
	}

	/**
	 * Updates the Table based on the selection of the given table.
	 * Perform lookups to remove any store files from the shared folder
     * view and to only display store files in the store view
	 */
    void updateTableFiles(DirectoryHolder dirHolder) {
		if (dirHolder == null)
			return;
		clearTable();
		File[] files = dirHolder.getFiles();
        
        for (int i = 0; i < files.length; i++) {
        	addUnsorted(files[i]);
        }
		forceResort();
    }
	
	/**
	 * Returns the <tt>File</tt> stored at the specified row in the list.
	 *
	 * @param row the row of the desired <tt>File</tt> instance in the
	 *            list
	 *
	 * @return a <tt>File</tt> instance associated with the specified row
	 *         in the table
	 */
    File getFile(int row) {
		return DATA_MODEL.getFile(row);
    }
	
	/**
	 * Returns the file desc object for the given row or <code>null</code> if
	 * there is none.
	 * @param row
	 * @return
	 */
	private FileDesc getFileDesc(int row) {
		return DATA_MODEL.getFileDesc(row);
	}
	
	/**
	 * Implements the {@link FileDescProvider} interface by returning all the
	 * selected filedescs.
	 */
	public FileDetails[] getFileDetails() {
		int[] sel = TABLE.getSelectedRows();
		List<FileDetails> files = new ArrayList<FileDetails>(sel.length);
		for (int i = 0; i < sel.length; i++) {
			FileDesc desc = getFileDesc(sel[i]);
			if (desc != null) {
			    //DPINJ: Fix!
				files.add(GuiCoreMediator.getLocalFileDetailsFactory().create(desc));
			}
		}
		if (files.isEmpty()) {
			return new FileDetails[0];
		}
		return files.toArray(new FileDetails[0]);
	}
    
    /**
	 * Accessor for the table that this class wraps.
	 *
	 * @return The <tt>JTable</tt> instance used by the library.
	 */
    JTable getTable() {
        return TABLE;
    }

    ButtonRow getButtonRow() {
        return BUTTON_ROW;
    }
    
    LibraryTableDataLine[] getSelectedLibraryLines() {
    	int[] selected = TABLE.getSelectedRows();
        LibraryTableDataLine[] lines = new LibraryTableDataLine[selected.length];
        for(int i = 0; i < selected.length; i++)
            lines[i] = DATA_MODEL.get(selected[i]);
        return lines;
    }

    /**
     * Accessor for the <tt>ListSelectionModel</tt> for the wrapped
     * <tt>JTable</tt> instance.
     */
    ListSelectionModel getSelectionModel() {
            return TABLE.getSelectionModel();
    }
    
    /**
     * Programatically starts a rename of the selected item.
     */
    void startRename() {
        int row = TABLE.getSelectedRow();
        if(row == -1)
            return;
        int viewIdx = TABLE.convertColumnIndexToView(LibraryTableDataLine.NAME_IDX);
        TABLE.editCellAt(row, viewIdx, LibraryTableCellEditor.EVENT);
    }
    
    /**
     * Shows the license window.
     */
    void showLicenseWindow() {
        LibraryTableDataLine ldl = DATA_MODEL.get(TABLE.getSelectedRow());
        if(ldl == null)
            return;
        FileDesc fd = ldl.getFileDesc();
        License license = fd.getLicense();
        URN urn = fd.getSHA1Urn();
        LimeXMLDocument doc = ldl.getXMLDocument();
        LicenseWindow window = LicenseWindow.create(license, urn, doc, this);
        GUIUtils.centerOnScreen(window);
        window.setVisible(true);
    }
    

    public void licenseVerified(License license) {
        DATA_MODEL.refresh();
    }

	
    /**
     * Returns the options offered to the user when removing files.
     * 
     * Depending on the platform these can be a subset of 
     * MOVE_TO_TRASH, DELETE, CANCEL.
     */
    private static Object[] createRemoveOptions() {
        if (OSUtils.supportsTrash()) {
            String trashLabel = OSUtils.isWindows() ? I18n.tr("Move to Recycle Bin")
                    : I18n.tr("Move to Trash");
            return new Object[] {
                    trashLabel, 
                    I18n.tr("Delete"),
                    I18n.tr("Cancel") 
            };
        }
        else {
            return new Object[] {
                    I18n.tr("Delete"),
                    I18n.tr("Cancel") 
            };
        }
    }
    
    /**
     * Override the default removal so we can actually stop sharing
     * and delete the file.
	 * Deletes the selected rows in the table.
	 * CAUTION: THIS WILL DELETE THE FILE FROM THE DISK.
	 */
	public void removeSelection() {
	    int[] rows = TABLE.getSelectedRows();
        if (rows.length == 0)
            return;

        if (TABLE.isEditing()) {
            TableCellEditor editor = TABLE.getCellEditor();
            editor.cancelCellEditing();
        }

        List<Tuple<File, FileDesc>> files = new ArrayList<Tuple<File, FileDesc>>(rows.length);
        
        // sort row indices and go backwards so list indices don't change when
        // removing the files from the model list
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            File file = DATA_MODEL.getFile(rows[i]);
            FileDesc fd = DATA_MODEL.getFileDesc(rows[i]);
            files.add(new Tuple<File, FileDesc>(file, fd));
        }
        
        CheckBoxListPanel<Tuple<File, FileDesc>> listPanel =
            new CheckBoxListPanel<Tuple<File, FileDesc>>(files, new TupleTextProvider(), true);
        listPanel.getList().setVisibleRowCount(4);
        
        // display list of files that should be deleted
        Object[] message = new Object[] {
                new MultiLineLabel(I18n.tr("Are you sure you want to delete the selected file(s), thus removing it from your computer?"), 400),
                Box.createVerticalStrut(ButtonRow.BUTTON_SEP),
                listPanel,
                Box.createVerticalStrut(ButtonRow.BUTTON_SEP)
        };
        
        // get platform dependent options which are displayed as buttons in the dialog
        Object[] removeOptions = createRemoveOptions();
        
        int option = JOptionPane.showOptionDialog(MessageService.getParentComponent(),
                message,
                I18n.tr("Message"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                removeOptions,
                removeOptions[0] /* default option */);
        
        if (option == removeOptions.length - 1 /* "cancel" option index */ 
                || option == JOptionPane.CLOSED_OPTION) {
            return;
        }
        
        // remove still selected files
        List<Tuple<File, FileDesc>> selected = listPanel.getSelectedElements();
        List<String> undeletedFileNames = new ArrayList<String>();
        FileManager fileManager = GuiCoreMediator.getFileManager();

        for (Tuple<File, FileDesc> tuple : selected) {
            File file = tuple.getFirst();
            FileDesc fd = tuple.getSecond();
//            if (_isIncomplete && hasActiveDownloader(file)) {
//                undeletedFileNames.add(getCompleteFileName(file));
//                continue;
//            }
            
            if (fd != null) 
                fileManager.removeFileIfShared(file);
            
            if (fd != null) { 
                GuiCoreMediator.getUploadManager().killUploadsForFileDesc(fd);
            }

            // removeOptions > 2 => OS offers trash options
            boolean removed = FileUtils.delete(file, removeOptions.length > 2 && option == 0 /* "move to trash" option index */);
            if (removed) {
                DATA_MODEL.remove(DATA_MODEL.getRow(file));
            }
            else {
                undeletedFileNames.add(getCompleteFileName(file));
            }
        }

        
		clearSelection();		
		
		if (undeletedFileNames.isEmpty()) {
			return;
		}
		
		// display list of files that could not be deleted
		message = new Object[] {
				new MultiLineLabel(I18n.tr("The following files could not be deleted. They may be in use by another application or are currently being downloaded to."), 400),
				Box.createVerticalStrut(ButtonRow.BUTTON_SEP),
				new JScrollPane(createFileList(undeletedFileNames))
		};
	
		JOptionPane.showMessageDialog(MessageService.getParentComponent(), 
				message,
				I18n.tr("Error"),
				JOptionPane.ERROR_MESSAGE);
    }
	
	/**
	 * Creates a JList of files and sets and makes it non-selectable. 
	 */
	private static JList createFileList(List<String> fileNames) {
	    JList fileList = new JList(fileNames.toArray());
        fileList.setVisibleRowCount(5);
        fileList.setCellRenderer(new FileNameListCellRenderer());
        //fileList.setSelectionForeground(fileList.getForeground());
        //fileList.setSelectionBackground(fileList.getBackground());
        fileList.setFocusable(false);
        return fileList;
	}
	
	/**
	 * Returns the human readable file name for incomplete files or
	 * just the regular file name otherwise. 
	 */
	private String getCompleteFileName(File file) {
		return file.getName();
	}
    
	/**
	 * Handles a name change of one of the files displayed.
	 *
	 * @param newName The new name of the file
	 *
	 * @return A <tt>String</tt> that is the name of the file
	 *         after this method is called. This is the new name if
	 *         the name change succeeded, and the old name otherwise.
	 */
	String handleNameChange(String newName) {
		int row = TABLE.getEditingRow();
		LibraryTableModel ltm = DATA_MODEL;
		
		File oldFile = ltm.getFile(row);
		String parent = oldFile.getParent();
		String nameWithExtension = newName + "." + ltm.getType(row);
		File newFile = new File(parent, nameWithExtension);
        if (!ltm.getName(row).equals(newName)) {
            if (oldFile.renameTo(newFile)) {
                GuiCoreMediator.getFileManager().renameFileIfSharedOrStore(oldFile, newFile);
                // Ideally, renameFileIfShared should immediately send RENAME or REMOVE
                // callbacks. But, if it doesn't, it should atleast have immediately
                // internally removed the file from being shared. So, we immediately
                // do a reinitialize on the oldFile to mark it as being not shared.
                DATA_MODEL.reinitialize(oldFile);
                return newName;
            }

            // notify the user that renaming failed
            GUIMediator.showError(I18n.tr("Unable to rename the file \'{0}\'. It may be in use by another application.", ltm.getName(row)));
			return ltm.getName(row);
		}
		return newName; 
	}

    public void handleActionKey() {
        int[] rows = TABLE.getSelectedRows();
		LibraryTableModel ltm = DATA_MODEL;
		File file;
		for (int i = 0; i < rows.length; i++) {
			file = ltm.getFile(rows[i]);
			// if it's a directory try to select it in the library tree
			// if it could be selected return
			if (file.isDirectory() 
				&& LibraryMediator.setSelectedDirectory(file))
				return;
		}
		launch();
    }

    /**
     * Resume incomplete downloads
     */    
    void resumeIncomplete() {        
        //For each selected row...
        int[] rows = TABLE.getSelectedRows();
        boolean startedDownload=false;
        List<Exception> errors = new ArrayList<Exception>();
        for (int i=0; i<rows.length; i++) {
            //...try to download the incomplete
            File incomplete = DATA_MODEL.getFile(rows[i]);
            try {
                GuiCoreMediator.getDownloadServices().download(incomplete);
                startedDownload=true;
            } catch (SaveLocationException e) { 
                // we must cache errors to display later so we don't wait
                // while the table might change in the background.
                errors.add(e);
            } catch(CantResumeException e) {
                errors.add(e);
            }
        }
        
        // traverse back through the errors and show them.
        for(int i = 0; i < errors.size(); i++) {
            Exception e = errors.get(i);
            if(e instanceof SaveLocationException) {
				SaveLocationException sle = (SaveLocationException)e;
				if (sle.getErrorCode() == SaveLocationException.FILE_ALREADY_DOWNLOADING) {
					GUIMediator.showError(I18n.tr("You are already downloading this file to \"{0}\".", sle.getFile()),
					        QuestionsHandler.ALREADY_DOWNLOADING);
				}
				else {
					String msg = CoreExceptionHandler.getSaveLocationErrorString(sle);
					GUIMediator.showError(msg);
				}
            } else if ( e instanceof CantResumeException ) {
                GUIMediator.showError(I18n.tr("The file \"{0}\" is not a valid incomplete file and cannot be resumed.", 
                        ((CantResumeException)e).getFilename()),
                        QuestionsHandler.CANT_RESUME);
            }
        }       

        //Switch to download tab (if we actually started anything).
        if (startedDownload)
            switchToDownloadTab();
    }

    private void switchToDownloadTab() {
        GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
    }

    /**
	 * Launches the associated applications for each selected file
	 * in the library if it can.
	 */
    void launch() {
    	int[] rows = TABLE.getSelectedRows();
        if (rows.length == 0) {
       	 return;
        }
    	LaunchableProvider[] providers = new LaunchableProvider[rows.length];
//    	if (_isIncomplete) {
//    		for (int i = 0; i < rows.length; i++) {
//				providers[i] = new IncompleteProvider(DATA_MODEL.getFile(rows[i]));
//			}
//    	}
//    	else {
    		for (int i = 0; i < rows.length; i++) {
				providers[i] = new CompleteProvider(DATA_MODEL.getFile(rows[i]));
			}
    	//}
    	GUILauncher.launch(providers);
    }

	/**
	 * Handles the selection rows in the library window,
	 * enabling or disabling buttons and chat menu items depending on
	 * the values in the selected rows.
	 * 
	 * @param row the index of the first row that is selected
	 */
	public void handleSelection(int row) {
		int[] sel = TABLE.getSelectedRows();
		if (sel.length == 0) {
			handleNoSelection();
			return;
		} 
		
		File selectedFile = getFile(sel[0]);
		boolean firstShared = GuiCoreMediator.getFileManager().isFileShared(selectedFile);
		
		//  always turn on Launch, Delete, Magnet Lookup, Bitzi Lookup
		LAUNCH_ACTION.setEnabled(true);
		DELETE_ACTION.setEnabled(true);
		
		if (sel.length == 1 && selectedFile.isFile() && selectedFile.getParentFile() != null) {
            OPEN_IN_FOLDER_ACTION.setEnabled(true);
        } else {
            OPEN_IN_FOLDER_ACTION.setEnabled(false);
        }
		
		//  turn on Enqueue if play list is visible and a selected item is playable
		if (GUIMediator.isPlaylistVisible()) {
			boolean found = false;
			for (int i = 0; i < sel.length; i++)
	            if (PlaylistMediator.isPlayableFile(DATA_MODEL.getFile(sel[i]))) {
					found = true;
					break;
	            }
			ENQUEUE_ACTION.setEnabled(found);
        } else
			ENQUEUE_ACTION.setEnabled(false);

		RENAME_ACTION.setEnabled(LibraryMediator.isRenameEnabled() && sel.length == 1);
		 
		//  enable Share File action when any selected file is not shared
		boolean shareAllowed = false;
		boolean unshareAllowed = false;
		boolean shareFolderAllowed = false;
		boolean unshareFolderAllowed = false;
		boolean foundDir = false;
		for (int i = 0; i < sel.length; i++) {
			File file = getFile(sel[i]);
			if (file.isDirectory()) {
				
				//  turn off delete (only once) if non-torrent directory found
				if (!foundDir){
					DELETE_ACTION.setEnabled(false);
					foundDir = true;
				}
				if (!GuiCoreMediator.getFileManager().isFolderShared(file))
					shareFolderAllowed = true;
				else
					unshareFolderAllowed = true;
				
			} else {
				if (!GuiCoreMediator.getFileManager().isFileShared(file)) {
					if (!SharingUtils.isFilePhysicallyShareable(file))
						continue;
					shareAllowed = true;
				} else {
					unshareAllowed = true;
				}
				
				if (shareAllowed && unshareAllowed && shareFolderAllowed && unshareFolderAllowed)
					break;
			}
			
			if (TorrentSaveFolderComponent.isParentOrChild(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue(), file, "")) {
				shareAllowed = false;
				unshareAllowed = false;
				shareFolderAllowed = false;
				unshareFolderAllowed = false;
			}
			
			
		}
		
		//  enable / disable advanced items if file shared / not shared
		MAGNET_LOOKUP_ACTION.setEnabled(firstShared);

		COPY_MAGNET_TO_CLIPBOARD_ACTION.setEnabled(getFileDesc(sel[0]) != null);
	}

	/**
	 * Handles the deselection of all rows in the library table,
	 * disabling all necessary buttons and menu items.
	 */
	public void handleNoSelection() {
		LAUNCH_ACTION.setEnabled(false);
		OPEN_IN_FOLDER_ACTION.setEnabled(false);
		ENQUEUE_ACTION.setEnabled(false);
		DELETE_ACTION.setEnabled(false);
		
		RENAME_ACTION.setEnabled(false);

		COPY_MAGNET_TO_CLIPBOARD_ACTION.setEnabled(false);
		MAGNET_LOOKUP_ACTION.setEnabled(false);
	}

	/**
	 * Refreshes the enabledness of the Enqueue button based
	 * on the player enabling state. 
	 */
	public void setPlayerEnabled(boolean value) {
		handleSelection(TABLE.getSelectedRow());
	}

	public boolean setFileSelected(File file) {
	    int i = DATA_MODEL.getRow(file);
	    if (i != -1) {
	        TABLE.setSelectedRow(i);
	        TABLE.ensureSelectionVisible();
	        return true;
	    }
	    return false;
	}
	
	private boolean hasExploreAction() {
        return OSUtils.isWindows() || OSUtils.isMacOSX();
    }

    ///////////////////////////////////////////////////////
    //  ACTIONS
    ///////////////////////////////////////////////////////

    private final class LaunchAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 949208465372392591L;

        public LaunchAction () {
			putValue(Action.NAME, I18n.tr
					("Launch"));
			putValue(Action.SHORT_DESCRIPTION,
					 I18n.tr("Launch Selected Files"));
			putValue(LimeAction.ICON_NAME, "LIBRARY_LAUNCH");
		}
		
        public void actionPerformed(ActionEvent ae) {
			launch();
        }
    }
    
    private final class OpenInFolderAction extends AbstractAction {
        
        /**
         * 
         */
        private static final long serialVersionUID = 1693310684299300459L;

        public OpenInFolderAction () {
            putValue(Action.NAME, I18n.tr("Open in Folder"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Folder Containing a Selected File"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_LAUNCH");
        }
        
        public void actionPerformed(ActionEvent ae) {
            int[] sel = TABLE.getSelectedRows();
            if (sel.length == 0) {
                return;
            } 
            
            File selectedFile = getFile(sel[0]);
            if (selectedFile.isFile() && selectedFile.getParentFile() != null) {
                GUIMediator.launchExplorer(selectedFile.getParentFile());
            }
        }
    }
	
    private final class EnqueueAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 9153310119076594713L;

        public EnqueueAction () {
			putValue(Action.NAME, I18n.tr
					("Enqueue"));
			putValue(Action.SHORT_DESCRIPTION,
					 I18n.tr("Add Selected Files to the Playlist"));
			putValue(LimeAction.ICON_NAME, "LIBRARY_TO_PLAYLIST");
		}
		
        public void actionPerformed(ActionEvent ae) {
			//get the selected file. If there are more than 1 we add all
			int[] rows = TABLE.getSelectedRows();
            List<File> files = new ArrayList<File>();
			for (int i = 0; i < rows.length; i++) {
				int index = rows[i]; // current index to add
				File file = DATA_MODEL.getFile(index);
				if (GUIMediator.isPlaylistVisible() && PlaylistMediator.isPlayableFile(file))
                    files.add(file);
			}
            LibraryMediator.instance().addFilesToPlayList(files);
        }
    }

    private final class RemoveAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = -8704093935791256631L;

        public RemoveAction () {
			putValue(Action.NAME, I18n.tr
					("Delete"));
			putValue(Action.SHORT_DESCRIPTION,
					 I18n.tr("Delete Selected Files"));
			putValue(LimeAction.ICON_NAME, "LIBRARY_DELETE");
		}
		
        public void actionPerformed(ActionEvent ae) {
            REMOVE_LISTENER.actionPerformed(ae);
		}
    }
	
    
    private final class RenameAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 2673219925804729384L;

        public RenameAction () {
			putValue(Action.NAME, I18n.tr
					("Rename"));
			//  "LIBRARY_RENAME"   ???
			//  "LIBRARY_RENAME_BUTTON_TIP"   ???			
		}
		
        public void actionPerformed(ActionEvent ae) {
			startRename();
		}
    }

	private final class MagnetLookupAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 5081688548976571828L;

        public MagnetLookupAction() {
			putValue(Action.NAME, I18n.tr
					("Show Magnet Details"));
		}
		
        public void actionPerformed(ActionEvent e) {
            //doMagnetLookup();
        }
    }

	/**
	 * Sets an icon based on the filename extension. 
	 */
	private static class FileNameListCellRenderer extends SubstanceDefaultListCellRenderer {
		
		/**
         * 
         */
        private static final long serialVersionUID = 5064313639046811749L;

        @Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			String extension = FileUtils.getFileExtension(value.toString());
			if (extension != null) {
				setIcon(IconManager.instance().getIconForExtension(extension));
			}
			return this;
		}
	}
	
	
	
	/**
	 * Renders the file part of the Tuple<File, FileDesc> in CheckBoxList<Tuple<File, FileDesc>>.
	 */
	private class TupleTextProvider implements CheckBoxList.TextProvider<Tuple<File, FileDesc>> {
            
        public Icon getIcon(Tuple<File, FileDesc> obj) {
            String extension = FileUtils.getFileExtension(obj.getFirst());
            if (extension != null) {
                return IconManager.instance().getIconForExtension(extension);
            }
            return null;
        }
        public String getText(Tuple<File, FileDesc> obj) {
            return getCompleteFileName(obj.getFirst());
        }
        
        public String getToolTipText(Tuple<File, FileDesc> obj) {
            return obj.getFirst().getAbsolutePath();
        }
        
    }
	
	private static class IncompleteProvider implements LaunchableProvider {

		private final File incompleteFile;
		
		public IncompleteProvider(File incompleteFile) {
			this.incompleteFile = incompleteFile;
		}
		
		public Downloader getDownloader() {
			return GuiCoreMediator.getDownloadManager().getDownloaderForIncompleteFile(incompleteFile);
		}

		public File getFile() {
			return incompleteFile;
		}
	}
	
	private static class CompleteProvider extends IncompleteProvider {
		
		public CompleteProvider(File file) {
			super(file);
		}
		
		public Downloader getDownloader() {
			return null;
		}
	}
}
