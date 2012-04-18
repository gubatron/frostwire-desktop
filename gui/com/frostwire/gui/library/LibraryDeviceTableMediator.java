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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableCellEditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ThreadExecutor;
import org.limewire.util.FilenameUtils;
import org.limewire.util.StringUtils;

import com.frostwire.gui.filters.TableLineFilter;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.frostwire.gui.player.DeviceAudioSource;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.util.QueryUtils;

public class LibraryDeviceTableMediator extends AbstractLibraryTableMediator<LibraryDeviceTableModel, LibraryDeviceTableDataLine, FileDescriptor> {

    private static final Log LOG = LogFactory.getLog(LibraryDeviceTableMediator.class);
    /**
     * Variables so the PopupMenu & ButtonRow can have the same listeners
     */
    public static Action LAUNCH_ACTION;
    private Action saveToAction;

    private Device device;
    private byte fileType;
    
    private FileDescriptorFilter FILE_DESCRIPTOR_FILTER;

    /**
     * instance, for singelton access
     */
    private static LibraryDeviceTableMediator INSTANCE;

    public static LibraryDeviceTableMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryDeviceTableMediator();
        }
        return INSTANCE;
    }

    /**
     * Build some extra listeners
     */
    protected void buildListeners() {
        super.buildListeners();

        LAUNCH_ACTION = new LaunchAction();
        saveToAction = new SaveToAction();
    }

    /**
     * Set up the constants
     */
    protected void setupConstants() {
        super.setupConstants();
        MAIN_PANEL = new PaddedPanel();
        FILE_DESCRIPTOR_FILTER = new FileDescriptorFilter();
        DATA_MODEL = new LibraryDeviceTableModel(FILE_DESCRIPTOR_FILTER);
        TABLE = new LimeJTable(DATA_MODEL);
        Action[] aa = new Action[] { LAUNCH_ACTION, saveToAction, OPTIONS_ACTION };
        BUTTON_ROW = new ButtonRow(aa, ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
    }

    // inherit doc comment
    protected JPopupMenu createPopupMenu() {
        if (TABLE.getSelectionModel().isSelectionEmpty())
            return null;

        JPopupMenu menu = new SkinPopupMenu();

        menu.add(new SkinMenuItem(LAUNCH_ACTION));
        menu.add(new SkinMenuItem(saveToAction));

        int[] rows = TABLE.getSelectedRows();

        menu.addSeparator();
        LibraryDeviceTableDataLine line = DATA_MODEL.get(rows[0]);
        menu.add(createSearchSubMenu(line));

        return menu;
    }

    private JMenu createSearchSubMenu(LibraryDeviceTableDataLine dl) {
        JMenu menu = new SkinMenu(I18n.tr("Search"));

        if (dl != null) {
            String str = buildQueryString(dl.getInitializeObject());
            String keywords = QueryUtils.createQueryString(str);
            if (keywords.length() > 0)
                menu.add(new SkinMenuItem(new SearchAction(keywords)));
        }

        if (menu.getItemCount() == 0)
            menu.setEnabled(false);

        return menu;
    }

    private String buildQueryString(FileDescriptor fd) {
        String str = FilenameUtils.getBaseName(fd.filePath);
        if (fd.album != null && !fd.album.toLowerCase().contains("unknown")) {
            str += " " + fd.album;
        }
        if (fd.artist != null && !fd.artist.toLowerCase().contains("unknown")) {
            str += " " + fd.artist;
        }
        return str;
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
    private LibraryDeviceTableMediator() {
        super("LIBRARY_DEVICE_TABLE");
        setMediaType(MediaType.getAnyTypeMediaType());
        ThemeMediator.addThemeObserver(this);
    }

    /**
     * Sets up drag & drop for the table.
     */
    protected void setupDragAndDrop() {
        TABLE.setDragEnabled(true);
        TABLE.setDropMode(DropMode.INSERT_ROWS);
        // TABLE.setTransferHandler(new LibraryDeviceTableTransferHandler(this));
    }

    @Override
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
        TABLE.setDefaultRenderer(PlayableIconCell.class, new PlayableIconCellRenderer());
        TABLE.setDefaultRenderer(PlayableCell.class, new PlayableCellRenderer());
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
    
    @Override
    public void handleMouseDoubleClick(MouseEvent e) {
        super.handleMouseDoubleClick(e);
        
        if (LAUNCH_ACTION.isEnabled()) {
            LAUNCH_ACTION.actionPerformed(null);
        } else if (saveToAction.isEnabled()) {
            saveToAction.actionPerformed(null);
        }
    }

    /**
     * Updates the Table based on the selection of the given table.
     * Perform lookups to remove any store files from the shared folder
     * view and to only display store files in the store view
     */
    void updateTableFiles(final Device device, final byte fileType) {
        clearTable();

        this.device = device;
        this.fileType = fileType;
        this.DATA_MODEL.setDevice(device);

        BackgroundExecutorService.schedule(new Runnable() {

            @Override
            public void run() {
                List<FileDescriptor> fds = device.browse(fileType);

                if (!LibraryDeviceTableMediator.this.device.equals(device) || LibraryDeviceTableMediator.this.fileType != fileType) {
                    return; // selected another node in the tree
                }

                for (int i = 0; i < fds.size(); i++) {
                    addUnsorted(fds.get(i));
                }
                forceResort();
                
                LibraryMediator.instance().getLibraryExplorer().executePendingRunnables();
            }
        });
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

    LibraryDeviceTableDataLine[] getSelectedLibraryLines() {
        int[] selected = TABLE.getSelectedRows();
        LibraryDeviceTableDataLine[] lines = new LibraryDeviceTableDataLine[selected.length];
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

    public void handleActionKey() {
        if (fileType == DeviceConstants.FILE_TYPE_AUDIO || fileType == DeviceConstants.FILE_TYPE_RINGTONES) {
            playSong();
        }
    }

    private void playSong() {
        LibraryDeviceTableDataLine line = DATA_MODEL.get(TABLE.getSelectedRow());
        if (line == null) {
            return;
        }

        try {
            URL url = new URL(device.getDownloadURL(line.getInitializeObject()));
            AudioSource audioSource = new DeviceAudioSource(url, device, line.getInitializeObject());
            if (AudioPlayer.isPlayableFile(audioSource)) {
                AudioPlayer.instance().asyncLoadSong(audioSource, true, true, null, getFileView());
            }
        } catch (Throwable e) {
            LOG.error("Error loading the streaming", e);
        }
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

        SEND_TO_FRIEND_ACTION.setEnabled(false);

        FileDescriptor fd = DATA_MODEL.get(sel[0]).getInitializeObject();

        LAUNCH_ACTION.setEnabled(sel.length == 1 && (fileType == DeviceConstants.FILE_TYPE_AUDIO || fileType == DeviceConstants.FILE_TYPE_RINGTONES) && AudioPlayer.isPlayableFile(fd.filePath));
        saveToAction.setEnabled(true);
        
        if (sel.length == 1) {
            LibraryMediator.instance().getLibraryCoverArt().setDefault();
        }
    }

    /**
     * Handles the deselection of all rows in the library table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {
        LAUNCH_ACTION.setEnabled(false);
        SEND_TO_FRIEND_ACTION.setEnabled(false);
        saveToAction.setEnabled(false);
    }

    /**
     * Refreshes the enabledness of the Enqueue button based
     * on the player enabling state. 
     */
    public void setPlayerEnabled(boolean value) {
        handleSelection(TABLE.getSelectedRow());
    }

    private void downloadSelectedItems() {
        List<AbstractLibraryTableDataLine<FileDescriptor>> selectedLines = getSelectedLines();

        List<FileDescriptor> fds = new ArrayList<FileDescriptor>(selectedLines.size());

        for (AbstractLibraryTableDataLine<FileDescriptor> line : selectedLines) {
            fds.add(line.getInitializeObject());
        }
        
        ThreadExecutor.startThread(new DownloadTask(LibrarySettings.LIBRARY_FROM_DEVICE_DATA_DIR_SETTING.getValue(), device, fds.toArray(new FileDescriptor[0])), "DownloadFromDevice");
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
            playSong();
        }
    }

    private final class SaveToAction extends AbstractAction {

        private static final long serialVersionUID = 8400749433148927596L;

        public SaveToAction() {
            putValue(Action.NAME, I18n.tr("Save"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Save"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Save Selected Files To Folder"));
            putValue(LimeAction.ICON_NAME, "DEVICE_SAVE");
        }

        public void actionPerformed(ActionEvent e) {
            downloadSelectedItems();
        }
    }

    @Override
    public List<AudioSource> getFileView() {
        int size = DATA_MODEL.getRowCount();
        List<AudioSource> result = new ArrayList<AudioSource>(size);
        for (int i = 0; i < size; i++) {
            try {
                URL url = new URL(device.getDownloadURL(DATA_MODEL.get(i).getInitializeObject()));
                result.add(new DeviceAudioSource(url, device, DATA_MODEL.get(i).getInitializeObject()));
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
        //        Playlist playlist = AudioPlayer.instance().getCurrentPlaylist();
        //        if (playlist != null && playlist.equals(currentPlaylist)) {
        //            if (AudioPlayer.instance().getPlaylistFilesView() != null) {
        //                AudioPlayer.instance().setPlaylistFilesView(getFileView());
        //            }
        //        }
    }

    @Override
    protected AudioSource createAudioSource(LibraryDeviceTableDataLine line) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void filter(String query) {
        FILE_DESCRIPTOR_FILTER.setQuery(query);
        DATA_MODEL.filtersChanged();
    }
    
    @Override
    public void clearTable() {
        super.clearTable();
        FILE_DESCRIPTOR_FILTER.setQuery(null);
    }
    
    class FileDescriptorFilter implements TableLineFilter<LibraryDeviceTableDataLine> {
        
        private String query;
        private Set<String> tokens;
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            if (StringUtils.isNullOrEmpty(query, true) || query.equals(".")) {
                this.query = null;
                this.tokens = null;
            } else {
                this.query = StringUtils.removeDoubleSpaces(query);
                this.tokens = new HashSet<String>(Arrays.asList(this.query.toLowerCase().split(" ")));
            }
        }
        
        @Override
        public boolean allow(LibraryDeviceTableDataLine node) {
            if (tokens == null) {
                return true;
            }
            FileDescriptor fd = node.getInitializeObject();
            
            String keywords = (fd.title + " " + fd.artist + " " + fd.album + " " + fd.year).toLowerCase();

            boolean foundMatch = true;

            for (String token : tokens) {
                if (!keywords.contains(token)) {
                    foundMatch = false;
                    break;
                }
            }
            
            return foundMatch;
        }
    }
}
