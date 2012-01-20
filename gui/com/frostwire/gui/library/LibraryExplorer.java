package com.frostwire.gui.library;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.frostwire.gui.android.Device;
import com.frostwire.gui.android.DeviceConstants;
import com.frostwire.gui.android.UITool;
import com.frostwire.gui.library.android.DeviceFileTypeTreeNode;
import com.frostwire.gui.library.android.DeviceTreeNode;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.NamedMediaType;

public class LibraryExplorer extends AbstractLibraryListPanel {

    private static final long serialVersionUID = -5354238047697767760L;

    private DefaultTreeModel model;
    private JTree tree;

    private DefaultMutableTreeNode devicesNode;

    public LibraryExplorer() {
        setupUI();
    }

    @Override
    public void refresh() {
    }

    public void handleDeviceNew(Device device) {
        DeviceTreeNode deviceNode = new DeviceTreeNode(device);
        devicesNode.add(deviceNode);
        model.insertNodeInto(deviceNode, devicesNode, devicesNode.getChildCount() - 1);
        tree.expandPath(new TreePath(devicesNode.getPath()));

        System.out.println("New Device: " + device);

        refreshDeviceNode(device);
    }

    public void handleDeviceAlive(Device device) {
        refreshDeviceNode(device);
    }

    public void handleDeviceStale(Device device) {
        DeviceTreeNode node = findNode(device);
        model.removeNodeFromParent(node);
    }

    public void refreshSelection(boolean clearCache) {
        TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        if (node instanceof DeviceFileTypeTreeNode) {
            DeviceFileTypeTreeNode deviceFileTypeNode = (DeviceFileTypeTreeNode) node;
            LibraryMediator.instance().updateTableFiles(deviceFileTypeNode.getDevice(), deviceFileTypeNode.getFileType());
        }

        /*
        DirectoryHolder directoryHolder = getSelectedDirectoryHolder();

        //STARRED
        if (directoryHolder instanceof StarredDirectoryHolder) {
            Playlist playlist = LibraryMediator.getLibrary().getStarredPlaylist();
            LibraryMediator.instance().updateTableItems(playlist);
            String status = LibraryUtils.getPlaylistDurationInDDHHMMSS(playlist) + ", " + playlist.getItems().size() + " " + I18n.tr("tracks");
            LibraryMediator.instance().getLibrarySearch().setStatus(status);
            
        } 
        //RADIO
        else if (directoryHolder instanceof InternetRadioDirectoryHolder) {
            
            List<InternetRadioStation> internetRadioStations = LibraryMediator.getLibrary().getInternetRadioStations();
            LibraryMediator.instance().showInternetRadioStations(internetRadioStations);
        } 
        //TORRENTS
        else if (directoryHolder instanceof TorrentDirectoryHolder) {
            LibraryMediator.instance().updateTableFiles(node.getDirectoryHolder());
        }
        //FINISHED
        else if (directoryHolder instanceof SavedFilesDirectoryHolder) {
            if (clearCache) {
                ((SavedFilesDirectoryHolder) directoryHolder).clearCache();
            }
            LibraryMediator.instance().updateTableFiles(
                    node.getDirectoryHolder());
        } 
        //MEDIA TYPES
        else if (directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
            MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
            if (clearCache) {
                mtsfdh.clearCache();
            }
            
            LibraryMediator.instance().updateTableFiles(node.getDirectoryHolder());
            
            BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(
                    mtsfdh));
            
        }   */

        LibraryMediator.instance().getLibrarySearch().clear();
    }

    protected void setupUI() {
        setLayout(new BorderLayout());
        GUIMediator.addRefreshListener(this);

        setupModel();
        setupTree();
        //setupPopupMenu();

        add(new JScrollPane(tree));
    }

    private void setupModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

        addNodesPerMediaType(root);

        DefaultMutableTreeNode internetRadio = new DefaultMutableTreeNode(I18n.tr("Radio"));
        root.add(internetRadio);

        DefaultMutableTreeNode starred = new DefaultMutableTreeNode(I18n.tr("Starred"));
        root.add(starred);

