package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.GUIMediator;

public class SourceRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private SourceHolder sourceHolder;
    private final Map<String,ImageIcon> sourceIcons;
    
    public SourceRenderer() {
        super();
        sourceIcons = new HashMap<>();
        initIcons();
        initUI();
        initMouseListener();
    }
    
    private void initIcons() {
        sourceIcons.put("soundcloud", GUIMediator.getThemeImage("soundcloud_off"));
        sourceIcons.put("youtube", GUIMediator.getThemeImage("youtube_on"));
        sourceIcons.put("archive.org", GUIMediator.getThemeImage("archive_source"));
        sourceIcons.put("isohunt", GUIMediator.getThemeImage("isohunt_source"));
        sourceIcons.put("clearbits", GUIMediator.getThemeImage("clearbits_source"));
        sourceIcons.put("extratorrent", GUIMediator.getThemeImage("extratorrent_source"));
        sourceIcons.put("kat", GUIMediator.getThemeImage("kat_source"));
        sourceIcons.put("mininova", GUIMediator.getThemeImage("mininova_source"));
        sourceIcons.put("tpb", GUIMediator.getThemeImage("tpb_source"));
        sourceIcons.put("vertor", GUIMediator.getThemeImage("vertor_source"));
        sourceIcons.put("default", GUIMediator.getThemeImage("seeding_small"));
    }

    private void initUI() {
        final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        setCursor(handCursor);
    }

    private void initMouseListener() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSourceHolder() != null) {
                    getSourceHolder().getUISearchResult().showDetails(true);
                    e.consume();
                    UXStats.instance().log(UXAction.SEARCH_RESULT_SOURCE_VIEW);
                }
            }
        };
        
        addMouseListener(mouseAdapter);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columns) {
        this.setOpaque(true);
        this.setEnabled(true);
        if (isSelected) {
            this.setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            this.setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }
        this.updateUI((SourceHolder) value, table, row);
        
        return super.getTableCellRendererComponent(table, getText(), isSelected, hasFocus, row, columns);
    }
    
    private void updateUI(SourceHolder value, JTable table, int row) {
        this.sourceHolder = value;
        updateIcon();
        updateLinkLabel(table);
    }

    private void updateIcon() {
        if (getSourceHolder() != null) {
            String sourceName = getSourceHolder().getSourceName().toLowerCase();
            if (sourceName.contains("-")) {
                sourceName = sourceName.substring(0, sourceName.indexOf("-")).trim();
            }
            
            ImageIcon icon = sourceIcons.get(sourceName);
            if (icon != null) {
                setIcon(icon);
            } else {
                System.out.println("no icon for " + sourceName);
                setIcon(sourceIcons.get("default"));
            }
        }
    }
    
    private void updateLinkLabel(JTable table) {
        if (getSourceHolder() != null) {
            setText(getSourceHolder().getSourceNameHTML());
            syncFont(table, this);
        }
    }
    
    private void syncFont(JTable table, JComponent c) {
        Font tableFont = table.getFont();
        if (tableFont != null && !tableFont.equals(c.getFont())) {
            c.setFont(tableFont);
        }
    }
    
    private SourceHolder getSourceHolder() {
        return this.sourceHolder;
    }
}