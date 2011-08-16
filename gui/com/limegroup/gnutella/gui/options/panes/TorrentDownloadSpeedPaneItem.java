package com.limegroup.gnutella.gui.options.panes;

import java.awt.Font;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gudy.azureus2.core3.config.COConfigurationManager;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;

public class TorrentDownloadSpeedPaneItem extends AbstractPaneItem {
	
	private static final int MAX_SLIDER_VALUE = (100*1024)+1;

	public final static String TITLE_DOWNLOAD_SPEED = I18n.tr("BitTorrent Global Tranfer Speeds");

	public final static String DESCRIPTION_DOWNLOAD_SPEED = I18n.tr("Set the Maximum BitTorrent transfer speeds in KB/s.\nTip: Use your keyboard arrows for more precision");

	private final String LABEL_DOWNLOAD_SPEED = I18n.tr("Download Speed:");

	/** Speeds in Kilobytes/sec 
	 * From 1Kb to 100Mb - 101 == Unlimited.
	 * */
	private JSlider DOWNLOAD_SLIDER = new JSlider(1, MAX_SLIDER_VALUE);

	private final JLabel DOWNLOAD_SLIDER_LABEL = new JLabel();

	private int storedDownloadSpeed;
	
	private String globalDownloadSpeedConfigKey = "Max Download Speed KBs";
	

	public final static String DESCRIPTION_UPLOAD_SPEED = I18n.tr("<html>Set the Maximum BitTorrent upload speed in KB/s.<p>Note: Minimum upload speeds could be automatically adjusted depending on your Maximum download speed.</html>");

	

	private final String LABEL_UPLOAD_SPEED = I18n.tr("Upload Speed:");

	/** Speeds in Kilobytes/sec 
	 * From 1Kb to 100Mb - 101 == Unlimited.
	 * */
	private JSlider UPLOAD_SLIDER = new JSlider(1, MAX_SLIDER_VALUE);

	private final JLabel UPLOAD_SLIDER_LABEL = new JLabel();

	private int storedUploadSpeed;

	private String globalUploadSpeedConfigKey = "Max Upload Speed KBs";
	
