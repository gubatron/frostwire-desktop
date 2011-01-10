package com.limegroup.bittorrent.gui.options.panes;

import java.awt.Font;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.TransferSpeedValidator;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;

public class BitTorrentDownloadSpeedPaneItem extends AbstractPaneItem {

	public final static String TITLE = I18n.tr("BitTorrent download speed");

	public final static String LABEL = I18n.tr("Set the Maximum BitTorrent download speed in KB/s.");

	private final String LABEL_SPEED = I18n.tr("Download Speed:");

	/** Speeds in Kilobytes/sec 
	 * From 56kbit to 100mbit - 101 == Unlimited.
	 * */
	private JSlider DOWNLOAD_SLIDER = new JSlider(56, 101*1024);

	private final JLabel SLIDER_LABEL = new JLabel();

	private int storedDownloadSpeed;
	
	private String configKey = "Max Download Speed KBs";
	
	public BitTorrentDownloadSpeedPaneItem() {
		super(TITLE, LABEL);

		DOWNLOAD_SLIDER.setMajorTickSpacing(1024);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		JLabel label1 = new JLabel(I18n.tr("Min speed"));
		JLabel label2 = new JLabel(I18n.tr("Max speed"));
		Font font = new Font("Helvetica", Font.BOLD, 10);
		label1.setFont(font);
		label2.setFont(font);
		labelTable.put(56, label1);
		labelTable.put(101*1024, label2);

		DOWNLOAD_SLIDER.setLabelTable(labelTable);
		DOWNLOAD_SLIDER.setPaintLabels(true);

		DOWNLOAD_SLIDER.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateSpeedLabel();
			}
		});

		LabeledComponent comp = new LabeledComponent(LABEL_SPEED, SLIDER_LABEL,
				LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
		add(DOWNLOAD_SLIDER);
		add(getVerticalSeparator());
		add(comp.getComponent());
	}

	private void updateSpeedLabel() {
		float value = DOWNLOAD_SLIDER.getValue();

		System.out.println("updateSpeedLabel: " + value);
		
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(2);

		String labelText = String.valueOf(formatter.format(value)) + " KB/s";
	
		if (value > 100*1024) {
			SLIDER_LABEL.setText(I18n.tr("Unlimited"));
		} else {
			SLIDER_LABEL.setText(labelText);
		}
	}

	@Override
	public void initOptions() {
		storedDownloadSpeed = COConfigurationManager.getIntParameter(configKey);
		
		if (storedDownloadSpeed == 0) {
			DOWNLOAD_SLIDER.setValue(101*1024);
			SLIDER_LABEL.setText(I18n.tr("Unlimited"));
		} else {
			DOWNLOAD_SLIDER.setValue(storedDownloadSpeed);
		}
		
		updateSpeedLabel();
	}

	@Override
	public boolean applyOptions() throws IOException {
		int newSpeed = DOWNLOAD_SLIDER.getValue();
		int cValue = 0; //unlimited
		
		if (newSpeed <= 100*1024) {
			cValue = ((Integer) new TransferSpeedValidator(configKey,
	                new Integer(newSpeed)).getValue()).intValue();
		} 
		
		COConfigurationManager.setParameter(configKey, cValue);
		COConfigurationManager.save();
		
		return false;
	}

	@Override
	public boolean isDirty() {
		return storedDownloadSpeed != DOWNLOAD_SLIDER.getValue();
	}

}