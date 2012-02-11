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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSplitPane;


public class LibraryLeftPanel extends JPanel {

    private static final long serialVersionUID = -2924157073406477820L;

    public static final int MIN_WIDTH = 155;
    public static final int MAX_WIDTH = 300;

    private final LibraryExplorer libraryExplorer;
    private final LibraryPlaylists libraryPlaylists;
    private final LibraryCoverArt libraryCoverArt;
    
    private final JSplitPane splitPane;

    public LibraryLeftPanel(LibraryExplorer libraryExplorer, LibraryPlaylists libraryPlaylists, LibraryCoverArt libraryCoverArt) {
        this.libraryExplorer = libraryExplorer;
        this.libraryPlaylists = libraryPlaylists;
        this.libraryCoverArt = libraryCoverArt;
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        setupUI();
    }

    protected void setupUI() {
        setLayout(new GridBagLayout());

         //Prepare a split pane with explorers
        splitPane.setTopComponent(libraryExplorer);
        splitPane.setBottomComponent(libraryPlaylists);
        splitPane.setDividerLocation(0.5);
        splitPane.setAutoscrolls(true);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.gridx=0;
        c.gridy=0;
        c.insets = new Insets(0,0,0,0);
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        add(splitPane,c);
        
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_END;
        c.fill = GridBagConstraints.BOTH;
        c.gridx=0;
        c.gridy=1;
        c.insets = new Insets(0,0,0,0);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridheight = 1;
        c.gridwidth = 1;

        add(Box.createVerticalStrut(2));
        
        add(libraryCoverArt,c);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });
    }

    protected void layoutComponents() {
        Dimension size = getSize();

        // layout files and playlists takes whatever is left in height
        splitPane.setSize(new Dimension(size.width-4,size.height-(size.width+4)));
        splitPane.setPreferredSize(new Dimension(size.width-4,size.height-(size.width+4)));
        splitPane.setMinimumSize(new Dimension(MIN_WIDTH,size.height-(MAX_WIDTH+4)));

        // the size of the cover art is proportional to the width available.
        int coverArtWidth = size.width > MAX_WIDTH ? MAX_WIDTH : size.width-4;
        libraryCoverArt.setLocation(0, size.height - coverArtWidth);
        libraryCoverArt.setSize(coverArtWidth, coverArtWidth);
        libraryCoverArt.setPreferredSize(new Dimension(coverArtWidth,coverArtWidth));
        libraryCoverArt.setMaximumSize(new Dimension(coverArtWidth,coverArtWidth));
        
        repaint();
    }

}