	public TorrentDownloadSpeedPaneItem() {
		super(TITLE_DOWNLOAD_SPEED, DESCRIPTION_DOWNLOAD_SPEED);

		DOWNLOAD_SLIDER.setMajorTickSpacing(1024);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		JLabel label1 = new JLabel(I18n.tr("Min speed"));
		JLabel label2 = new JLabel(I18n.tr("Max speed"));
		Font font = new Font("Helvetica", Font.BOLD, 10);
		label1.setFont(font);
		label2.setFont(font);
		labelTable.put(1, label1);
		labelTable.put(101*1024, label2);

		DOWNLOAD_SLIDER.setLabelTable(labelTable);
		DOWNLOAD_SLIDER.setPaintLabels(true);

		DOWNLOAD_SLIDER.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateSpeedLabel(DOWNLOAD_SLIDER,DOWNLOAD_SLIDER_LABEL);
			}
		});

		LabeledComponent comp = new LabeledComponent(LABEL_DOWNLOAD_SPEED, DOWNLOAD_SLIDER_LABEL,
				LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
		add(DOWNLOAD_SLIDER);
		add(getVerticalSeparator());
		add(comp.getComponent());
		
		///
		
		UPLOAD_SLIDER.setMajorTickSpacing(1024);

		Hashtable<Integer, JLabel> labelTableUp = new Hashtable<Integer, JLabel>();
		JLabel label1Up = new JLabel(I18n.tr("Min speed"));
		JLabel label2Up = new JLabel(I18n.tr("Max speed"));
		label1Up.setFont(font);
		label2Up.setFont(font);
		labelTableUp.put(1, label1Up);
		labelTableUp.put(101*1024, label2Up);

		UPLOAD_SLIDER.setLabelTable(labelTableUp);
		UPLOAD_SLIDER.setPaintLabels(true);

		UPLOAD_SLIDER.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateSpeedLabel(UPLOAD_SLIDER,UPLOAD_SLIDER_LABEL);
			}
		});

		LabeledComponent compUp = new LabeledComponent(LABEL_UPLOAD_SPEED, UPLOAD_SLIDER_LABEL,
				LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);

		JLabel DESCRIPTION_UPLOAD_SPEED_LABEL = new JLabel(DESCRIPTION_UPLOAD_SPEED);
		//
		
		add(DESCRIPTION_UPLOAD_SPEED_LABEL);
		add(UPLOAD_SLIDER);
		add(getVerticalSeparator());
		add(compUp.getComponent());
		
	}

	private void updateSpeedLabel(JSlider slider,JLabel label) {
		float value = slider.getValue();
		
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(2);

		String labelText = String.valueOf(formatter.format(value)) + " KB/s";
	
		if (value > 100*1024) {
			label.setText(I18n.tr("Unlimited"));
		} else {
			label.setText(labelText);
		}
	}

	@Override
	public void initOptions() {
		storedDownloadSpeed = COConfigurationManager.getIntParameter(globalDownloadSpeedConfigKey);
		
		if (storedDownloadSpeed == 0) {
			DOWNLOAD_SLIDER.setValue(101*1024);
			DOWNLOAD_SLIDER_LABEL.setText(I18n.tr("Unlimited"));
		} else {
			DOWNLOAD_SLIDER.setValue(storedDownloadSpeed);
		}
		
		updateSpeedLabel(DOWNLOAD_SLIDER, DOWNLOAD_SLIDER_LABEL);

		storedUploadSpeed = COConfigurationManager.getIntParameter(globalUploadSpeedConfigKey);
		
		if (storedUploadSpeed == 0) {
			UPLOAD_SLIDER.setValue(101*1024);
			UPLOAD_SLIDER_LABEL.setText(I18n.tr("Unlimited"));
		} else {
			UPLOAD_SLIDER.setValue(storedUploadSpeed);
		}
		
		updateSpeedLabel(UPLOAD_SLIDER, UPLOAD_SLIDER_LABEL);
	}

	@Override
	public boolean applyOptions() throws IOException {
		int newUpload = UPLOAD_SLIDER.getValue();
		int newDownload = DOWNLOAD_SLIDER.getValue();
		
		//normalize to azureus world if you have to.
		
		if (newUpload == MAX_SLIDER_VALUE) {
			newUpload = 0;
		}
		
		if (newDownload == MAX_SLIDER_VALUE) {
			newDownload = 0;
		}
		
		
		//if you're trying to download 
		if (newUpload < (newDownload/2) && (newUpload != 0)) {
			newUpload = (newDownload+1)/2;
		} 
		//wanna receive? you gotta give.
		else if (newDownload == 0) {
			newUpload = 0;
		}
		
		COConfigurationManager.setParameter(globalDownloadSpeedConfigKey, newDownload);
		COConfigurationManager.setParameter(globalUploadSpeedConfigKey, newUpload);
		
		DOWNLOAD_SLIDER.setValue((newDownload == 0) ? MAX_SLIDER_VALUE : newDownload);
		UPLOAD_SLIDER.setValue((newUpload == 0) ? MAX_SLIDER_VALUE : newUpload);
		
		COConfigurationManager.save();
		
		updateSpeedLabel(UPLOAD_SLIDER, UPLOAD_SLIDER_LABEL);
		updateSpeedLabel(DOWNLOAD_SLIDER, DOWNLOAD_SLIDER_LABEL);
		
		return false;
	}

	@Override
	public boolean isDirty() {
		//return storedDownloadSpeed != DOWNLOAD_SLIDER.getValue() || storedUploadSpeed != UPLOAD_SLIDER.getValue();
		return false;
	}

}