package com.frostwire.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.frostwire.gui.components.SlideshowPanel.SlideshowListener;

public class SlideshowPanelControls extends JPanel implements SlideshowListener {

	private static final long serialVersionUID = 7167253192165957777L;
	private SlideshowPanel _thePanel;

	private ButtonGroup _buttonGroup;
	private List<JRadioButton> _buttons;
	
	private MouseAdapter _selectionAdapter;
	
	public SlideshowPanelControls(SlideshowPanel panel) {
		_thePanel = panel;
		_thePanel.addListener(this);
		
		buildMouseAdapter();
		buildButtons();
	}

	private void buildMouseAdapter() {
		_selectionAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onRadioButtonClicked(e);
			}
		};		
	}

	protected void onRadioButtonClicked(MouseEvent e) {
		int selectedIndex = _buttons.indexOf(e.getSource());
		_thePanel.switchToSlide(selectedIndex);
	}

	private void buildButtons() {
		int numSlides = _thePanel.getNumSlides();

		_buttonGroup = new ButtonGroup();
		_buttons = new ArrayList<JRadioButton>(numSlides);
		
		for (int i=0; i < numSlides; i++) {
			JRadioButton radio = new JRadioButton();
			radio.addMouseListener(_selectionAdapter);
			
			//add to the list
			_buttons.add(radio);
			
			//add to the button group
			_buttonGroup.add(radio);

			//add to the panel
			add(radio);
		}
	}

	@Override
	public void onSlideChanged() {
		_buttons.get(_thePanel.getCurrentSlideIndex()).setSelected(true);
	}

}
