package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableCellEditor;

import org.limewire.collection.CollectionUtils;
import org.limewire.collection.Tuple;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.bittorrent.CreateTorrentDialog;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.CheckBoxList;
import com.limegroup.gnutella.gui.CheckBoxListPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.MultiLineLabel;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.gui.util.GUILauncher;
import com.limegroup.gnutella.gui.util.GUILauncher.LaunchableProvider;
import com.limegroup.gnutella.util.QueryUtils;

/**
 * This class wraps the JTable that displays files in the library,
 * controlling access to the table and the various table properties.
 * It is the Mediator to the Table part of the Library display.
 */
final class LibraryFilesTableMediator extends AbstractLibraryTableMediator<LibraryFilesTableModel, LibraryFilesTableDataLine, File> {

    /**
     * Variables so the PopupMenu & ButtonRow can have the same listeners
     */
    public static Action LAUNCH_ACTION;
    public static Action OPEN_IN_FOLDER_ACTION;
    public static Action ADD_TO_PLAYLIST_ACTION;
    public static Action CREATE_TORRENT_ACTION;
    public static Action DELETE_ACTION;
    public static Action RENAME_ACTION;
    public static Action SEND_TO_ITUNES_ACTION;

    /**
     * instance, for singelton access
     */
    private static LibraryFilesTableMediator INSTANCE;

