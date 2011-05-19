package com.limegroup.gnutella.gui.library;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.limewire.util.OSUtils;
import org.limewire.util.StringUtils;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.FileTransfer;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.playlist.PlaylistMediator;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.gui.tables.DefaultMouseListener;
import com.limegroup.gnutella.gui.tables.MouseObserver;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * This class forms a wrapper around the tree that controls navigation between
 * shared folders. It constructs the tree and supplies access to it. It also
 * controls tree directory selection, deletion, etc.
 */
final class LibraryTree extends JTree implements MouseObserver {

    /**
     * 
     */
    private static final long serialVersionUID = -6122455765140267448L;
    
	///////////////////////////////////////////////////////////////////////////
	//  Nodes
	///////////////////////////////////////////////////////////////////////////
	
    /**
	 * Constant for the root node of the tree.
	 */
	private final LibraryTreeNode ROOT_NODE;
	private RootSharedFilesDirectoryHolder rsfdh = new RootSharedFilesDirectoryHolder();
	
	/** The Torrent Data Saved Folder */
    private LibraryTreeNode torrentDataFilesNode;
	private final SavedFilesDirectoryHolder torrentsfdh = new SavedFilesDirectoryHolder(
			SharingSettings.TORRENT_DATA_DIR_SETTING, 
		    I18n.tr("Torrent Saved Files"));
	
	/** The dot torrents folder */
	private LibraryTreeNode dotTorrentFilesNode;
	private final TorrentDirectoryHolder torrentdh = new TorrentDirectoryHolder();

	/** The shared files node. It's an empty meta node. */
	private LibraryTreeNode sharedFilesNode;
	
	private LibraryTreeNode searchResultsNode;
	private final LibrarySearchResultsHolder lsrdh = new LibrarySearchResultsHolder();
	
	/** Constant for the tree model. */
    private final DefaultTreeModel TREE_MODEL;
    
	///////////////////////////////////////////////////////////////////////////
	//  Singleton Pattern
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Singleton instance of this class.
	 */
	private static LibraryTree INSTANCE;
    
	/**
	 * @return the <tt>LibraryTree</tt> instance
	 */
	public static LibraryTree instance() {
	    if (INSTANCE == null) {
	        INSTANCE = new LibraryTree();
	    }
	    return INSTANCE;
	}

