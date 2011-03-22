package com.limegroup.gnutella.gui.library;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GUIConstants;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.library.RecursiveSharingDialog.State;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.sharing.ShareManager;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.UISettings;


/**
 * This class functions as an initializer for all of the elements
 * of the library and as a mediator between library objects.
 */
public final class LibraryMediator implements ThemeObserver {

	/**
	 * The primary panel that contains all of the library elements.
	 */
	private static JPanel MAIN_PANEL;
	private static final CardLayout VIEW_LAYOUT = new CardLayout();
	private static JPanel VIEW_PANEL;

	/**
     * Constant handle to the <tt>LibraryTree</tt> library controller.
     */
    private static LibraryTree LIBRARY_TREE;
	private static JScrollPane TREE_SCROLL_PANE;
    
    /**
     * Constant handle to the <tt>LibraryTable</tt> that displays the files
     * in a given directory.
     */
    private static LibraryTableMediator LIBRARY_TABLE;

    private static final String TABLE_KEY = "LIBRARY_TABLE";
    private static final String SHARED_KEY = "SHARED";
    /**
     * Constant handle to the file update handler.
     */
    private final HandleFileUpdate FILE_UPDATER = new HandleFileUpdate();
   
	/** Panel for the Shared Files node. */
	private static JPanel jpShared = null;

    ///////////////////////////////////////////////////////////////////////////
	//  Singleton Pattern
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Singleton instance of this class.
	 */
	private static LibraryMediator INSTANCE;
    
	/**
	 * @return the <tt>LibraryMediator</tt> instance
	 */
	public static LibraryMediator instance() {
	    if (INSTANCE == null) {
	        INSTANCE = new LibraryMediator();
	    }
	    return INSTANCE;
	}

