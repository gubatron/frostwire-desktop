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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import org.limewire.util.OSUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.LibraryPlaylists.LibraryPlaylistsListCell;
import com.frostwire.gui.library.LibraryPlaylistsTableTransferable.PlaylistDragItem;
import com.frostwire.gui.player.MediaPlayer;
import com.limegroup.gnutella.gui.dnd.DNDUtils;

class LibraryPlaylistsTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -3874985752229848555L;
    
    private final JList<?> list;
    private final LibraryPlaylists libPlaylists;

    public LibraryPlaylistsTransferHandler(JList<?> list, LibraryPlaylists libPlaylists) {
        this.list = list;
        this.libPlaylists = libPlaylists;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        
    	DropLocation location = support.getDropLocation();
        int index = list.locationToIndex(location.getDropPoint());
        
        if ( support.isDataFlavorSupported(LibraryPlaylistsTableTransferable.PLAYLIST_ITEM) || 
             support.isDataFlavorSupported(LibraryPlaylistsTableTransferable.ITEM_ARRAY) ) {
            return true;
        } else if (DNDUtils.containsFileFlavors(support.getDataFlavors())) {
            if (OSUtils.isMacOSX()) {
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
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        DropLocation location = support.getDropLocation();
        int index = list.locationToIndex(location.getDropPoint());
        
        if (index == -1) {
        	return false;
        }
        
        LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) list.getModel().getElementAt(index);
        Playlist playlist = cell.getPlaylist();
        
        if (support.isDataFlavorSupported(LibraryPlaylistsTableTransferable.PLAYLIST_ITEM)) {
        	
        	// handle PLAYLIST_ITEM drop
        	
        	// adjust index to apply to insertion point, not to cell being hovered over
        	Rectangle rect = list.getUI().getCellBounds(list, index, index);
        	Point point = location.getDropPoint();
            if (point.y - rect.y > rect.height / 2) {
                index+=1;
            }
        	
        	Transferable transferable = support.getTransferable();
        	PlaylistDragItem playlistDragItem;
			
        	try {
				playlistDragItem = (PlaylistDragItem) transferable.getTransferData(LibraryPlaylistsTableTransferable.PLAYLIST_ITEM);
				libPlaylists.movePlaylistToNewIndex(playlistDragItem.originalIndex, index);
        	
        	} catch (Exception e) {
				e.printStackTrace();
			}
			
        	
        } else {
            
        	// handle ITEM_ARRAY or other drop
	        
            Rectangle rect = list.getUI().getCellBounds(list, index, index);
            if (!rect.contains(location.getDropPoint())) {
                index = 0;
            }
            
	        if (playlist == null) {
	            try {
	                Transferable transferable = support.getTransferable();
	                if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
	                    PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistsTableTransferable.Item[]) transferable.getTransferData(LibraryPlaylistsTableTransferable.ITEM_ARRAY));
	                    LibraryUtils.createNewPlaylist(playlistItems);
	                } else {
	                    File[] files = DNDUtils.getFiles(support.getTransferable());
	                    if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
	                        LibraryUtils.createNewPlaylist(files[0]);
	                    } else {
	                        LibraryUtils.createNewPlaylist(files);
	                    }
	                }
	                list.setSelectedIndex(list.getModel().getSize() - 1);
	                LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
	            } catch (Exception e) {
	                return false;
	            }
	        } else {
	            try {
	                Transferable transferable = support.getTransferable();
	                if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
	                    PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistsTableTransferable.Item[]) transferable.getTransferData(LibraryPlaylistsTableTransferable.ITEM_ARRAY));
	                    LibraryUtils.asyncAddToPlaylist(playlist, playlistItems);
	                } else {
	                    File[] files = DNDUtils.getFiles(support.getTransferable());
	                    if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
	                        LibraryUtils.asyncAddToPlaylist(playlist, files[0]);
	                    } else {
	                        LibraryUtils.asyncAddToPlaylist(playlist, files);
	                    }
	                }
	                //_list.setSelectedIndex(index);
	                //refreshSelection();
	            } catch (Exception e) {
	                return false;
	            }
	        }
        }

        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE | LINK;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) list.getSelectedValue();
        int index = list.getSelectedIndex();
        
        if (cell != null && cell.getPlaylist() != null && cell.getPlaylist().getItems().size() > 0) {
            return new LibraryPlaylistsTableTransferable(cell.getPlaylist(), index);
        } else {
            return null;
        }
    }
}
