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

import java.io.File;

import com.frostwire.alexandria.InternetRadioStation;
import com.limegroup.gnutella.gui.tables.HashBasedDataLineModel;

/**
 * Library specific DataLineModel.
 * Uses HashBasedDataLineModel instead of BasicDataLineModel
 * for quicker access to row's based on the file.
 */
final class LibraryInternetRadioTableModel extends HashBasedDataLineModel<LibraryInternetRadioTableDataLine, InternetRadioStation> {

	/**
     * 
     */
    private static final long serialVersionUID = 3332011897003403390L;

    LibraryInternetRadioTableModel() {
	    super(LibraryInternetRadioTableDataLine.class);
	}
	
    /**
     * Creates a new LibraryTableDataLine
     */
    public LibraryInternetRadioTableDataLine createDataLine() {
        return new LibraryInternetRadioTableDataLine();
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
    public int add(LibraryInternetRadioTableDataLine o) {
        return add(o, getRowCount());
    }
    
    /**
     * Reinitializes a dataline that is using the given initialize object.
     */
    void reinitialize(File f) {
        if(contains(f)) {
//            int row = getRow(f);
//            get(row).initialize(f);
//            fireTableRowsUpdated(row, row);
        }
    }
    
    /**
     * Reinitializes a dataline from using one file to use another.
     */
    void reinitialize(File old, File now) {
        if(contains(old)) {
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
	    return null;//new File(get(row).getInitializeObject().getFilePath());
	}
	
	/**
	 * Returns a boolean specifying whether or not specific cell in the table
	 * is editable.
	 *
	 * @param row the row of the table to access
	 *
	 * @param col the column of the table to access
	 *
	 * @return <code>true</code> if the specified cell is editable,
	 *         <code>false</code> otherwise
	 */
	public boolean isCellEditable(int row, int col) {
	    return col == LibraryInternetRadioTableDataLine.WEBSITE_IDX || 
	           col == LibraryInternetRadioTableDataLine.BOOKMARKED_IDX ||
	           col == LibraryInternetRadioTableDataLine.ACTIONS_IDX;
	}
}