    /** 
	 * Constructs a new <tt>LibraryMediator</tt> instance to manage calls
	 * between library components.
	 */
    private LibraryMediator() {
        getComponent(); // creates MAIN_PANEL
		GUIMediator.setSplashScreenString(
		    I18n.tr("Loading Library Window..."));
		ThemeMediator.addThemeObserver(this);

		addView(getLibraryTable().getScrolledTablePane(), TABLE_KEY);
		
		//  Create split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTreeScrollPanel(), getViewPanel());
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
		DividerLocationSettingUpdater.install(splitPane,
				UISettings.UI_LIBRARY_TREE_DIVIDER_LOCATION);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(getLibraryTree().getButtonRow(), BorderLayout.WEST);
		buttonPanel.add(getLibraryTree().getButtonRow(), BorderLayout.CENTER);
		
		//  Layout main panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(GUIConstants.SEPARATOR, GUIConstants.SEPARATOR,
								GUIConstants.SEPARATOR, GUIConstants.SEPARATOR);
		MAIN_PANEL.add(new LibrarySearchPanel(GuiCoreMediator.getQueryRequestFactory()), gbc);
		gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(GUIConstants.SEPARATOR, GUIConstants.SEPARATOR, GUIConstants.SEPARATOR, GUIConstants.SEPARATOR);
		MAIN_PANEL.add(splitPane, gbc);
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, GUIConstants.SEPARATOR, GUIConstants.SEPARATOR, GUIConstants.SEPARATOR);
		MAIN_PANEL.add(buttonPanel, gbc);
		updateTheme();		
		
		//  Set the initial selection in the LibraryTree
		getLibraryTree().setInitialSelection();
	}

	// inherit doc comment
	public void updateTheme() {
	    getLibraryTree().updateTheme();
//		Color tableColor = SkinHandler.getTableBackgroundColor();
//		TREE_SCROLL_PANE.getViewport().setBackground(tableColor);
	}

	/**
	 * Returns the <tt>JComponent</tt> that contains all of the elements of
	 * the library.
	 *
	 * @return the <tt>JComponent</tt> that contains all of the elements of
	 * the library.
	 */
	public JComponent getComponent() {
	    if (MAIN_PANEL == null) {
	        MAIN_PANEL = new JPanel(new GridBagLayout());
	    }
		return MAIN_PANEL;
	}
	
	public static JPanel getViewPanel() {
	    if (VIEW_PANEL == null) {
	        VIEW_PANEL = new JPanel(VIEW_LAYOUT);
	    }
	    return VIEW_PANEL;
	}
	
    /**
	 * Tells the library to launch the application associated with the 
	 * selected row in the library. 
	 */
    public void launchLibraryFile() {
        getLibraryTable().launch();
    }
    
    /**
	 * Deletes the currently selected rows in the table. 
	 */
    public void deleteLibraryFile() {
        getLibraryTable().removeSelection();
    }
        
	/**
	 * Removes the gui elements of the library tree and table.
	 */
	public void clearLibrary() {
	    getLibraryTable().clearTable();
        getLibraryTree().clear();
        quickRefresh();
	}
    
    /**
     * Returns the directory that's currently visible from the table.
     */
    public File getVisibleDirectory() {
        return getLibraryTree().getSelectedDirectory();
    }
    
    /**
     * Quickly refreshes the library.
     *
     * This is only done if a saved or incomplete folder is selected,
     * in case an incomplete file was deleted or a new file (not shared)
     * was added to a save directory.
     */
    public void quickRefresh() {
	    DirectoryHolder dh = getLibraryTree().getSelectedDirectoryHolder();
		if(dh instanceof SavedFilesDirectoryHolder || dh instanceof IncompleteDirectoryHolder)
            updateTableFiles(dh);
    }
    
    /**
     * Forces a refresh of the currently selected folder.
     */
    public void forceRefresh() {
        updateTableFiles(getLibraryTree().getSelectedDirectoryHolder());
    }

	/**
	 * Handles events created by the FileManager.  Passes these events on to
	 * the LibraryTableMediator or LibraryTree as necessary.
	 */
    public void handleFileManagerEvent(final FileManagerEvent evt) {
        getLibraryTree().handleFileManagerEvent(evt);
        getLibraryTable().handleFileManagerEvent(evt, getLibraryTree().getSelectedDirectoryHolder());		
    }
		
    /** 
	 * Displays a file chooser for selecting a new folder to share and 
	 * adds that new folder to the settings and FileManager.
	 */
    public void addSharedLibraryFolder() {
		File dir = FileChooserHandler.getInputDirectory();
		if (dir == null)
			return;
		addSharedLibraryFolder(dir);
    }
	
	public void addSharedLibraryFolder(final File dir) {
		if(ShareManager.checkAndWarnNewSharedFolder(dir)) {    		
    		final RecursiveSharingDialog dialog = new RecursiveSharingDialog(GUIMediator.getAppFrame(), dir);
    		if (dialog.showChooseDialog(MessageService.getParentComponent()) == State.OK) {
    			BackgroundExecutorService.schedule(new Runnable() {
    			    public void run() {
    			        GuiCoreMediator.getFileManager().addSharedFolders(dialog.getRootsToShare(), dialog.getFoldersToExclude());
    	            }
    	        });	
    		}
		}
	}
	
	/**
	 * Update the this file's statistic
	 */
	public void updateSharedFile(final File file) {
	    // if the library table is visible, and
	    // if the selected directory is null
	    // or if we the file exists in a directory
	    // other than the one we selected, then there
	    // is no need to update.
	    // the user will see the newest stats when he/she 
	    // selects the directory.
	    DirectoryHolder dh = getLibraryTree().getSelectedDirectoryHolder();
		if(getLibraryTable().getTable().isShowing() && dh != null && dh.accept(file)) {
		    // pass the update off to the file updater
		    // this way, only one Runnable is ever created,
		    // instead of allocating a new one every single time
		    // a query is hit.
		    // Very useful for large libraries and generic searches (ala: mp3)
		    FILE_UPDATER.addFileUpdate(file);
	    }
	}
	
	public void setAnnotateEnabled(boolean enabled) {
	    getLibraryTable().setAnnotateEnabled(enabled);
	}

    /** 
	 * Removes the selected folder from the shared folder group.. 
	 */
    public void unshareLibraryFolder() {
        getLibraryTree().unshareLibraryFolder();
    }

    /**
     * Adds a file to the playlist.
     */
    void addFileToPlayList(File toAdd) {
        GUIMediator.getPlayList().addFileToPlaylist(toAdd);
    }
    
    /**
     * Adds a list of files to add to the playlist
     */
    void addFilesToPlayList(List<File> toAdd) {
        GUIMediator.getPlayList().addFilesToPlaylist(toAdd.toArray( new File[toAdd.size()]));
    }

    /** 
	 * Obtains the shared files for the given directory and updates the 
	 * table accordingly.
	 *
	 * @param selectedDir the currently selected directory in
	 *        the library
	 */
    static void updateTableFiles(DirectoryHolder dirHolder) {
        getLibraryTable().updateTableFiles(dirHolder);
		showView(TABLE_KEY);
    }
    
	/** Returns true if this is showing the special incomplete directory,
     *  false if showing normal files. */
    public static boolean incompleteDirectoryIsSelected() {
        return getLibraryTree().incompleteDirectoryIsSelected();        
    }
    
    /**
	 * Whether or not the files in the table can currently be renamed.
	 * 
	 * TODO another hack to disallow renames for the search results holder
	 * clean this up
	 */
    static boolean isRenameEnabled() { 
    	return !getLibraryTree().searchResultDirectoryIsSelected()
    		&& !getLibraryTree().incompleteDirectoryIsSelected();
    }
    
    /**
     *  Class to handle updates to shared file stats
     *  without creating tons of runnables.
     *  Idea taken from HandleQueryString in VisualConnectionCallback
     */
    private static final class HandleFileUpdate implements Runnable {
        private Vector<File>  list;
        private boolean active;
    
        public HandleFileUpdate( ) {
            list   = new Vector<File>();
            active = false;
        }
    
        public void addFileUpdate(File f) {
            list.addElement(f);
            if(active == false) {
                active = true;
                SwingUtilities.invokeLater(this);
            }
        }
    
        public void run() {
            try {
                File f;
                while (list.size() > 0) {
                    f = list.firstElement();
                    list.removeElementAt(0);
                    getLibraryTable().update(f);
                }
			} catch (IndexOutOfBoundsException e) {
        	    //this really should never happen, but
        	    //who really cares if we're not sharing it?
			} finally {
			    active = false;
            }
        }
    }    
	
	/**
	 * Shows the Shared Files view in response to selection of the Shared Files
	 * in the LibraryTree.
	 */
	public static void showSharedFiles() {
		if (jpShared == null) {
			jpShared = new JPanel(new BorderLayout());
			JPanel jpInternal = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(0, 0, ButtonRow.BUTTON_SEP, 0);
			jpInternal.add(new JLabel(I18n.tr("You can configure the folders you share in FrostWire\'s Options.")), gbc);
			gbc.gridy = 1;
			jpInternal.add(new JButton(new ConfigureOptionsAction(
					OptionsConstructor.SHARED_KEY,
					I18n.tr("Configure..."),
					I18n.tr("You can configure the folders you share in FrostWire\'s Options."))), gbc);
			jpShared.add(jpInternal, BorderLayout.CENTER);
			jpShared.setBorder(BorderFactory.createEtchedBorder());
            addView(jpShared, SHARED_KEY);
        }
        showView(SHARED_KEY);
    }

	public static void showView(String key) {
		VIEW_LAYOUT.show(getViewPanel(), key);
	}
	
	public static void addView(Component c, String key) {
		getViewPanel().add(c, key);
	}

	/**
	 * Sets the selected directory in the LibraryTree.
	 * 
	 * @return true if the directory exists in the tree and could be selected
	 */
	public static boolean setSelectedDirectory(File dir) {
		return getLibraryTree().setSelectedDirectory(dir);		
	}

	/**
	 * Selects the file in the library tab.
	 *
	 * @return true if the directory exists in the tree and could be selected
	 */
	public static boolean setSelectedFile(File file) {
	    boolean selected = getLibraryTree().setSelectedDirectory(file.getParentFile());
	    if (selected) {
	        return getLibraryTable().setFileSelected(file);
	    }
	    return false;
	}

	/**
	 * Updates the Library GUI based on whether the player is enabled. 
	 */
	public void setPlayerEnabled(boolean value) {
	    getLibraryTable().setPlayerEnabled(value);
		getLibraryTree().setPlayerEnabled(value);
	}
	
	private static LibraryTree getLibraryTree() {
	    if (LIBRARY_TREE == null) {
	        LIBRARY_TREE = LibraryTree.instance();
	        LIBRARY_TREE.setBorder(BorderFactory.createEmptyBorder(2,0,0,0));
	    }
	    return LIBRARY_TREE;
	}
	
	private JScrollPane getTreeScrollPanel() {
	    if (TREE_SCROLL_PANE == null) {
	        TREE_SCROLL_PANE = new JScrollPane(getLibraryTree());
	    }
	    return TREE_SCROLL_PANE;
	}
	
	private static LibraryTableMediator getLibraryTable() {
	    if (LIBRARY_TABLE == null) {
	        LIBRARY_TABLE = LibraryTableMediator.instance();
	    }
	    return LIBRARY_TABLE;
	}
}
