package com.limegroup.gnutella.gui;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;


/**
 * A JFrame that uses LimeWire's icon.
 */
public class LimeJFrame extends JFrame {

    
	/**
     * 
     */
    private static final long serialVersionUID = -8237193535978264490L;

    public LimeJFrame() throws HeadlessException {
        super();
        initialize();
    }

    public LimeJFrame(GraphicsConfiguration arg0) {
        super(arg0);
        initialize();
    }

    public LimeJFrame(String arg0, GraphicsConfiguration arg1) {
        super(arg0, arg1);
        initialize();
    }

    public LimeJFrame(String arg0) throws HeadlessException {
        super(arg0);
        initialize();
    }
    
    private void initialize() {
        ImageIcon limeIcon = GUIMediator.getThemeImage(GUIConstants.FROSTWIRE_64x64_ICON);
        setIconImage(limeIcon.getImage());
        if (OSUtils.isMacOSX()) {
            setupPopupHide();
        }
    }

    // Overrides addNotify() to change to a platform specific icon right afterwards.
    @Override
	public void addNotify() {
		super.addNotify();

		// Replace the Swing icon with a prettier platform-specific one
		SystemUtils.setWindowIcon(this, GUIConstants.FROSTWIRE_EXE_FILE);
	}
    
    private void setupPopupHide() {
        addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent e) {
                for (JPopupMenu menu : getPopups()) {
                    menu.setVisible(false);
                }
            }
        });
    }
    
    private static List<JPopupMenu> getPopups() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();

        List<JPopupMenu> list = new ArrayList<JPopupMenu>(p.length);
        for (MenuElement element : p) {
            if (element instanceof JPopupMenu) {
                list.add((JPopupMenu) element);
            }
        }
        return list;
    }
}
