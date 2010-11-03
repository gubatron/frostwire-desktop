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

public class RedispatchMouseListener implements MouseListener, MouseMotionListener {
    private JList list;
    private MouseEvent mousePressedEvent;
    private MouseListener[] mouseListeners;
    private MouseMotionListener[] motionListeners;
    public RedispatchMouseListener(JList list) {
        this.list = list;
        mouseListeners = list.getMouseListeners();
        motionListeners = list.getMouseMotionListeners();
        for (MouseListener l : mouseListeners)
            list.removeMouseListener(l);
        for (MouseMotionListener l : motionListeners)
            list.removeMouseMotionListener(l);
        list.addMouseMotionListener(this);
    }
    
    public Component getComponentAt(MouseEvent e) {
        int index = list.locationToIndex(e.getPoint());
        int x = e.getX() - list.indexToLocation(index).x;
        int y = e.getY() - list.indexToLocation(index).y;
        JPanel renderer = (JPanel) list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false);
        return renderer.getComponentAt(x, y);            
    }
    
    public Rectangle getRepaintBounds(MouseEvent e) {
        int index = list.locationToIndex(e.getPoint());
        Point p = list.indexToLocation(index);
        JPanel renderer = (JPanel) list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false);
        return new Rectangle(p.x, p.y, renderer.getPreferredSize().width, renderer.getPreferredSize().height);
    }
    
    public void mouseClicked(MouseEvent e) {           
        Component c = getComponentAt(e);
        if (c instanceof JButton ||
        	c instanceof JLabel) {
            c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
            list.repaint(getRepaintBounds(e));
        }
        else
            for (MouseListener l : mouseListeners)
                l.mouseClicked(e);
    }
    public void mousePressed(MouseEvent e) {
        Component c = getComponentAt(e);
        mousePressedEvent = e;
        if (c instanceof JButton) {
            c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
            list.repaint(getRepaintBounds(e));
        }
        else 
            for (MouseListener l : mouseListeners)
                l.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
        if (mousePressedEvent != null) {
            Component c = getComponentAt(mousePressedEvent);
            if (c instanceof JButton) {
                c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_RELEASED, e.getWhen(), e.getModifiers(), 0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                list.repaint(getRepaintBounds(mousePressedEvent));
            }
        }
        else
            for (MouseListener l : mouseListeners)
                l.mouseReleased(e);
    }
    public void mouseEntered(MouseEvent e) {
        for (MouseListener l : mouseListeners)
            l.mouseEntered(e);
    }
    public void mouseExited(MouseEvent e) {
        for (MouseListener l : mouseListeners)
            l.mouseExited(e);
    }

    public void mouseDragged(MouseEvent e) {
        if (mousePressedEvent != null) {
            Component c = getComponentAt(mousePressedEvent);
            if (c instanceof JButton)
                return;
        }
        for (MouseMotionListener l : motionListeners)
            l.mouseDragged(e);            
    }

    public void mouseMoved(MouseEvent e) {
        for (MouseMotionListener l : motionListeners)
            l.mouseMoved(e);            
    }
}