	/**
	 * Constructs the tree and its primary listeners,visualization options,
	 * editors, etc.
	 */
	private LibraryTree() {	
	    
	    ROOT_NODE = new LibraryTreeNode(new RootNodeDirectoryHolder(""));
	    TREE_MODEL = new DefaultTreeModel(ROOT_NODE);
	    
		setModel(TREE_MODEL);
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		setInvokesStopCellEditing(true);
		setShowsRootHandles(true);	
		putClientProperty("JTree.lineStyle", "None");
		setCellRenderer(new LibraryTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(this);

		makePopupMenu();
		makeButtonRow();
		addMouseListener(new DefaultMouseListener(this));

		// add libray search results node
		searchResultsNode = new LibraryTreeNode(lsrdh);
		addNode(ROOT_NODE, searchResultsNode);
		
		//add .torrents node
		dotTorrentFilesNode = new LibraryTreeNode(torrentdh);
		addNode(ROOT_NODE, dotTorrentFilesNode);

		//add torrent saved node
		torrentDataFilesNode = new LibraryTreeNode(torrentsfdh);
		addNode(ROOT_NODE, torrentDataFilesNode);
		addPerMediaTypeDirectories();
		
		//add shared node
		sharedFilesNode = new LibraryTreeNode(rsfdh);
		//addNode(ROOT_NODE, sharedFilesNode);
		
		updateTheme();
		
		// TODO dnd install LimeDropTarget
		setDragEnabled(true);
        setTransferHandler(new MulticastTransferHandler(new LibraryTreeTransferHandler(), 
														DNDUtils.DEFAULT_TRANSFER_HANDLERS));

        addTreeSelectionListener(new LibraryTreeSelectionListener());
	}

	/**
	 * Adds a child node to the parent making sure the event is propagated 
	 * to the tree.
	 * @param parent
	 * @param child
	 * @param expand whether or not to expand the parent node so that the child
	 * is visible
	 */
	private void addNode(LibraryTreeNode parent, LibraryTreeNode child, boolean expand) {
		//  if parent already has child, expand (if necessary) and return
		if (parent.getIndex(child) != -1) {
			if (expand)
				expandPath(new TreePath(parent.getPath()));
			return;
		}
		
		//  insert shared folders alphabetically (and before the individually shared folder)
		int children = parent.getChildCount();
		int insert = 0;
		
        // There are two non SharedFilesDirectoryHolders that are inserted:
        //   the 'torrent' holder & the 'specially shared files' holder
        // Of these, we want special first, torrent second.
		if(!(child.getDirectoryHolder() instanceof SharedFilesDirectoryHolder)) {
            insert = children;
            // decrease insert by one if it's the specially shared & torrent is visible
            if(insert != 0)
                insert--;
		} else {
    		for(; insert < children; insert++) {
                LibraryTreeNode current = (LibraryTreeNode)parent.getChildAt(insert);
                File f = current.getFile();
                if(f == null                       // nor specially shared files
                  || StringUtils.compareFullPrimary(f.getName(), child.getFile().getName()) >= 0) // alphabetically
    		        break;
    		}
        }
        
		TREE_MODEL.insertNodeInto(child, parent, insert);
		
		if (expand || (parent == sharedFilesNode && !isExpanded(new TreePath(sharedFilesNode.getPath())))) { 
			expandPath(new TreePath(parent.getPath()));
		}
	}
	
	private void addNode(LibraryTreeNode parent, LibraryTreeNode child) {
		addNode(parent, child, false);
	}
	
	private void addPerMediaTypeDirectories() {
	    addPerMediaTypeDirectory(NamedMediaType.getFromMediaType(MediaType.getProgramMediaType()));
	    addPerMediaTypeDirectory(NamedMediaType.getFromMediaType(MediaType.getVideoMediaType()));
	    addPerMediaTypeDirectory(NamedMediaType.getFromMediaType(MediaType.getDocumentMediaType()));
	    addPerMediaTypeDirectory(NamedMediaType.getFromMediaType(MediaType.getImageMediaType()));
	    addPerMediaTypeDirectory(NamedMediaType.getFromMediaType(MediaType.getAudioMediaType()));
	}
	
	private void addPerMediaTypeDirectory(NamedMediaType nm) {
	    DirectoryHolder dh = new MediaTypeSavedFilesDirectoryHolder(nm.getMediaType());
        LibraryTreeNode node = new LibraryTreeNode(dh);
        addNode(torrentDataFilesNode, node, true);
	}
	
	// inherit doc comment
	public void updateTheme() {
	}
	
	/**
	 * Sets the initial selection to the Saved Files folder.
	 */
	public void setInitialSelection() {
		TreePath tp = new TreePath(torrentDataFilesNode.getPath());
		setSelectionPath(tp);
	}

	/**
	 * Adds the visual representation of this folder to the library.
	 * 
	 * @param dir
	 *            the <tt>File</tt> instance denoting the abstract pathname of
	 *            the new shared directory to add to the library.
	 */
	private void addDirectoryToNode(File dir, LibraryTreeNode node, boolean isStoreNode) { 
		SharedFilesDirectoryHolder dh = new SharedFilesDirectoryHolder(dir, isStoreNode);
		
		LibraryTreeNode current = new LibraryTreeNode(dh);

		// See if this is the parent of any existing nodes.  If so, 
		// redirect that node to be here.
		int children = node.getChildCount();
		for(int i = children - 1; i >= 0; i--) {
			LibraryTreeNode child = (LibraryTreeNode)node.getChildAt(i);
			File f = child.getFile();
			if(f != null && dir.equals(f.getParentFile())) {
				TREE_MODEL.removeNodeFromParent(child);
				addNode(current, child);
			}
		}

        // Add this into the correct position.
		File parent = dir.getParentFile();
		LibraryTreeNode parentNode = null;
		if (parent != null) {
			 parentNode = getNodeForFolder(parent, node);
		}			
		if (parentNode == null)
		    parentNode = node;

		addNode(parentNode, current);
	}

	/**
	 * Handles events created by the FileManager. Adds or removes nodes from the
	 * tree as necessary.
	 */
	public void handleFileManagerEvent(final FileManagerEvent evt) {
	    switch(evt.getType()) {
	    case ADD_FOLDER:
	        File[] files = evt.getFiles();
	        addDirectoryToNode(files[0], sharedFilesNode, false);
	        break;
	    case REMOVE_FOLDER:
	        File removed = evt.getFiles()[0];
	        removeFolder(removed, sharedFilesNode);
	        break;

		}
	}
	
	/**
	 * Removes the given folder from the list of shared folders.
	 *
	 * If there are any children of this node when it is removed, they
	 * are moved up to be children of the 'Shared Folder' node.
	 *
	 * This 100% relies on the fact that FileManager.removeFolder sends events
	 * from the children first.
	 */
	void removeFolder(File folder, LibraryTreeNode treeNode) {
	    LibraryTreeNode node = getNodeForFolder(folder, treeNode);
	    if(node == null)
	        return;
	        
        if(getSelectedNode() == node)
	        setSelectionPath(new TreePath(treeNode.getPath()));	            

        int childCount = node.getChildCount();
	    for(int i = childCount - 1; i >= 0; i--) {
	        // Move any leftover children to be children of sharedFiles.
            LibraryTreeNode child = (LibraryTreeNode)node.getChildAt(i);
            TREE_MODEL.removeNodeFromParent(child);
            addNode(treeNode, child);
        }
        
        // Remove this node.
        TREE_MODEL.removeNodeFromParent(node);
    }
    
    /**
     * Gets the LibraryTreeNode that represents this folder.
     */
    LibraryTreeNode getNodeForFolder(File folder, LibraryTreeNode parent) {
        int children = parent.getChildCount();
        for(int i = children - 1; i >= 0; i--) {
            LibraryTreeNode child = (LibraryTreeNode)parent.getChildAt(i);
            File childFile = child.getFile();
            if(childFile != null) {
                if(childFile.equals(folder))
                    return child;
                if(child.isAncestorOf(folder))
                    return getNodeForFolder(folder, child);
            }
        }
        return null;
    }
	
    /**
	 * Adds files to the playlist recursively.
	 */
    void addPlayListEntries() {
		if (incompleteDirectoryIsSelected() || !GUIMediator.isPlaylistVisible())
			return;

		final DirectoryHolder dh = getSelectedDirectoryHolder();
		if (dh == null)
			return;
		
		if (PlaylistMediator.getInstance() == null)
			return;

		PlaylistMediator pm = GUIMediator.getPlayList();
		if(pm == null) {
		    return;
        }

        pm.addFilesToPlaylist(dh.getFiles());
    }
	
	/**
	 * Returns true if the given node is in the Shared Files subtree. 
	 */
	private boolean canBeUnshared(LibraryTreeNode node) {
		if (node == null)
			return false;
		if (node == sharedFilesNode)
			return false;
		if (node.getParent() == null)
			return false;
		
		if (node.getParent() == sharedFilesNode)
			return true;
		
		return canBeUnshared((LibraryTreeNode)node.getParent());
	}

	/**
	 * Returns false in the following cases:
	 * <ul>
	 * <li>The node represents the incomplete directory.
	 * <li>The directory behind the node is null.
	 * <li>The directory is already shared either explicitly or recursively 
	 * because its parent is shared.
	 * </ul>
	 * @param node
	 * @return
	 */
	private boolean canBeShared(LibraryTreeNode node) {
		if (node == null) {
			return false;
		}
		
		File dir = node.getDirectoryHolder().getDirectory();
		if (dir == null || GuiCoreMediator.getFileManager().isFolderShared(dir)) {
			return false;
		}
		
		return true;
	}
	
	public DirectoryHolder getSelectedDirectoryHolder() {
		TreePath path = getSelectionPath();
		if (path != null)
			return ((LibraryTreeNode)path.getLastPathComponent()).getDirectoryHolder();
		return null;
	}
    
    public DirectoryHolder getHolderForPoint(Point p) {
        TreePath path = getPathForLocation(p.x, p.y);
        if(path != null) {
            LibraryTreeNode node = (LibraryTreeNode)path.getLastPathComponent();
            if(node != null)
                return node.getDirectoryHolder();
        }
        return null;
    }
	
	/**
	 * Returns a boolean indicating whether or not the current mouse drop event
	 * is dropping to the incomplete folder.
	 * 
	 * @param mousePoint
	 *            the <tt>Point</tt> instance representing the location of the
	 *            mouse release
	 * @return <tt>true</tt> if the mouse was released on the Incomplete
	 *         folder, <tt>false</tt> otherwise
	 */
	boolean droppingToIncompleteFolder(Point mousePoint) {
		return false;
	}

	/**
	 * Returns the File object associated with the currently selected directory.
	 * 
	 * @return the currently selected directory in the library, or <tt>null</tt>
	 *         if no directory is selected
	 */ 
	File getSelectedDirectory() {
		LibraryTreeNode node = getSelectedNode();
		if (node == null)
			return null;
		return node.getDirectoryHolder().getDirectory();
	}
	
	LibraryTreeNode getSelectedNode() {
		return (LibraryTreeNode)getLastSelectedPathComponent();
	}

	/**
	 * Returns the top-level directories as an array of <tt>File</tt> objects
	 * for updating the shared directories in the <tt>SettingsManager</tt>.
	 * 
	 * @return the array of top-level directories as <tt>File</tt> objects
	 */
	File[] getSharedDirectories() {
		int length = sharedFilesNode.getChildCount();
		List<File> newFiles = new ArrayList<File>(length);
		// collect all but the child that holds the specially shared files
		for (int i = 0; i < length - 1; i++) {
			LibraryTreeNode node = (LibraryTreeNode)sharedFilesNode.getChildAt(i);
			newFiles.add(node.getDirectoryHolder().getDirectory());
		}
		return newFiles.toArray(new File[0]);
	}

	/**
	 * Removes all shared directories from the visual display 
	 * and changes the selection if any of them were selected.
	 */ 
	void clear() {
	    boolean selected = false;
		int count = sharedFilesNode.getChildCount();
		// count down, but do not remove node 0
		for (int i = count - 1; i >= 0; i--) {
		    TreeNode node = sharedFilesNode.getChildAt(i);
		    if(node == getSelectedNode())
		        selected = true;
            sharedFilesNode.remove(i);
		}
		
		if (selected)
			setSelectionPath(new TreePath(sharedFilesNode));
        
        // remove all the store files
        selected = false;
        TREE_MODEL.reload();
	}

	/**
	 * Stops sharing the selected folder in the library if there is a folder
	 * selected, if the folder is not the save folder, or if the folder is not a
	 * subdirectory of a "root" shared folder.
	 */
	void unshareLibraryFolder() {
		LibraryTreeNode node = getSelectedNode(); 

		if (node == null)
			return;

		if (incompleteDirectoryIsSelected()) {
			showIncompleteFolderMessage();
		} else if (!canBeUnshared(node)) {
			GUIMediator.showMessage(I18n.tr("FrostWire cannot stop sharing this folder."));
		} else {
			String msg = I18n.tr("Are you sure you want to stop sharing this folder?");				
			DialogOption response = GUIMediator.showYesNoMessage(msg, QuestionsHandler.UNSHARE_DIRECTORY, DialogOption.YES);
			if (response != DialogOption.YES)
				return;

            final File file = node.getFile();				
		    BackgroundExecutorService.schedule(new Runnable() {
		        public void run() {
		            GuiCoreMediator.getFileManager().removeFolderIfShared(file);
                }
            });
		}
	}


	/**
	 * Returns whether or not the incomplete directory is selected in the tree.
	 * 
	 * @return <tt>true</tt> if the incomplete directory is selected,
	 *         <tt>false</tt> otherwise
	 */
	boolean incompleteDirectoryIsSelected() {
		return false;
	}
	
	/**
	 * Returns whethere the search results holder is currently selected in 
	 * the tree.
	 */
	boolean searchResultDirectoryIsSelected() {
		LibraryTreeNode selected = getSelectedNode();
		return selected == searchResultsNode;
	}
	
	boolean sharedFoldersNodeIsSelected() {
		return getSelectedNode() == sharedFilesNode;
	}

	/**
	 * Shows a message indicating that a specific action cannot be performed on
	 * the incomplete directory (such as changing its name).
	 * 
	 * @param action
	 *            the error that occurred
	 */
	private void showIncompleteFolderMessage() {
		GUIMediator.showError(I18n.tr("FrostWire will not allow you delete to the folder reserved for incomplete files."));
	}

	/**
	 * Selection listener that changes the files displayed in the table if the
	 * user chooses a new directory in the tree.
	 */
	private class LibraryTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {	
			LibraryTreeNode node = getSelectedNode();

			unshareAction.setEnabled(canBeUnshared(node));
			shareAction.setEnabled(canBeShared(node));
			addDirToPlaylistAction.setEnabled(isEnqueueable());
			
			if (node == null)
				return;
			
			if (node == sharedFilesNode)
				LibraryMediator.showSharedFiles();
			else
				LibraryMediator.updateTableFiles(node.getDirectoryHolder());	    
		}

	}

