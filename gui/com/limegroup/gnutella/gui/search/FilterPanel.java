package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frostwire.gui.components.LabeledRangeSlider;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledTextField;

public class FilterPanel extends JPanel {

	/**
     * 
     */
	private static final long serialVersionUID = 346186974515196119L;

	private LabeledRangeSlider _rangeSliderSeeds;
	private LabeledRangeSlider _rangeSliderSize;
	private LabeledTextField _keywordFilterTextField;

	private GeneralResultFilter _activeFilter;

	private final Map<ResultPanel, GeneralResultFilter> ACTIVE_FILTERS = new HashMap<ResultPanel, GeneralResultFilter>();

	public FilterPanel() {
		setupUI();
		setSlidersEnabled(false);
	}

	protected void setupUI() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		setBorder(new TitledBorder(I18n.tr("Filter Results")));

		_rangeSliderSeeds = new LabeledRangeSlider("Download Sources", null, 0,
				1000);
		_rangeSliderSeeds.setPreferredSize(new Dimension(80,
				(int) _rangeSliderSeeds.getPreferredSize().getHeight()));
		_rangeSliderSeeds.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rangeSliderSeeds_stateChanged(e);
			}
		});
		add(_rangeSliderSeeds, c);

		_rangeSliderSize = new LabeledRangeSlider("File Size", null, 0, 1000);
		_rangeSliderSize.setPreferredSize(new Dimension(80,
				(int) _rangeSliderSize.getPreferredSize().getHeight()));
		_rangeSliderSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rangeSliderSize_stateChanged(e);
			}
		});
		add(_rangeSliderSize, c);

		_keywordFilterTextField = new LabeledTextField("Name", 40, -1,
				100);
		
		_keywordFilterTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				keywordFilterChanged(e);
			}
		});

		add(_keywordFilterTextField, c);
	};

	protected void keywordFilterChanged(KeyEvent e) {
		if (_activeFilter != null) {
			_activeFilter.updateKeywordFiltering(_keywordFilterTextField
					.getText());
		}
	}

	protected void rangeSliderSeeds_stateChanged(ChangeEvent e) {
		if (_activeFilter != null) {
			_activeFilter.setRangeSeeds(_rangeSliderSeeds.getValue(),
					_rangeSliderSeeds.getUpperValue());
		}
	}

	protected void rangeSliderSize_stateChanged(ChangeEvent e) {
		if (_activeFilter != null) {
			_activeFilter.setRangeSize(_rangeSliderSize.getValue(),
					_rangeSliderSize.getUpperValue());
		}
	}

	public void setSlidersEnabled(boolean enabled) {
		_rangeSliderSeeds.setEnabled(enabled);
		_rangeSliderSize.setEnabled(enabled);
	}

	public void reset() {
		_activeFilter = null;
		_rangeSliderSeeds.setMinimum(0);
		_rangeSliderSeeds.setMaximum(1000);
		_rangeSliderSeeds.setValue(0);
		_rangeSliderSeeds.setUpperValue(1000);
		_rangeSliderSize.setMinimum(0);
		_rangeSliderSize.setMaximum(1000);
		_rangeSliderSize.setValue(0);
		_rangeSliderSize.setUpperValue(1000);

		_rangeSliderSeeds.getMinimumValueLabel().setText(I18n.tr("0"));
		_rangeSliderSeeds.getMaximumValueLabel().setText(I18n.tr("Max"));
		_rangeSliderSize.getMinimumValueLabel().setText(I18n.tr("0"));
		_rangeSliderSize.getMaximumValueLabel().setText(I18n.tr("Max"));
	}

	private void reset(GeneralResultFilter filter) {
		_activeFilter = null;
		_rangeSliderSeeds.setMinimum(0);
		_rangeSliderSeeds.setMaximum(1000);
		_rangeSliderSeeds.setValue(filter.getMinSeeds());
		_rangeSliderSeeds.setUpperValue(filter.getMaxSeeds());
		_rangeSliderSize.setMinimum(0);
		_rangeSliderSize.setMaximum(1000);
		_rangeSliderSize.setValue(filter.getMinSize());
		_rangeSliderSize.setUpperValue(filter.getMaxSize());

		if (filter.getMinResultsSeeds() == Integer.MAX_VALUE) {
			_rangeSliderSeeds.getMinimumValueLabel().setText(I18n.tr("0"));
		} else {
			_rangeSliderSeeds.getMinimumValueLabel().setText(
					String.valueOf(filter.getMinResultsSeeds()));
		}
		if (filter.getMaxResultsSeeds() == 0) {
			_rangeSliderSeeds.getMaximumValueLabel().setText(I18n.tr("Max"));
		} else {
			_rangeSliderSeeds.getMaximumValueLabel().setText(
					String.valueOf(filter.getMaxResultsSeeds()));
		}
		if (filter.getMinResultsSize() == Long.MAX_VALUE) {
			_rangeSliderSize.getMinimumValueLabel().setText(I18n.tr("0"));
		} else {
			_rangeSliderSize.getMinimumValueLabel().setText(
					GUIUtils.toUnitbytes(filter.getMinResultsSize()));
		}
		if (filter.getMaxResultsSize() == 0) {
			_rangeSliderSize.getMaximumValueLabel().setText(I18n.tr("Max"));
		} else {
			_rangeSliderSize.getMaximumValueLabel().setText(
					GUIUtils.toUnitbytes(filter.getMaxResultsSize()));
		}
	}

	public void clearFilters() {
		ACTIVE_FILTERS.clear();
		setSlidersEnabled(false);
		reset();
	}

	public void setFilterFor(ResultPanel rp) {
		GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
		if (filter == null) {
			filter = new GeneralResultFilter(rp, _rangeSliderSeeds,
					_rangeSliderSize);
			ACTIVE_FILTERS.put(rp, filter);
			rp.filterChanged(filter, 1);
		}
		setActiveFilter(filter);
	}

	private void setActiveFilter(GeneralResultFilter filter) {
		_activeFilter = null;
		setSlidersEnabled(true);
		reset(filter);
		_activeFilter = filter;
	}

	public void panelReset(ResultPanel rp) {
		GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
		if (filter != null) {
			ACTIVE_FILTERS.remove(rp);
			setFilterFor(rp);
		}
	}

	public boolean panelRemoved(ResultPanel rp) {
		GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
		if (filter != null) {
			ACTIVE_FILTERS.remove(rp);
		}
		return ACTIVE_FILTERS.isEmpty();
	}
}
