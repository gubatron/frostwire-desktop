/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.limegroup.gnutella.gui.themes;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicSliderUI;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.ClassicFillPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.TransitionAwareUI;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.painter.SeparatorPainterUtils;
import org.pushingpixels.substance.internal.utils.*;
import org.pushingpixels.substance.internal.utils.icon.SubstanceIconFactory;

import com.frostwire.gui.components.RangeSlider;

/**
 * UI for sliders in <b>Substance</b> look and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SkinRangeSliderUI extends BasicSliderUI implements
        TransitionAwareUI {
    /**
     * Surrogate button model for tracking the thumb transitions.
     */
    private ButtonModel thumbModel;

    /**
     * Listener for transition animations.
     */
    private RolloverControlListener substanceRolloverListener;

    /**
     * Listener on property change events.
     */
    private PropertyChangeListener substancePropertyChangeListener;

    protected StateTransitionTracker stateTransitionTracker;

    /**
     * Icon for horizontal sliders.
     */
    protected Icon horizontalIcon;

    /**
     * Icon for sliders without labels and ticks.
     */
    protected Icon roundIcon;

    /**
     * Icon for vertical sliders.
     */
    protected Icon verticalIcon;

    /**
     * Cache of track images.
     */
    protected static final LazyResettableHashMap<BufferedImage> trackCache = new LazyResettableHashMap<BufferedImage>(
            "SubstanceSliderUI.track");
    
    protected Rectangle upperThumbRect;
    protected boolean upperThumbSelected;

    private transient boolean lowerDragging;
    private transient boolean upperDragging;

    protected RangeSlider slider;
    protected int lastUpperValue;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
     */
    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createRangeSliderUI(comp);
    }

    /**
     * Simple constructor.
     * 
     * @param rangeSlider
     *            Slider.
     */
    public SkinRangeSliderUI(RangeSlider slider) {
        super(null);
        this.thumbModel = new DefaultButtonModel();
        this.thumbModel.setArmed(false);
        this.thumbModel.setSelected(false);
        this.thumbModel.setPressed(false);
        this.thumbModel.setRollover(false);
        this.thumbModel.setEnabled(slider.isEnabled());

        this.stateTransitionTracker = new StateTransitionTracker(slider,
                this.thumbModel);
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#calculateTrackRect()
     */
    @Override
    protected void calculateTrackRect() {
        super.calculateTrackRect();
        if (this.slider.getOrientation() == SwingConstants.HORIZONTAL) {
            this.trackRect.y = 3
                    + (int) Math.ceil(SubstanceSizeUtils
                            .getFocusStrokeWidth(SubstanceSizeUtils
                                    .getComponentFontSize(this.slider)))
                    + this.insetCache.top;
        }
    }

    /**
     * Returns the rectangle of track for painting.
     * 
     * @return The rectangle of track for painting.
     */
    private Rectangle getPaintTrackRect() {
        int trackLeft = 0, trackRight = 0, trackTop = 0, trackBottom = 0;
        int trackWidth = this.getTrackWidth();
        if (this.slider.getOrientation() == SwingConstants.HORIZONTAL) {
            trackTop = 3 + this.insetCache.top + 2 * this.focusInsets.top;
            trackBottom = trackTop + trackWidth - 1;
            trackRight = this.trackRect.width;
            return new Rectangle(this.trackRect.x + trackLeft, trackTop,
                    trackRight - trackLeft, trackBottom - trackTop);
        } else {
            if (this.slider.getPaintLabels() || this.slider.getPaintTicks()) {
                if (this.slider.getComponentOrientation().isLeftToRight()) {
                    trackLeft = trackRect.x + this.insetCache.left
                            + this.focusInsets.left;
                    trackRight = trackLeft + trackWidth - 1;
                } else {
                    trackRight = trackRect.x + trackRect.width
                            - this.insetCache.right - this.focusInsets.right;
                    trackLeft = trackRight - trackWidth - 1;
                }
            } else {
                // horizontally center the track
                if (this.slider.getComponentOrientation().isLeftToRight()) {
                    trackLeft = (this.insetCache.left + this.focusInsets.left
                            + this.slider.getWidth() - this.insetCache.right - this.focusInsets.right)
                            / 2 - trackWidth / 2;
                    trackRight = trackLeft + trackWidth - 1;
                } else {
                    trackRight = (this.insetCache.left + this.focusInsets.left
                            + this.slider.getWidth() - this.insetCache.right - this.focusInsets.right)
                            / 2 + trackWidth / 2;
                    trackLeft = trackRight - trackWidth - 1;
                }
            }
            trackBottom = this.trackRect.height - 1;
            return new Rectangle(trackLeft, this.trackRect.y + trackTop,
                    trackRight - trackLeft, trackBottom - trackTop);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#paintTrack(java.awt.Graphics)
     */
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D graphics = (Graphics2D) g.create();

        boolean drawInverted = this.drawInverted();

        Rectangle paintRect = this.getPaintTrackRect();

        // Width and height of the painting rectangle.
        int width = paintRect.width;
        int height = paintRect.height;

        if (this.slider.getOrientation() == JSlider.VERTICAL) {
            // apply rotation / translate transformation on vertical
            // slider tracks
            int temp = width;
            width = height;
            height = temp;
            AffineTransform at = graphics.getTransform();
            at.translate(paintRect.x, width + paintRect.y);
            at.rotate(-Math.PI / 2);
            graphics.setTransform(at);
        } else {
            graphics.translate(paintRect.x, paintRect.y);
        }

        StateTransitionTracker.ModelStateInfo modelStateInfo = this.stateTransitionTracker
                .getModelStateInfo();

        SubstanceColorScheme trackSchemeUnselected = SubstanceColorSchemeUtilities
                .getColorScheme(this.slider,
                        this.slider.isEnabled() ? ComponentState.ENABLED
                                : ComponentState.DISABLED_UNSELECTED);
        SubstanceColorScheme trackBorderSchemeUnselected = SubstanceColorSchemeUtilities
                .getColorScheme(this.slider, ColorSchemeAssociationKind.BORDER,
                        this.slider.isEnabled() ? ComponentState.ENABLED
                                : ComponentState.DISABLED_UNSELECTED);
        this.paintSliderTrack(graphics, drawInverted, trackSchemeUnselected,
                trackBorderSchemeUnselected, width, height);

        Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = modelStateInfo
                .getStateContributionMap();
        for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
                .entrySet()) {
            ComponentState activeState = activeEntry.getKey();
            if (!activeState.isActive())
                continue;

            float contribution = activeEntry.getValue().getContribution();
            if (contribution == 0.0f)
                continue;

            graphics.setComposite(LafWidgetUtilities.getAlphaComposite(
                    this.slider, contribution, g));

            SubstanceColorScheme activeFillScheme = SubstanceColorSchemeUtilities
                    .getColorScheme(this.slider, activeState);
            SubstanceColorScheme activeBorderScheme = SubstanceColorSchemeUtilities
                    .getColorScheme(this.slider,
                            ColorSchemeAssociationKind.BORDER, activeState);
            this.paintSliderTrackSelected(graphics, drawInverted, paintRect,
                    activeFillScheme, activeBorderScheme, width, height);
        }

        graphics.dispose();
    }

    /**
     * Paints the slider track.
     * 
     * @param graphics
     *            Graphics.
     * @param drawInverted
     *            Indicates whether the value-range shown for the slider is
     *            reversed.
     * @param fillColorScheme
     *            Fill color scheme.
     * @param borderScheme
     *            Border color scheme.
     * @param width
     *            Track width.
     * @param height
     *            Track height.
     */
    private void paintSliderTrack(Graphics2D graphics, boolean drawInverted,
            SubstanceColorScheme fillColorScheme,
            SubstanceColorScheme borderScheme, int width, int height) {
        Graphics2D g2d = (Graphics2D) graphics.create();

        SubstanceFillPainter fillPainter = ClassicFillPainter.INSTANCE;
        SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
                .getBorderPainter(this.slider);

        int componentFontSize = SubstanceSizeUtils
                .getComponentFontSize(this.slider);
        int borderDelta = (int) Math.floor(SubstanceSizeUtils
                .getBorderStrokeWidth(componentFontSize) / 2.0);
        float radius = SubstanceSizeUtils
                .getClassicButtonCornerRadius(componentFontSize) / 2.0f;
        int borderThickness = (int) SubstanceSizeUtils
                .getBorderStrokeWidth(componentFontSize);

        HashMapKey key = SubstanceCoreUtilities.getHashKey(width, height,
                radius, borderDelta, borderThickness, fillColorScheme
                        .getDisplayName(), borderScheme.getDisplayName());

        BufferedImage trackImage = trackCache.get(key);
        if (trackImage == null) {
            trackImage = SubstanceCoreUtilities.getBlankImage(width + 1,
                    height + 1);
            Graphics2D cacheGraphics = trackImage.createGraphics();

            Shape contour = SubstanceOutlineUtilities.getBaseOutline(width + 1,
                    height + 1, radius, null, borderDelta);

            fillPainter.paintContourBackground(cacheGraphics, slider, width,
                    height, contour, false, fillColorScheme, false);

            GeneralPath contourInner = SubstanceOutlineUtilities
                    .getBaseOutline(width + 1, height + 1, radius
                            - borderThickness, null, borderThickness
                            + borderDelta);
            borderPainter.paintBorder(cacheGraphics, slider, width + 1,
                    height + 1, contour, contourInner, borderScheme);

            trackCache.put(key, trackImage);
            cacheGraphics.dispose();
        }

        g2d.drawImage(trackImage, 0, 0, null);

        g2d.dispose();
    }

    /**
     * Paints the selected part of the slider track.
     * 
     * @param graphics
     *            Graphics.
     * @param drawInverted
     *            Indicates whether the value-range shown for the slider is
     *            reversed.
     * @param paintRect
     *            Selected portion.
     * @param fillScheme
     *            Fill color scheme.
     * @param borderScheme
     *            Border color scheme.
     * @param width
     *            Track width.
     * @param height
     *            Track height.
     */
    private void paintSliderTrackSelected(Graphics2D graphics,
            boolean drawInverted, Rectangle paintRect,
            SubstanceColorScheme fillScheme, SubstanceColorScheme borderScheme,
            int width, int height) {

        Graphics2D g2d = (Graphics2D) graphics.create();
        Insets insets = this.slider.getInsets();
        insets.top /= 2;
        insets.left /= 2;
        insets.bottom /= 2;
        insets.right /= 2;

        SubstanceFillPainter fillPainter = SubstanceCoreUtilities
                .getFillPainter(this.slider);
        SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
                .getBorderPainter(this.slider);
        float radius = SubstanceSizeUtils
                .getClassicButtonCornerRadius(SubstanceSizeUtils
                        .getComponentFontSize(slider)) / 2.0f;
        int borderDelta = (int) Math.floor(SubstanceSizeUtils
                .getBorderStrokeWidth(SubstanceSizeUtils
                        .getComponentFontSize(slider)) / 2.0);

        // fill selected portion
        if (this.slider.isEnabled()) {
            if (this.slider.getOrientation() == SwingConstants.HORIZONTAL) {
                int middleOfThumb = this.thumbRect.x
                        + (this.thumbRect.width / 2) - paintRect.x;
                int middleOfUpperThumb = this.upperThumbRect.x
                        + (this.upperThumbRect.width / 2) - paintRect.x;
                int fillMinX;
                int fillMaxX;

                if (drawInverted) {
                    fillMinX = middleOfThumb;
                    fillMaxX = middleOfUpperThumb;
                } else {
                    fillMinX = middleOfThumb;
                    fillMaxX = middleOfUpperThumb;
                }

                int fillWidth = fillMaxX - fillMinX;
                int fillHeight = height + 1;
                if ((fillWidth > 0) && (fillHeight > 0)) {
                    Shape contour = SubstanceOutlineUtilities.getBaseOutline(
                            fillWidth, fillHeight, radius, null, borderDelta);
                    g2d.translate(fillMinX, 0);
                    fillPainter.paintContourBackground(g2d, this.slider,
                            fillWidth, fillHeight, contour, false, fillScheme,
                            false);
                    borderPainter.paintBorder(g2d, this.slider, fillWidth,
                            fillHeight, contour, null, borderScheme);
                }
            } else {
                int middleOfThumb = this.thumbRect.y
                        + (this.thumbRect.height / 2) - paintRect.y;
                int fillMin;
                int fillMax;

                if (this.drawInverted()) {
                    fillMin = 0;
                    fillMax = middleOfThumb;
                    // fix for issue 368 - inverted vertical sliders
                    g2d.translate(width + 2 - middleOfThumb, 0);
                } else {
                    fillMin = middleOfThumb;
                    fillMax = width + 1;
                }

                int fillWidth = fillMax - fillMin;
                int fillHeight = height + 1;
                if ((fillWidth > 0) && (fillHeight > 0)) {
                    Shape contour = SubstanceOutlineUtilities.getBaseOutline(
                            fillWidth, fillHeight, radius, null, borderDelta);

                    fillPainter.paintContourBackground(g2d, this.slider,
                            fillWidth, fillHeight, contour, false, fillScheme,
                            false);
                    borderPainter.paintBorder(g2d, this.slider, fillWidth,
                            fillHeight, contour, null, borderScheme);
                }
            }
        }
        g2d.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#getThumbSize()
     */
    @Override
    protected Dimension getThumbSize() {
        Icon thumbIcon = this.getIcon();
        return new Dimension(thumbIcon.getIconWidth(), thumbIcon
                .getIconHeight());
    }

    /**
     * Returns the thumb icon for the associated slider.
     * 
     * @return The thumb icon for the associated slider.
     */
    protected Icon getIcon() {
        if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
            if (this.slider.getPaintTicks() || this.slider.getPaintLabels())
                return this.horizontalIcon;
            else
                return this.roundIcon;
        } else {
            if (this.slider.getPaintTicks() || this.slider.getPaintLabels())
                return this.verticalIcon;
            else
                return this.roundIcon;
        }
    }
    
    @Override
    public void paintThumb(Graphics g) {
    }
    
    public void paintThumb(Graphics g, Rectangle thumbRect) {
        Graphics2D graphics = (Graphics2D) g.create();
        // graphics.setComposite(TransitionLayout.getAlphaComposite(slider));
        Rectangle knobBounds = thumbRect;
        // System.out.println(thumbRect);

        graphics.translate(knobBounds.x, knobBounds.y);

        Icon icon = this.getIcon();
        if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
            if (icon != null)
                icon.paintIcon(this.slider, graphics, -1, 0);
        } else {
            if (this.slider.getComponentOrientation().isLeftToRight()) {
                if (icon != null)
                    icon.paintIcon(this.slider, graphics, 0, -1);
            } else {
                if (icon != null)
                    icon.paintIcon(this.slider, graphics, 0, 1);
            }
        }

        // graphics.translate(-knobBounds.x, -knobBounds.y);
        graphics.dispose();
    }
    
    public void paintThumbs(Graphics g) {
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

    private void paintLowerThumb(Graphics g) {
        paintThumb(g, thumbRect);
    }

    private void paintUpperThumb(Graphics g) {
        paintThumb(g, upperThumbRect);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics,
     * javax.swing.JComponent)
     */
    @Override
    public void paint(Graphics g, final JComponent c) {
        Graphics2D graphics = (Graphics2D) g.create();

        ComponentState currState = ComponentState.getState(this.thumbModel,
                this.slider);
        float alpha = SubstanceColorSchemeUtilities.getAlpha(this.slider,
                currState);

        BackgroundPaintingUtils.updateIfOpaque(graphics, c);

        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        final Rectangle clip = graphics.getClipBounds();

        if (!clip.intersects(trackRect) && slider.getPaintTrack())
            calculateGeometry();

        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(this.slider,
                alpha, g));
        if (slider.getPaintTrack() && clip.intersects(trackRect)) {
            paintTrack(graphics);
        }
        if (slider.getPaintTicks() && clip.intersects(tickRect)) {
            paintTicks(graphics);
        }
        paintFocus(graphics);
        if (clip.intersects(thumbRect) || clip.intersects(upperThumbRect)) {
            paintThumbs(graphics);
        }
        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(this.slider,
                1.0f, g));
        if (slider.getPaintLabels() && clip.intersects(labelRect)) {
            paintLabels(graphics);
        }

        graphics.dispose();
    }

    @Override
    public StateTransitionTracker getTransitionTracker() {
        return this.stateTransitionTracker;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pushingpixels.substance.Trackable#isInside(java.awt.event.MouseEvent)
     */
    public boolean isInside(MouseEvent me) {
        Rectangle thumbB = this.thumbRect;
        if (thumbB == null)
            return false;
        Rectangle upperThumbB = this.upperThumbRect;
        if (upperThumbB == null) {
            return false;
        }
        return thumbB.contains(me.getX(), me.getY()) || upperThumbB.contains(me.getX(), me.getY());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.plaf.basic.BasicSliderUI#installDefaults(javax.swing.JSlider)
     */
    @Override
    protected void installDefaults(JSlider slider) {
        super.installDefaults(slider);
        Font f = slider.getFont();
        if (f == null || f instanceof UIResource) {
            slider.setFont(new FontUIResource(SubstanceLookAndFeel
                    .getFontPolicy().getFontSet("Substance", null)
                    .getControlFont()));
        }
        int size = SubstanceSizeUtils.getSliderIconSize(SubstanceSizeUtils
                .getComponentFontSize(slider));
        // System.out.println("Slider size : " + size);
        this.horizontalIcon = SubstanceIconFactory.getSliderHorizontalIcon(
                size, false);
        this.roundIcon = SubstanceIconFactory.getSliderRoundIcon(size);
        this.verticalIcon = SubstanceIconFactory.getSliderVerticalIcon(size,
                false);

        int focusIns = (int) Math.ceil(2.0 * SubstanceSizeUtils
                .getFocusStrokeWidth(SubstanceSizeUtils
                        .getComponentFontSize(slider)));
        this.focusInsets = new Insets(focusIns, focusIns, focusIns, focusIns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.plaf.basic.BasicSliderUI#installListeners(javax.swing.JSlider
     * )
     */
    @Override
    protected void installListeners(final JSlider slider) {
        super.installListeners(slider);

        // fix for defect 109 - memory leak on changing skin
        this.substanceRolloverListener = new RolloverControlListener(this,
                this.thumbModel);
        slider.addMouseListener(this.substanceRolloverListener);
        slider.addMouseMotionListener(this.substanceRolloverListener);

        this.substancePropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equals(evt.getPropertyName())) {
                    SkinRangeSliderUI.this.thumbModel.setEnabled(slider
                            .isEnabled());
                }
                if ("font".equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            slider.updateUI();
                        }
                    });
                }
            }
        };
        this.slider
                .addPropertyChangeListener(this.substancePropertyChangeListener);

        this.stateTransitionTracker.registerModelListeners();
        this.stateTransitionTracker.registerFocusListeners();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.plaf.basic.BasicSliderUI#uninstallListeners(javax.swing.JSlider
     * )
     */
    @Override
    protected void uninstallListeners(JSlider slider) {
        super.uninstallListeners(slider);

        // fix for defect 109 - memory leak on changing skin
        slider.removeMouseListener(this.substanceRolloverListener);
        slider.removeMouseMotionListener(this.substanceRolloverListener);
        this.substanceRolloverListener = null;

        slider
                .removePropertyChangeListener(this.substancePropertyChangeListener);
        this.substancePropertyChangeListener = null;

        this.stateTransitionTracker.unregisterModelListeners();
        this.stateTransitionTracker.unregisterFocusListeners();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#paintFocus(java.awt.Graphics)
     */
    @Override
    public void paintFocus(Graphics g) {
        SubstanceCoreUtilities.paintFocus(g, this.slider, this.slider, this,
                null, null, 1.0f, (int) Math.ceil(SubstanceSizeUtils
                        .getFocusStrokeWidth(SubstanceSizeUtils
                                .getComponentFontSize(this.slider))) / 2);
    }

    /**
     * Returns the amount that the thumb goes past the slide bar.
     * 
     * @return Amount that the thumb goes past the slide bar.
     */
    protected int getThumbOverhang() {
        return (int) (this.getThumbSize().getHeight() - this.getTrackWidth()) / 2;
    }

    /**
     * Returns the shorter dimension of the track.
     * 
     * @return Shorter dimension of the track.
     */
    protected int getTrackWidth() {
        return SubstanceSizeUtils.getSliderTrackSize(SubstanceSizeUtils
                .getComponentFontSize(this.slider));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#getTickLength()
     */
    @Override
    protected int getTickLength() {
        return SubstanceSizeUtils.getSliderTickSize(SubstanceSizeUtils
                .getComponentFontSize(this.slider));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#paintTicks(java.awt.Graphics)
     */
    @Override
    public void paintTicks(Graphics g) {
        Rectangle tickBounds = this.tickRect;
        SubstanceColorScheme tickScheme = SubstanceColorSchemeUtilities
                .getColorScheme(this.slider,
                        ColorSchemeAssociationKind.SEPARATOR, this.slider
                                .isEnabled() ? ComponentState.ENABLED
                                : ComponentState.DISABLED_UNSELECTED);
        if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
            int value = this.slider.getMinimum()
                    + this.slider.getMinorTickSpacing();
            int xPos = 0;

            if ((this.slider.getMinorTickSpacing() > 0)
                    && (this.slider.getMajorTickSpacing() > 0)) {
                // collect x's of the minor ticks
                java.util.List<Integer> minorXs = new ArrayList<Integer>();
                while (value < this.slider.getMaximum()) {
                    int delta = value - this.slider.getMinimum();
                    if (delta % this.slider.getMajorTickSpacing() != 0) {
                        xPos = this.xPositionForValue(value);
                        minorXs.add(xPos - 1);
                    }
                    value += this.slider.getMinorTickSpacing();
                }
                // and paint them in one call
                SeparatorPainterUtils.paintVerticalLines(g, this.slider,
                        tickScheme, tickBounds.y, minorXs,
                        tickBounds.height / 2, 0.75f);
            }

            if (this.slider.getMajorTickSpacing() > 0) {
                // collect x's of the major ticks
                java.util.List<Integer> majorXs = new ArrayList<Integer>();
                value = this.slider.getMinimum()
                        + this.slider.getMajorTickSpacing();
                while (value < this.slider.getMaximum()) {
                    xPos = this.xPositionForValue(value);
                    majorXs.add(xPos - 1);
                    value += this.slider.getMajorTickSpacing();
                }
                // and paint them in one call
                SeparatorPainterUtils.paintVerticalLines(g, this.slider,
                        tickScheme, tickBounds.y, majorXs, tickBounds.height,
                        0.75f);
            }
        } else {
            g.translate(tickBounds.x, 0);

            int value = this.slider.getMinimum()
                    + this.slider.getMinorTickSpacing();
            int yPos = 0;

            boolean ltr = this.slider.getComponentOrientation().isLeftToRight();
            if (this.slider.getMinorTickSpacing() > 0) {
                // collect y's of the minor ticks
                java.util.List<Integer> minorYs = new ArrayList<Integer>();
                int offset = 0;
                if (!ltr) {
                    offset = tickBounds.width - tickBounds.width / 2;
                }

                while (value < this.slider.getMaximum()) {
                    yPos = this.yPositionForValue(value);
                    minorYs.add(yPos);
                    value += this.slider.getMinorTickSpacing();
                }

                // and paint them in one call
                SeparatorPainterUtils.paintHorizontalLines(g, this.slider,
                        tickScheme, offset, minorYs, tickBounds.width / 2,
                        ltr ? 0.75f : 0.25f, ltr);
            }

            if (this.slider.getMajorTickSpacing() > 0) {
                // collect y's of the major ticks
                java.util.List<Integer> majorYs = new ArrayList<Integer>();
                value = this.slider.getMinimum()
                        + this.slider.getMajorTickSpacing();

                while (value < this.slider.getMaximum()) {
                    yPos = this.yPositionForValue(value);
                    majorYs.add(yPos);
                    value += this.slider.getMajorTickSpacing();
                }

                // and paint them in one call
                SeparatorPainterUtils.paintHorizontalLines(g, this.slider,
                        tickScheme, 0, majorYs, tickBounds.width, ltr ? 0.75f
                                : 0.25f, ltr);
            }
            g.translate(-tickBounds.x, 0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#calculateTickRect()
     */
    @Override
    protected void calculateTickRect() {
        if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
            this.tickRect.x = this.trackRect.x;
            this.tickRect.y = this.trackRect.y + this.trackRect.height;
            this.tickRect.width = this.trackRect.width;
            this.tickRect.height = (this.slider.getPaintTicks()) ? this
                    .getTickLength() : 0;
        } else {
            this.tickRect.width = (this.slider.getPaintTicks()) ? this
                    .getTickLength() : 0;
            if (this.slider.getComponentOrientation().isLeftToRight()) {
                this.tickRect.x = this.trackRect.x + this.trackRect.width;
            } else {
                this.tickRect.x = this.trackRect.x - this.tickRect.width;
            }
            this.tickRect.y = this.trackRect.y;
            this.tickRect.height = this.trackRect.height;
        }

        if (this.slider.getPaintTicks()) {
            if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
                this.tickRect.y -= 3;
            } else {
                if (this.slider.getComponentOrientation().isLeftToRight()) {
                    this.tickRect.x -= 2;
                } else {
                    this.tickRect.x += 2;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#calculateLabelRect()
     */
    @Override
    protected void calculateLabelRect() {
        super.calculateLabelRect();
        if ((this.slider.getOrientation() == JSlider.VERTICAL)
                && !this.slider.getPaintTicks()
                && this.slider.getComponentOrientation().isLeftToRight()) {
            this.labelRect.x += 3;
        }
        if (this.slider.getOrientation() == JSlider.VERTICAL) {
            this.labelRect.width = getHeightOfTallestLabel();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#calculateThumbLocation()
     */
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        Rectangle trackRect = this.getPaintTrackRect();
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int valuePosition = xPositionForValue(slider.getValue());

            double centerY = trackRect.y + trackRect.height / 2.0;
            thumbRect.y = (int) (centerY - thumbRect.height / 2.0) + 1;

            thumbRect.x = valuePosition - thumbRect.width / 2;
        } else {
            int valuePosition = yPositionForValue(slider.getValue());

            double centerX = trackRect.x + trackRect.width / 2.0;
            thumbRect.x = (int) (centerX - thumbRect.width / 2.0) + 1;

            thumbRect.y = valuePosition - (thumbRect.height / 2);
        }
        
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int valuePosition = xPositionForValue(slider.getUpperValue());

            double centerY = trackRect.y + trackRect.height / 2.0;
            upperThumbRect.y = (int) (centerY - upperThumbRect.height / 2.0) + 1;

            upperThumbRect.x = valuePosition - upperThumbRect.width / 2;
        } else {
            int valuePosition = yPositionForValue(slider.getUpperValue());

            double centerX = trackRect.x + trackRect.width / 2.0;
            upperThumbRect.x = (int) (centerX - upperThumbRect.width / 2.0) + 1;

            upperThumbRect.y = valuePosition - (upperThumbRect.height / 2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.plaf.basic.BasicSliderUI#getPreferredSize(javax.swing.JComponent
     * )
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        this.recalculateIfInsetsChanged();
        Dimension d;
        if (this.slider.getOrientation() == JSlider.VERTICAL) {
            d = new Dimension(this.getPreferredVerticalSize());
            d.width = this.insetCache.left + this.insetCache.right;
            d.width += this.focusInsets.left + this.focusInsets.right;
            d.width += this.trackRect.width;
            if (this.slider.getPaintTicks())
                d.width += getTickLength();
            if (this.slider.getPaintLabels())
                d.width += getWidthOfWidestLabel();
            d.width += 3;
        } else {
            d = new Dimension(this.getPreferredHorizontalSize());
            d.height = this.insetCache.top + this.insetCache.bottom;
            d.height += this.focusInsets.top + this.focusInsets.bottom;
            d.height += this.trackRect.height;
            if (this.slider.getPaintTicks())
                d.height += getTickLength();
            if (this.slider.getPaintLabels())
                d.height += getHeightOfTallestLabel();
            d.height += 3;
        }

        return d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#setThumbLocation(int, int)
     */
    @Override
    public void setThumbLocation(int x, int y) {
        super.setThumbLocation(x, y);
        this.slider.repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#getPreferredHorizontalSize()
     */
    @Override
    public Dimension getPreferredHorizontalSize() {
        return new Dimension(SubstanceSizeUtils.getAdjustedSize(
                SubstanceSizeUtils.getComponentFontSize(this.slider), 200, 1,
                20, false), 21);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicSliderUI#getPreferredVerticalSize()
     */
    @Override
    public Dimension getPreferredVerticalSize() {
        return new Dimension(21, SubstanceSizeUtils.getAdjustedSize(
                SubstanceSizeUtils.getComponentFontSize(this.slider), 200, 1,
                20, false));
    }
    
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