	/**
	 * Private class that extends a DefaultMutableTreeNode. Using this class
	 * ensures that the "UserObjects" associated with the tree nodes will always
	 * be File objects.
	 */
	public final class LibraryTreeNode extends DefaultMutableTreeNode
	                                    implements FileTransfer {
		/**
         * 
         */
        private static final long serialVersionUID = -6006388424375212116L;
        
        private DirectoryHolder _holder;

		private LibraryTreeNode(DirectoryHolder holder) {
			super(holder);
		    _holder = holder;
		}
		
		public DirectoryHolder getDirectoryHolder() {
			return _holder;
		}

		public File getFile() {
			return _holder.getDirectory();
		}	  
		
		/**
		 * Determines if this Node can be an ancestor of given folder.
		 */
		public boolean isAncestorOf(File folder) {
		    File f = getFile();
		    return f != null && folder.getPath().startsWith(f.getPath());
		}
		
		/**
		 * Determines if this is the direct parent of a given folder.
		 */
		public boolean isParentOf(File folder) {
		    return folder.getParentFile().equals(getFile());
		}
		
		/**
		 * Returns a description of this node.
		 */
		public String toString() {
		    return getClass().getName() + ", file: " + getFile();
        }
	}

	/**
	 * Root node class the extends AbstractFileHolder
	 */
	private class RootNodeDirectoryHolder implements DirectoryHolder {
		
