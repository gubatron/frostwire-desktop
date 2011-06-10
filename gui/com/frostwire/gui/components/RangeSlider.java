package com.frostwire.gui.components;

import javax.swing.JSlider;

public class RangeSlider extends JSlider {

    /**
     * 
     */
    private static final long serialVersionUID = -8238895600671189387L;

    public RangeSlider() {
        setOrientation(HORIZONTAL);
    }

    public RangeSlider(int min, int max) {
        super(min, max);
        setOrientation(HORIZONTAL);
    }

    @Override
    public void updateUI() {
        setUI(new RangeSliderUI(this));
        updateLabelUIs();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        int oldExtent = getExtent();
        int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
        int newExtent = oldExtent + oldValue - newValue;

        getModel().setRangeProperties(newValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
    }

    public int getUpperValue() {
        return getValue() + getExtent();
    }

    public void setUpperValue(int value) {
        int lowerValue = getValue();
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);

        setExtent(newExtent);
    }
}