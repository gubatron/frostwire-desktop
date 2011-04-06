package com.limegroup.gnutella.gui.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.StateContributionInfo;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI.TableCellId;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceStripingUtils;
import org.pushingpixels.substance.internal.utils.UpdateOptimizationInfo;
import org.pushingpixels.substance.internal.utils.border.SubstanceTableCellBorder;

import com.limegroup.gnutella.gui.LimeJProgressBar;
import com.limegroup.gnutella.gui.themes.PlasticThemeSettings;

/**
 * This class handles rendering a <tt>JProgressBar</tt> for improved
 * performance in tables.
 */
public class ProgressBarRenderer extends LimeJProgressBar implements TableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 4078901049982402262L;

    private Border _selectedBorder;
    private Border _unselectedBorder;
    private Map<Color, Border> borders = new HashMap<Color, Border>();

    /**
     * Sets the font, border, and colors for the progress bar.
     *
     * @param table the <tt>JTable</tt> instance used to obtain the colors
     * to use for rendering
     */
    public ProgressBarRenderer() {
        setStringPainted(true);

        Font font = getFont();
        Font newFont;

        if (font == null || font.getName() == null) {
            newFont = new Font("Dialog", Font.BOLD, 9);
        } else {
            newFont = new Font(font.getName(), Font.BOLD, 9);
        }

        setFont(newFont);
    }

    /**
     * Overrides <tt>JComponent.setForeground</tt> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    public void setBackground(Color c) {
        super.setBackground(c);
        _unselectedBorder = getCachedOrNewBorder(c);
        if (_unselectedBorder != null)
            setBorder(_unselectedBorder);
    }

    /**
     * Gets a new or old border for this color.
     */
    public Border getCachedOrNewBorder(Color c) {
        if (c == null)
            return null;
        if (borders == null)
            return null;

        Border b = borders.get(c);
        if (b == null) {
            b = BorderFactory.createMatteBorder(2, 5, 2, 5, c);
            borders.put(c, b);
        }
        return b;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) {
        if (SubstanceLookAndFeel.isCurrentLookAndFeel()) {
            return getTableCellRendererComponentWithSubstance(table, value, isSel, hasFocus, row, column);
        } else {

            setValue(Math.min(100, getBarStatus(value)));
            setString(getDescription(value));

            Color uc = getBackgroundForRow(table, row);
            if (_selectedBorder == null && _unselectedBorder == null) {
                Color sc = table.getSelectionBackground();
                _selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, sc);
                _unselectedBorder = getCachedOrNewBorder(uc);
            }
            
            _unselectedBorder = getCachedOrNewBorder(uc);

            if (isSel) {
                setBorder(_selectedBorder);
                setBackground(table.getSelectionBackground());
            } else {
                setBorder(_unselectedBorder);
            }
            return this;
        }
    }

    /**
     * @param value the same value that initializes the cell
     * @return the String that should be displayed
     */
    protected String getDescription(Object value) {
        return Integer.toString(getBarStatus(value)) + " %";
    }

    /**
     * @param value the same value that initializes the cell
     * @return what the progress bar component should be set to
     */
    protected int getBarStatus(Object value) {
        return value == null ? 0 : ((Integer) value).intValue();
    }

    /*
     * The following methods are overridden as a performance measure to 
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and 
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }
        JComponent jp = (JComponent) p;
        // p should now be the JTable. 
        boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && jp.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text") {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }
    
    public Component getTableCellRendererComponentWithSubstance(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        TableUI tableUI = table.getUI();
        SubstanceTableUI ui = (SubstanceTableUI) tableUI;

        // Recompute the focus indication to prevent flicker - JTable
        // registers a listener on selection changes and repaints the
        // relevant cell before our listener (in TableUI) gets the
        // chance to start the fade sequence. The result is that the
        // first frame uses full opacity, and the next frame starts the
        // fade sequence. So, we use the UI delegate to compute the
        // focus indication.
        hasFocus = ui.isFocusedCell(row, column);

        TableCellId cellId = new TableCellId(row, column);

        StateTransitionTracker.ModelStateInfo modelStateInfo = ui
                .getModelStateInfo(cellId);
        ComponentState currState = ui.getCellState(cellId);
        // special case for drop location
        JTable.DropLocation dropLocation = table.getDropLocation();
        boolean isDropLocation = (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row && dropLocation.getColumn() == column);

        if (!isDropLocation && (modelStateInfo != null)) {
            if (ui.hasRolloverAnimations() || ui.hasSelectionAnimations()) {
                Map<ComponentState, StateContributionInfo> activeStates = modelStateInfo
                        .getStateContributionMap();
                SubstanceColorScheme colorScheme = getColorSchemeForState(
                        table, ui, currState);
                if (currState.isDisabled() || (activeStates == null)
                        || (activeStates.size() == 1)) {
                    super.setForeground(new ColorUIResource(colorScheme
                            .getForegroundColor()));
                } else {
                    float aggrRed = 0;
                    float aggrGreen = 0;
                    float aggrBlue = 0;
                    for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : modelStateInfo
                            .getStateContributionMap().entrySet()) {
                        ComponentState activeState = activeEntry.getKey();
                        SubstanceColorScheme scheme = getColorSchemeForState(
                                table, ui, activeState);
                        Color schemeFg = scheme.getForegroundColor();
                        float contribution = activeEntry.getValue()
                                .getContribution();
                        aggrRed += schemeFg.getRed() * contribution;
                        aggrGreen += schemeFg.getGreen() * contribution;
                        aggrBlue += schemeFg.getBlue() * contribution;
                    }
                    super.setForeground(new ColorUIResource(new Color(
                            (int) aggrRed, (int) aggrGreen, (int) aggrBlue)));
                }
            } else {
                SubstanceColorScheme scheme = getColorSchemeForState(table, ui,
                        currState);
                super.setForeground(new ColorUIResource(scheme
                        .getForegroundColor()));
            }
        } else {
            SubstanceColorScheme scheme = getColorSchemeForState(table, ui,
                    currState);
            if (isDropLocation) {
                scheme = SubstanceColorSchemeUtilities.getColorScheme(table,
                        ColorSchemeAssociationKind.TEXT_HIGHLIGHT, currState);
            }
            super
                    .setForeground(new ColorUIResource(scheme
                            .getForegroundColor()));
        }

        SubstanceStripingUtils.applyStripedBackground(table, row, this);

        this.setFont(table.getFont());

        TableCellId cellFocusId = new TableCellId(row, column);

        StateTransitionTracker focusStateTransitionTracker = ui
                .getStateTransitionTracker(cellFocusId);

        Insets regInsets = ui.getCellRendererInsets();
        if (hasFocus || (focusStateTransitionTracker != null)) {
            SubstanceTableCellBorder border = new SubstanceTableCellBorder(
                    regInsets, ui, cellFocusId);

            // System.out.println("[" + row + ":" + column + "] hasFocus : "
            // + hasFocus + ", focusState : " + focusState);
            if (focusStateTransitionTracker != null) {
                border.setAlpha(focusStateTransitionTracker
                        .getFocusStrength(hasFocus));
            }

            // special case for tables with no grids
            if (!table.getShowHorizontalLines()
                    && !table.getShowVerticalLines()) {
                this.setBorder(new CompoundBorder(new EmptyBorder(table
                        .getRowMargin() / 2, 0, table.getRowMargin() / 2, 0),
                        border));
            } else {
                this.setBorder(border);
            }
        } else {
            this.setBorder(new EmptyBorder(regInsets.top, regInsets.left,
                    regInsets.bottom, regInsets.right));
        }

        setValue(Math.min(100, getBarStatus(value)));
        setString(getDescription(value));
        this.setOpaque(false);
        this.setEnabled(table.isEnabled());
        return this;
    }
    
    private SubstanceColorScheme getColorSchemeForState(JTable table,
            SubstanceTableUI ui, ComponentState state) {
        UpdateOptimizationInfo updateOptimizationInfo = ui
                .getUpdateOptimizationInfo();
        if (state == ComponentState.ENABLED) {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(table,
                        state);
            } else {
                return updateOptimizationInfo.getDefaultScheme();
            }
        } else {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(table,
                        ColorSchemeAssociationKind.HIGHLIGHT, state);
            } else {
                return updateOptimizationInfo.getHighlightColorScheme(state);
            }
        }
    }
    
    /**
     * Returns the color that a specific row will be.
     * @param table 
     */
    public Color getBackgroundForRow(JTable table, int row) {
        if(row % 2 == 0) {
            return table.getBackground();
        } else {
            return PlasticThemeSettings.TABLE_ALTERNATE_COLOR.getValue();
        }
    }
}