		private String name;
		
		public RootNodeDirectoryHolder(String s) { this.name = s; }
		
		public File getDirectory() { return null; }

		public String getDescription() { return ""; }

		public File[] getFiles() { return new File[0]; }

		public String getName() { return name; }

		public boolean accept(File pathname) { return false; }
		
		public int size() { return 0; }
		
		public Icon getIcon() { return null; }
		
		public boolean isEmpty() { return true; }
	}
	
	private class RootSharedFilesDirectoryHolder extends RootNodeDirectoryHolder {

		public RootSharedFilesDirectoryHolder() {
			super(I18n.tr("Shared Files"));
		}
		
		public boolean accept(File file) {
			return GuiCoreMediator.getFileManager().isFileInCompletelySharedDirectory(file);
		}
		
		public Icon getIcon() {
			return GUIMediator.getThemeImage("shared_folder");
		}
	}
	
	private class UnshareAction extends AbstractAction {

		/**
         * 
         */
        private static final long serialVersionUID = -22198400547158328L;

        public UnshareAction() {
			putValue(Action.NAME,
					I18n.tr("Stop Sharing Folder"));
		}
		
		public void actionPerformed(ActionEvent e) {
			unshareLibraryFolder();
		}
	}
	
	private class AddDirectoryToPlaylistAction extends AbstractAction {

