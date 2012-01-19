package com.frostwire.gui.library;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.limewire.util.OSUtils;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.alexandria.db.LibraryDatabase;
import com.frostwire.gui.android.FileDescriptor;
import com.frostwire.gui.library.android.DeviceFileDescriptor;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;

class LibraryDeviceTableTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -360187293186425556L;

    private final LibraryDeviceTableMediator mediator;
    private final TransferHandler fallbackTransferHandler;

    public LibraryDeviceTableTransferHandler(LibraryDeviceTableMediator mediator) {
        this.mediator = mediator;
        this.fallbackTransferHandler = new MulticastTransferHandler(DNDUtils.DEFAULT_TRANSFER_HANDLERS);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return canImport(support, true);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support, false)) {
            return fallbackTransferHandler.importData(support);
        }

        try {
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int index = dl.getRow();
            int max = mediator.getTable().getModel().getRowCount();
            if (index < 0 || index > max)
               index = max;
            /*
            Transferable transferable = support.getTransferable();
            if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
                if (mediator.getCurrentPlaylist() != null) {
                    importPlaylistItemArrayData(transferable, index);
                }
            } else {
                if (mediator.getCurrentPlaylist() != null) {
                    File[] files = DNDUtils.getFiles(support.getTransferable());
                    if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
                        LibraryUtils.asyncAddToPlaylist(mediator.getCurrentPlaylist(), files[0], index);
                    } else {
                        LibraryUtils.asyncAddToPlaylist(mediator.getCurrentPlaylist(), files, index);
                    }
                }
            }*/
        } catch (Exception e) {
            return fallbackTransferHandler.importData(support);
        }

        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        List<AbstractLibraryTableDataLine<DeviceFileDescriptor>> lines = mediator.getSelectedLines();
        List<DeviceFileDescriptor> playlistItems = new ArrayList<DeviceFileDescriptor>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            playlistItems.add(lines.get(i).getInitializeObject());
        }
        return null;//new LibraryDeviceTableTransferable(playlistItems);
    }

    private boolean canImport(TransferSupport support, boolean fallback) {
        //support.setShowDropLocation(false);
        if (!mediator.getMediaType().equals(MediaType.getAudioMediaType())) {
            return fallback ? fallbackTransferHandler.canImport(support) : false;
        }
//        if (mediator.getCurrentPlaylist() != null && mediator.getCurrentPlaylist().getId() == LibraryDatabase.STARRED_PLAYLIST_ID) {
//            return false;
//        }

        if (support.isDataFlavorSupported(LibraryPlaylistsTableTransferable.ITEM_ARRAY)) {
            return true;
        } else if (DNDUtils.containsFileFlavors(support.getDataFlavors())) {
        	if (OSUtils.isMacOSX()) {
        		return true;
        	}
            try {
                File[] files = DNDUtils.getFiles(support.getTransferable());
                for (File file : files) {
                    if (AudioPlayer.isPlayableFile(file)) {
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
                return fallback ? fallbackTransferHandler.canImport(support) : false;
            } catch (InvalidDnDOperationException e) {
                // this case seems to be something special with the OS
                return true;
            } catch (Exception e) {
                return fallback ? fallbackTransferHandler.canImport(support) : false;
            }
        }

        return false;
    }
    
    private void importPlaylistItemArrayData(Transferable transferable, int index) throws UnsupportedFlavorException, IOException {
        PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistsTableTransferable.Item[]) transferable.getTransferData(LibraryPlaylistsTableTransferable.ITEM_ARRAY));
        //LibraryUtils.asyncAddToPlaylist(mediator.getCurrentPlaylist(), playlistItems, index);
    }
}
