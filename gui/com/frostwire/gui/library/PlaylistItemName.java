package com.frostwire.gui.library;

/**
 * Wraps the current dataline to be displayed in the table to pass it to the
 * {@link PlaylistItemNameRenderer}
 */
class PlaylistItemName implements Comparable<Object> {

	/**
	 * The current line to display in the table
	 */
	private final LibraryPlaylistsTableDataLine line;

	public PlaylistItemName(LibraryPlaylistsTableDataLine line) {
		this.line = line;
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
		
		PlaylistItemName other = (PlaylistItemName) o;

		if (other.getLine() == null ||other.getLine().getSongName() == null) {
			return 1;
		}
		
		if (getLine() == null || getLine().getSongName() == null) {
			return -1;
		}
		
		return line.getSongName().compareTo(other.getLine().getSongName());
	}
}
