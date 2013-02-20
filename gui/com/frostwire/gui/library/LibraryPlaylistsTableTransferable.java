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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.limegroup.gnutella.gui.dnd.FileTransferable;

public class LibraryPlaylistsTableTransferable implements Transferable {

    public static final DataFlavor ITEM_ARRAY = new DataFlavor(LibraryPlaylistsTableTransferable.Item[].class, "LibraryPlaylistTransferable.Item Array");
    public static final DataFlavor PLAYLIST_ITEM = new DataFlavor(PlaylistDragItem.class, "LibraryPlaylistTransferable.PlaylistDragItem");

    private final List<LibraryPlaylistsTableTransferable.Item> items;
    private final PlaylistDragItem playlistDragItem;
    private final FileTransferable fileTransferable;

    public LibraryPlaylistsTableTransferable(Playlist playlist, int originalIndex) {
    	
    	List<PlaylistItem> playlistItems = playlist.getItems();
    	items = LibraryUtils.convertToItems(playlistItems);
        
    	playlistDragItem = new PlaylistDragItem(originalIndex);
    	
        List<File> files = new ArrayList<File>(items.size());
        for (PlaylistItem item : playlistItems) {
            files.add(new File(item.getFilePath()));
        }
        fileTransferable = new FileTransferable(files);
    }
    
    public LibraryPlaylistsTableTransferable(List<PlaylistItem> playlistItems) {
    	
    	items = LibraryUtils.convertToItems(playlistItems);
        playlistDragItem = null;
    	
        List<File> files = new ArrayList<File>(items.size());
        for (PlaylistItem item : playlistItems) {
            files.add(new File(item.getFilePath()));
        }
        fileTransferable = new FileTransferable(files);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        List<DataFlavor> list = new ArrayList<DataFlavor>();
        list.addAll(Arrays.asList(fileTransferable.getTransferDataFlavors()));
        list.add(ITEM_ARRAY);
        
        if (playlistDragItem != null) {
        	list.add(PLAYLIST_ITEM);
        }
        
        return list.toArray(new DataFlavor[0]);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ITEM_ARRAY) || fileTransferable.isDataFlavorSupported(flavor) ||
        	   (flavor.equals(PLAYLIST_ITEM) && playlistDragItem != null);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(PLAYLIST_ITEM) ) {
        	return playlistDragItem;
        } else if (flavor.equals(ITEM_ARRAY) ) {
            return items.toArray(new Item[0]);
        } else {
            return fileTransferable.getTransferData(flavor);
        }
    }
    
    public static final class PlaylistDragItem implements Serializable {
    	
    	private static final long serialVersionUID = 9127568944630801656L;
		public int originalIndex;
    	
    	public PlaylistDragItem(int originalIndex) {
    		this.originalIndex = originalIndex;
    	}

    }
    
    public static final class Item implements Serializable {
        private static final long serialVersionUID = 928701185904989565L;

        public Item() {
        }

        public int id;
        public String filePath;
        public String fileName;
        public long fileSize;
        public String fileExtension;
        public String trackTitle;
        public float trackDurationInSecs;
        public String trackArtist;
        public String trackAlbum;
        public String coverArtPath;
        public String trackBitrate;
        public String trackComment;
        public String trackGenre;
        public String trackNumber;
        public String trackYear;
        public boolean starred;
    }
}
