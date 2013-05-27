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

package com.frostwire.gui.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.gui.player.DeviceMediaSource;
import com.frostwire.gui.player.InternetRadioAudioSource;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.gui.theme.ThemeSettings;
import com.limegroup.gnutella.gui.GUIMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class LibraryNameHolderRenderer extends JPanel implements TableCellRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryNameHolderRenderer.class);

    private JLabel labelText;
    private JLabel labelPlay;
    private JLabel labelDownload;

    private LibraryNameHolder libraryNameHolder;

    public LibraryNameHolderRenderer() {
        setupUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        this.setData((LibraryNameHolder) value, table, row, column);
        this.setOpaque(true);
        this.setEnabled(table.isEnabled());

        if (isSelected) {
            this.setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            this.setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        return this;
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints c;

        labelText = new JLabel();
        labelText.setHorizontalTextPosition(SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(labelText, c);

        labelPlay = new JLabel(GUIMediator.getThemeImage("search_result_play_over"));
        labelPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPlay_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 1;
        add(labelPlay, c);

        labelDownload = new JLabel(GUIMediator.getThemeImage("search_result_download_over"));
        labelDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelDownload_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.ipadx = 1;
        add(labelDownload, c);
    }

    private void labelPlay_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (libraryNameHolder != null && libraryNameHolder.getDataLine() != null) {

                MediaSource mediaSource = null;
                List<MediaSource> filesView = null;
                boolean playNextSong = false;
                Object dataLine = libraryNameHolder.getDataLine();

                if (dataLine instanceof LibraryFilesTableDataLine) {
                    mediaSource = new MediaSource(((LibraryFilesTableDataLine) dataLine).getFile());
                    filesView = LibraryFilesTableMediator.instance().getFilesView();
                    playNextSong = true;
                } else if (dataLine instanceof LibraryPlaylistsTableDataLine) {
                    mediaSource = new MediaSource(((LibraryPlaylistsTableDataLine) dataLine).getPlayListItem());
                    filesView = LibraryPlaylistsTableMediator.instance().getFilesView();
                    playNextSong = true;
                } else if (dataLine instanceof LibraryInternetRadioTableDataLine) {
                    LibraryInternetRadioTableDataLine irDataLine = (LibraryInternetRadioTableDataLine) dataLine;
                    mediaSource = new InternetRadioAudioSource(irDataLine.getInitializeObject().getUrl(), irDataLine.getInitializeObject());
                    filesView = LibraryInternetRadioTableMediator.instance().getFilesView();
                    playNextSong = false;
                } else if (dataLine instanceof LibraryDeviceTableDataLine) {
                    LibraryDeviceTableDataLine dl = (LibraryDeviceTableDataLine) dataLine;
                    Device device = LibraryMediator.instance().getLibraryExplorer().getSelectedDeviceFiles();
                    if (device != null) {
                        String url = device.getDownloadURL(dl.getInitializeObject());
                        mediaSource = new DeviceMediaSource(url, device, dl.getInitializeObject());
                        filesView = LibraryDeviceTableMediator.instance().getFilesView();
                        playNextSong = true;
                    }
                }

                if (mediaSource != null && !isSourceBeingPlayed()) {
                    labelPlay.setVisible(false);
                    MediaPlayer.instance().asyncLoadMedia(mediaSource, true, playNextSong, null, filesView);
                }
            }
        }
    }

    private void labelDownload_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (libraryNameHolder != null && libraryNameHolder.getDataLine() != null) {
                Object dataLine = libraryNameHolder.getDataLine();

                if (dataLine instanceof LibraryDeviceTableDataLine) {
                    Device device = LibraryMediator.instance().getLibraryExplorer().getSelectedDeviceFiles();
                    if (device != null) {
                        LibraryDeviceTableMediator.instance().downloadSelectedItems();
                    }
                }
            }
        }
    }

    private void setData(LibraryNameHolder value, JTable table, int row, int column) {
        try {
            libraryNameHolder = value;
            labelText.setText(value.toString());

            labelText.setFont(table.getFont());
            ThemeMediator.fixLabelFont(labelText);

            boolean showButtons = mouseIsOverRow(table, row);
            labelPlay.setVisible(showButtons && !isSourceBeingPlayed() && isPlayableDataLine());
            labelDownload.setVisible(showButtons && isDownloadableFromOtherDevice());
            setFontColor(libraryNameHolder.isPlaying(), table, row, column);
        } catch (Throwable e) {
            LOG.warn("Error puting data in name holder renderer");
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

    private boolean isDownloadableFromOtherDevice() {
        boolean result = false;
        if (libraryNameHolder != null && libraryNameHolder.getDataLine() != null) {
            Object dataLine = libraryNameHolder.getDataLine();

            if (dataLine instanceof LibraryDeviceTableDataLine) {

                Device device = LibraryMediator.instance().getLibraryExplorer().getSelectedDeviceFiles();
                if (device != null && !device.isLocal()) {
                    result = true;
                }
            }
        }
        return result;

    }

    private boolean isPlayableDataLine() {
        Object dl = libraryNameHolder.getDataLine();
        if (dl instanceof LibraryFilesTableDataLine) {
            return MediaPlayer.isPlayableFile(((LibraryFilesTableDataLine) dl).getFile());
        } else if (dl instanceof LibraryPlaylistsTableDataLine) {
            return true;
        } else if (dl instanceof LibraryInternetRadioTableDataLine) {
            return true;
        } else if (dl instanceof LibraryDeviceTableDataLine) {
            return MediaPlayer.isPlayableFile(((LibraryDeviceTableDataLine) dl).getInitializeObject().filePath);
        } else {
            return false;
        }
    }

    private boolean isSourceBeingPlayed() {
        if (libraryNameHolder == null) {
            return false;
        }

        return libraryNameHolder.isPlaying();
    }

    /**
     * Check what font color to use if this song is playing. 
     */
    private void setFontColor(boolean isPlaying, JTable table, int row, int column) {
        if (!libraryNameHolder.isExists()) {
            setForeground(ThemeSettings.FILE_NO_EXISTS_DATA_LINE_COLOR.getValue());
        } else if (isPlaying) {
            labelText.setForeground(ThemeSettings.PLAYING_DATA_LINE_COLOR.getValue());
        } else {
            labelText.setForeground(table.getForeground());
        }
    }
}