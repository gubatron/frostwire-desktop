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
import com.limegroup.gnutella.gui.dnd.FileTransferable;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;

class LibraryFilesTableTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -5962762524077270378L;

    private final LibraryFilesTableMediator mediator;
    private final TransferHandler fallbackTransferHandler;

    public LibraryFilesTableTransferHandler(LibraryFilesTableMediator mediator) {
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
                PlaylistItem[] playlistItems = LibraryUtils.convertToPlaylistItems((LibraryPlaylistTransferable.Item[]) transferable
                        .getTransferData(LibraryPlaylistTransferable.ITEM_ARRAY));
                LibraryUtils.createNewPlaylist(playlistItems);
            } else {
                File[] files = DNDUtils.getFiles(support.getTransferable());
                if (files.length == 1 && files[0].getAbsolutePath().endsWith(".m3u")) {
                    LibraryUtils.createNewPlaylist(files[0]);
                } else {
                    LibraryUtils.createNewPlaylist(files);
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
        List<AbstractLibraryTableDataLine<File>> lines = mediator.getSelectedLines();
        List<File> files = new ArrayList<File>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            files.add(lines.get(i).getFile());
        }
        return new FileTransferable(files);
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
                    } else if (file.isDirectory()) {
                        for (File childFile : file.listFiles()) {
                            if (AudioPlayer.isPlayableFile(childFile)) {
                                return true;
                            }
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
}
