package com.frostwire.gnutella.gui.android;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RedispatchMouseListener implements MouseListener, MouseMotionListener {
	
	private JList _list;
	private MouseEvent _mousePressedEvent;
	private MouseListener[] _mouseListeners;
	private MouseMotionListener[] _motionListeners;

	public RedispatchMouseListener(JList list) {
		_list = list;
		_mouseListeners = list.getMouseListeners();
		_motionListeners = list.getMouseMotionListeners();
		for (MouseListener l : _mouseListeners) {
			list.removeMouseListener(l);
		}
		for (MouseMotionListener l : _motionListeners) {
			list.removeMouseMotionListener(l);
		}
		list.addMouseMotionListener(this);
	}

	public Component getComponentAt(MouseEvent e) {
		int index = _list.locationToIndex(e.getPoint());
		int x = e.getX() - _list.indexToLocation(index).x;
		int y = e.getY() - _list.indexToLocation(index).y;
		JPanel renderer = (JPanel) _list.getCellRenderer().getListCellRendererComponent(_list, _list.getModel().getElementAt(index), index, false, false);
		return renderer.getComponentAt(x, y);
	}

	public Rectangle getRepaintBounds(MouseEvent e) {
		int index = _list.locationToIndex(e.getPoint());
		Point p = _list.indexToLocation(index);
		JPanel renderer = (JPanel) _list.getCellRenderer().getListCellRendererComponent(_list, _list.getModel().getElementAt(index), index, false, false);
		return new Rectangle(p.x, p.y, renderer.getPreferredSize().width, renderer.getPreferredSize().height);
	}

	public void mouseClicked(MouseEvent e) {
		Component c = getComponentAt(e);
		if (c instanceof JButton ||
            c instanceof JRadioButton ||
            c instanceof JLabel) {
			c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
			_list.repaint(getRepaintBounds(e));
		} else {
			for (MouseListener l : _mouseListeners) {
				l.mouseClicked(e);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		Component c = getComponentAt(e);
		_mousePressedEvent = e;
		if (c instanceof JButton ||
            c instanceof JRadioButton) {
			c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
			_list.repaint(getRepaintBounds(e));
		} else {
			for (MouseListener l : _mouseListeners) {
				l.mousePressed(e);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (_mousePressedEvent != null) {
			Component c = getComponentAt(_mousePressedEvent);
			if (c instanceof JButton ||
			    c instanceof JRadioButton) {
				c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_RELEASED, e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
				_list.repaint(getRepaintBounds(_mousePressedEvent));
			}
		} else {
			for (MouseListener l : _mouseListeners) {
				l.mouseReleased(e);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		for (MouseListener l : _mouseListeners) {
			l.mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		for (MouseListener l : _mouseListeners) {
			l.mouseExited(e);
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (_mousePressedEvent != null) {
			Component c = getComponentAt(_mousePressedEvent);
			if (c instanceof JButton) {
				return;
			}
		}
		for (MouseMotionListener l : _motionListeners) {
			l.mouseDragged(e);
		}
	}

	public void mouseMoved(MouseEvent e) {
		for (MouseMotionListener l : _motionListeners) {
			l.mouseMoved(e);
		}
	}
}
