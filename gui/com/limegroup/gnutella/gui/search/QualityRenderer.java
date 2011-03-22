package com.limegroup.gnutella.gui.search;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/**
 * This class handles rendering the "Quality" column in the search results.
 * It uses different labels depending on how many stars should be displayed.
 */
final class QualityRenderer extends SubstanceDefaultTableCellRenderer implements ThemeObserver {

	/**
     * 
     */
    private static final long serialVersionUID = -3140525602102010666L;

    /**
	 * Icon for one star.
	 */
	private Icon STAR_ONE;

	/**
	 * Icon for two stars.
	 */
	private Icon STAR_TWO;

	/**
	 * Icon for three stars.
	 */
	private Icon STAR_THREE;

	/**
	 * Icon for four stars.
	 */
	private Icon STAR_FOUR;
	
	/**
	 * Icon for five stars.
	 */
	private Icon STAR_FIVE;
	
	/**
	 * Icon for a saved file.
	 */
	private Icon SAVED_FILE;
	
	/**
	 * Icon for a downloading file.
	 */
	private Icon DOWNLOADING_FILE;

	/**
	 * Icon for an incomplete file.
	 */
	private Icon INCOMPLETE_FILE;
	
    /**
     * Icon for a spam file.
     */
    private Icon SPAM_FILE;
    
    /** Icon for rendering a secure result. */
    private Icon SECURE_FILE;
    
    /**
     * 'Quality' for spam file results.
     */
    static final int SPAM_FILE_QUALITY = 1003;
    
	/**
	 * 'Quality' for saved file results.
	 */
	static final int SAVED_FILE_QUALITY = 1002;
	
	/**
	 * 'Quality' for downloading file results.
	 */
	static final int DOWNLOADING_FILE_QUALITY = 1001;
	
	/**
	 * 'Quality' for files that are incomplete (but not downloading)
	 */
	static final int INCOMPLETE_FILE_QUALITY = 1000;
    
    /** 'Quality' for files that are considered secure results. */
    static final int SECURE_QUALITY = 999;
    
    /**
     * Number of stars ("quality") for multicast results.
     */
    static final int MULTICAST_QUALITY = 4;
    
    /**
     * Number of stars ("quality") for results from non-firewalled hosts with
     * free upload slots.
     */
    static final int EXCELLENT_QUALITY = 3;
    
    /**
     * Number of stars ("quality") for results that have a good chance of 
     * success.
     */
    static final int GOOD_QUALITY = 2;
    
    /**
     * Number of stars ("quality") for results that have a fair chance of 
     * success.
     */
    static final int FAIR_QUALITY = 1;
    
    /**
     * Number of stars ("quality") for results that have a poor chance of
     * success.
     */
    static final int POOR_QUALITY = 0;
    
	/**
	 * Makes all of the star labels opaque and sets their borders.
	 */
	public QualityRenderer() {
	    setHorizontalAlignment(SwingConstants.CENTER);
        
		updateTheme();
		ThemeMediator.addThemeObserver(this);
	}

	public void updateTheme() {
		STAR_ONE = GUIMediator.getThemeImage("01_star");
		STAR_TWO = GUIMediator.getThemeImage("02_star");
		STAR_THREE = GUIMediator.getThemeImage("03_star");
		STAR_FOUR = GUIMediator.getThemeImage("04_star");
		STAR_FIVE = GUIMediator.getThemeImage("05_star");
		SAVED_FILE = GUIMediator.getThemeImage("complete");
		DOWNLOADING_FILE = GUIMediator.getThemeImage("downloading");
		INCOMPLETE_FILE = GUIMediator.getThemeImage("incomplete");
        SPAM_FILE = GUIMediator.getThemeImage("spam_mini");
        // that's: lime hi res, not lime hires. :)
        SECURE_FILE = GUIMediator.getThemeImage("frosthires");
	}

	/**
	 * Returns the <tt>Component</tt> that displays the stars based
	 * on the number of stars in the <tt>QualityHolder</tt> object.
	 */
	public Component getTableCellRendererComponent
		(JTable table,Object value,boolean isSelected,
		 boolean hasFocus,int row,int column) {
		    
        // Since "value" can be null, make sure we handle that case by simply
        // setting the quality to poor.
		int numStars = value == null ? POOR_QUALITY : 
		    ((Integer)value).intValue();
		
		Icon curIcon;
		
		switch(numStars) {
        case SECURE_QUALITY:
            curIcon = SECURE_FILE; break;
        case SPAM_FILE_QUALITY:
            curIcon = SPAM_FILE; break;
        case SAVED_FILE_QUALITY:
            curIcon = SAVED_FILE; break;
        case DOWNLOADING_FILE_QUALITY:
            curIcon = DOWNLOADING_FILE; break;
        case INCOMPLETE_FILE_QUALITY:
            curIcon = INCOMPLETE_FILE; break;
        case MULTICAST_QUALITY:
            curIcon = STAR_FIVE; break;
        case EXCELLENT_QUALITY:
            curIcon = STAR_FOUR; break;
        case GOOD_QUALITY:
            curIcon = STAR_THREE; break;
        case FAIR_QUALITY:
            curIcon = STAR_TWO; break;
        default:
            curIcon = STAR_ONE;
        }
		
		setIcon(curIcon);
		return super.getTableCellRendererComponent(
	            table, null, isSelected, hasFocus, row, column);
	}
}
