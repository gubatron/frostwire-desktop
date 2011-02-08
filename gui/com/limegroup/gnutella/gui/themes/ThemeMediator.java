package com.limegroup.gnutella.gui.themes;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MultiLineLabel;
import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;


/**
 * Class that mediates between themes and FrostWire.
 */
public class ThemeMediator {
    
    /**
     * <tt>List</tt> of <tt>ThemeObserver</tt> classes to notify of
     * ui components of theme changes.
     */
    private static final List<ThemeObserver> THEME_OBSERVERS = new LinkedList<ThemeObserver>();
    
    public static void updateComponentHierarchy() {
        SwingUtilities.updateComponentTreeUI(GUIMediator.getMainOptionsComponent());
        TipOfTheDayMediator.instance().updateComponentTreeUI();
        SwingUtilities.updateComponentTreeUI(GUIMediator.getAppFrame());
        NotifyUserProxy.instance().updateUI();
        updateThemeObservers();
    }

    /**
     * Adds the specified <tt>ThemeObserver</tt> instance to the list of
     * <tt>ThemeObserver</tt>s that should be notified whenever the theme
     * changes.
     *
     * @param observer the <tt>ThemeObserver</tt> to add to the notification
     *  list
     */
    public static void addThemeObserver(ThemeObserver observer) {
	    THEME_OBSERVERS.add(observer);
    }

    /**
     * Removes the specified <tt>ThemeObserver</tt> instance from the list
     * of <tt>ThemeObserver</tt>s.  This is necessary to allow the removed
     * component to be garbage-collected.
     *
     * @param observer the <tt>ThemeObserver</tt> to remove from the
     *  notification list
     */
    public static void removeThemeObserver(ThemeObserver observer) {
        THEME_OBSERVERS.remove(observer);
    }

    /**
     * Updates all theme observers.
     */
    public static void updateThemeObservers() {
        for(ThemeObserver curObserver : THEME_OBSERVERS) {
    	    curObserver.updateTheme();
        }

        GUIMediator.getMainOptionsComponent().validate();
        GUIMediator.getAppFrame().validate();
    }

    /**
     * Shows a dialog with an error message about selecting a theme.
     */
    public static void showThemeError(String name) {
        Dimension size = new Dimension(300, 100);
        
        final JDialog d = new JDialog(GUIMediator.getAppFrame());
        d.setModal(true);
        d.setResizable(false);
        d.setTitle(I18n.tr("Invalid Skin"));
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        BoxPanel body = new BoxPanel(BoxPanel.Y_AXIS);
        
        JPanel text = new BoxPanel(BoxPanel.Y_AXIS);
        MultiLineLabel label = new MultiLineLabel(
						  I18n.tr("The skin you selected is out of date. Please download the latest version of the skin."), 250);
    	label.setFont(new Font("Dialog", Font.BOLD, 12));
    	text.add(Box.createVerticalGlue());
    	text.add(GUIUtils.center(label));
    	text.add(Box.createVerticalGlue());
    		
    	BoxPanel buttons = new BoxPanel(BoxPanel.X_AXIS);
    	
    	JButton getNew = new JButton(I18n.tr(
								   "Get New Skins"));
        JButton later = new JButton(I18n.tr(
								  "Later"));
        getNew.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			GUIMediator.openURL("http://dl.frostwire.com/skins/4.17.0/");
    		    d.dispose();
    		    d.setVisible(false);
    		}
	    });
        later.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    		    d.dispose();
    		    d.setVisible(false);
    		}
	    });
    		
    	buttons.add(getNew);
    	buttons.add(GUIMediator.getHorizontalSeparator());
    	buttons.add(later);
    		
    	body.add(text);
        body.add(buttons);
        body.setPreferredSize(size);
        d.getContentPane().add(body);
        d.pack();
        d.setLocationRelativeTo(GUIMediator.getAppFrame());
        d.setVisible(true);
    }
    
    /*private void changeTheme() {
        SwingUtils.invokeNowOrLater(new Runnable() {
            public void run() {

                final boolean wasDecoratedByOS = !_frame
                        .isUndecorated();

                try {

                    UIManager.setLookAndFeel(item.getlookAndFeel());

                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }

                    boolean canBeDecoratedByLAF = UIManager
                            .getLookAndFeel()
                            .getSupportsWindowDecorations();
                    if (canBeDecoratedByLAF == wasDecoratedByOS) {
                        boolean wasVisible = _frame.isVisible();

                        _frame.setVisible(false);
                        _frame.dispose();
                        if (!canBeDecoratedByLAF
                                || _wasOriginallyDecoratedByOS) {
                            // see the java docs under the method
                            // JFrame.setDefaultLookAndFeelDecorated(boolean
                            // value) for description of these 2 lines:
                            _frame.setUndecorated(false);
                            _frame.getRootPane()
                                    .setWindowDecorationStyle(
                                            JRootPane.NONE);

                        } else {
                            _frame.setUndecorated(true);
                            _frame.getRootPane()
                                    .setWindowDecorationStyle(
                                            JRootPane.FRAME);
                        }
                        _frame.setVisible(wasVisible);
                        // wasDecoratedByOS = !frame.isUndecorated();
                    }

                } catch (Exception e) {
                }
            }
        });
    }*/
}
