/*
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

package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.synth.SynthSliderUI;

/**
 * Initial implementation from: https://github.com/ernieyu/Swing-range-slider
 */
final class RangeSliderUI extends SynthSliderUI {

    protected Rectangle upperThumbRect;
    protected boolean upperThumbSelected;

    private transient boolean lowerDragging;
    private transient boolean upperDragging;

    protected RangeSlider slider;
    protected int lastUpperValue;

    public RangeSliderUI(RangeSlider b) {
        super(b);
    }

    @Override
    public void installUI(JComponent c) {
        slider = (RangeSlider) c;
        upperThumbRect = new Rectangle();
        lastUpperValue = slider.getUpperValue();
        super.installUI(c);
    }

    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    @Override
    protected ChangeListener createChangeListener(JSlider slider) {
        return new ChangeHandler();
    }

    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        upperThumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /**
     * Updates the locations for both thumbs.
     */
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();

        if (slider.getSnapToTicks()) {
            int upperValue = slider.getValue() + slider.getExtent();
            int snappedValue = upperValue;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            if (tickSpacing != 0) {
                // If it's not on a tick, change the value
                if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (upperValue - slider.getMinimum()) / (float) tickSpacing;
                    int whichTick = Math.round(temp);

                    // This is the fix for the bug #6401380
                    if (temp - (int) temp == .5 && upperValue < lastUpperValue) {
                        whichTick--;
                    }
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != upperValue) {
                    slider.setExtent(snappedValue - slider.getValue());
                }
            }
        }

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int upperPosition = xPositionForValue(slider.getValue() + slider.getExtent());

            upperThumbRect.x = upperPosition - (upperThumbRect.width / 2);
            upperThumbRect.y = trackRect.y;
        } else {
            int upperPosition = yPositionForValue(slider.getValue() + slider.getExtent());

            upperThumbRect.x = trackRect.x;
            upperThumbRect.y = upperPosition - (upperThumbRect.height / 2);
        }
    }

    /**
     * Paints the slider.  The selected thumb is always painted on top of the
     * other thumb.
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        Rectangle clipRect = g.getClipBounds();
        if (upperThumbSelected) {
            if (clipRect.intersects(thumbRect)) {
                paintLowerThumb(g);
            }
            if (clipRect.intersects(upperThumbRect)) {
                paintUpperThumb(g);
            }

        } else {
            if (clipRect.intersects(upperThumbRect)) {
                paintUpperThumb(g);
            }
            if (clipRect.intersects(thumbRect)) {
                paintLowerThumb(g);
            }
        }
    }

    @Override
    public void paintThumb(Graphics g) {
    }

    public void paintThumb(Graphics g, Rectangle thumbRect) {
        Rectangle knobBounds = thumbRect;
        int w = knobBounds.width;
        int h = knobBounds.height;

        g.translate(knobBounds.x, knobBounds.y);

        if (slider.isEnabled()) {
            g.setColor(slider.getBackground());
        } else {
            g.setColor(slider.getBackground().darker());
        }

        Boolean paintThumbArrowShape = (Boolean) slider.getClientProperty("Slider.paintThumbArrowShape");

        if ((!slider.getPaintTicks() && paintThumbArrowShape == null) || paintThumbArrowShape == Boolean.FALSE) {

            // "plain" version
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawLine(0, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 0, w - 1, h - 1);

            g.setColor(getHighlightColor());
            g.drawLine(0, 0, 0, h - 2);
            g.drawLine(1, 0, w - 2, 0);

            g.setColor(getShadowColor());
            g.drawLine(1, h - 2, w - 2, h - 2);
            g.drawLine(w - 2, 1, w - 2, h - 3);
        } else if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int cw = w / 2;
            g.fillRect(1, 1, w - 3, h - 1 - cw);
            Polygon p = new Polygon();
            p.addPoint(1, h - cw);
            p.addPoint(cw - 1, h - 1);
            p.addPoint(w - 2, h - 1 - cw);
            g.fillPolygon(p);

            g.setColor(getHighlightColor());
            g.drawLine(0, 0, w - 2, 0);
            g.drawLine(0, 1, 0, h - 1 - cw);
            g.drawLine(0, h - cw, cw - 1, h - 1);

            g.setColor(Color.black);
            g.drawLine(w - 1, 0, w - 1, h - 2 - cw);
            g.drawLine(w - 1, h - 1 - cw, w - 1 - cw, h - 1);

            g.setColor(getShadowColor());
            g.drawLine(w - 2, 1, w - 2, h - 2 - cw);
            g.drawLine(w - 2, h - 1 - cw, w - 1 - cw, h - 2);
        } else { // vertical
            int cw = h / 2;
            if (slider.getComponentOrientation().isLeftToRight()) {
                g.fillRect(1, 1, w - 1 - cw, h - 3);
                Polygon p = new Polygon();
                p.addPoint(w - cw - 1, 0);
                p.addPoint(w - 1, cw);
                p.addPoint(w - 1 - cw, h - 2);
                g.fillPolygon(p);

                g.setColor(getHighlightColor());
                g.drawLine(0, 0, 0, h - 2); // left
                g.drawLine(1, 0, w - 1 - cw, 0); // top
                g.drawLine(w - cw - 1, 0, w - 1, cw); // top slant

                g.setColor(Color.black);
                g.drawLine(0, h - 1, w - 2 - cw, h - 1); // bottom
                g.drawLine(w - 1 - cw, h - 1, w - 1, h - 1 - cw); // bottom slant

                g.setColor(getShadowColor());
                g.drawLine(1, h - 2, w - 2 - cw, h - 2); // bottom
                g.drawLine(w - 1 - cw, h - 2, w - 2, h - cw - 1); // bottom slant
            } else {
                g.fillRect(5, 1, w - 1 - cw, h - 3);
                Polygon p = new Polygon();
                p.addPoint(cw, 0);
                p.addPoint(0, cw);
                p.addPoint(cw, h - 2);
                g.fillPolygon(p);

                g.setColor(getHighlightColor());
                g.drawLine(cw - 1, 0, w - 2, 0); // top
                g.drawLine(0, cw, cw, 0); // top slant

                g.setColor(Color.black);
                g.drawLine(0, h - 1 - cw, cw, h - 1); // bottom slant
                g.drawLine(cw, h - 1, w - 1, h - 1); // bottom

                g.setColor(getShadowColor());
                g.drawLine(cw, h - 2, w - 2, h - 2); // bottom
                g.drawLine(w - 1, 1, w - 1, h - 2); // right
            }
        }

        g.translate(-knobBounds.x, -knobBounds.y);
    }

    /**
     * Paints the thumb for the lower value using the specified graphics object.
     */
    private void paintLowerThumb(Graphics g) {
        paintThumb(g, thumbRect);
    }

    /**
     * Paints the thumb for the upper value using the specified graphics object.
     */
    private void paintUpperThumb(Graphics g) {
        paintThumb(g, upperThumbRect);
    }

    /** 
     * Sets the location of the upper thumb, and repaints the slider.  This is
     * called when the upper thumb is dragged to repaint the slider.  The
     * <code>setThumbLocation()</code> method performs the same task for the
     * lower thumb.
     */
    private void setUpperThumbLocation(int x, int y) {
        Rectangle upperUnionRect = new Rectangle();
        upperUnionRect.setBounds(upperThumbRect);

        upperThumbRect.setLocation(x, y);

        SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y, upperThumbRect.width, upperThumbRect.height, upperUnionRect);
        slider.repaint(upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height);
    }

    /**
     * Moves the selected thumb in the specified direction by a block increment.
     * This method is called when the user presses the Page Up or Down keys.
     */
    public void scrollByBlock(int direction) {
        synchronized (slider) {
            int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
            if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum()) {
                blockIncrement = 1;
            }
            int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

            if (upperThumbSelected) {
                int oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            } else {
                int oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    /**
     * Moves the selected thumb in the specified direction by a unit increment.
     * This method is called when the user presses one of the arrow keys.
     */
    public void scrollByUnit(int direction) {
        synchronized (slider) {
            int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

            if (upperThumbSelected) {
                int oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            } else {
                int oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    public class ChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent arg0) {
            if (!lowerDragging && !upperDragging) {
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    public class RangeTrackListener extends TrackListener {

        @Override
        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            // Determine which thumb is pressed.  If the upper thumb is 
            // selected (last one dragged), then check its position first;
            // otherwise check the position of the lower thumb first.
            boolean lowerPressed = false;
            boolean upperPressed = false;
            if (upperThumbSelected) {
                if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                } else if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    lowerPressed = true;
                }
            } else {
                if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    lowerPressed = true;
                } else if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                }
            }

            // Handle lower thumb pressed.
            if (lowerPressed) {
                switch (slider.getOrientation()) {
                case JSlider.VERTICAL:
                    offset = currentMouseY - thumbRect.y;
                    break;
                case JSlider.HORIZONTAL:
                    offset = currentMouseX - thumbRect.x;
                    break;
                }
                upperThumbSelected = false;
                lowerDragging = true;
                return;
            }
            lowerDragging = false;

            // Handle upper thumb pressed.
            if (upperPressed) {
                switch (slider.getOrientation()) {
                case JSlider.VERTICAL:
                    offset = currentMouseY - upperThumbRect.y;
                    break;
                case JSlider.HORIZONTAL:
                    offset = currentMouseX - upperThumbRect.x;
                    break;
                }
                upperThumbSelected = true;
                upperDragging = true;
                return;
            }
            upperDragging = false;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lowerDragging = false;
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (lowerDragging) {
                slider.setValueIsAdjusting(true);
                moveLowerThumb();

            } else if (upperDragging) {
                slider.setValueIsAdjusting(true);
                moveUpperThumb();
            }
        }

        @Override
        public boolean shouldScroll(int direction) {
            return false;
        }

        /**
         * Moves the location of the lower thumb, and sets its corresponding 
         * value in the slider.
         */
        private void moveLowerThumb() {
            int thumbMiddle = 0;

            switch (slider.getOrientation()) {
            case JSlider.VERTICAL:
                int halfThumbHeight = thumbRect.height / 2;
                int thumbTop = currentMouseY - offset;
                int trackTop = trackRect.y;
                int trackBottom = trackRect.y + (trackRect.height - 1);
                int vMax = yPositionForValue(slider.getValue() + slider.getExtent());

                // Apply bounds to thumb position.
                if (drawInverted()) {
                    trackBottom = vMax;
                } else {
                    trackTop = vMax;
                }
                thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                setThumbLocation(thumbRect.x, thumbTop);

                // Update slider value.
                thumbMiddle = thumbTop + halfThumbHeight;
                slider.setValue(valueForYPosition(thumbMiddle));
                break;

            case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = currentMouseX - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);
                int hMax = xPositionForValue(slider.getValue() + slider.getExtent());

                // Apply bounds to thumb position.
                if (drawInverted()) {
                    trackLeft = hMax;
                } else {
                    trackRight = hMax;
                }
                thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                setThumbLocation(thumbLeft, thumbRect.y);

                // Update slider value.
                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setValue(valueForXPosition(thumbMiddle));
                break;

            default:
                return;
            }
        }

        /**
         * Moves the location of the upper thumb, and sets its corresponding 
         * value in the slider.
         */
        private void moveUpperThumb() {
            int thumbMiddle = 0;

            switch (slider.getOrientation()) {
            case JSlider.VERTICAL:
                int halfThumbHeight = thumbRect.height / 2;
                int thumbTop = currentMouseY - offset;
                int trackTop = trackRect.y;
                int trackBottom = trackRect.y + (trackRect.height - 1);
                int vMin = yPositionForValue(slider.getValue());

                // Apply bounds to thumb position.
                if (drawInverted()) {
                    trackTop = vMin;
                } else {
                    trackBottom = vMin;
                }
                thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                setUpperThumbLocation(thumbRect.x, thumbTop);

                // Update slider extent.
                thumbMiddle = thumbTop + halfThumbHeight;
                slider.setExtent(valueForYPosition(thumbMiddle) - slider.getValue());
                break;

            case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = currentMouseX - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);
                int hMin = xPositionForValue(slider.getValue());

                // Apply bounds to thumb position.
                if (drawInverted()) {
                    trackRight = hMin;
                } else {
                    trackLeft = hMin;
                }
                thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                setUpperThumbLocation(thumbLeft, thumbRect.y);

                // Update slider extent.
                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setExtent(valueForXPosition(thumbMiddle) - slider.getValue());
                break;

            default:
                return;
            }
        }
    }
}