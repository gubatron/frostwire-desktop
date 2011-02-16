package com.limegroup.gnutella.gui;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * This class constructs an <tt>Initializer</tt> instance that constructs
 * all of the necessary classes for the application.
 */
public class Main {
	
    private static URL CHOSEN_SPLASH_URL = null;

    /** 
	 * Creates an <tt>Initializer</tt> instance that constructs the 
	 * necessary classes for the application.
	 *
	 * @param args the array of command line arguments
	 */
	public static void main(String args[]) {
		if (OSUtils.isMacOSX()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
		System.out.println("1: Main.main("+args+")");
		
	    Frame splash = null;
	    try {
            if (OSUtils.isMacOSX()) {
                // Register GURL to receive AppleEvents, such as magnet links.
                // Use reflection to not slow down non-OSX systems.
                // "GURLHandler.getInstance().register();"
				Class<?> clazz = Class.forName("com.limegroup.gnutella.gui.GURLHandler");
                Method getInstance = clazz.getMethod("getInstance", new Class[0]);
                Object gurl = getInstance.invoke(null, new Object[0]);
                Method register = gurl.getClass().getMethod("register", new Class[0]);
                register.invoke(gurl, new Object[0]);

                if (isOlderThanLeopard()) {
                	System.setProperty("java.nio.preferSelect", 
                			String.valueOf(
                					System.getProperty("java.version").startsWith("1.5")));
                } else {
                	System.setProperty("java.nio.preferSelect", "false");
                }
            }
            
			// show initial splash screen only if there are no arguments
            if (args == null || args.length == 0)
				splash = showInitialSplash();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                    	
                        SubstanceLookAndFeel.setSkin("com.frostwire.gnutella.gui.skin.SeaGlassSkin");
                        UIManager.put("PopupMenuUI", "com.frostwire.gnutella.gui.skin.SkinPopupMenuUI");
                    	UIManager.put("MenuItemUI", "com.frostwire.gnutella.gui.skin.SkinMenuItemUI");
                    	UIManager.put("MenuUI", "com.frostwire.gnutella.gui.skin.SkinMenuUI");
                    	UIManager.put("CheckBoxMenuItemUI", "com.frostwire.gnutella.gui.skin.SkinCheckBoxMenuItemUI");
                    	UIManager.put("MenuBarUI", "com.frostwire.gnutella.gui.skin.SkinMenuBarUI");
                    	UIManager.put("RadioButtonMenuItemUI", "com.frostwire.gnutella.gui.skin.SkinRadioButtonMenuItemUI");
                    	UIManager.put("PopupMenuSeparatorUI", "com.frostwire.gnutella.gui.skin.SkinPopupMenuSeparatorUI");
                    	
                    } catch (Exception e) {
                        System.out.println("Substance engine failed to initialize");
                    }
                }
            });
            
            // load the GUI through reflection so that we don't reference classes here,
            // which would slow the speed of class-loading, causing the splash to be
            // displayed later.
            try {
                Class.forName("com.limegroup.gnutella.gui.GUILoader").getMethod("load", new Class[] { String[].class, Frame.class })
                        .invoke(null, new Object[] { args, splash });
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
	/**
	 * Shows the initial splash window.
	 */
	private static Frame showInitialSplash() {
	    Frame splashFrame = null;
        Image image = null;
        //URL imageURL = ClassLoader.getSystemResource("org/limewire/gui/images/splash.png");
        URL imageURL = getChosenSplashURL();
        if (imageURL != null) {
            image = Toolkit.getDefaultToolkit().createImage(imageURL);
            if (image != null) {
                splashFrame = AWTSplashWindow.splash(image);
            }
        }

	    //System.out.println("Main.java showInitialSplash()");
	    return splashFrame;
    }
	
	/**
	 * Tries to get a random splash every time. It keeps track of the 
	 * last 2 shown splashes to avoid recent collisions.
	 * @return
	 */
	public static final URL getChosenSplashURL() {
	    if (CHOSEN_SPLASH_URL != null)
	        return CHOSEN_SPLASH_URL;
	    
        final String splashPath = "com/frostwire/splash/";
	    final int max_splashes = 20; //20 splashes
	    
	    //different splash every minute... that way it round robins forward in a loop.
	    final int randomSplash = 1+(Calendar.getInstance().get(Calendar.MINUTE) % max_splashes);
	    
	    CHOSEN_SPLASH_URL = ClassLoader.getSystemResource(splashPath + randomSplash + ".jpg");
	    return CHOSEN_SPLASH_URL;
	} 
    
    /** Determines if this is running a Mac OSX lower than Leopard */
    private static boolean isOlderThanLeopard() {
      String version = System.getProperty("os.version");
      StringTokenizer tk = new StringTokenizer(version,".");
      int major = Integer.parseInt(tk.nextToken());
      int minor = Integer.parseInt(tk.nextToken());
      return major==10 && minor < 6;
    }
    
}
