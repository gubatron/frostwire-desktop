package com.frostwire.gui.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.frostwire.alexandria.PlaylistItem;

public class LibraryPlaylistTransferable implements Transferable {

    public static final DataFlavor ITEM_ARRAY = new DataFlavor(LibraryPlaylistTransferable.Item[].class, "LibraryPlaylistTransferable.Item Array");

    private final List<LibraryPlaylistTransferable.Item> items;

    public LibraryPlaylistTransferable(List<PlaylistItem> playlistItems) {
        items = LibraryUtils.convertToItems(playlistItems);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { ITEM_ARRAY };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ITEM_ARRAY);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return items.toArray(new Item[0]);
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
        public String artistName;
        public String albumName;
        public String coverArtPath;
        public String bitrate;
        public String comment;
        public String genre;
        public String track;
        public String year;
    }
}
