package com.frostwire.gui.library;

/**
 * Wraps the current dataline to be displayed in the table to pass it to the
 * {@link PlaylistItemNameRenderer}
 */
class PlaylistItemStar implements Comparable<PlaylistItemStar> {

	/**
	 * The current line to display in the table
	 */
	private final LibraryPlaylistsTableDataLine line;
	private boolean isPlaying;
	private boolean exists;

	public PlaylistItemStar(LibraryPlaylistsTableDataLine line, boolean isPlaying, boolean exists) {
		this.line = line;
		this.isPlaying = isPlaying;
		this.exists = exists;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public boolean exists() {
	    return exists;
	}

	/**
	 * @return the current dataline
	 */
	public LibraryPlaylistsTableDataLine getLine() {
		return line;
	}

	public int compareTo(PlaylistItemStar o) {
	    return Boolean.valueOf(line.getInitializeObject().isStarred()).compareTo(o.line.getInitializeObject().isStarred());
	}
}
