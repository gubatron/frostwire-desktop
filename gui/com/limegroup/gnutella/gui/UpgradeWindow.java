package com.limegroup.gnutella.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.limewire.i18n.I18nMarker;

/**
 * Dialog for upgrading.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class UpgradeWindow {	

	/**
	 * Constant handle to the <tt>JDialog</tt> that contains about 
	 * information.
	 */
	private JDialog dialog;

	/**
	 * Constant handle to the main <tt>BoxPanel</tt> instance.
	 */
	private final BoxPanel MAIN_PANEL = new BoxPanel(BoxPanel.Y_AXIS);

	/**
	 * Constant handle to the <tt>ImageIcon</tt> to use for the about 
	 * window.
	 */
	private final ImageIcon ICON = 
		GUIMediator.getThemeImage("searching");

	/**
	 * Constant dimension for the dialog.
	 */
	private final Dimension DIALOG_DIMENSION = new Dimension(380, 180);

	/**
	 * Constructs the elements of the about window.
	 */
	UpgradeWindow(boolean pro) {
        final String titleKey;
	    final String messageKey;
	    final String upgradeKey;
	    final String upgradeURL;
	    final String upgradeWhy;
	    final boolean randomButtons;
	    final boolean useButtonTips;
	    final boolean useLime;
	    int labelWidth = 300;
	    if(pro) {
	        titleKey = I18nMarker.marktr("Upgrade FrostWire");
	        messageKey = I18nMarker
                    .marktr("For Turbo-Charged downloads, get FrostWire. Thank you for helping keep the Internet open by running FrostWire.");
	        upgradeKey = I18nMarker.marktr("Upgrade to FrostWire?");
	        upgradeURL = "http://www.frostwire.com/";
	        upgradeWhy = "http://www.frostwire.com/forum";
	        randomButtons = true;
	        useButtonTips = true;
	        useLime = true;
        } else {
            titleKey = I18nMarker.marktr("Upgrade Your Java");
            messageKey = I18nMarker
                    .marktr("FrostWire has detected that you are using an out-of-date version of Java. FrostWire looks and acts much better with newer versions of Java. Features include support for scroll-wheels on mice, a Windows XP look, and many other improvements.");
            upgradeKey = I18nMarker.marktr("Would you like to get your free Java upgrade?");
            upgradeURL = getJavaUpgradeURL();
            upgradeWhy = "http://www.frostwire.com";
            randomButtons = false;
            useButtonTips = false;
            useLime = false;
        }
	    
		dialog = new JDialog(GUIMediator.getAppFrame());
		dialog.setModal(true);
		dialog.setResizable(true);
		dialog.setTitle(I18n.tr(titleKey));

		// set the main panel's border
		Border border = BorderFactory.createEmptyBorder(12,6,6,6);

		BoxPanel topPanel = new BoxPanel(BoxPanel.X_AXIS);
		if(useLime) {
		    topPanel.add(new JLabel(ICON));
		    labelWidth -= ICON.getIconWidth();
        }

	    topPanel.add(GUIMediator.getHorizontalSeparator());
	    topPanel.add(GUIMediator.getHorizontalSeparator());
	    topPanel.add(GUIMediator.getHorizontalSeparator());
        
		String labelStart = 
		    I18n.tr(messageKey);
		String labelEnd = 
		    I18n.tr(upgradeKey);
		MultiLineLabel label = new MultiLineLabel(labelStart, labelWidth);
		label.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel label2 = new JLabel(labelEnd);
		label2.setFont(new Font("Dialog", Font.PLAIN, 12));

		BoxPanel labelPanel = new BoxPanel(BoxPanel.Y_AXIS);
		labelPanel.add(Box.createVerticalGlue());
		labelPanel.add(label);
		labelPanel.add(Box.createVerticalStrut(10));
		BoxPanel questionPanel = new BoxPanel(BoxPanel.X_AXIS);
		questionPanel.add(Box.createVerticalGlue());
		questionPanel.add(label2);
		questionPanel.add(Box.createVerticalGlue());

		topPanel.add(labelPanel);

		MAIN_PANEL.setBorder(border);
		MAIN_PANEL.setPreferredSize(DIALOG_DIMENSION);
		dialog.setSize(DIALOG_DIMENSION);
		
		ActionListener upgradeDialogListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIMediator.openURL(upgradeURL);
				dialog.dispose();
				dialog.setVisible(false);
			}
		};
		ActionListener whyDialogListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIMediator.openURL(upgradeWhy);
				dialog.dispose();
				dialog.setVisible(false);
			}
		};

		ActionListener closeDialogListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				dialog.setVisible(false);
			}
		};

		ActionListener[] listeners = {
			upgradeDialogListener,
			whyDialogListener,
			closeDialogListener
		};
		
		// Shuffle the buttons around.
		List<ActionListener> shuffled = Arrays.asList(listeners);
		if(randomButtons)
		    Collections.shuffle(shuffled);
		listeners = shuffled.toArray(listeners);
		// now make buttonKeys & buttonTips accordingly.
		String[] buttonKeys = new String[listeners.length];
		String[] buttonTips = new String[listeners.length];
		for(int i = 0; i < listeners.length; i++) {
		    if(listeners[i] == upgradeDialogListener) {
		        buttonKeys[i] = I18nMarker.marktr("Yes");
		        if(useButtonTips)
		            buttonTips[i] = I18nMarker.marktr("Get FrostWire Now"); //for PRO 
		    } else if (listeners[i] == whyDialogListener) {
		        buttonKeys[i] = I18nMarker.marktr("Why");
		        if(useButtonTips)
		            buttonTips[i] = I18nMarker.marktr("What does frostwire give me?");
		    } else if (listeners[i] == closeDialogListener) {
		        buttonKeys[i] = I18nMarker.marktr("Later");
		        if(useButtonTips)
		            buttonTips[i] = I18nMarker.marktr("Get FrostWire Later");
		    }
		}

		ButtonRow buttons = 
		    new ButtonRow(buttonKeys, buttonTips, listeners);

		MAIN_PANEL.add(topPanel);
		MAIN_PANEL.add(questionPanel);
		MAIN_PANEL.add(Box.createVerticalStrut(10));
		MAIN_PANEL.add(buttons);
		dialog.getContentPane().add(MAIN_PANEL);
		dialog.pack();
	}
	
	/**
	 * Builds the URL for a java upgrade.
	 */
	private String getJavaUpgradeURL() {
	    // valid locales are: en, de, es, fr, it, ja, ko, sv, zh
	    Locale loc = GUIMediator.getLocale();
	    String language = loc.getLanguage();
	    if(!language.equals("en") &&
	       !language.equals("de") &&
	       !language.equals("es") &&
	       !language.equals("fr") &&
	       !language.equals("it") &&
	       !language.equals("ja") &&
	       !language.equals("ko") &&
	       !language.equals("sv") &&
	       !language.equals("zh"))
            language = "en";
            
        return "http://www.frostwire.com/javabuttonu/" + language;
    }

	/**
	 * Displays the "PRO" dialog window to the user.
	 */
	static void showProDialog() {
	    UpgradeWindow window = new UpgradeWindow(true);
		window.dialog.setLocationRelativeTo(GUIMediator.getAppFrame());
		try {
		    window.dialog.setVisible(true);
        } catch(InternalError ie) {
            // happens occasionally, ignore.
        }
	}
	
	/**
	 * Displays the 'Java' dialog window to the user.
	 */
	static void showJavaDialog() {
	    UpgradeWindow window = new UpgradeWindow(false);
		window.dialog.setLocationRelativeTo(GUIMediator.getAppFrame());
		try {
		    window.dialog.setVisible(true);
        } catch(InternalError ie) {
            // happens occasionally, ignore.
        }
	}
}