		/**
         * 
         */
        private static final long serialVersionUID = -4408516187152426542L;

        public AddDirectoryToPlaylistAction() {
			putValue(Action.NAME,
					I18n.tr("Add Folder Contents to Playlist"));
		}
		
		public void actionPerformed(ActionEvent e) {
			addPlayListEntries();			
		}
	}
	
	
	private class LibraryTreeCellRenderer extends SubstanceDefaultTreeCellRenderer {
		
		/**
         * 
         */
        private static final long serialVersionUID = -5541614151018217164L;

        public LibraryTreeCellRenderer() {
			setOpaque(false);
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, 
				boolean sel, boolean expanded, boolean leaf, int row, 
				boolean focused) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, focused);
			LibraryTreeNode node = (LibraryTreeNode)value;
			DirectoryHolder dh = node.getDirectoryHolder();
			setText(dh.getName());
			setToolTipText(dh.getDescription());
			Icon icon = dh.getIcon();
			if (icon != null) {
				setIcon(icon);
			}
			return this;
		}
	}

	private class ShareAction extends AbstractAction {

		/**
         * 
         */
        private static final long serialVersionUID = 4071173294983759719L;

        public ShareAction() {
			putValue(Action.NAME, I18n.tr
					("Share Folder"));
		}
		
		public void actionPerformed(ActionEvent e) {
			LibraryMediator.instance().addSharedLibraryFolder(getSelectedDirectory());
		}
	}
    
	
	private class RefreshAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 412879927060208864L;

        public RefreshAction() {
			putValue(Action.NAME, I18n.tr
					("Refresh"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr
					("Reload Shared Folders"));
			putValue(LimeAction.ICON_NAME, "LIBRARY_REFRESH");
		}

		public void actionPerformed(ActionEvent e) {
			GuiCoreMediator.getFileManager().loadSettings();
		}
		
	}

	private class ExploreAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 2767346265174793478L;

        public ExploreAction() {
			putValue(Action.NAME, I18n.tr
					("Explore"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr
					("Open Library Folder"));
			putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
		}

		public void actionPerformed(ActionEvent e) {
			File exploreDir = getSelectedDirectory();
			if (exploreDir == null)
				return;
			
			GUIMediator.launchExplorer(exploreDir);
		}
		
	}

	/**
	 * Enable enqueue action when non-incomplete, non-shared, and has a playable file. 
	 */
	private boolean isEnqueueable() {
		LibraryTreeNode node = getSelectedNode();
		boolean enqueueable = false;
		if (node != null && node != sharedFilesNode) {
			File[] files = node.getDirectoryHolder().getFiles();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					if (GUIMediator.isPlaylistVisible() && PlaylistMediator.isPlayableFile(files[i]))
						enqueueable = true;
				}
			}
		}
		return enqueueable;
	}

	/**
	 * Updates the LibraryTree based on whether the player is enabled. 
	 */
	public void setPlayerEnabled(boolean value) {
		addDirToPlaylistAction.setEnabled(isEnqueueable());
	}
	

	///////////////////////////////////////////////////////////////////////////
	//  Popups
	///////////////////////////////////////////////////////////////////////////
	
	/** Constant for the popup menu. */
	private final JPopupMenu DIRECTORY_POPUP = new SkinPopupMenu();
	private Action shareAction = new ShareAction();
	private Action unshareAction = new UnshareAction();
	private Action addDirToPlaylistAction = new AddDirectoryToPlaylistAction();
	private Action refreshAction = new RefreshAction(); 
	private Action exploreAction = new ExploreAction();
	private Action configureSharingAction = new ConfigureOptionsAction(
            OptionsConstructor.SHARED_KEY,
            I18n.tr("Options"),
            I18n.tr("You can configure the folders you share in FrostWire\'s Options."));
            
	private ButtonRow BUTTON_ROW;
	
	/**
	 * Constructs the popup menu that appears in the tree on a right mouse
	 * click.
	 */
	private void makePopupMenu() {
        DIRECTORY_POPUP.add(new SkinMenuItem(addDirToPlaylistAction));
		DIRECTORY_POPUP.addSeparator();
		DIRECTORY_POPUP.add(new SkinMenuItem(refreshAction));
		if (hasExploreAction()) {
			DIRECTORY_POPUP.add(new SkinMenuItem(exploreAction));
		}
		DIRECTORY_POPUP.addSeparator();
		
		DIRECTORY_POPUP.add(new SkinMenuItem(new ConfigureOptionsAction(
                OptionsConstructor.SHARED_KEY,
                I18n.tr("Configure Sharing Options"),
                I18n.tr("You can configure the folders you share in FrostWire\'s Options."))));
	}

	private boolean hasExploreAction() {
		return OSUtils.isWindows() || OSUtils.isMacOSX();
	}

	private void makeButtonRow() {
		if (hasExploreAction()) {
			BUTTON_ROW = new ButtonRow(new Action[] { refreshAction,
					exploreAction, configureSharingAction }, ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
		} else {
			BUTTON_ROW = new ButtonRow(new Action[] { refreshAction, configureSharingAction },
					ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
		}
	}
	
	public Component getButtonRow() {
		return BUTTON_ROW;
	}

	///////////////////////////////////////////////////////////////////////////
	//  MouseObserver implementation
	///////////////////////////////////////////////////////////////////////////
	
	public void handleMouseClick(MouseEvent e) {
	    DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
	    if (directoryHolder != null && directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
	        MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
	        BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(mtsfdh));
	    }
	}

    /**
     * Handles when the mouse is double-clicked.
     */
    public void handleMouseDoubleClick(MouseEvent e) { }
    
    /**
     * Handles a right-mouse click.
     */
    public void handleRightMouseClick(MouseEvent e) { }
    
    /**
     * Handles a trigger to the popup menu.
     */
    public void handlePopupMenu(MouseEvent e) {
		int row = getRowForLocation(e.getX(), e.getY());
		if(row == -1)
			return;

		setSelectionRow(row);
		DIRECTORY_POPUP.show(this, e.getX(), e.getY());
    }

	/**
	 * Sets the tree selection to be the given directory, if it exists.
	 * 
	 * @return true if the directory exists in the tree and could be selected
	 */
	public boolean setSelectedDirectory(File dir) {
		if (dir == null || !dir.isDirectory())
			return false;
		LibraryTreeNode ltn = getNodeForFolder(dir, sharedFilesNode);
		if (ltn == null) {
	    	ltn = getNodeForFolder(dir, sharedFilesNode);
        	if( ltn == null) {
        		return false;
        	}
        }
		
		setSelectionPath(new TreePath(ltn.getPath()));
		return true;
	}

       
	void setSearchResultsNodeSelected() {
		clearSelection();
		TreePath path = new TreePath(new Object[] { ROOT_NODE, searchResultsNode });
		scrollPathToVisible(path);
		setSelectionPath(path);
	}
	
	LibrarySearchResultsHolder getSearchResultsHolder() {
		return lsrdh;
	}
	
	private static final class SearchByMediaTypeRunnable implements Runnable {
	    
	    private final MediaTypeSavedFilesDirectoryHolder _mtsfdh;
	    
        public SearchByMediaTypeRunnable(MediaTypeSavedFilesDirectoryHolder mtsfdh) {
            _mtsfdh = mtsfdh;
        }

        public void run() {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryMediator.instance().clearLibraryTable();
                }
            });
            
            File file = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();
            search(file);
        }
        
        private void search(File file) {
            
            if (!file.isDirectory()) {
                return;
            }
            
            List<File> directories = new ArrayList<File>();
            final List<File> files = new ArrayList<File>();
            
            for (File child : file.listFiles()) {
                if (child.isHidden()) {
                    continue;
                }
                if (child.isDirectory()) {
                    directories.add(child);
                } else if (_mtsfdh.accept(child)) {
                    files.add(child);
                }
            }
            
            Runnable r = new Runnable() {
                public void run() {
                    LibraryMediator.instance().addFilesToLibraryTable(files);
                }
            };
            GUIMediator.safeInvokeLater(r);
            
            for (File directory : directories) {
                search(directory);
            }
        }
    }
}