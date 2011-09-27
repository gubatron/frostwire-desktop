package com.frostwire.gui.library;

import com.limegroup.gnutella.gui.tables.SizeHolder;

public class PlayableCell implements Comparable<PlayableCell> {
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
			
			if (wrappedObject instanceof SizeHolder) {
				if (((SizeHolder) wrappedObject).getSize()==0) {
					return "--";
				}
			}
			
			return wrappedObject.toString();
		}
		return "";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compareTo(PlayableCell o) {
		if (wrappedObject instanceof Comparable &&
			wrappedObject != null && o.wrappedObject != null &&
			wrappedObject.getClass().equals(o.wrappedObject.getClass())) {
			return ((Comparable) wrappedObject).compareTo(o.wrappedObject);
		}
		
		return toString().compareTo(o.toString());
	}
}
