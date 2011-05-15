package com.limegroup.gnutella.gui.options;

import org.limewire.service.ErrorService;

import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;

/**
 * Static factory class that creates the option panes based on their keys.
 * <p>
 * This class constructs all of the elements of the options window.  To add
 * a new option, this class should be used.  This class allows for options
 * to be added to already existing panes as well as for options to be added
 * to new panes that you can also add here.  To add a new top-level pane,
 * create a new <tt>OptionsPaneImpl</tt> and call the addOption method.
 * To add option items to that pane, add subclasses of
 * <tt>AbstractPaneItem</tt>.
 */
class OptionsPaneFactory {
    
    /**
     * Constructs a new OptionsPaneFactory.
     *
     * Due to intermixing within Saved & Shared pane items, these two need special
     * setups.
     */
    OptionsPaneFactory() {
    }
    
	/**
	 * Creates the options pane for a key. 
	 * @param key keys are listed in {@link OptionsConstructor}.
	 * @return
	 */
	OptionsPane createOptionsPane(OptionsTreeNode node) {
	    Class<? extends AbstractPaneItem>[] clazzes = node.getClasses();
	    if (clazzes != null) {
            final OptionsPane pane = new OptionsPaneImpl(node.getTitleKey());
	        for (Class<? extends AbstractPaneItem> clazz : clazzes) {
                try {
                    pane.add(clazz.newInstance());
                } catch (Exception e) {
                    ErrorService.error(e);
                }
	        }
	        return pane;
	    } else {
	        throw new IllegalArgumentException("no options pane for this key: " + node.getTitleKey());
		}
	}

}
