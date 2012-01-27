package com.frostwire.gui.library;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.db.LibraryDatabase;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.DeviceAudioSource;
import com.frostwire.gui.player.InternetRadioAudioSource;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;

public class LibraryIconTree extends JTree {

    private static final long serialVersionUID = 3025054051505168836L;

    private static final Log LOG = LogFactory.getLog(LibraryIconTree.class);

    private Image speaker;

    public LibraryIconTree() {
        loadIcons();
    }

    public LibraryIconTree(TreeModel dataModel) {
        super(dataModel);
        loadIcons();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        try {
            AudioPlayer player = AudioPlayer.instance();

            if (player.getState() != MediaPlaybackState.Stopped) {
                if (player.getCurrentSong() instanceof InternetRadioAudioSource) {
                    TreePath path = getRadioPath();
                    if (path != null) {
                        paintIcon(g, speaker, path);
                    }
                } else if (player.getCurrentSong() instanceof DeviceAudioSource) {
                    TreePath path = getDeviceFileTypePath((DeviceAudioSource) player.getCurrentSong());
                    if (path != null) {
                        paintIcon(g, speaker, path);
                    }
                } else if (player.getCurrentSong() != null && player.getCurrentPlaylist() == null && player.getPlaylistFilesView() != null) {
                    TreePath path = getAudioPath();
                    if (path != null) {
                        paintIcon(g, speaker, path);
                    }
                } else if (player.getCurrentSong() != null && player.getCurrentPlaylist() != null && player.getPlaylistFilesView() != null) {
                    TreePath path = getPlaylistPath(player.getCurrentPlaylist());
                    if (path != null) {
                        paintIcon(g, speaker, path);
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("Error painting the speaker icon, e:" + e.getMessage());
        }
    }

    private TreePath getDeviceFileTypePath(DeviceAudioSource audioSource) {
        Enumeration<?> e = ((LibraryNode) getModel().getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            LibraryNode node = (LibraryNode) e.nextElement();
            if (node instanceof DeviceFileTypeTreeNode) {
                Device device = ((DeviceFileTypeTreeNode) node).getDevice();
                byte fileType = ((DeviceFileTypeTreeNode) node).getFileType();
                if (device.equals(audioSource.getDevice()) && fileType == audioSource.getFileDescriptor().fileType) {
                    return new TreePath(node.getPath());
                }
            }
        }
        return null;
    }

    private void loadIcons() {
        speaker = GUIMediator.getThemeImage("speaker").getImage();
    }

    private void paintIcon(Graphics g, Image image, TreePath path) {
        Rectangle rect = getUI().getPathBounds(this, path);
        Dimension lsize = rect.getSize();
        Point llocation = rect.getLocation();
        g.drawImage(image, llocation.x + lsize.width - speaker.getWidth(null) - 4, llocation.y + (lsize.height - speaker.getHeight(null)) / 2, null);
    }

    private TreePath getAudioPath() {
        Enumeration<?> e = ((LibraryNode) getModel().getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            LibraryNode node = (LibraryNode) e.nextElement();
            if (node instanceof DirectoryHolderNode) {
                DirectoryHolder holder = ((DirectoryHolderNode) node).getDirectoryHolder();
                if (holder instanceof MediaTypeSavedFilesDirectoryHolder && ((MediaTypeSavedFilesDirectoryHolder) holder).getMediaType().equals(MediaType.getAudioMediaType())) {
                    return new TreePath(node.getPath());
                }
            }
        }
        return null;
    }

    private TreePath getRadioPath() {
        Enumeration<?> e = ((LibraryNode) getModel().getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            LibraryNode node = (LibraryNode) e.nextElement();
            if (node instanceof DirectoryHolderNode) {
                DirectoryHolder holder = ((DirectoryHolderNode) node).getDirectoryHolder();
                if (holder instanceof InternetRadioDirectoryHolder) {
                    return new TreePath(node.getPath());
                }
            }
        }
        return null;
    }

    private TreePath getPlaylistPath(Playlist playlist) {
        if (playlist.getId() == LibraryDatabase.STARRED_PLAYLIST_ID) {
            Enumeration<?> e = ((LibraryNode) getModel().getRoot()).depthFirstEnumeration();
            while (e.hasMoreElements()) {
                LibraryNode node = (LibraryNode) e.nextElement();
                if (node instanceof DirectoryHolderNode) {
                    DirectoryHolder holder = ((DirectoryHolderNode) node).getDirectoryHolder();
                    if (holder instanceof StarredDirectoryHolder) {
                        return new TreePath(node.getPath());
                    }
                }
            }
        }

        return null;
    }
}
