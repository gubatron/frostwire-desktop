/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.AlphaIcon;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.StreamableSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class SearchResultActionsRenderer extends JPanel implements TableCellRenderer {

    private JLabel labelPlay;
    private JLabel labelPartialDownload;
    private JLabel labelDownload;

    private UISearchResult sr;
    
    private final float BUTTONS_TRANSPARENCY = 0.35f;
    private final ImageIcon play_solid;
    private final AlphaIcon play_transparent;
    private final ImageIcon download_solid;
    private final AlphaIcon download_transparent;
    private final ImageIcon details_solid;
    private final AlphaIcon details_transparent;
    private boolean showSolid;

    public SearchResultActionsRenderer() {
        play_solid = GUIMediator.getThemeImage("search_result_play_over");
        play_transparent = new AlphaIcon(play_solid, BUTTONS_TRANSPARENCY);
        
        download_solid = GUIMediator.getThemeImage("search_result_download_over");
        download_transparent = new AlphaIcon(download_solid, BUTTONS_TRANSPARENCY);
        
        details_solid = GUIMediator.getThemeImage("search_result_details_over");
        details_transparent = new AlphaIcon(details_solid, BUTTONS_TRANSPARENCY);
        
        setupUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        this.setData((SearchResultActionsHolder) value, table, row);
        this.setOpaque(true);
        this.setEnabled(table.isEnabled());

        if (isSelected) {
            this.setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            this.setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        //TODO: Try removing the mouse listeners of the labels and add them again somewhere around here with another method
        resetMouseListeners();
        return this;
    }

    private void resetMouseListeners() {
        MouseListener[] mouseListeners = labelDownload.getMouseListeners();
        if (mouseListeners != null && mouseListeners.length > 0) {
            for (MouseListener l : mouseListeners) {
                labelDownload.removeMouseListener(l);
            }
        }
        labelDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelDownload_mouseReleased(e);
            }
        });
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        labelPlay = new JLabel(play_transparent);
        labelPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPlay_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        //c.fill = GridBagConstraints.HORIZONTAL;
        //c.weightx = 1.0;
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 3;
        add(labelPlay, c);

        labelDownload = new JLabel(download_transparent);
        labelDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelDownload_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 3;
        add(labelDownload, c);
        
        labelPartialDownload = new JLabel(details_solid);
        labelPartialDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPartialDownload_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 3;
        add(labelPartialDownload, c);

    }

    private void labelPlay_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (sr.getSearchResult() instanceof StreamableSearchResult && !isStreamableSourceBeingPlayed(sr)) {
                sr.play();
                updatePlayButton();
            }
        }
    }

    private void labelPartialDownload_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (sr.getSearchResult() instanceof CrawlableSearchResult) {
                sr.download(true);
            }
        }
    }

    private void labelDownload_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            sr.download(false);
        }
    }

    private void setData(SearchResultActionsHolder value, JTable table, int row) {
        this.sr = value.getSearchResult();
        showSolid = mouseIsOverRow(table, row);
        updatePlayButton();
        
        labelDownload.setIcon(showSolid ? download_solid : download_transparent);
        labelPartialDownload.setIcon(showSolid ? details_solid : details_transparent);
        
        labelPlay.setVisible(sr.getSearchResult() instanceof StreamableSearchResult);
        labelDownload.setVisible(true);
        labelPartialDownload.setVisible(sr.getSearchResult() instanceof CrawlableSearchResult);
    }

    private void updatePlayButton() {
        labelPlay.setIcon((isStreamableSourceBeingPlayed(sr)) ? GUIMediator.getThemeImage("speaker") : (showSolid) ? play_solid : play_transparent);
    }
    
    private boolean mouseIsOverRow(JTable table, int row) {
        boolean mouseOver = false;

        try {
            TableUI ui = table.getUI();
            if (ui instanceof SkinTableUI) {
                mouseOver = ((SkinTableUI) ui).getRowAtMouse() == row;
            }
        } catch (Throwable e) {
            // ignore
        }
        return mouseOver;
    }

    private boolean isStreamableSourceBeingPlayed(UISearchResult sr) {
        if (!(sr instanceof StreamableSearchResult)) {
            return false;
        }

        StreamableSearchResult ssr = (StreamableSearchResult) sr;
        return MediaPlayer.instance().isThisBeingPlayed(ssr.getStreamUrl());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updatePlayButton();
    }    
}