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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.core.FileDescriptor;
import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.gui.tables.HashBasedDataLineModel;

/**
 * Library specific DataLineModel.
 * Uses HashBasedDataLineModel instead of BasicDataLineModel
 * for quicker access to row's based on the file.
 */
final class LibraryDeviceTableModel extends HashBasedDataLineModel<LibraryDeviceTableDataLine, FileDescriptor> {

    private static final long serialVersionUID = 2859783399965055446L;

    private Device device;

    /**
     * The filter to use in this row filter.
     */
    private final TableLineFilter<LibraryDeviceTableDataLine> FILTER;

    /**
     * A list of all filtered results.
     */
    protected final List<LibraryDeviceTableDataLine> HIDDEN;

    LibraryDeviceTableModel(TableLineFilter<LibraryDeviceTableDataLine> f) {
        super(LibraryDeviceTableDataLine.class);

        if (f == null)
            throw new NullPointerException("null filter");

        FILTER = f;
        HIDDEN = new ArrayList<LibraryDeviceTableDataLine>();
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Creates a new LibraryTableDataLine
     */
    public LibraryDeviceTableDataLine createDataLine() {
        return new LibraryDeviceTableDataLine(this);
    }

    /**
     * Override the normal refresh.
     * Because the DataLine's don't cache any data,
     * we can just call update & they'll show the correct info
     * now.
     */
    public Object refresh() {
        fireTableRowsUpdated(0, getRowCount());
        return null;
    }

    /**
     * OVerride default so new ones get added to the end
     */
    @Override
    public int add(LibraryDeviceTableDataLine o, int row) {
        if (!allow(o)) {
            HIDDEN.add(o);
            return -1;
        } else {
            return super.add(o, row);
        }
    }
    
    /**
     * Override the dataline add so we can re-initialize files
     * to include the FileDesc.  Necessary for changing pending status
     * to shared status.
     */
    //    @Override
    //	public int add(LibraryPlaylistsTableDataLine dl, int row) {
    //	    File init = dl.getInitializeObject();
    //	    if ( !contains(init) ) {
    //	        return forceAdd(dl, row);
    //	    } else {
    //	        FileDesc fd = dl.getFileDesc();
    //	        if ( fd != null ) {
    //	            row = getRow(init);
    //	            get( row ).setFileDesc(fd);
    //	            fireTableRowsUpdated( row, row );
    //	        }
    //	        // we aren't going to use this dl, so clean it up.
    //	        dl.cleanup();
    //	    }
    //	    return -1;
    //    }

    /**
     * Reinitializes a dataline that is using the given initialize object.
     */
    void reinitialize(File f) {
        if (contains(f)) {
            //            int row = getRow(f);
            //            get(row).initialize(f);
            //            fireTableRowsUpdated(row, row);
        }
    }

    /**
     * Reinitializes a dataline from using one file to use another.
     */
    void reinitialize(File old, File now) {
        if (contains(old)) {
            //            int row = getRow(old);
            //            get(row).initialize(now);
            //            initializeObjectChanged(old, now);
            //            fireTableRowsUpdated(row, row);
        }
    }

    /**
     * Returns the file object stored in the given row.
     *
     * @param row  The row of the file
     *
     * @return  The <code>File</code> object stored at the specified row
     */
    File getFile(int row) {
        return new File(get(row).getInitializeObject().filePath);
    }

    @Override
    public void clear() {
        super.clear();
        HIDDEN.clear();
    }

    /**
     * Determines whether or not the specified line is allowed by the filter.
     */
    private boolean allow(LibraryDeviceTableDataLine tl) {
        return FILTER.allow(tl);
    }

    /**
     * Notification that the filters have changed.
     */
    void filtersChanged() {
        rebuild();
        fireTableDataChanged();
    }

    /**
     * Rebuilds the internal map to denote a new filter.
     */
    private void rebuild() {
        List<LibraryDeviceTableDataLine> existing = new ArrayList<LibraryDeviceTableDataLine>(_list);
        List<LibraryDeviceTableDataLine> hidden = new ArrayList<LibraryDeviceTableDataLine>(HIDDEN);

        clear();

        // For stuff in _list, we can just re-add the DataLines as-is.
        for (int i = 0; i < existing.size(); i++) {
            //if (isSorted()) {
            //see override of getSortedPosition.
            //rebuild only seems to happen when we first build the table
            //in which case addSorted takes care of business by invoking getSortedPosition.
            addSorted(existing.get(i));
            //} else {
            //  add(existing.get(i));
            //  }
        }

        // Merge the hidden TableLines
        for (int i = 0; i < hidden.size(); i++) {
            LibraryDeviceTableDataLine tl = hidden.get(i);

            //if(isSorted()) {
            addSorted(tl);
            //} else {
            //    add(tl);
            // }
        }

    }
    
    public boolean isCellEditable(int row, int col) {
        return col == LibraryDeviceTableDataLine.TITLE_IDX;
    }
}
