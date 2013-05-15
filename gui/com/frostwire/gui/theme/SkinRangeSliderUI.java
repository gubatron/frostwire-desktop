package com.frostwire.gui.theme;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.synth.SynthSliderUI;

import com.limegroup.gnutella.gui.search.RangeSlider2;

// adapted from http://www.java2s.com/Code/Java/Swing-Components/ThumbSliderExample2.htm
public class SkinRangeSliderUI extends BasicSliderUI {

    MThumbSliderAdditionalUI additonalUi;

    MouseInputAdapter mThumbTrackListener;

    public static ComponentUI createUI(JComponent c) {
        return new SkinRangeSliderUI((JSlider) c);
    }

    public SkinRangeSliderUI() {
        super(null);
    }

    public SkinRangeSliderUI(JSlider b) {
        super(b);
    }

    public void installUI(JComponent c) {
        additonalUi = new MThumbSliderAdditionalUI(this);
        additonalUi.installUI(c);
        mThumbTrackListener = createMThumbTrackListener((JSlider) c);
        super.installUI(c);
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        additonalUi.uninstallUI(c);
        additonalUi = null;
        mThumbTrackListener = null;
    }

    protected MouseInputAdapter createMThumbTrackListener(JSlider slider) {
        return additonalUi.trackListener;
    }

    protected TrackListener createTrackListener(JSlider slider) {
        return null;
    }

    protected ChangeListener createChangeListener(JSlider slider) {
        return additonalUi.changeHandler;
    }

    protected void installListeners(JSlider slider) {
        slider.addMouseListener(mThumbTrackListener);
        slider.addMouseMotionListener(mThumbTrackListener);
        slider.addFocusListener(focusListener);
        slider.addComponentListener(componentListener);
        slider.addPropertyChangeListener(propertyChangeListener);
        slider.getModel().addChangeListener(changeListener);
    }

    protected void uninstallListeners(JSlider slider) {
        slider.removeMouseListener(mThumbTrackListener);
        slider.removeMouseMotionListener(mThumbTrackListener);
        slider.removeFocusListener(focusListener);
        slider.removeComponentListener(componentListener);
        slider.removePropertyChangeListener(propertyChangeListener);
        slider.getModel().removeChangeListener(changeListener);
    }

    protected void calculateGeometry() {
        super.calculateGeometry();
        additonalUi.calculateThumbsSize();
        additonalUi.calculateThumbsLocation();
    }

    protected void calculateThumbLocation() {
    }

    Rectangle zeroRect = new Rectangle();

    public void paint(Graphics g, JComponent c) {

        Rectangle clip = g.getClipBounds();
        thumbRect = zeroRect;

        super.paint(g, c);

        int thumbNum = additonalUi.getThumbNum();
        Rectangle[] thumbRects = additonalUi.getThumbRects();

        for (int i = thumbNum - 1; 0 <= i; i--) {
            if (clip.intersects(thumbRects[i])) {
                thumbRect = thumbRects[i];

                paintThumb(g);

            }
        }
    }

    public void scrollByBlock(int direction) {
    }

    public void scrollByUnit(int direction) {
    }

    //
    // MThumbSliderAdditional
    //
    public Rectangle getTrackRect() {
        return trackRect;
    }

    public Dimension getThumbSize() {
        return super.getThumbSize();
    }

    public int xPositionForValue(int value) {
        return super.xPositionForValue(value);
    }

    public int yPositionForValue(int value) {
        return super.yPositionForValue(value);
    }

    static class MThumbSliderAdditionalUI {

        RangeSlider2 mSlider;

        SkinRangeSliderUI ui;

        Rectangle[] thumbRects;

        int thumbNum;

        private transient boolean isDragging;

        Icon thumbRenderer;

        Rectangle trackRect;

        ChangeHandler changeHandler;

        TrackListener trackListener;

        public MThumbSliderAdditionalUI(SkinRangeSliderUI ui) {
            this.ui = ui;
        }

        public void installUI(JComponent c) {
            mSlider = (RangeSlider2) c;
            thumbNum = mSlider.getThumbNum();
            thumbRects = new Rectangle[thumbNum];
            for (int i = 0; i < thumbNum; i++) {
                thumbRects[i] = new Rectangle();
            }
            isDragging = false;
            trackListener = new MThumbSliderAdditionalUI.TrackListener(mSlider);
            changeHandler = new ChangeHandler();
        }

        public void uninstallUI(JComponent c) {
            thumbRects = null;
            trackListener = null;
            changeHandler = null;
        }

        protected void calculateThumbsSize() {
            Dimension size = ui.getThumbSize();
            for (int i = 0; i < thumbNum; i++) {
                thumbRects[i].setSize(size.width, size.height);
            }
        }

