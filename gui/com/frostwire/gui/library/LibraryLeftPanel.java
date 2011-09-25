package com.frostwire.gui.library;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

public class LibraryLeftPanel extends JPanel {

    private static final long serialVersionUID = -2924157073406477820L;

    public static final int MIN_WIDTH = 155;
    public static final int MAX_WIDTH = 300;

    private final LibraryFiles libraryFiles;
    private final LibraryPlaylists libraryPlaylists;
    private final LibraryCoverArt libraryCoverArt;

    public LibraryLeftPanel(LibraryFiles libraryFiles, LibraryPlaylists libraryPlaylists, LibraryCoverArt libraryCoverArt) {
        this.libraryFiles = libraryFiles;
        this.libraryPlaylists = libraryPlaylists;
        this.libraryCoverArt = libraryCoverArt;

        setupUI();
    }

    protected void setupUI() {
        setLayout(null);

        add(libraryFiles);
        add(libraryPlaylists);
        add(libraryCoverArt);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });
    }

    protected void layoutComponents() {
        Dimension size = getSize();

        // layout cover art
        int coverArtWidth = size.width > MAX_WIDTH ? MAX_WIDTH : size.width;
        libraryCoverArt.setLocation(0, coverArtWidth < size.height ? size.height - coverArtWidth : 0);
        libraryCoverArt.setSize(coverArtWidth, coverArtWidth);

        // layout files and playlists
        int heightMinusCover = size.height - coverArtWidth;
        int fileRowHeight = libraryFiles.getRowDimension().height;
        int playlistRowHeight = libraryPlaylists.getRowDimension().height;

        if (3 * (fileRowHeight + playlistRowHeight) > heightMinusCover) {
            // too small, split even
            libraryFiles.setLocation(0, 0);
            libraryFiles.setSize(size.width, heightMinusCover / 2);
            libraryPlaylists.setLocation(0, heightMinusCover / 2);
            libraryPlaylists.setSize(size.width, heightMinusCover - heightMinusCover / 2);
        } else if ((libraryFiles.getRowsCount() + 1) * fileRowHeight + 3 * playlistRowHeight > heightMinusCover) {
            // too small for complete display of files
            int libraryFilesHeight = heightMinusCover - 3 * playlistRowHeight;
            libraryFiles.setLocation(0, 0);
            libraryFiles.setSize(size.width, libraryFilesHeight);
            libraryPlaylists.setLocation(0, libraryFilesHeight);
            libraryPlaylists.setSize(size.width, heightMinusCover - libraryFilesHeight);
        } else {
            // complete display of files
            int libraryFilesHeight = (libraryFiles.getRowsCount() + 1) * fileRowHeight;
            libraryFiles.setLocation(0, 0);
            libraryFiles.setSize(size.width, libraryFilesHeight);
            libraryPlaylists.setLocation(0, libraryFilesHeight);
            libraryPlaylists.setSize(size.width, heightMinusCover - libraryFilesHeight);
        }
    }

}
