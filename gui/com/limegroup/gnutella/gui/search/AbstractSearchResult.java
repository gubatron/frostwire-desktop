package com.limegroup.gnutella.gui.search;

import java.awt.Color;

import org.limewire.collection.ApproximateMatcher;
import org.limewire.util.I18NConvert;

import com.limegroup.gnutella.gui.themes.ThemeSettings;

/**
 * A single SearchResult. These are returned in the {@link SearchInputPanel} and
 * are used to create {@link TableLine}s to show search results.
 * 
 * (A collection of RemoteFileDesc, HostData, and Set of alternate locations.)
 */
public abstract class AbstractSearchResult implements SearchResult {

    /**
     * The processed version of the filename used for approximate matching.
     * Not allocated until a match must be done.  The assumption here is that
     * all matches will use the same ApproximateMatcher.
     * 
     * TODO: when we move to Java 1.3, this should be a weak reference so the
     * memory is reclaimed after GC. */
    private String processedFilename;

    public String getFilenameNoExtension() {
        String fullname = getFileName();
        if (fullname == null) {
            throw new NullPointerException("getFileName() can't return a null result");
        }
        int i = fullname.lastIndexOf(".");
        if (i < 0) {
            return fullname;
        }
        return I18NConvert.instance().compose(fullname.substring(0, i));
    }

    public String getExtension() {
        String fullname = getFileName();
        if (fullname == null) {
            throw new NullPointerException("getFileName() can't return a null result");
        }
        int i = fullname.lastIndexOf(".");
        if (i < 0) {
            return "";
        }
        return fullname.substring(i + 1);
    }

    /**
     * Gets the processed filename.
     */
    private String getProcessedFilename(ApproximateMatcher matcher) {
        if (processedFilename != null) {
            return processedFilename;
        }
        return processedFilename = matcher.process(getFilenameNoExtension());
    }

    public final int match(SearchResult sr, final ApproximateMatcher matcher) {

        if (!(sr instanceof AbstractSearchResult)) {
            return 3;
        }

        AbstractSearchResult o = (AbstractSearchResult) sr;

        // Same file type?
        if (!getExtension().equals(o.getExtension())) {
            return 1;
        }

        long thisSize = getSize();
        long thatSize = o.getSize();

        // Sizes same?
        if (thisSize != thatSize) {
            return 2;
        }

        // Preprocess the processed fileNames
        getProcessedFilename(matcher);
        o.getProcessedFilename(matcher);

        // Filenames close?  This is the most expensive test, so it should go
        // last.  Allow 5% edit difference in filenames or 4 characters,
        // whichever is smaller.
        int allowedDifferences = Math.round(Math.min(0.10f * (getFilenameNoExtension().length()), 0.10f * (o.getFilenameNoExtension().length())));
        allowedDifferences = Math.min(allowedDifferences, 4);
        if (!matcher.matches(getProcessedFilename(matcher), o.getProcessedFilename(matcher), allowedDifferences)) {
            return 3;
        }
        
        return 0;
    }

    /**
     * Wether or not this result can be marked as Junk.
     * @return
     */
    public boolean canBeMarkedAsJunk() {
        return false;
    }
    
    public boolean isOverrideRowColor() {
        return false;
    }
    
    public Color getEvenRowColor() {
        return ThemeSettings.DEFAULT_TABLE_EVEN_ROW_COLOR.getValue();
    }
    
    public Color getOddRowColor() {
        return ThemeSettings.DEFAULT_TABLE_ODD_ROW_COLOR.getValue();
    }
        
}