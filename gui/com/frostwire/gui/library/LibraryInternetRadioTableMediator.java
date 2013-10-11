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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.limewire.util.OSUtils;
import org.limewire.util.StringUtils;

import com.frostwire.alexandria.InternetRadioStation;
import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.player.InternetRadioAudioSource;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.gui.theme.SkinMenu;
import com.frostwire.gui.theme.SkinMenuItem;
import com.frostwire.gui.theme.SkinPopupMenu;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.search.GenericCellEditor;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameEditor;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.util.GUILauncher;
import com.limegroup.gnutella.gui.util.GUILauncher.LaunchableProvider;
import com.limegroup.gnutella.settings.TablesHandlerSettings;
import com.limegroup.gnutella.util.QueryUtils;

/**
 * This class wraps the JTable that displays files in the library,
 * controlling access to the table and the various table properties.
 * It is the Mediator to the Table part of the Library display.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
final class LibraryInternetRadioTableMediator extends AbstractLibraryTableMediator<LibraryInternetRadioTableModel, LibraryInternetRadioTableDataLine, InternetRadioStation> {

    private static final InternetRadioBookmarkRenderer INTERNET_RADIO_BOOKMARK_RENDERER = new InternetRadioBookmarkRenderer();
    private Action importRadioStationAction;
    private Action copyStreamUrlAction;
    private Action LAUNCH_ACTION;
    private Action DELETE_ACTION;

    /**
     * instance, for singelton access
     */
    private static LibraryInternetRadioTableMediator INSTANCE;

    public static LibraryInternetRadioTableMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryInternetRadioTableMediator();
        }
        return INSTANCE;
    }

    /**
     * Build some extra listeners
     */
    protected void buildListeners() {
        super.buildListeners();

        importRadioStationAction = new AddRadioStationAction();
        copyStreamUrlAction = new CopyStreamUrlAction();
        LAUNCH_ACTION = new LaunchAction();
        DELETE_ACTION = new RemoveFromStationsAction();
    }

    /**
     * Set up the constants
     */
    protected void setupConstants() {
        super.setupConstants();
        MAIN_PANEL = new PaddedPanel();
        DATA_MODEL = new LibraryInternetRadioTableModel();
        TABLE = new LimeJTable(DATA_MODEL);
        Action[] aa = new Action[] { LAUNCH_ACTION, DELETE_ACTION, importRadioStationAction, OPTIONS_ACTION };
        BUTTON_ROW = new ButtonRow(aa, ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
    }

    // inherit doc comment
    protected JPopupMenu createPopupMenu() {
        if (TABLE.getSelectionModel().isSelectionEmpty())
            return null;

        JPopupMenu menu = new SkinPopupMenu();

        menu.add(new SkinMenuItem(LAUNCH_ACTION));
        menu.add(new SkinMenuItem(importRadioStationAction));
        menu.add(new SkinMenuItem(copyStreamUrlAction));
        menu.add(new SkinMenuItem(DELETE_ACTION));

        int[] rows = TABLE.getSelectedRows();

        DELETE_ACTION.setEnabled(true);

        menu.addSeparator();
        LibraryInternetRadioTableDataLine line = DATA_MODEL.get(rows[0]);
        menu.add(createSearchSubMenu(line));

        return menu;
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        TABLE.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (LibraryUtils.isRefreshKeyEvent(e)) {
                    //LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                }
            }
        });
    }

    private JMenu createSearchSubMenu(LibraryInternetRadioTableDataLine dl) {
        JMenu menu = new SkinMenu(I18n.tr("Search"));

        if (dl != null) {
            String keywords = QueryUtils.createQueryString(dl.getInitializeObject().getName());
            if (keywords.length() > 0)
                menu.add(new SkinMenuItem(new SearchAction(keywords)));
        }

        if (menu.getItemCount() == 0)
            menu.setEnabled(false);

        return menu;
    }

    /**
     * Upgrade getScrolledTablePane to public access.
     */
    public JComponent getScrolledTablePane() {
        return super.getScrolledTablePane();
    }

    /* Don't display anything for this.  The LibraryMediator will do it. */
    protected void updateSplashScreen() {
    }

    /**
     * Note: This is set up for this to work.
     * Polling is not needed though, because updates
     * already generate update events.
     */
    private LibraryInternetRadioTableMediator() {
        super("LIBRARY_INTERNET_RADIO_TABLE");
        setMediaType(MediaType.getAudioMediaType());

        LimeTableColumn genreColumn = LibraryInternetRadioTableDataLine.GENRE_COLUMN;

        if (genreColumn != null && TablesHandlerSettings.getVisibility(genreColumn.getId(), genreColumn.getDefaultVisibility()).getValue()) {
            DATA_MODEL.sort(LibraryInternetRadioTableDataLine.GENRE_IDX); //ascending
        }
    }

    /**
     * Sets up drag & drop for the table.
     */
    protected void setupDragAndDrop() {
        TABLE.setDragEnabled(true);
        TABLE.setDropMode(DropMode.INSERT_ROWS);
        // TABLE.setTransferHandler(new LibraryPlaylistsTableTransferHandler(this));
    }

    @Override
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
        TABLE.setDefaultRenderer(PlayableCell.class, new PlayableCellRenderer());
        TABLE.setDefaultRenderer(InternetRadioBookmark.class, INTERNET_RADIO_BOOKMARK_RENDERER);
    }

    /**
     * Sets the default editors.
     */
    protected void setDefaultEditors() {
        TableColumnModel model = TABLE.getColumnModel();
        TableColumn tc;
        tc = model.getColumn(LibraryInternetRadioTableDataLine.WEBSITE_IDX);
        tc.setCellEditor(new ActionIconAndNameEditor());

        //Hey Gosling, nice inconsistency here...
        //Why not TABLE.setDefaultCellEditor(Clazz, EditorObj)???

        tc = model.getColumn(LibraryInternetRadioTableDataLine.BOOKMARKED_IDX);
        tc.setCellEditor(new InternetRadioBookmarkEditor(new InternetRadioBookmarkRenderer()));

        TABLE.addMouseMotionListener(new MouseMotionAdapter() {
            int currentCellColumn = -1;
            int currentCellRow = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                Point hit = e.getPoint();
                int hitColumn = TABLE.columnAtPoint(hit);
                int hitRow = TABLE.rowAtPoint(hit);
                if (currentCellRow != hitRow || currentCellColumn != hitColumn) {
                    if (TABLE.getCellRenderer(hitRow, hitColumn) instanceof InternetRadioBookmarkRenderer) {
                        TABLE.editCellAt(hitRow, hitColumn);
                    }
                    currentCellColumn = hitColumn;
                    currentCellRow = hitRow;
                }
            }
        });
        
        tc = model.getColumn(LibraryInternetRadioTableDataLine.ACTIONS_IDX);
        tc.setCellEditor(new GenericCellEditor(new LibraryActionsRenderer()));
    }

    /**
     * Cancels all editing of fields in the tree and table.
     */
    void cancelEditing() {
        if (TABLE.isEditing()) {
            TableCellEditor editor = TABLE.getCellEditor();
            editor.cancelCellEditing();
        }
    }

    /**
     * Adds the mouse listeners to the wrapped <tt>JTable</tt>.
     *
     * @param listener the <tt>MouseInputListener</tt> that handles mouse events
     *                 for the library
     */
    void addMouseInputListener(final MouseInputListener listener) {
        TABLE.addMouseListener(listener);
        TABLE.addMouseMotionListener(listener);
    }

    /**
     * Updates the Table based on the selection of the given table.
     * Perform lookups to remove any store files from the shared folder
     * view and to only display store files in the store view
     */
    void updateTableItems(List<InternetRadioStation> items) {
        if (items == null) {
            return;
        }

        clearTable();
        for (final InternetRadioStation item : items) {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    addUnsorted(item);
                }
            });
        }

        forceResort();
    }

    /**
     * Returns the <tt>File</tt> stored at the specified row in the list.
     *
     * @param row the row of the desired <tt>File</tt> instance in the
     *            list
     *
     * @return a <tt>File</tt> instance associated with the specified row
     *         in the table
     */
    File getFile(int row) {
        return DATA_MODEL.getFile(row);
    }

    /**
     * Accessor for the table that this class wraps.
     *
     * @return The <tt>JTable</tt> instance used by the library.
     */
    JTable getTable() {
        return TABLE;
    }

    ButtonRow getButtonRow() {
        return BUTTON_ROW;
    }

    LibraryInternetRadioTableDataLine[] getSelectedLibraryLines() {
        int[] selected = TABLE.getSelectedRows();
        LibraryInternetRadioTableDataLine[] lines = new LibraryInternetRadioTableDataLine[selected.length];
        for (int i = 0; i < selected.length; i++)
            lines[i] = DATA_MODEL.get(selected[i]);
        return lines;
    }

    /**
     * Accessor for the <tt>ListSelectionModel</tt> for the wrapped
     * <tt>JTable</tt> instance.
     */
    ListSelectionModel getSelectionModel() {
        return TABLE.getSelectionModel();
    }

    /**
     * Programatically starts a rename of the selected item.
     */
    void startRename() {
        int row = TABLE.getSelectedRow();
        if (row == -1)
            return;
        //int viewIdx = TABLE.convertColumnIndexToView(LibraryPlaylistsTableDataLine.NAME_IDX);
        //TABLE.editCellAt(row, viewIdx, LibraryTableCellEditor.EVENT);
    }

    /**
     * Shows the license window.
     */
    void showLicenseWindow() {
        //        LibraryTableDataLine ldl = DATA_MODEL.get(TABLE.getSelectedRow());
        //        if(ldl == null)
        //            return;
        //        FileDesc fd = ldl.getFileDesc();
        //        License license = fd.getLicense();
        //        URN urn = fd.getSHA1Urn();
        //        LimeXMLDocument doc = ldl.getXMLDocument();
        //        LicenseWindow window = LicenseWindow.create(license, urn, doc, this);
        //        GUIUtils.centerOnScreen(window);
        //        window.setVisible(true);
    }

    /**
     * Delete selected items from a playlist (not from disk)
     */
    public void removeSelection() {
        LibraryInternetRadioTableDataLine[] lines = getSelectedLibraryLines();

        int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), I18n.trn(I18n.tr("Are you sure you want to remove the selected radio station?"), I18n.tr("Are you sure you want to remove the selected radio stations?"), lines.length), I18n.tr("Are you sure?"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        for (LibraryInternetRadioTableDataLine line : lines) {
            InternetRadioStation item = line.getInitializeObject();
            item.delete();
        }

        LibraryMediator.instance().getLibraryExplorer().selectRadio();
        clearSelection();

        super.removeSelection();
    }

    public void handleActionKey() {
        playSong();
    }

    private void playSong() {
        LibraryInternetRadioTableDataLine line = DATA_MODEL.get(TABLE.getSelectedRow());
        if (line == null || line.getInitializeObject() == null || line.getInitializeObject().getUrl() == null) {
            return;
        }

        try {
            MediaSource audioSource = new InternetRadioAudioSource(line.getInitializeObject().getUrl(), line.getInitializeObject());
            MediaPlayer.instance().asyncLoadMedia(audioSource, true, false, null, getFilesView());
            UXStats.instance().log(UXAction.LIBRARY_PLAY_AUDIO_FROM_RADIO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches the associated applications for each selected file
     * in the library if it can.
     */
    void launch() {
        int[] rows = TABLE.getSelectedRows();
        if (rows.length == 0) {
            return;
        }

        File selectedFile = DATA_MODEL.getFile(rows[0]);

        if (OSUtils.isWindows()) {
            if (selectedFile.isDirectory()) {
                GUIMediator.launchExplorer(selectedFile);
                return;
            } else if (!MediaPlayer.isPlayableFile(selectedFile)) {
                GUIMediator.launchFile(selectedFile);
                return;
            }

        }

        LaunchableProvider[] providers = new LaunchableProvider[rows.length];
        for (int i = 0; i < rows.length; i++) {
            providers[i] = new FileProvider(DATA_MODEL.getFile(rows[i]));
        }
        GUILauncher.launch(providers);
        UXStats.instance().log(UXAction.LIBRARY_PLAY_AUDIO_FROM_RADIO);
    }

    /**
     * Handles the selection rows in the library window,
     * enabling or disabling buttons and chat menu items depending on
     * the values in the selected rows.
     * 
     * @param row the index of the first row that is selected
     */
    public void handleSelection(int row) {
        int[] sel = TABLE.getSelectedRows();
        if (sel.length == 0) {
            handleNoSelection();
            return;
        }

        //File selectedFile = getFile(sel[0]);

        copyStreamUrlAction.setEnabled(true);
        LAUNCH_ACTION.setEnabled(true);
        DELETE_ACTION.setEnabled(true);
        SEND_TO_FRIEND_ACTION.setEnabled(false);

        if (sel.length == 1) {
            LibraryMediator.instance().getLibraryCoverArt().setDefault();
        }
    }

    /**
     * Handles the deselection of all rows in the library table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {

        copyStreamUrlAction.setEnabled(false);
        LAUNCH_ACTION.setEnabled(false);
        DELETE_ACTION.setEnabled(false);

        SEND_TO_FRIEND_ACTION.setEnabled(false);
    }

    /**
     * Refreshes the enabledness of the Enqueue button based
     * on the player enabling state. 
     */
    public void setPlayerEnabled(boolean value) {
        handleSelection(TABLE.getSelectedRow());
    }

    public boolean setPlaylistItemSelected(InternetRadioStation item) {
        int i = DATA_MODEL.getRow(item);

        if (i != -1) {
            TABLE.setSelectedRow(i);
            TABLE.ensureSelectionVisible();
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////
    //  ACTIONS
    ///////////////////////////////////////////////////////

    private final class LaunchAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 949208465372392591L;

        public LaunchAction() {
            putValue(Action.NAME, I18n.tr("Launch"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Launch Selected Files"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_LAUNCH");
        }

        public void actionPerformed(ActionEvent ae) {
            launch();
        }
    }

    public static final class AddRadioStationAction extends AbstractAction {

        private static final long serialVersionUID = 7087376528613706765L;

        public AddRadioStationAction() {
            super(I18n.tr("Add Radio"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Add a new Radio Station. You must enter the Stream's URL"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_ADD_RADIO_STATION");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String input = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Radio Station's stream URL"), I18n.tr("Add Radio Station"), JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (!StringUtils.isNullOrEmpty(input, true)) {
                LibraryUtils.asyncAddRadioStation(input);
            }
        }
    }

    private final class RemoveFromStationsAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -8704093935791256631L;

        public RemoveFromStationsAction() {
            putValue(Action.NAME, I18n.tr("Delete"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Delete Radio Station"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_DELETE");
        }

        public void actionPerformed(ActionEvent ae) {
            REMOVE_LISTENER.actionPerformed(ae);
        }
    }

    private static class FileProvider implements LaunchableProvider {

        private final File _file;

        public FileProvider(File file) {
            _file = file;
        }

        public File getFile() {
            return _file;
        }
    }

    @Override
    public List<MediaSource> getFilesView() {
        int size = DATA_MODEL.getRowCount();
        List<MediaSource> result = new ArrayList<MediaSource>(size);
        for (int i = 0; i < size; i++) {
            try {
                String url = DATA_MODEL.get(i).getInitializeObject().getUrl();
                result.add(new InternetRadioAudioSource(url, DATA_MODEL.get(i).getInitializeObject()));
            } catch (Throwable e) {
            }
        }
        return result;
    }

    @Override
    protected void sortAndMaintainSelection(int columnToSort) {
        super.sortAndMaintainSelection(columnToSort);
        resetAudioPlayerFileView();
    }

    private void resetAudioPlayerFileView() {
        Playlist playlist = MediaPlayer.instance().getCurrentPlaylist();
        if (playlist == null) {
            if (MediaPlayer.instance().getPlaylistFilesView() != null) {
            	MediaPlayer.instance().setPlaylistFilesView(getFilesView());
            }
        }
    }

    private final class CopyStreamUrlAction extends AbstractAction {

        private static final long serialVersionUID = 5603390659365617618L;

        public CopyStreamUrlAction() {
            putValue(Action.NAME, I18n.tr("Copy Stream URL"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Copy Stream URL"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Copy Stream URL"));
        }

        public void actionPerformed(ActionEvent e) {
            LibraryInternetRadioTableDataLine[] lines = getSelectedLibraryLines();
            String str = "";
            for (int i = 0; i < lines.length; i++) {
                str += lines[i].getInitializeObject().getUrl();
                str += "\n";
            }
            GUIMediator.setClipboardContent(str);
        }
    }

    @Override
    protected MediaSource createMediaSource(LibraryInternetRadioTableDataLine line) {
        return new InternetRadioAudioSource(line.getInitializeObject().getUrl(), line.getInitializeObject());
    }
}