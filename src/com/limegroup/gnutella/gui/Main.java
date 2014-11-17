/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;


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

        //System.out.println("Main::main() arch=" + System.getProperty("os.arch") + " is_64_bit=" + OSUtils.isMachineX64()) ;
        if (!OSUtils.isMachineX64())
        {
            GUIMediator.safeInvokeLater(new Runnable() {
                @Override
                public void run() {
                    String error = I18n.tr("<br>Sorry, you need a 64-bit computer to use FrostWire</b>");
                    GUIMediator.showError("<html>" + error + "</html>");
                    System.exit(0);
                }
            });
            return;
        }

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
                if (isOlderThanLeopard()) {
                    System.setProperty("java.nio.preferSelect", String.valueOf(System.getProperty("java.version").startsWith("1.5")));
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
                Class.forName("com.limegroup.gnutella.gui.GUILoader").getMethod("load", new Class[] { String[].class, Frame.class }).invoke(null, new Object[] { args, splash });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Throwable e) {
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
