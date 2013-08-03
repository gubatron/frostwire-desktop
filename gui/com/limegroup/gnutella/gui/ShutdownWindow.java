package com.limegroup.gnutella.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.frostwire.gui.theme.ThemeMediator;

class ShutdownWindow extends JDialog {
    
    /**
     * 
     */
    private static final long serialVersionUID = 446845150731872693L;
    private ImageIcon backgroundImage;

    public ShutdownWindow() {
        super(GUIMediator.getAppFrame());
        backgroundImage = ResourceManager.getImageFromResourcePath("org/limewire/gui/images/app_shutdown.jpg");
        setResizable(false);
        setTitle(I18n.tr("Shutting down FrostWire..."));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JComponent pane = (JComponent)getContentPane();
        
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(I18n.tr("Please wait while FrostWire shuts down..."));
        label.setFont(new Font("Dialog", Font.PLAIN, 12));
        pane.add(new JLabel(backgroundImage),c);
        pane.add(label, c);
        
        
        
        
        JProgressBar bar = new LimeJProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(false);
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.CENTER;
        pane.add(bar, c);
        
        ((JComponent)getContentPane()).setPreferredSize(new Dimension(800, 500));
        pack();
    }
    
    @Override
    public void paintComponents(Graphics g) {
        System.out.println(backgroundImage.getImage());
        g.drawImage(backgroundImage.getImage(),0,0,null);
    }
    
    public static void main(String[] args) {
        ShutdownWindow window = new ShutdownWindow();
        window.setVisible(true);
    }
}