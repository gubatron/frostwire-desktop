package com.frostwire.gui.library;

/**
 * Wraps the current dataline to be displayed in the table to pass it to the
 * {@link PlaylistItemNameRenderer}
 */
class InternetRadioBookmark implements Comparable<InternetRadioBookmark> {

	/**
	 * The current line to display in the table
	 */
	private final LibraryInternetRadioTableDataLine line;
	private boolean isPlaying;

	public InternetRadioBookmark(LibraryInternetRadioTableDataLine line, boolean isPlaying) {
		this.line = line;
		this.isPlaying = isPlaying;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	

	/**
	 * @return the current dataline
	 */
	public LibraryInternetRadioTableDataLine getLine() {
		return line;
	}

	public int compareTo(InternetRadioBookmark o) {
	    return -1*Boolean.valueOf(line.getInitializeObject().isBookmarked()).compareTo(o.line.getInitializeObject().isBookmarked());
	}
}
