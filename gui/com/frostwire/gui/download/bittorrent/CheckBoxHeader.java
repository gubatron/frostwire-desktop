package com.frostwire.gui.download.bittorrent;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1291080480398111225L;

    protected CheckBoxHeader _rendererComponent;
    protected int _column;
    protected boolean _mousePressed = false;
    
    private static String _text;

    public CheckBoxHeader(String text, boolean selected, ItemListener itemListener) {
        _text = text;
        setSelected(selected);
        _rendererComponent = this;
        _rendererComponent.addItemListener(itemListener);
        setHorizontalAlignment(JLabel.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                _rendererComponent.setForeground(header.getForeground());
                _rendererComponent.setBackground(header.getBackground());
                _rendererComponent.setFont(header.getFont());
                header.addMouseListener(_rendererComponent);
            }
        }
        setColumn(column);
        _rendererComponent.setText(_text);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return _rendererComponent;
    }

    protected void setColumn(int column) {
        this._column = column;
    }

    public int getColumn() {
        return _column;
    }

    protected void handleClickEvent(MouseEvent e) {
        if (_mousePressed) {
            _mousePressed = false;
            JTableHeader header = (JTableHeader) (e.getSource());
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);

            if (viewColumn == this._column && e.getClickCount() == 1 && column != -1) {
                doClick();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        handleClickEvent(e);
        ((JTableHeader) e.getSource()).repaint();
    }

    public void mousePressed(MouseEvent e) {
        _mousePressed = true;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    public void setSelected(boolean selected, boolean triggerEvent) {
        if (!triggerEvent) {
            ItemListener[] listeners = getItemListeners();
            
            for (int i = 0; i < listeners.length; i++) {
                removeItemListener(listeners[i]);
            }
            setSelected(selected);
            
            for (int i = 0; i < listeners.length; i++) {
                addItemListener(listeners[i]);
            }
            invalidate();
        } else {
            setSelected(selected);
        }
    }
}