        protected void calculateThumbsLocation() {
            for (int i = 0; i < thumbNum; i++) {
                if (mSlider.getSnapToTicks()) {
                    int tickSpacing = mSlider.getMinorTickSpacing();
                    if (tickSpacing == 0) {
                        tickSpacing = mSlider.getMajorTickSpacing();
                    }
                    if (tickSpacing != 0) {
                        int sliderValue = mSlider.getValueAt(i);
                        int snappedValue = sliderValue;
                        //int min = mSlider.getMinimumAt(i);
                        int min = mSlider.getMinimum();
                        if ((sliderValue - min) % tickSpacing != 0) {
                            float temp = (float) (sliderValue - min) / (float) tickSpacing;
                            int whichTick = Math.round(temp);
                            snappedValue = min + (whichTick * tickSpacing);
                            mSlider.setValueAt(snappedValue, i);
                        }
                    }
                }
                trackRect = getTrackRect();
                if (mSlider.getOrientation() == JSlider.HORIZONTAL) {
                    int value = mSlider.getValueAt(i);
                    int valuePosition = ui.xPositionForValue(value);
                    thumbRects[i].x = valuePosition - (thumbRects[i].width / 2);
                    thumbRects[i].y = trackRect.y;

                } else {
                    int valuePosition = ui.yPositionForValue(mSlider.getValueAt(i)); // need
                    thumbRects[i].x = trackRect.x;
                    thumbRects[i].y = valuePosition - (thumbRects[i].height / 2);
                }
            }
        }

        public int getThumbNum() {
            return thumbNum;
        }

        public Rectangle[] getThumbRects() {
            return thumbRects;
        }

        private static Rectangle unionRect = new Rectangle();

        public void setThumbLocationAt(int x, int y, int index) {
            Rectangle rect = thumbRects[index];
            unionRect.setBounds(rect);

            rect.setLocation(x, y);
            SwingUtilities.computeUnion(rect.x, rect.y, rect.width, rect.height, unionRect);
            mSlider.repaint(unionRect.x, unionRect.y, unionRect.width, unionRect.height);
        }

        public Rectangle getTrackRect() {
            return ui.getTrackRect();
        }

        public class ChangeHandler implements ChangeListener {
            public void stateChanged(ChangeEvent e) {
                if (!isDragging) {
                    calculateThumbsLocation();
                    mSlider.repaint();
                }
            }
        }

        public class TrackListener extends MouseInputAdapter {
            protected transient int offset;

            protected transient int currentMouseX, currentMouseY;

            protected Rectangle adjustingThumbRect = null;

            protected int adjustingThumbIndex;

            protected RangeSlider2 slider;

            protected Rectangle trackRect;

            TrackListener(RangeSlider2 slider) {
                this.slider = slider;
            }

            public void mousePressed(MouseEvent e) {
                if (!slider.isEnabled()) {
                    return;
                }
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                slider.requestFocus();

                for (int i = 0; i < thumbNum; i++) {
                    Rectangle rect = thumbRects[i];
                    if (rect.contains(currentMouseX, currentMouseY)) {

                        switch (slider.getOrientation()) {
                        case JSlider.VERTICAL:
                            offset = currentMouseY - rect.y;
                            break;
                        case JSlider.HORIZONTAL:
                            offset = currentMouseX - rect.x;
                            break;
                        }
                        isDragging = true;
                        slider.setValueIsAdjusting(true);
                        adjustingThumbRect = rect;
                        adjustingThumbIndex = i;
                        return;
                    }
                }
            }

            public void mouseDragged(MouseEvent e) {
                if (!slider.isEnabled() || !isDragging || !slider.getValueIsAdjusting() || adjustingThumbRect == null) {
                    return;
                }
                int thumbMiddle = 0;
                currentMouseX = e.getX();
                currentMouseY = e.getY();

                Rectangle rect = thumbRects[adjustingThumbIndex];
                trackRect = getTrackRect();
                switch (slider.getOrientation()) {
                case JSlider.VERTICAL:
                    int halfThumbHeight = rect.height / 2;
                    int thumbTop = e.getY() - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);

                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setThumbLocationAt(rect.x, thumbTop, adjustingThumbIndex);

                    thumbMiddle = thumbTop + halfThumbHeight;
                    mSlider.setValueAt(ui.valueForYPosition(thumbMiddle), adjustingThumbIndex);
                    break;

                case JSlider.HORIZONTAL:
                    int halfThumbWidth = rect.width / 2;
                    int thumbLeft = e.getX() - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);

                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setThumbLocationAt(thumbLeft, rect.y, adjustingThumbIndex);

                    thumbMiddle = thumbLeft + halfThumbWidth;
                    mSlider.setValueAt(ui.valueForXPosition(thumbMiddle), adjustingThumbIndex);
                    break;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (!slider.isEnabled()) {
                    return;
                }
                offset = 0;
                isDragging = false;
                mSlider.setValueIsAdjusting(false);
                mSlider.repaint();
            }

            public boolean shouldScroll(int direction) {
                return false;
            }

        }
    }
}
