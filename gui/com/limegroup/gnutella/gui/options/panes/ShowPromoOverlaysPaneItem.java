package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.limewire.i18n.I18nMarker;

import com.frostwire.settings.UpdateManagerSettings;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;

/**
 * Pane to let the user decide wether or not to see the FrostClick promotions.
 */
public final class ShowPromoOverlaysPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("FrostClick Promotions");
    
    public final static String LABEL = I18n.tr("You can enable or disable the FrostClick Promotion on the welcome screen. FrostClick promotions help artists and content creators distribute their content legally and freely to hundreds of thousands of people via FrostWire, BitTorrent and Gnutella. Keep this option on to support file sharing and the future of content distribution.");

	/**
	 * Constant for the key of the locale-specific <tt>String</tt> for the 
	 * frostclick promotions enabled check box label..
	 */
    private final String SHOW_PROMOTION_OVERLAYS_LABEL = 
        I18nMarker.marktr("Enable FrostClick Promotions (highly recommended):");
    
    /**
	 * Constant for the check box that specifies whether to enable or 
	 * disable frostclick promos
	 */
    private final JCheckBox CHECK_BOX = new JCheckBox();
    
    /**
	 * The constructor constructs all of the elements of this
	 * <tt>AbstractPaneItem</tt>.
	 * 
	 * @param key the key for this <tt>AbstractPaneItem</tt> that the
	 *        superclass uses to generate locale-specific keys
	 */
	public ShowPromoOverlaysPaneItem() {
		super(TITLE, LABEL);
		
		LabeledComponent c = new LabeledComponent(SHOW_PROMOTION_OVERLAYS_LABEL,
				CHECK_BOX, LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
		add(c.getComponent());
	}

    /**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Sets the options for the fields in this <tt>PaneItem</tt> when the 
	 * window is shown.
	 */
    public void initOptions() {
        CHECK_BOX.setSelected(UpdateManagerSettings.SHOW_PROMOTION_OVERLAYS.getValue());
    }

    /**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Applies the options currently set in this window, displaying an
	 * error message to the user if a setting could not be applied.
	 *
	 * @throws IOException if the options could not be applied for some reason
	 */
    public boolean applyOptions() throws IOException {
    	UpdateManagerSettings.SHOW_PROMOTION_OVERLAYS.setValue(CHECK_BOX.isSelected());
        return false;
    }
    
    public boolean isDirty() {
        return UpdateManagerSettings.SHOW_PROMOTION_OVERLAYS.getValue() != CHECK_BOX.isSelected();   
    }    
}


