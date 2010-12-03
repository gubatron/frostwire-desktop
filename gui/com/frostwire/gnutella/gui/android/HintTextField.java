package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3191287673317585610L;

    private final String _hint;
    private Color _color;
    private Color _hintColor;

    public HintTextField(final String hint) {
        super(hint);
        _hint = hint;
        _color = Color.BLACK;
        _hintColor = Color.LIGHT_GRAY;
        setForeground(_hintColor);
        addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().length() == 0) {
            setForeground(_color);
            super.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().length() == 0) {
            clear();
        }
    }

    @Override
    public String getText() {
        String typed = super.getText();
        return typed.equals(_hint) ? "" : typed;
    }

    public void clear() {
        setForeground(_hintColor);
        super.setText(_hint);
    }
}
