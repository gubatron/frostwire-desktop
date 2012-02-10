package com.frostwire.gui.bittorrent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.SkinCustomUI;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSeedingSettingComponent extends JPanel {
	
	private static final long serialVersionUID = 571313077597756384L;
	
	private boolean _precheck;
	private boolean _border;
	
	private ButtonGroup _radioGroup;

	private JRadioButton _seedingRadioButton;

	private JRadioButton _notSeedingRadioButton;

	/**
	 * 
	 * @param precheck - whether or not to pre-select one of the radio buttons.
	 */
	public TorrentSeedingSettingComponent(boolean precheck, boolean useBorder) {
		
		_precheck = precheck;
		_border = useBorder;
		
		setupUI();
	}

	private void setupUI() {
		setLayout(new GridBagLayout());
		
		if (_border) {
		    this.putClientProperty(SkinCustomUI.CLIENT_PROPERTY_DARK_DARK_NOISE, true);
			setBorder(ThemeMediator.CURRENT_THEME.getCustomUI().createTitledBorder(I18n.tr("Seeding Settings")));
		}

		initRadioButtons();
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(5,5,5,5);
		c.weightx = 1.0;
		
		add(_seedingRadioButton,c);
		add(_notSeedingRadioButton,c);
		
	}

	private void initRadioButtons() {
		_seedingRadioButton = new JRadioButton(I18n.tr("<html><strong>Seed finished torrent downloads.</strong> BitTorrent users on the internet will be able<br/>to download file chunks of the data your torrents seed. (Recommended)</html>"));
		_notSeedingRadioButton = new JRadioButton(I18n.tr("<html><strong>Don't seed finished torrent downloads.</strong> BitTorrent users on the internet may<br/>only download file chunks of that torrent from you while you're downloading its<br/>data files. <strong>Some trackers will penalize this Leeching behavior</strong>.</html>"));
		_radioGroup = new ButtonGroup();
		_radioGroup.add(_seedingRadioButton);
		_radioGroup.add(_notSeedingRadioButton);
		
		if (_precheck) {
			if (SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
				_seedingRadioButton.setSelected(true);
				_notSeedingRadioButton.setSelected(false);
			} else {
				_notSeedingRadioButton.setSelected(true);
				_seedingRadioButton.setSelected(false);
			}
		}
	}

	public boolean wantsSeeding() {
		return _seedingRadioButton.isSelected();
	}

	public boolean hasOneBeenSelected() {
		return _seedingRadioButton.isSelected() || _notSeedingRadioButton.isSelected();
	}
	
}
