package com.frostwire.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import com.limegroup.gnutella.gui.I18n;

public class LabeledRangeSlider extends JPanel {

	private static final long serialVersionUID = 17039376083098413L;
	
	private final RangeSlider slider;
	
	private final JLabel titleLabel;
	private final JLabel minLabel;
	private final JLabel maxLabel;

	/**
	 * 
	 * @param title - No need to pass through I18n.tr()
	 * @param defaultMaxText - optional. No need to pass through I18n.tr()
	 * @param minValue
	 * @param maxValue
	 */
	public LabeledRangeSlider(String title, String defaultMaxText, int minValue, int maxValue) {
		slider = new RangeSlider(minValue,maxValue);
		slider.setValue(minValue);
		slider.setUpperValue(maxValue);

		titleLabel = new JLabel(I18n.tr(title));
		
		minLabel = new JLabel(String.valueOf(minValue));
		
		if (defaultMaxText == null) {
			maxLabel = new JLabel(I18n.tr("Max"));
		} else {
			maxLabel = new JLabel(I18n.tr(defaultMaxText));
		}
		
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		//add the title
		add(titleLabel, c);
		
		//add the slider
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		add(slider, c);
		
		//add the min and max labels
		c = new GridBagConstraints();
		c.gridx=0;
		c.anchor=GridBagConstraints.LINE_START;
		add(minLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(Box.createGlue(),c);
		
		c = new GridBagConstraints();
		c.gridx=2;
		c.anchor=GridBagConstraints.LINE_START;
		add(maxLabel, c);
	}

	public void addChangeListener(ChangeListener listener) {
		slider.addChangeListener(listener);
	}

    /**
     * Sets the lower value in the range.
     */
    public void setValue(int value) {
    	slider.setValue(value);
    }
    
    public int getValue() {
    	return slider.getValue();
    }

    public int getUpperValue() {
        return slider.getUpperValue();
    }

    public void setUpperValue(int value) {
    	slider.setUpperValue(value);    	
    }
    
    public void setMinimum(int min) {
    	slider.setMinimum(min);
    }
    
    public void setMaximum(int max) {
    	slider.setMaximum(max);
    }
    
    public JLabel getMinimumValueLabel() {
    	return minLabel;
    }
    
    public JLabel getMaximumValueLabel() {
    	return maxLabel;
    }
    
    public JLabel getTitleLabel() {
    	return titleLabel;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slider.setEnabled(enabled);
    }
}
