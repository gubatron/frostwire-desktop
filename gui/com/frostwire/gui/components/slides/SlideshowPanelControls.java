package com.frostwire.gui.components.slides;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.frostwire.gui.components.slides.SlideshowPanel.SlideshowListener;


public class SlideshowPanelControls extends JPanel implements SlideshowListener {

	private static final long serialVersionUID = 7167253192165957777L;
	private final SlideshowPanel _thePanel;

	private ButtonGroup _buttonGroup;
	private List<JRadioButton> _buttons;
	
	private ItemListener _selectionAdapter;
	
	public SlideshowPanelControls(SlideshowPanel panel) {
		_thePanel = panel;
		_thePanel.setListener(this);
		
		buildButtons();
		autoSelectCurrentSlideButton();
		buildItemListener();
		attachListeners();
	}

	public void autoSelectCurrentSlideButton() {
		int currentSlideIndex = _thePanel.getCurrentSlideIndex();
		if (currentSlideIndex!=-1) {
			_buttons.get(currentSlideIndex).setSelected(true);
		} else {
			_buttons.get(0).setSelected(true);
		}
	}

	private void buildItemListener() {
		_selectionAdapter = new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (((JRadioButton)e.getItemSelectable()).isSelected()) {				
					onRadioButtonClicked(e);				
				}
			}
		};
	}

	protected void onRadioButtonClicked(ItemEvent e) {
		int selectedIndex = _buttons.indexOf(e.getSource());
		_thePanel.switchToSlide(selectedIndex);
	}

	private void buildButtons() {
		int numSlides = _thePanel.getNumSlides();

		_buttonGroup = new ButtonGroup();
		_buttons = new ArrayList<JRadioButton>(numSlides);
		
		for (int i=0; i < numSlides; i++) {
			JRadioButton radio = new JRadioButton();
			
			//add to the list
			_buttons.add(radio);
			
			//add to the button group
			_buttonGroup.add(radio);

			//add to the panel
			add(radio);
		}
	}
	
	private void attachListeners() {
		for (int i=0; i < _buttons.size(); i++) {
			_buttons.get(i).addItemListener(_selectionAdapter);
		}
	}

	@Override
	public void onSlideChanged() {
		int currentSlideIndex = _thePanel.getCurrentSlideIndex();
		JRadioButton button = _buttons.get(currentSlideIndex);
		
		ItemListener[] itemListeners = button.getItemListeners();

		for (ItemListener listener : itemListeners) {
			button.removeItemListener(listener);
		}
		
		button.setSelected(true);
		
		for (ItemListener listener : itemListeners) {
			button.addItemListener(listener);
		}
		
	}

}
