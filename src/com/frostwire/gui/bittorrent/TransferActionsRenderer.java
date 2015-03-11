/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
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

package com.frostwire.gui.bittorrent;

import com.frostwire.JsonEngine;
import com.frostwire.gui.AlphaIcon;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.logging.Logger;
import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.SearchResult;
import com.frostwire.search.StreamableSearchResult;
import com.frostwire.search.archiveorg.ArchiveorgTorrentSearchResult;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.torrent.PaymentOptions.PaymentMethod;
import com.frostwire.util.StringUtils;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.FWAbstractJPanelTableCellRenderer;
import com.limegroup.gnutella.gui.search.SearchResultActionsHolder;
import com.limegroup.gnutella.gui.search.UISearchResult;
import com.limegroup.gnutella.gui.tables.TableActionLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author gubatron
 * @author aldenml
 */
public final class TransferActionsRenderer extends FWAbstractJPanelTableCellRenderer {

    private static final Logger LOG = Logger.getLogger(TransferActionsRenderer.class);

    private  static final float BUTTONS_TRANSPARENCY = 0.85f;
    private  static final ImageIcon play_solid;
    private  static final AlphaIcon play_transparent;

    private JLabel labelPlay;
    private boolean showSolid;

    static {
        play_solid = GUIMediator.getThemeImage("search_result_play_over");
        play_transparent = new AlphaIcon(play_solid, BUTTONS_TRANSPARENCY);
    }

    public TransferActionsRenderer() {
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        labelPlay = new JLabel(play_transparent);
        labelPlay.setToolTipText(I18n.tr("Play/Preview"));
        labelPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPlay_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 3;
        add(labelPlay, c);

        setEnabled(true);
    }

    @Override
    protected void updateUIData(Object dataHolder, JTable table, int row, int column) {
        updateUIData((TransferHolder) dataHolder, table, row, column);
    }

    private void updateUIData(TransferHolder actionsHolder, JTable table, int row, int column) {
        showSolid = mouseIsOverRow(table, row);
        updatePlayButton();
        labelPlay.setVisible(isSearchResultPlayable());
    }

    private boolean isSearchResultPlayable() {
        boolean playable = false;
//        if (searchResult.getSearchResult() instanceof StreamableSearchResult) {
//            playable = ((StreamableSearchResult) searchResult.getSearchResult()).getStreamUrl() != null;
//            if (playable && searchResult.getExtension() != null) {
//                MediaType mediaType = MediaType.getMediaTypeForExtension(searchResult.getExtension());
//                playable = mediaType != null && (mediaType.equals(MediaType.getAudioMediaType())) || mediaType.equals(MediaType.getVideoMediaType());
//            }
//        }
        return playable;
    }

    private void updatePlayButton() {
        //labelPlay.setIcon((isStreamableSourceBeingPlayed(searchResult)) ? GUIMediator.getThemeImage("speaker") : (showSolid) ? play_solid : play_transparent);
    }

    private void labelPlay_mouseReleased(MouseEvent e) {
//        if (e.getButton() == MouseEvent.BUTTON1) {
//            if (searchResult.getSearchResult() instanceof StreamableSearchResult && !isStreamableSourceBeingPlayed(searchResult)) {
//                searchResult.play();
//                updatePlayButton();
//            }
//
//            uxLogMediaPreview();
//        }
    }

    private void uxLogMediaPreview() {
//        MediaType mediaType = MediaType.getMediaTypeForExtension(searchResult.getExtension());
//        if (mediaType != null) {
//            boolean isVideo = mediaType.equals(MediaType.getVideoMediaType());
//            UXStats.instance().log(isVideo ? UXAction.SEARCH_RESULT_VIDEO_PREVIEW : UXAction.SEARCH_RESULT_AUDIO_PREVIEW);
//        }
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
