package com.frostwire.gui.library;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;

class LibraryPlaylistsTableTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -360187293186425556L;
    
    private final LibraryPlaylistsTableMediator mediator;
    private final TransferHandler fallbackTransferHandler;

    public LibraryPlaylistsTableTransferHandler(LibraryPlaylistsTableMediator mediator) {
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
            Transferable transferable = support.getTransferable();
            if (DNDUtils.contains(transferable.getTransferDataFlavors(), LibraryPlaylistTransferable.ITEM_ARRAY)) {
                if (mediator.getCurrentPlaylist() != null) {
                    PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistTransferable.Item[]) transferable.getTransferData(LibraryPlaylistTransferable.ITEM_ARRAY));
                    LibraryUtils.asyncAddToPlaylist(mediator.getCurrentPlaylist(), playlistItems);
                }
            } else {
                if (mediator.getCurrentPlaylist() != null) {
                    File[] files = DNDUtils.getFiles(support.getTransferable());
                    LibraryUtils.asyncAddToPlaylist(mediator.getCurrentPlaylist(), files);
                }
            }
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
        List<AbstractLibraryTableDataLine<PlaylistItem>> lines = mediator.getSelectedLines();
        List<PlaylistItem> playlistItems = new ArrayList<PlaylistItem>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            playlistItems.add(lines.get(i).getInitializeObject());
        }
        return new LibraryPlaylistTransferable(playlistItems);
    }

    private boolean canImport(TransferSupport support, boolean fallback) {
        if (!mediator.getMediaType().equals(MediaType.getAudioMediaType())) {
            return fallback ? fallbackTransferHandler.canImport(support) : false;
        }

        if (support.isDataFlavorSupported(LibraryPlaylistTransferable.ITEM_ARRAY)) {
            return true;
        } else if (DNDUtils.containsFileFlavors(support.getDataFlavors())) {
            try {
                File[] files = DNDUtils.getFiles(support.getTransferable());
                for (File file : files) {
                    if (AudioPlayer.isPlayableFile(file)) {
                        return true;
                    }
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
}
