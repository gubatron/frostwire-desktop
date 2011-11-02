package com.frostwire.gui.library;

import java.io.File;

import com.frostwire.alexandria.InternetRadioStation;
import com.limegroup.gnutella.gui.tables.ColoredCellImpl;
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
	 * Returns the file extension for the given row.
	 *
	 * @param row  The row of the file
	 *
	 * @return  A <code>String</code> object containing the file extension
	 */
	String getType(int row) {
	    return (String)(
	             (ColoredCellImpl)get(row).getValueAt(
	                LibraryInternetRadioTableDataLine.TYPE_IDX)).getValue();
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
	    return col == LibraryInternetRadioTableDataLine.WEBSITE_IDX || col == LibraryInternetRadioTableDataLine.BOOKMARKED_IDX;
	}
}
