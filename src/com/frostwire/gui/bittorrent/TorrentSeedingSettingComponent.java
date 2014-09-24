package com.frostwire.gui.bittorrent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frostwire.gui.theme.ThemeMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSeedingSettingComponent extends JPanel {
	
	private static final long serialVersionUID = 571313077597756384L;
	
	private boolean _precheck;
	private boolean _border;
	
	private ButtonGroup radioGroup;

	private JRadioButton seedingRadioButton;

	private JRadioButton notSeedingRadioButton;
	
	private JCheckBox handPickedSeedingCheckbox;

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
		    setBorder(ThemeMediator.createTitledBorder(I18n.tr("Seeding Settings")));
		}

		initOptionButtons();
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(5,5,5,5);
		c.weightx = 1.0;
		
		add(seedingRadioButton,c);
		add(notSeedingRadioButton,c);
		add(handPickedSeedingCheckbox,c);
		
	}

	private class SeedingRadioButtonChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            handPickedSeedingCheckbox.setEnabled(seedingRadioButton.isSelected());
        }
	}
	
	private void initOptionButtons() {
		seedingRadioButton = new JRadioButton(I18n.tr("<html><strong>Seed finished torrent downloads.</strong> BitTorrent users on the internet will be able<br/>to download file chunks of the data your torrents seed. (Recommended)</html>"));
		notSeedingRadioButton = new JRadioButton(I18n.tr("<html><strong>Don't seed finished torrent downloads.</strong> BitTorrent users on the internet may<br/>only download file chunks of that torrent from you while you're downloading its<br/>data files. <strong>Some trackers will penalize this Leeching behavior</strong>.</html>"));
		radioGroup = new ButtonGroup();
		radioGroup.add(seedingRadioButton);
		radioGroup.add(notSeedingRadioButton);
		
		if (_precheck) {
			if (SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
				seedingRadioButton.setSelected(true);
				notSeedingRadioButton.setSelected(false);
			} else {
				notSeedingRadioButton.setSelected(true);
				seedingRadioButton.setSelected(false);
			}
		}
		
		handPickedSeedingCheckbox = new JCheckBox(I18n.tr("<html><strong>Seed handpicked torrent files.</strong> Handpicked file torrents downloads (Partial Downloads) can result in seeding data chunks of neighboring incomplete files.<br/>This feature is <strong>recommended for advanced users only</strong> as it can lead to downloading parts of files you may have not explicitly chosen for download.</html>"));
		handPickedSeedingCheckbox.setSelected(SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue());
		handPickedSeedingCheckbox.setEnabled(seedingRadioButton.isSelected());
		
		SeedingRadioButtonChangeListener seedingRadioButtonChangeListener = new SeedingRadioButtonChangeListener();
		seedingRadioButton.addChangeListener(seedingRadioButtonChangeListener);
	    notSeedingRadioButton.addChangeListener(seedingRadioButtonChangeListener);
	}

	public boolean wantsSeeding() {
		return seedingRadioButton.isSelected();
	}

	public boolean hasOneBeenSelected() {
		return seedingRadioButton.isSelected() || notSeedingRadioButton.isSelected();
	}
	
	public boolean wantsHandpickedSeeding() {
	    return handPickedSeedingCheckbox.isSelected();
	}
}