        DefaultMutableTreeNode torrents = new DefaultMutableTreeNode(I18n.tr("Torrents"));
        root.add(torrents);

        DefaultMutableTreeNode finishedDownloads = new DefaultMutableTreeNode(I18n.tr("Finished Downloads"));
        root.add(finishedDownloads);

        devicesNode = new DefaultMutableTreeNode(I18n.tr("Devices"));
        root.add(devicesNode);

        model = new DefaultTreeModel(root);
    }

    private void setupTree() {
        tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new LibraryExplorerTreeSelectionListener());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(tree);
    }

    private void addNodesPerMediaType(DefaultMutableTreeNode root) {
        addNodePerMediaType(root, NamedMediaType.getFromMediaType(MediaType.getAudioMediaType()));
        addNodePerMediaType(root, NamedMediaType.getFromMediaType(MediaType.getVideoMediaType()));
        addNodePerMediaType(root, NamedMediaType.getFromMediaType(MediaType.getImageMediaType()));
        addNodePerMediaType(root, NamedMediaType.getFromMediaType(MediaType.getProgramMediaType()));
        addNodePerMediaType(root, NamedMediaType.getFromMediaType(MediaType.getDocumentMediaType()));
    }

    private void addNodePerMediaType(DefaultMutableTreeNode root, NamedMediaType nm) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(nm.getName());
        root.add(node);
    }

    private DeviceTreeNode findNode(Device device) {
        DeviceTreeNode deviceNode = null;

        for (int i = 0; i < devicesNode.getChildCount() && deviceNode == null; i++) {
            TreeNode node = devicesNode.getChildAt(i);
            if (node instanceof DeviceTreeNode) {
                if (((DeviceTreeNode) node).getDevice().equals(device)) {
                    deviceNode = (DeviceTreeNode) node;
                }
            }
        }

        return deviceNode;
    }

    private DeviceFileTypeTreeNode findNode(DeviceTreeNode deviceNode, byte fileType) {
        DeviceFileTypeTreeNode deviceFileTypeNode = null;

        for (int i = 0; i < deviceNode.getChildCount() && deviceFileTypeNode == null; i++) {
            TreeNode node = deviceNode.getChildAt(i);
            if (node instanceof DeviceFileTypeTreeNode) {
                if (((DeviceFileTypeTreeNode) node).getFileType() == fileType) {
                    deviceFileTypeNode = (DeviceFileTypeTreeNode) node;
                }
            }
        }

        return deviceFileTypeNode;
    }

    private void refreshDeviceNode(Device device) {
        DeviceTreeNode node = findNode(device);

        if (node == null) {
            return; // weird case, no need to do anything
        }

        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_AUDIO);
        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_PICTURES);
        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_VIDEOS);
        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_DOCUMENTS);
        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_APPLICATIONS);
        refreshDeviceFileTypeNode(node, DeviceConstants.FILE_TYPE_RINGTONES);
    }

    private void refreshDeviceFileTypeNode(DeviceTreeNode deviceNode, byte fileType) {
        DeviceFileTypeTreeNode node = findNode(deviceNode, fileType);

        if (node == null) {
            if (UITool.getNumSharedFiles(deviceNode.getDevice().getFinger(), fileType) > 0) {
                node = new DeviceFileTypeTreeNode(deviceNode.getDevice(), fileType);
                model.insertNodeInto(node, deviceNode, 0);
                sortFileTypeNodes(deviceNode);
                tree.expandPath(new TreePath(deviceNode.getPath()));
            }
        } else {
            if (UITool.getNumSharedFiles(deviceNode.getDevice().getFinger(), fileType) == 0) {
                model.removeNodeFromParent(node);
            }
        }
    }

    private void sortFileTypeNodes(DeviceTreeNode deviceNode) {
        // TODO
    }

    private class LibraryExplorerTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();

            if (node == null) {
                return;
            }

            LibraryMediator.instance().getLibraryPlaylists().clearSelection();
            LibraryMediator.instance().refreshBottomActions();

            refreshSelection(false);
        }
    }
}
