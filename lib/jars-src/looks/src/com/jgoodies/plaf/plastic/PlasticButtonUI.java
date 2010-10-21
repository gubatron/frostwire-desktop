/*
 * Copyright (c) 2003 JGoodies Karsten Lentzsch. All Rights Reserved.
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
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of 
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

package com.jgoodies.plaf.plastic;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.LookAndFeel;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;
import com.jgoodies.plaf.common.ButtonMarginListener;

/**
 * The JGoodies Plastic L&amp;F implementation of <code>ButtonUI</code>.
 * <p>
 * It differs from the superclass <code>MetalButtonUI</code> in that 
 * it can add a pseudo 3D effect, and that it listens to the 
 * <code>jgoodies.isNarrow</code> property to choose an appropriate margin.
 *
 * @author Karsten Lentzsch
 */
public class PlasticButtonUI extends MetalButtonUI {

    private static final PlasticButtonUI INSTANCE = new PlasticButtonUI();
    private final Border ROLLOVER_BORDER = BorderFactory.createRaisedBevelBorder();
    private final Border PRESSED_BORDER = BorderFactory.createLoweredBevelBorder();

    private boolean borderPaintsFocus;

    public static ComponentUI createUI(JComponent b) {
        return INSTANCE;
    }

    /**
     * Installs defaults and honors the client property <code>isNarrow</code>.
     */
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        LookUtils.installNarrowMargin(b, getPropertyPrefix());
        b.setRolloverEnabled(true);
        borderPaintsFocus =
            Boolean.TRUE.equals(UIManager.get("Button.borderPaintsFocus"));
    }

    /**
     * Installs an extra listener for a change of the isNarrow property.
     */
    public void installListeners(AbstractButton b) {
        super.installListeners(b);
        PropertyChangeListener listener =
            new ButtonMarginListener(getPropertyPrefix());
        b.putClientProperty(ButtonMarginListener.CLIENT_KEY, listener);
        b.addPropertyChangeListener(Options.IS_NARROW_KEY, listener);
    }

    /**
     * Uninstalls the extra listener for a change of the isNarrow property.
     */
    public void uninstallListeners(AbstractButton b) {
        super.uninstallListeners(b);
        PropertyChangeListener listener =
            (PropertyChangeListener) b.getClientProperty(
                ButtonMarginListener.CLIENT_KEY);
        b.removePropertyChangeListener(listener);
    }

    // Painting ***************************************************************
    Rectangle rect3D = new Rectangle();
    public void update(Graphics g, JComponent c) {
        super.update(g, c);
        
        AbstractButton b = (AbstractButton) c;
        if (c.isOpaque()) {
            if (isToolBarButton(b)) {
                c.setOpaque(false);
            } else if (b.isContentAreaFilled()) {
                if(b.isRolloverEnabled() && b.getModel().isRollover())
                    g.setColor(c.getBackground().brighter());
                else
                    g.setColor(c.getBackground());
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                if (is3D(b)) {
                    rect3D.x = rect3D.y = 1;
                    rect3D.width = c.getWidth() - 2;
                    rect3D.height = c.getHeight() - 1;
                    PlasticUtils.add3DEffekt(g, rect3D);
                }
            }
        } else {
            ButtonModel bm = b.getModel();
            if(bm.isArmed() && bm.isPressed())
                PRESSED_BORDER.paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
            else if(b.isRolloverEnabled() && bm.isRollover())
                ROLLOVER_BORDER.paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
        }
        
        paint(g, c);
    }
    
    /**
     * Paints the icon a bit lower & to the right if we're pressed.
     */
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        ButtonModel bm = ((AbstractButton)c).getModel();
        if(bm.isArmed() && bm.isPressed()) {
            iconRect.x += 1;
            iconRect.y += 1;
        }
        super.paintIcon(g, c, iconRect);
    }
    
    /**
     * Paints the text a bit lower & to the right if we're pressed.
     */
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        ButtonModel bm = ((AbstractButton)c).getModel();
        if(bm.isArmed() && bm.isPressed()) {
            textRect.x += 1;
            textRect.y += 1;
        }
        super.paintText(g, c, textRect, text);
    }
        

    /**
     * Paints the focus with close to the button's border.
     */
    protected void paintFocus(
        Graphics g,
        AbstractButton b,
        Rectangle viewRect,
        Rectangle textRect,
        Rectangle iconRect) {

        if (borderPaintsFocus) {
            return;
        }

        boolean isDefault =
            b instanceof JButton && ((JButton) b).isDefaultButton();
        int topLeftInset = isDefault ? 3 : 2;
        int width = b.getWidth() - 1 - topLeftInset * 2;
        int height = b.getHeight() - 1 - topLeftInset * 2;

        g.setColor(getFocusColor());
        g.drawRect(topLeftInset, topLeftInset, width - 1, height - 1);
    }

    // Private Helper Code **************************************************************

    /**
     * Checks and answers if this is button is in a tool bar.
     * 
     * @param b   the button to check
     * @return true if in tool bar, false otherwise
     */
    protected boolean isToolBarButton(AbstractButton b) {
        Container parent = b.getParent();
        return parent != null
            && (parent instanceof JToolBar
                || parent.getParent() instanceof JToolBar);
    }

    /**
     * Checks and answers if this button shall use a pseudo 3D effect
     * 
     * @param b  the button to check
     * @return true indicates a 3D effect, false flat
     */
    protected boolean is3D(AbstractButton b) {
        if (PlasticUtils.force3D(b))
            return true;
        if (PlasticUtils.forceFlat(b))
            return false;
        ButtonModel model = b.getModel();
        return PlasticUtils.is3D("Button.")
            && b.isBorderPainted()
            && model.isEnabled()
            && !(model.isPressed() && model.isArmed())
            && !(b.getBorder() instanceof EmptyBorder);

        /*
         * Implementation note regarding the last line: instead of checking 
         * for the EmptyBorder in NetBeans, I'd prefer to just check the
         * 'borderPainted' property. I'd recommend to the NetBeans developers,
         * to switch this property on and off, instead of changing the border.
         */
    }

}