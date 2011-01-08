package com.limegroup.bittorrent.gui.options.panes;

import java.awt.Font;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;
import com.limegroup.gnutella.settings.ConnectionSettings;

public class BitTorrentDownloadSpeedPaneItem extends AbstractPaneItem {

	public final static String TITLE = "BitTorrent download speed";

	public final static String LABEL = "You can set the BitTorrent download speed.";

	private final String LABEL_SPEED = "Download Speed:";

	private final JSlider DOWNLOAD_SLIDER = new JSlider(0, 100);

	private final JLabel SLIDER_LABEL = new JLabel();

	private int storedDownloadSpeed;

	public BitTorrentDownloadSpeedPaneItem() {
		super(TITLE, LABEL);

		DOWNLOAD_SLIDER.setMajorTickSpacing(10);
		DOWNLOAD_SLIDER.setPaintTicks(true);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		JLabel label1 = new JLabel("Min speed");
		JLabel label2 = new JLabel("Max speed");
		Font font = new Font("Helvetica", Font.BOLD, 10);
		label1.setFont(font);
		label2.setFont(font);
		labelTable.put(0, label1);
		labelTable.put(100, label2);

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

		Float f = new Float((value / 100.0)
				* ConnectionSettings.CONNECTION_SPEED.getValue() / 8.f);

		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(2);

		String labelText = String.valueOf(formatter.format(f)) + " KB/s";

		SLIDER_LABEL.setText(labelText);
	}

	@Override
	public void initOptions() {
		// TODO Read BitTorrent speed configuration
		storedDownloadSpeed = 70;

		DOWNLOAD_SLIDER.setValue(storedDownloadSpeed);
		updateSpeedLabel();
	}

	@Override
	public boolean applyOptions() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

}