    public static LibraryFilesTableMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryFilesTableMediator();
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
        ADD_TO_PLAYLIST_ACTION = new AddToPlaylistAction();
        CREATE_TORRENT_ACTION = new CreateTorrentAction();
        DELETE_ACTION = new RemoveAction();
        RENAME_ACTION = new RenameAction();
        SEND_TO_ITUNES_ACTION = new SendAudioFilesToiTunes();
        
    }
    
    @Override
    protected void addListeners() {
    	super.addListeners();
    	
    	TABLE.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyReleased(KeyEvent e) {
        		if (LibraryUtils.isRefreshKeyEvent(e)) {
        			LibraryMediator.instance().getLibraryExplorer().refreshSelection(true);
        		}        		
        	}
		});

    }
    
    @Override
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
        TABLE.setDefaultRenderer(PlayableIconCell.class, new PlayableIconCellRenderer());
        TABLE.setDefaultRenderer(PlayableCell.class, new PlayableCellRenderer());
    }

    /**
     * Set up the constants
     */
    protected void setupConstants() {
        MAIN_PANEL = null;
        DATA_MODEL = new LibraryFilesTableModel();
        TABLE = new LimeJTable(DATA_MODEL);
        DATA_MODEL.setTable(TABLE);
        Action[] aa = new Action[] { LAUNCH_ACTION, ADD_TO_PLAYLIST_ACTION, DELETE_ACTION };

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

        menu.add(new SkinMenuItem(CREATE_TORRENT_ACTION));
        
        if (areAllSelectedFilesPlayable()) {
            menu.add(createAddToPlaylistSubMenu());
        }
        
        menu.add(new SkinMenuItem(SEND_TO_FRIEND_ACTION));
        menu.add(new SkinMenuItem(SEND_TO_ITUNES_ACTION));

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
                ADD_TO_PLAYLIST_ACTION.setEnabled(false);
            DELETE_ACTION.setEnabled(true);
            RENAME_ACTION.setEnabled(false);
        } else {
            if (GUIMediator.isPlaylistVisible() && AudioPlayer.isPlayableFile(DATA_MODEL.getFile(rows[0])))
                ADD_TO_PLAYLIST_ACTION.setEnabled(true);
            DELETE_ACTION.setEnabled(true);
            // only allow single selection for renames
            //RENAME_ACTION.setEnabled(LibraryMediator.isRenameEnabled() && rows.length == 1);
        }

        LibraryFilesTableDataLine line = DATA_MODEL.get(rows[0]);
        menu.add(createSearchSubMenu(line));

        return menu;
    }

	private boolean areAllSelectedFilesPlayable() {
		boolean selectionIsAllAudio = true;
        int[] selectedRows = TABLE.getSelectedRows();
        for (int i : selectedRows) {
        	if (!AudioPlayer.isPlayableFile(DATA_MODEL.get(i).getInitializeObject())) {
        		selectionIsAllAudio = false;
        		break;
        	}        	
        }
		return selectionIsAllAudio;
	}

    private JMenu createSearchSubMenu(LibraryFilesTableDataLine dl) {
        SkinMenu menu = new SkinMenu(I18n.tr("Search"));

        if (dl != null) {
            File f = dl.getInitializeObject();
            String keywords = QueryUtils.createQueryString(f.getName());
            if (keywords.length() > 0)
                menu.add(new SkinMenuItem(new SearchAction(keywords)));
        }

        if (menu.getItemCount() == 0)
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
    protected void updateSplashScreen() {
    }

    /**
     * Note: This is set up for this to work.
     * Polling is not needed though, because updates
     * already generate update events.
     */
    private LibraryFilesTableMediator() {
        super("LIBRARY_FILES_TABLE");
        ThemeMediator.addThemeObserver(this);
    }

    /**
     * Sets up drag & drop for the table.
     */
    protected void setupDragAndDrop() {
        TABLE.setDragEnabled(true);
        TABLE.setTransferHandler(new LibraryFilesTableTransferHandler(this));
    }

    /**
     * there is no actual component that holds all of this table.
     * The LibraryMediator is real the holder.
     */
    public JComponent getComponent() {
        return null;
    }

    /**
     * Sets the default editors.
     */
    protected void setDefaultEditors() {
        //TableColumnModel model = TABLE.getColumnModel();
        //TableColumn tc = model.getColumn(LibraryFilesTableDataLine.NAME_IDX);
        //tc.setCellEditor(new LibraryTableCellEditor(this));
    }

    /**
     * Cancels all editing of fields in the tree and table.
     */
    void cancelEditing() {
        if (TABLE.isEditing()) {
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
        if (dirHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
            setMediaType(((MediaTypeSavedFilesDirectoryHolder) dirHolder).getMediaType());
        } else {
            setMediaType(MediaType.getAnyTypeMediaType());
        }
        clearTable();
        
        List<List<File>> partitionedFiles = CollectionUtils.split(100, Arrays.asList(dirHolder.getFiles()));
        
        for (List<File> partition : partitionedFiles) {
        	final List<File> fPartition = partition;
        	
        	BackgroundExecutorService.schedule(new Runnable() {

				@Override
				public void run() {
		        	SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							for (File file : fPartition) {
								addUnsorted(file);
							}
							LibraryMediator.instance().getLibrarySearch().addResults(fPartition.size());
						}
		        	});
		        	Thread.yield();
				}
        	});
        	
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
        if (row == -1)
            return;
        //int viewIdx = TABLE.convertColumnIndexToView(LibraryFilesTableDataLine.NAME_IDX);
        //TABLE.editCellAt(row, viewIdx, LibraryTableCellEditor.EVENT);
    }

    /**
     * Shows the license window.
     */
    void showLicenseWindow() {
        //        LibraryTableDataLine ldl = DATA_MODEL.get(TABLE.getSelectedRow());
        //        if(ldl == null)
        //            return;
        //        FileDesc fd = ldl.getFileDesc();
        //        License license = fd.getLicense();
        //        URN urn = fd.getSHA1Urn();
        //        LimeXMLDocument doc = ldl.getXMLDocument();
        //        LicenseWindow window = LicenseWindow.create(license, urn, doc, this);
        //        GUIUtils.centerOnScreen(window);
        //        window.setVisible(true);
    }

    /**
     * Returns the options offered to the user when removing files.
     * 
     * Depending on the platform these can be a subset of 
     * MOVE_TO_TRASH, DELETE, CANCEL.
     */
    private static Object[] createRemoveOptions() {
        if (OSUtils.supportsTrash()) {
            String trashLabel = OSUtils.isWindows() ? I18n.tr("Move to Recycle Bin") : I18n.tr("Move to Trash");
            return new Object[] { trashLabel, I18n.tr("Delete"), I18n.tr("Cancel") };
        } else {
            return new Object[] { I18n.tr("Delete"), I18n.tr("Cancel") };
        }
    }
    
    public List<AudioSource> getFileView() {
        int size = DATA_MODEL.getRowCount();
        List<AudioSource> result = new ArrayList<AudioSource>(size);
        for (int i = 0; i < size; i++) {
            try {
            	File file = DATA_MODEL.get(i).getFile();
            	if (AudioPlayer.isPlayableFile(file)) {
            		result.add(new AudioSource(DATA_MODEL.get(i).getFile()));
            	}
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return result;
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

        CheckBoxListPanel<Tuple<File, FileDesc>> listPanel = new CheckBoxListPanel<Tuple<File, FileDesc>>(files, new TupleTextProvider(), true);
        listPanel.getList().setVisibleRowCount(4);

        // display list of files that should be deleted
        Object[] message = new Object[] {
                new MultiLineLabel(I18n.tr("Are you sure you want to delete the selected file(s), thus removing it from your computer?"), 400),
                Box.createVerticalStrut(ButtonRow.BUTTON_SEP), listPanel, Box.createVerticalStrut(ButtonRow.BUTTON_SEP) };

        // get platform dependent options which are displayed as buttons in the dialog
        Object[] removeOptions = createRemoveOptions();

        int option = JOptionPane.showOptionDialog(MessageService.getParentComponent(), message, I18n.tr("Message"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, removeOptions, removeOptions[0] /* default option */);

        if (option == removeOptions.length - 1 /* "cancel" option index */
                || option == JOptionPane.CLOSED_OPTION) {
            return;
        }

        // remove still selected files
        List<Tuple<File, FileDesc>> selected = listPanel.getSelectedElements();
        List<String> undeletedFileNames = new ArrayList<String>();

        for (Tuple<File, FileDesc> tuple : selected) {
            File file = tuple.getFirst();
            FileDesc fd = tuple.getSecond();
            //            if (_isIncomplete && hasActiveDownloader(file)) {
            //                undeletedFileNames.add(getCompleteFileName(file));
            //                continue;
            //            }

            if (fd != null) {
                //GuiCoreMediator.getUploadManager().killUploadsForFileDesc(fd);
            }

            // removeOptions > 2 => OS offers trash options
            boolean removed = FileUtils.delete(file, removeOptions.length > 2 && option == 0 /* "move to trash" option index */);
            if (removed) {
                if (AudioPlayer.instance().isThisBeingPlayed(file)) {
                    AudioPlayer.instance().stop();
                }
                DATA_MODEL.remove(DATA_MODEL.getRow(file));
            } else {
                undeletedFileNames.add(getCompleteFileName(file));
            }
        }

        clearSelection();

        if (undeletedFileNames.isEmpty()) {
            return;
        }

        // display list of files that could not be deleted
        message = new Object[] {
                new MultiLineLabel(
                        I18n.tr("The following files could not be deleted. They may be in use by another application or are currently being downloaded to."),
                        400), Box.createVerticalStrut(ButtonRow.BUTTON_SEP), new JScrollPane(createFileList(undeletedFileNames)) };

        JOptionPane.showMessageDialog(MessageService.getParentComponent(), message, I18n.tr("Error"), JOptionPane.ERROR_MESSAGE);
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
        LibraryFilesTableModel ltm = DATA_MODEL;

        File oldFile = ltm.getFile(row);
        String parent = oldFile.getParent();
        String nameWithExtension = newName + "." + ltm.getType(row);
        File newFile = new File(parent, nameWithExtension);
        if (!ltm.getName(row).equals(newName)) {
            if (oldFile.renameTo(newFile)) {
                // GuiCoreMediator.getFileManager().renameFileIfSharedOrStore(oldFile, newFile);
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
        LibraryFilesTableDataLine line = DATA_MODEL.get(TABLE.getSelectedRow());
        if (line == null) {
            return;
        }
        if (getMediaType().equals(MediaType.getAudioMediaType()) && AudioPlayer.isPlayableFile(line.getFile())) {
            AudioPlayer.instance().asyncLoadSong(new AudioSource(line.getFile()), true, true, null, getFileView());
            return;
        }

        int[] rows = TABLE.getSelectedRows();
        //LibraryTableModel ltm = DATA_MODEL;
        //File file;
        for (int i = 0; i < rows.length; i++) {
            //file = ltm.getFile(rows[i]);
            // if it's a directory try to select it in the library tree
            // if it could be selected return
            //			if (file.isDirectory() 
            //				&& LibraryMediator.setSelectedDirectory(file))
            //				return;
        }

        launch();
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

        File selectedFile = DATA_MODEL.getFile(rows[0]);

        if (OSUtils.isWindows()) {
            if (selectedFile.isDirectory()) {
                GUIMediator.launchExplorer(selectedFile);
                return;
            } else if (!AudioPlayer.isPlayableFile(selectedFile)) {
                GUIMediator.launchFile(selectedFile);
                return;
            }

        }

        LaunchableProvider[] providers = new LaunchableProvider[rows.length];
        for (int i = 0; i < rows.length; i++) {
            providers[i] = new FileProvider(DATA_MODEL.getFile(rows[i]));
        }
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

        //  always turn on Launch, Delete, Magnet Lookup, Bitzi Lookup
        LAUNCH_ACTION.setEnabled(true);
        DELETE_ACTION.setEnabled(true);

        if (selectedFile != null && !selectedFile.getName().endsWith(".torrent")) {
            CREATE_TORRENT_ACTION.setEnabled(sel.length == 1);
        }
        
        if (selectedFile != null) {
            SEND_TO_FRIEND_ACTION.setEnabled(sel.length == 1);

            if (getMediaType().equals(MediaType.getAnyTypeMediaType())) {
            	boolean atLeastOneIsPlayable  = false;
            	
            	for (int i : sel) {
            		if (AudioPlayer.isPlayableFile(getFile(i))) {
            			atLeastOneIsPlayable = true;
            			break;
            		}
            	}
            	
            	SEND_TO_ITUNES_ACTION.setEnabled(atLeastOneIsPlayable);
            } else {
            	SEND_TO_ITUNES_ACTION.setEnabled(getMediaType().equals(MediaType.getAudioMediaType()));
            }
        }

        if (sel.length == 1 && selectedFile.isFile() && selectedFile.getParentFile() != null) {
            OPEN_IN_FOLDER_ACTION.setEnabled(true);
        } else {
            OPEN_IN_FOLDER_ACTION.setEnabled(false);
        }

        //  turn on Enqueue if play list is visible and a selected item is playable
        if (GUIMediator.isPlaylistVisible()) {
            boolean found = false;
            for (int i = 0; i < sel.length; i++) {
                if (AudioPlayer.isPlayableFile(DATA_MODEL.getFile(sel[i]))) {
                    found = true;
                    break;
                }
            }
            
            ADD_TO_PLAYLIST_ACTION.setEnabled(found);
        } else {
            ADD_TO_PLAYLIST_ACTION.setEnabled(false);
        }
        
        if (sel.length == 1) {
            LibraryMediator.instance().getLibraryCoverArt().setFile(selectedFile);
        }
        
        LibraryMediator.instance().refreshBottomActions();
    }

    /**
     * Handles the deselection of all rows in the library table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {
        LAUNCH_ACTION.setEnabled(false);
        OPEN_IN_FOLDER_ACTION.setEnabled(false);
        SEND_TO_FRIEND_ACTION.setEnabled(false);
        ADD_TO_PLAYLIST_ACTION.setEnabled(false);
        CREATE_TORRENT_ACTION.setEnabled(false);
        DELETE_ACTION.setEnabled(false);
        RENAME_ACTION.setEnabled(false);
        SEND_TO_ITUNES_ACTION.setEnabled(false);
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

        public LaunchAction() {
            putValue(Action.NAME, I18n.tr("Launch"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Launch Selected Files"));
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

        public OpenInFolderAction() {
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

    private final class AddToPlaylistAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 9153310119076594713L;

        public AddToPlaylistAction() {
            putValue(Action.NAME, I18n.tr("Enqueue"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Add Selected Files to the Playlist"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_TO_PLAYLIST");
        }

        public void actionPerformed(ActionEvent ae) {
            //get the selected file. If there are more than 1 we add all
            int[] rows = TABLE.getSelectedRows();
            List<File> files = new ArrayList<File>();
            for (int i = 0; i < rows.length; i++) {
                int index = rows[i]; // current index to add
                File file = DATA_MODEL.getFile(index);
                if (GUIMediator.isPlaylistVisible() && AudioPlayer.isPlayableFile(file))
                    files.add(file);
            }
            //LibraryMediator.instance().addFilesToPlayList(files);
        }
    }

    private final class CreateTorrentAction extends AbstractAction {

        private static final long serialVersionUID = 1898917632888388860L;

        public CreateTorrentAction() {
            super(I18n.tr("Create New Torrent"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Create a new .torrent file"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            File selectedFile = DATA_MODEL.getFile(TABLE.getSelectedRow());

            //can't create torrents out of empty folders.
            if (selectedFile.isDirectory() && selectedFile.listFiles().length == 0) {
                JOptionPane.showMessageDialog(null, I18n.tr("The folder you selected is empty."), I18n.tr("Invalid Folder"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            //can't create torrents if the folder/file can't be read
            if (!selectedFile.canRead()) {
                JOptionPane.showMessageDialog(null, I18n.tr("Error: You can't read on that file/folder."), I18n.tr("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            CreateTorrentDialog dlg = new CreateTorrentDialog(GUIMediator.getAppFrame());
            dlg.setChosenContent(selectedFile);
            dlg.setVisible(true);

        }
    }

    private final class RemoveAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -8704093935791256631L;

        public RemoveAction() {
            putValue(Action.NAME, I18n.tr("Delete"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Delete Selected Files"));
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

        public RenameAction() {
            putValue(Action.NAME, I18n.tr("Rename"));
            //  "LIBRARY_RENAME"   ???
            //  "LIBRARY_RENAME_BUTTON_TIP"   ???			
        }

        public void actionPerformed(ActionEvent ae) {
            startRename();
        }
    }
    
	private class SendAudioFilesToiTunes extends AbstractAction {

		private static final long serialVersionUID = 4726989286129406765L;

		public SendAudioFilesToiTunes() {
			putValue(Action.NAME, I18n.tr("Send to iTunes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Send audio files to iTunes"));
    	}
    	
    	@Override
		public void actionPerformed(ActionEvent e) {
            int[] rows = TABLE.getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                int index = rows[i]; // current index to add
                File file = DATA_MODEL.getFile(index);
                
				iTunesMediator.instance().scanForSongs(file);                
            }
		}
    }

    /**
     * Sets an icon based on the filename extension.      */
    private static class FileNameListCellRenderer extends SubstanceDefaultListCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = 5064313639046811749L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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

    private static class FileProvider implements LaunchableProvider {

        private final File _file;

        public FileProvider(File file) {
            _file = file;
        }

        public File getFile() {
            return _file;
        }
    }
    
    @Override
    protected void sortAndMaintainSelection(int columnToSort) {
        super.sortAndMaintainSelection(columnToSort);
        resetAudioPlayerFileView();
    }

    public void resetAudioPlayerFileView() {
        Playlist playlist = AudioPlayer.instance().getCurrentPlaylist();
        if (playlist == null) {
        	AudioPlayer.instance().setPlaylistFilesView(getFileView());
        }
    }

    @Override
    protected AudioSource createAudioSource(LibraryFilesTableDataLine line) {
        if (AudioPlayer.isPlayableFile(line.getInitializeObject())) {
            return new AudioSource(line.getInitializeObject());
        } else {
            return null;
        }
    }
}
