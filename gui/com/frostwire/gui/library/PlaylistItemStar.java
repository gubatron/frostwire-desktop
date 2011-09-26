package com.frostwire.gui.library;

/**
 * Wraps the current dataline to be displayed in the table to pass it to the
 * {@link PlaylistItemNameRenderer}
 */
class PlaylistItemStar implements Comparable<Object> {

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

	public int compareTo(Object o) {
		if (o == null) {
			return 1;
		}
		
		PlaylistItemStar other = (PlaylistItemStar) o;

		if (other.getLine() == null ||other.getLine().getSongName() == null) {
			return 1;
		}
		
		if (getLine() == null || getLine().getSongName() == null) {
			return -1;
		}
		
		return line.getSongName().compareTo(other.getLine().getSongName());
	}
}
