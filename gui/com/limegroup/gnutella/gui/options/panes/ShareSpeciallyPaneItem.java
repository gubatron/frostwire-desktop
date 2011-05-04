package com.limegroup.gnutella.gui.options.panes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.i18n.I18nMarker;

import com.frostwire.GuiFrostWireUtils;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

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
		setExplanationText(false);
		JPanel container = new JPanel(new BorderLayout());
		container.add(explanationLabel, BorderLayout.EAST);
		add(container);
	}
	
	private void setExplanationText(boolean showMessage) {
        if (CHECK_BOX.isSelected()) {
            explanationLabel.setText(I18n.tr("All downloads will be shared. INDIVIDUAL FILE NOTICE (FORGOT PREVIOUS FILES)"));
            if (showMessage) {
                JOptionPane.showMessageDialog(null, "Clear and Prominent message about how individual files are shared");
            }
        } else {
            explanationLabel.setText(I18n.tr("Only downloads in shared folders will be shared. INDIVIDUAL FILE NOTICE"));
        }
	}

	public void initOptions() {
	    ItemListener[] l = CHECK_BOX.getItemListeners();
	    CHECK_BOX.removeItemListener(l[0]);
        CHECK_BOX.setSelected
        	(SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue());
        CHECK_BOX.addItemListener(l[0]);
	}

	public boolean applyOptions() throws IOException {
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
