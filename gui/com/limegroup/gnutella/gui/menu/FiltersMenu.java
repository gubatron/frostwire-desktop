package com.limegroup.gnutella.gui.menu;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.OpenLinkAction;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.settings.ContentSettings;

/**
 * Filters menu.
 */
public class FiltersMenu extends AbstractMenu {

    public FiltersMenu() {
        super(I18n.tr("Filter&s"));
        
        addMenuItem(new ConfigureOptionsAction(OptionsConstructor.CONTENT_FILTER_KEY,
                I18n.tr("&Configure Content Filters"), 
                I18n.tr("Configure FrostWire\'s Content Filtering Options")));
        addMenuItem(new OpenLinkAction(ContentSettings.LEARN_MORE_URL, 
                I18n.tr("&Learn More..."),
                I18n.tr("Learn More about FrostWire\'s Content Filtering System")));
    }

}
