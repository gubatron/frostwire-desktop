package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.ThemeMediator;

public class SourceRenderer extends JPanel implements TableCellRenderer {

    private SourceHolder sourceHolder;
    private JLabel sourceIcon;
    private JLabel sourceLabel;
    
    public SourceRenderer() {
        initUI();
    }
    
    private void initUI() {
        //setLayout(new MigLayout("debug, fillx, insets 0 0 0 0, gapx 4px"));
        sourceIcon = new JLabel();
        sourceLabel = new JLabel();
        add(sourceIcon);//,"w 16px!, h 16px!");
        add(sourceLabel);//, "growx");
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columns) {
        this.updateUI((SourceHolder) value, table, row);
        this.setOpaque(true);
        this.setEnabled(table.isEnabled());

        if (isSelected) {
            this.setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            this.setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        return this;
    }

    private void updateUI(SourceHolder value, JTable table, int row) {
        this.sourceHolder = value;
        updateIcon();
        updateLinkLabel(table);
    }
    
    private void syncFont(JTable table, JComponent c) {
        Font tableFont = table.getFont();
        if (tableFont != null && !tableFont.equals(c.getFont())) {
            c.setFont(tableFont);
        }
    }

    private void updateLinkLabel(JTable table) {
        if (getSourceHolder() != null) {
            sourceLabel.setText(getSourceHolder().getSourceNameHTML());
            syncFont(table, sourceLabel);
        }
    }

    private void updateIcon() {
        if (getSourceHolder() != null) {
            System.out.println("SourceRenderer.updateIcon: how can we get the icon now? " + getSourceHolder().getSourceName());
        }
    }

    /**
     * This will act more like a 'setMouseListener' that will put the given MouseListener implementation
     * to this component, and every subcomponent. It'll remove previous listeners if they exist to avoid leaks.
     */
    @Override
    public synchronized void addMouseListener(MouseListener l) {
        removeAllMouseListeners(this);
        removeAllMouseListeners(sourceIcon);
        removeAllMouseListeners(sourceLabel);
        
        super.addMouseListener(l);
        sourceIcon.addMouseListener(l);
        sourceLabel.addMouseListener(l);
    }
    
    private void removeAllMouseListeners(Component c) {
        MouseListener[] mouseListeners = c.getMouseListeners();
        if (mouseListeners != null && mouseListeners.length > 0) {
            for (MouseListener ml : mouseListeners) {
                c.removeMouseListener(ml);
            }
        }
    }

    private SourceHolder getSourceHolder() {
        return this.sourceHolder;
    }
}
