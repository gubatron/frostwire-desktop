/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.library;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.OSUtils;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.player.MediaPlayer;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.dnd.DNDUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
final class LibraryFilesTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -3874985752229848555L;

    private static final Log LOG = LogFactory.getLog(LibraryFilesTransferHandler.class);

    private final JTree tree;

    public LibraryFilesTransferHandler(JTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean canImport(TransferSupport support) {

        try {
            LibraryNode node = getNodeFromLocation(support.getDropLocation());

            if (!(node instanceof DirectoryHolderNode) && !(node instanceof DeviceNode)) {
                return false;
            }

            if (node instanceof DirectoryHolderNode) {
                DirectoryHolder dirHolder = ((DirectoryHolderNode) node).getDirectoryHolder();
                if ((!(dirHolder instanceof MediaTypeSavedFilesDirectoryHolder) || !((MediaTypeSavedFilesDirectoryHolder) dirHolder).getMediaType().equals(MediaType.getAudioMediaType())) && !(dirHolder instanceof StarredDirectoryHolder)) {
                    return false;
                }
            }

            if (support.isDataFlavorSupported(LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
                return true;
            } else if (DNDUtils.containsFileFlavors(support.getDataFlavors())) {
                if (OSUtils.isMacOSX()) {
                    return true;
                }

                if (node instanceof DeviceNode) {
                    return true;
                }

                try {
                    File[] files = DNDUtils.getFiles(support.getTransferable());
                    for (File file : files) {
                        if (MediaPlayer.isPlayableFile(file)) {
                            return true;
                        } else if (file.isDirectory()) {
                            if (LibraryUtils.directoryContainsAudio(file)) {
                                return true;
                            }
                        }
                    }
                    if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
                        return true;
                    }
                } catch (InvalidDnDOperationException e) {
                    // this case seems to be something special with the OS
                    return true;
                }
            }
        } catch (Throwable e) {
            LOG.error("Error in LibraryFilesTransferHandler processing", e);
        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            Transferable transferable = support.getTransferable();
            LibraryNode node = getNodeFromLocation(support.getDropLocation());

            if (node instanceof DeviceNode) {
                File[] files = null;
                if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
                    PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistsTableTransferable.Item[]) transferable.getTransferData(LibraryPlaylistsTableTransferable.ITEM_ARRAY));
                    files = LibraryUtils.convertToFiles(playlistItems);
                } else {
                    files = DNDUtils.getFiles(support.getTransferable());
                }

                if (files != null) {
                    ((DeviceNode) node).getDevice().upload(files);
                }

            } else {

                if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
                    PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistsTableTransferable.Item[]) transferable.getTransferData(LibraryPlaylistsTableTransferable.ITEM_ARRAY));
                    LibraryUtils.createNewPlaylist(playlistItems, isStarredDirectoryHolder(support.getDropLocation()));
                } else {
                    File[] files = DNDUtils.getFiles(support.getTransferable());
                    if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
                        LibraryUtils.createNewPlaylist(files[0], isStarredDirectoryHolder(support.getDropLocation()));
                    } else {
                        LibraryUtils.createNewPlaylist(files, isStarredDirectoryHolder(support.getDropLocation()));
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("Error in LibraryFilesTransferHandler processing", e);
        }

        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE | LINK;
    }

    private boolean isStarredDirectoryHolder(DropLocation location) {
        LibraryNode node = getNodeFromLocation(location);
        if (node instanceof DirectoryHolderNode) {
            DirectoryHolder dirHolder = ((DirectoryHolderNode) node).getDirectoryHolder();
            return dirHolder instanceof StarredDirectoryHolder;
        } else {
            return false;
        }
    }

    private LibraryNode getNodeFromLocation(DropLocation location) {
        TreePath path = tree.getUI().getClosestPathForLocation(tree, location.getDropPoint().x, location.getDropPoint().y);
        return path != null ? (LibraryNode) path.getLastPathComponent() : null;
    }
}
