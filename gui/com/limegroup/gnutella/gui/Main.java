package com.limegroup.gnutella.gui;

import java.awt.Frame;
import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.limewire.util.OSUtils;

import com.frostwire.gui.theme.ThemeMediator;

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
	    ThemeMediator.changeTheme();
	    
	    System.setProperty("sun.awt.noerasebackground", "true");

        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
		if (OSUtils.isMacOSX()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
		}
		//System.out.println("1: Main.main("+args+")");
		
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
        URL imageURL = getChosenSplashURL();
        if (imageURL != null) {
            try {
                image = ImageIO.read(imageURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (image != null) {
                splashFrame = AWTSplashWindow.splash(image);
            }
        }

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
	    
	    final String splashPath = "org/limewire/gui/images/app_splash.jpg";
	    
	    CHOSEN_SPLASH_URL = ClassLoader.getSystemResource(splashPath);
	    return CHOSEN_SPLASH_URL;
	}
    
    /** Determines if this is running a Mac OSX lower than Leopard */
    private static boolean isOlderThanLeopard() {
        String version = System.getProperty("os.version");
        StringTokenizer tk = new StringTokenizer(version, ".");
        int major = Integer.parseInt(tk.nextToken());
        int minor = Integer.parseInt(tk.nextToken());
        return major == 10 && minor < 6;
    }
}
