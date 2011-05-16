package com.limegroup.gnutella.gui.options.panes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.limewire.i18n.I18nMarker;

import com.frostwire.GuiFrostWireUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.SharingSettings;

public class ShareSpeciallyPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Download Sharing");
    
    public final static String LABEL = I18n.tr("You can share files that you download to unshared folders.");

	/**
	 * Constant for the key of the locale-specific <tt>String</tt> for the 
	 * check box that allows the user to connect automatically or not
	 */
	private final String CHECK_BOX_LABEL = 
		I18nMarker.marktr("Share Finished Downloads:");
	
	/**
	 * Explains what downloads are currently being shared.
	 */
	private final JLabel explanationLabel = new JLabel();

	/**
	 * Constant for the check box that determines whether or not 
	 * to send OOB searches.
	 */
	private final JCheckBox CHECK_BOX = new JCheckBox();
	
	public ShareSpeciallyPaneItem() {
	    super(TITLE, LABEL);
	    
		LabeledComponent comp = new LabeledComponent(CHECK_BOX_LABEL,
				CHECK_BOX, LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
		add(comp.getComponent());

		explanationLabel.setFont(explanationLabel.getFont().deriveFont(Math.max(explanationLabel.getFont().getSize() - 2.0f, 9.0f)).deriveFont(Font.PLAIN));
        CHECK_BOX.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setExplanationText(true);
            }
        });
		
		JPanel container = new JPanel(new BorderLayout());
		container.add(explanationLabel, BorderLayout.EAST);
		add(container);
	}
	
	private void setExplanationText(boolean showMessage) {
        if (CHECK_BOX.isSelected()) {
            explanationLabel.setText(I18n.tr("Currently sharing downloaded files to the 'Save Folder' with everybody"));
            if (showMessage) {
            	SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
                        I18n.tr("All files downloaded to the 'Save Folder' will be shared as 'individually shared files' with everybody on the network.\nYour 'Save Folder' won't be shared as a whole unless you decide to make it a shared folder.\n\nYou can check the files you are sharing individually in the Library Tab."),
                        I18n.tr("How finished downloads are being shared"),
                        JOptionPane.WARNING_MESSAGE);
					}});
            	 
            }
        } else {
            explanationLabel.setText(I18n.tr("<html>Currently not sharing downloaded files to the 'Save Folder' with anybody.<p>If you were sharing many individual files it might take a while for FrostWire to stop sharing them.</html>"));
        }
	}

	public void initOptions() {
	    ItemListener[] l = CHECK_BOX.getItemListeners();
	    CHECK_BOX.removeItemListener(l[0]);
        CHECK_BOX.setSelected
        	(SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue());
        CHECK_BOX.addItemListener(l[0]);
        setExplanationText(false);
	}

	public boolean applyOptions() throws IOException {
		if (!isDirty()) {
			return false;
		}
		
		SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.
			setValue(CHECK_BOX.isSelected());
		
		GuiFrostWireUtils.correctIndividuallySharedFiles();
		
        return false;
	}

    public boolean isDirty() {
        return SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue() 
        	!= CHECK_BOX.isSelected();
    }
	
}
