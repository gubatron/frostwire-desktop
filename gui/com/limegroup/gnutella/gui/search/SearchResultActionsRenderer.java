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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

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

    public SearchResultActionsRenderer() {
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

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected final void paintComponent(Graphics g) {
        super.paintComponent(g);
        updatePlayButtons();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        labelPlay = new JLabel(GUIMediator.getThemeImage("search_result_play_over"));
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

        labelPartialDownload = new JLabel(GUIMediator.getThemeImage("search_result_details_over"));
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

        labelDownload = new JLabel(GUIMediator.getThemeImage("search_result_download_over"));
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
    }

    private void labelPlay_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (sr.getSearchResult() instanceof StreamableSearchResult && !isStreamableSourceBeingPlayed(sr)) {
                sr.play();
                updatePlayButtons();
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
        boolean showButtons = mouseIsOverRow(table, row);
        labelPlay.setVisible(showButtons && (sr.getSearchResult() instanceof StreamableSearchResult));
        labelPartialDownload.setVisible(showButtons && sr.getSearchResult() instanceof CrawlableSearchResult);
        labelDownload.setVisible(showButtons);

        if (showButtons) {
            updatePlayButtons();
        }

        if (isStreamableSourceBeingPlayed(sr)) {
            labelPlay.setVisible(true);
        }
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

    private void updatePlayButtons() {
        labelPlay.setIcon((isStreamableSourceBeingPlayed(sr)) ? GUIMediator.getThemeImage("speaker") : GUIMediator.getThemeImage("search_result_play_over"));
    }

    private boolean isStreamableSourceBeingPlayed(UISearchResult sr) {
        if (!(sr instanceof StreamableSearchResult)) {
            return false;
        }

        StreamableSearchResult ssr = (StreamableSearchResult) sr;
        return MediaPlayer.instance().isThisBeingPlayed(ssr.getStreamUrl());
    }
}