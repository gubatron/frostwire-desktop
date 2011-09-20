package com.frostwire.gui.library;

public class PlayableCell {
	private boolean isPlaying;

	private Object wrappedObject;

	public PlayableCell(Object wrapMe, boolean isPlaying) {
		wrappedObject = wrapMe;
		this.isPlaying = isPlaying;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	@Override
	public String toString() {
		if (wrappedObject!=null) {
			return wrappedObject.toString();
		}
		return "";
	}
}
