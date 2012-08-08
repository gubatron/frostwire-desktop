/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.themes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.TableCellRenderer;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceConstants.Side;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.painter.HighlightPainterUtils;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;
import org.pushingpixels.substance.internal.utils.UpdateOptimizationInfo;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SkinTableUI extends SubstanceTableUI {
    
    private UpdateOptimizationInfo updateInfo;
   
    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createTableUI(comp);
    }
    
    protected void paintCell(Graphics g, Rectangle cellRect,
            Rectangle highlightCellRect, int row, int column) {
        
        this.updateInfo = getUpdateOptimizationInfo();
        
        // System.out.println("Painting " + row + ":" + column);
        Component rendererComponent = null;
        if (!this.table.isEditing() || this.table.getEditingRow() != row
                || this.table.getEditingColumn() != column) {
            TableCellRenderer renderer = this.table
                    .getCellRenderer(row, column);
            boolean isSubstanceRenderer = isSubstanceDefaultRenderer(renderer);
            rendererComponent = this.table.prepareRenderer(renderer, row,
                    column);
            boolean isSubstanceRendererComponent = isSubstanceDefaultRenderer(rendererComponent);
            if (isSubstanceRenderer && !isSubstanceRendererComponent) {
                throw new IllegalArgumentException(
                        "Renderer extends the SubstanceDefaultTableCellRenderer but does not return one in its getTableCellRendererComponent() method");
            }

            if (!isSubstanceRenderer) {
                rendererPane.paintComponent(g, rendererComponent, table,
                        cellRect.x, cellRect.y, cellRect.width,
                        cellRect.height, true);
                return;
            }
        }

        Graphics2D g2d = (Graphics2D) g.create();
        // fix for issue 183 - passing the original Graphics context
        // to compute the alpha composite. If the table is in a JXPanel
        // (component from SwingX) and it has custom alpha value set,
        // then the original graphics context will have a SRC_OVER
        // alpha composite applied to it.
        g2d.setComposite(LafWidgetUtilities.getAlphaComposite(this.table, g));

        TableCellId cellId = new TableCellId(row, column);

        StateTransitionTracker.ModelStateInfo modelStateInfo = this
                .getModelStateInfo(cellId);
        Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = ((modelStateInfo == null) ? null
                : modelStateInfo.getStateContributionMap());
        // optimize for tables that don't initiate rollover
        // or selection animations
//        if (!updateInfo.hasRolloverAnimations
//                && !updateInfo.hasSelectionAnimations)
//            activeStates = null;
        ComponentState currState = ((modelStateInfo == null) ? this
                .getCellState(cellId) : modelStateInfo.getCurrModelState());

        boolean hasHighlights = (currState != ComponentState.ENABLED)
                || (activeStates != null);
        if (activeStates != null) {
            for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> stateEntry : activeStates
                    .entrySet()) {
                hasHighlights = (this.updateInfo.getHighlightAlpha(stateEntry
                        .getKey()) * stateEntry.getValue().getContribution() > 0.0f);
                if (hasHighlights)
                    break;
            }
        } else {
            hasHighlights = (this.updateInfo.getHighlightAlpha(currState) > 0.0f);
        }

        Set<SubstanceConstants.Side> highlightOpenSides = null;
        float highlightBorderAlpha = 0.0f;

        if (hasHighlights) {
            // compute the highlight visuals, but only if there are
            // highlights on this cell (optimization)
            highlightOpenSides = EnumSet.noneOf(Side.class);
            // show highlight border only when the table grid is not shown
            highlightBorderAlpha = (table.getShowHorizontalLines() || table
                    .getShowVerticalLines()) ? 0.0f : 0.8f;
            if (!table.getColumnSelectionAllowed()
                    && table.getRowSelectionAllowed()) {
                // if row selection is on and column selection is off, we
                // will show the highlight for the entire row

                // all cells have open left side
                highlightOpenSides.add(SubstanceConstants.Side.LEFT);
                // all cells have open right side
                highlightOpenSides.add(SubstanceConstants.Side.RIGHT);
            }
            if (table.getColumnSelectionAllowed()
                    && !table.getRowSelectionAllowed()) {
                // if row selection is off and column selection is on, we
                // will show the highlight for the entire column

                // the top side is open for all rows except the
                // first, or when the table header is visible
                highlightOpenSides.add(SubstanceConstants.Side.TOP);
                // all cells but the last have open bottom side
                highlightOpenSides.add(SubstanceConstants.Side.BOTTOM);
            }
            if (row > 1) {
                ComponentState upperNeighbourState = this
                        .getCellState(new TableCellId(row - 1, column));
                if (currState == upperNeighbourState) {
                    // the cell above it is in the same state
                    highlightOpenSides.add(SubstanceConstants.Side.TOP);
                }
            }
            if (column > 1) {
                ComponentState leftNeighbourState = this
                        .getCellState(new TableCellId(row, column - 1));
                if (currState == leftNeighbourState) {
                    // the cell to the left is in the same state
                    highlightOpenSides.add(SubstanceConstants.Side.LEFT);
                }
            }
            if (row == 0) {
                highlightOpenSides.add(SubstanceConstants.Side.TOP);
            }
            if (row == (table.getRowCount() - 1)) {
                highlightOpenSides.add(SubstanceConstants.Side.BOTTOM);
            }
            if (column == 0) {
                highlightOpenSides.add(SubstanceConstants.Side.LEFT);
            }
            if (column == (table.getColumnCount() - 1)) {
                highlightOpenSides.add(SubstanceConstants.Side.RIGHT);
            }
        }

        boolean isRollover = this.rolledOverIndices.contains(cellId);
        if (this.table.isEditing() && this.table.getEditingRow() == row
                && this.table.getEditingColumn() == column) {
            Component component = this.table.getEditorComponent();
            component.applyComponentOrientation(this.table
                    .getComponentOrientation());

            if (hasHighlights) {
                float extra = SubstanceSizeUtils
                        .getBorderStrokeWidth(SubstanceSizeUtils
                                .getComponentFontSize(this.table
                                        .getTableHeader()));
                float extraWidth = highlightOpenSides
                        .contains(SubstanceConstants.Side.LEFT) ? 0.0f : extra;
                float extraHeight = highlightOpenSides
                        .contains(SubstanceConstants.Side.TOP) ? 0.0f : extra;
                Rectangle highlightRect = new Rectangle(highlightCellRect.x
                        - (int) extraWidth, highlightCellRect.y
                        - (int) extraHeight, highlightCellRect.width
                        + (int) extraWidth, highlightCellRect.height
                        + (int) extraHeight);
                if (activeStates == null) {
                    float alpha = this.updateInfo.getHighlightAlpha(currState);
                    if (alpha > 0.0f) {
                        SubstanceColorScheme fillScheme = this.updateInfo
                                .getHighlightColorScheme(currState);
                        SubstanceColorScheme borderScheme = this.updateInfo
                                .getHighlightBorderColorScheme(currState);
                        g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
                                this.table, alpha, g));
                        HighlightPainterUtils.paintHighlight(g2d,
                                this.rendererPane, component, highlightRect,
                                highlightBorderAlpha, highlightOpenSides,
                                fillScheme, borderScheme);
                        g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
                                this.table, g));
                    }
                } else {
                    for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> stateEntry : activeStates
                            .entrySet()) {
                        ComponentState activeState = stateEntry.getKey();
                        float alpha = this.updateInfo
                                .getHighlightAlpha(activeState)
                                * stateEntry.getValue().getContribution();
                        if (alpha == 0.0f)
                            continue;
                        SubstanceColorScheme fillScheme = this.updateInfo
                                .getHighlightColorScheme(activeState);
                        SubstanceColorScheme borderScheme = this.updateInfo
                                .getHighlightBorderColorScheme(activeState);
                        g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
                                this.table, alpha, g));
                        HighlightPainterUtils.paintHighlight(g2d,
                                this.rendererPane, component, highlightRect,
                                highlightBorderAlpha, highlightOpenSides,
                                fillScheme, borderScheme);
                        g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
                                this.table, g));
                    }
                }
            }

            component.setBounds(cellRect);
            component.validate();
        } else {
            boolean isWatermarkBleed = this.updateInfo.toDrawWatermark;
            if (rendererComponent != null) {
                if (!isWatermarkBleed) {
                    Color background = rendererComponent.getBackground();
                    // optimization - only render background if it's different
                    // from the table background
                    if ((background != null)
                            && (!table.getBackground().equals(background) || this.updateInfo.isInDecorationArea)) {
                        // fill with the renderer background color
                        g2d.setColor(background);
                        g2d.fillRect(highlightCellRect.x, highlightCellRect.y,
                                highlightCellRect.width,
                                highlightCellRect.height);
                    }
                } else {
                    BackgroundPaintingUtils.fillAndWatermark(g2d, this.table,
                            rendererComponent.getBackground(),
                            highlightCellRect);
                }
            }

            if (hasHighlights) {
                JTable.DropLocation dropLocation = table.getDropLocation();
                if (dropLocation != null && !dropLocation.isInsertRow()
                        && !dropLocation.isInsertColumn()
                        && dropLocation.getRow() == row
                        && dropLocation.getColumn() == column) {
                    // mark drop location
                    SubstanceColorScheme scheme = SubstanceColorSchemeUtilities
                            .getColorScheme(table,
                                    ColorSchemeAssociationKind.TEXT_HIGHLIGHT,
                                    currState);
                    SubstanceColorScheme borderScheme = SubstanceColorSchemeUtilities
                            .getColorScheme(table,
                                    ColorSchemeAssociationKind.BORDER,
                                    currState);
                    float extra = SubstanceSizeUtils
                            .getBorderStrokeWidth(SubstanceSizeUtils
                                    .getComponentFontSize(this.table
                                            .getTableHeader()));
                    HighlightPainterUtils.paintHighlight(g2d,
                            this.rendererPane, rendererComponent,
                            new Rectangle(highlightCellRect.x - (int) extra,
                                    highlightCellRect.y - (int) extra,
                                    highlightCellRect.width + (int) extra,
                                    highlightCellRect.height + (int) extra),
                            0.8f, null, scheme, borderScheme);
                } else {
                    float extra = SubstanceSizeUtils
                            .getBorderStrokeWidth(SubstanceSizeUtils
                                    .getComponentFontSize(this.table
                                            .getTableHeader()));
                    float extraWidth = highlightOpenSides
                            .contains(SubstanceConstants.Side.LEFT) ? 0.0f
                            : extra;
                    float extraHeight = highlightOpenSides
                            .contains(SubstanceConstants.Side.TOP) ? 0.0f
                            : extra;
                    Rectangle highlightRect = new Rectangle(highlightCellRect.x
                            - (int) extraWidth, highlightCellRect.y
                            - (int) extraHeight, highlightCellRect.width
                            + (int) extraWidth, highlightCellRect.height
                            + (int) extraHeight);
                    if (activeStates == null) {
                        SubstanceColorScheme fillScheme = this.updateInfo
                                .getHighlightColorScheme(currState);
                        SubstanceColorScheme borderScheme = this.updateInfo
                                .getHighlightBorderColorScheme(currState);
                        float alpha = this.updateInfo
                                .getHighlightAlpha(currState);
                        if (alpha > 0.0f) {
                            g2d.setComposite(LafWidgetUtilities
                                    .getAlphaComposite(this.table, alpha, g));
                            HighlightPainterUtils.paintHighlight(g2d,
                                    this.rendererPane, rendererComponent,
                                    highlightRect, highlightBorderAlpha,
                                    highlightOpenSides, fillScheme,
                                    borderScheme);
                            g2d.setComposite(LafWidgetUtilities
                                    .getAlphaComposite(this.table, g));
                        }
                    } else {
                        for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> stateEntry : activeStates
                                .entrySet()) {
                            ComponentState activeState = stateEntry.getKey();
                            SubstanceColorScheme fillScheme = this.updateInfo
                                    .getHighlightColorScheme(activeState);
                            SubstanceColorScheme borderScheme = this.updateInfo
                                    .getHighlightBorderColorScheme(activeState);
                            float alpha = this.updateInfo
                                    .getHighlightAlpha(activeState)
                                    * stateEntry.getValue().getContribution();
                            if (alpha > 0.0f) {
                                g2d.setComposite(LafWidgetUtilities
                                        .getAlphaComposite(this.table, alpha, g));
                                HighlightPainterUtils.paintHighlight(g2d,
                                        this.rendererPane, rendererComponent,
                                        highlightRect, highlightBorderAlpha,
                                        highlightOpenSides, fillScheme,
                                        borderScheme);
                                g2d.setComposite(LafWidgetUtilities
                                        .getAlphaComposite(this.table, g));
                            }
                        }
                    }
                }
            }

            rendererComponent.applyComponentOrientation(this.table
                    .getComponentOrientation());
            if (rendererComponent instanceof JComponent) {
                // Play with opacity to make our own gradient background
                // on selected elements to show.
                JComponent jRenderer = (JComponent) rendererComponent;
                // Compute the selection status to prevent flicker - JTable
                // registers a listener on selection changes and repaints
                // the relevant cell before our listener (in TableUI) gets
                // the chance to start the fade sequence. The result is that
                // the first frame uses full opacity, and the next frame
                // starts the fade sequence. So, we use the UI delegate to
                // compute the selection status.
                boolean isSelected = this.selectedIndices
                        .containsKey(cellId);
                boolean newOpaque = !(isSelected || isRollover || hasHighlights);

                if (this.updateInfo.toDrawWatermark)
                    newOpaque = false;

                Map<Component, Boolean> opacity = new HashMap<Component, Boolean>();
                if (!newOpaque)
                    SubstanceCoreUtilities.makeNonOpaque(jRenderer, opacity);
                this.rendererPane.paintComponent(g2d, rendererComponent,
                        this.table, cellRect.x, cellRect.y, cellRect.width,
                        cellRect.height, true);
                if (!newOpaque)
                    SubstanceCoreUtilities.restoreOpaque(jRenderer, opacity);
            } else {
                this.rendererPane.paintComponent(g2d, rendererComponent,
                        this.table, cellRect.x, cellRect.y, cellRect.width,
                        cellRect.height, true);
            }
        }
        g2d.dispose();
    }
    
    private boolean isSubstanceDefaultRenderer(Object instance) {
        return (instance instanceof SubstanceDefaultTableCellRenderer)
                || (instance instanceof SubstanceDefaultTableCellRenderer.BooleanRenderer) || (instance instanceof SkinTableCellRenderer);
    }
}
