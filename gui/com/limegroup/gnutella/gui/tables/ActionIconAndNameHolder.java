package com.limegroup.gnutella.gui.tables;

import javax.swing.Icon;


/**
 * Default implementation of IconAndNameHolder.
 *
 * Stores an Icon and a String so that both can be displayed
 * in a single column.
 */
public final class ActionIconAndNameHolder implements Comparable<ActionIconAndNameHolder> {
	
	private final Icon _icon;
	private final String _name;
	
	public ActionIconAndNameHolder(Icon icon, String name) {
	    _icon = icon;
	    _name = name;
    }
	
	public int compareTo(ActionIconAndNameHolder o) {
	    return AbstractTableMediator.compare(_name, o._name);
	}
	
	public Icon getIcon() {
	    return _icon;
	}
	
	public String getName() {
	    return _name;
	}
	
	public String toString() {
	    return _name;
    }
}
