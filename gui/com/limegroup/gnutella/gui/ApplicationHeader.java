package com.limegroup.gnutella.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;

import com.frostwire.gui.player.AudioPlayerComponent;
import com.frostwire.gui.tabs.Tab;
import com.limegroup.gnutella.gui.GUIMediator.Tabs;
import com.limegroup.gnutella.gui.themes.ThemeMediator;

public class ApplicationHeader extends JPanel {

    /*
    * The property to store the selected icon in.
    */
    private static final String SELECTED = "SELECTED_ICON";
    
    /**
     * The property to store the unselected icon in.
     */
    private static final String DESELECTED = "DESELECTED_ICON";
    
    /**
     * The clicker forwarder.
     */
    private final MouseListener CLICK_FORWARDER = new Clicker();
    

    
    /**
     * The listener for changing the highlighting of buttons.
     */
    private final ItemListener HIGHLIGHTER = new Highlighter();


    private static final long serialVersionUID = 4800214468508213106L;

    /** image used for the background */
    private Image tile;

    /** Button background for selected button */
    private final Image headerButtonBackgroundSelected;
    
    /** Button background for unselected button */
    private final Image headerButtonBackgroundUnselected;
    
    private LogoPanel logoPanel;

    public ApplicationHeader(Map<Tabs, Tab> tabs) {
        setLayout(new BorderLayout());
        
        tile = GUIMediator.getThemeImage("application_header_background").getImage();
        
        headerButtonBackgroundSelected = GUIMediator.getThemeImage("selected_header_button_background").getImage();
        headerButtonBackgroundUnselected = GUIMediator.getThemeImage("unselected_header_button_background").getImage();
        
        setSizes();
        initBackground();

        addTabButtons(tabs);
        addLogoPanel();
        addAudioPlayerComponent();
    }

    private void initBackground() {

    }

    @Override
    protected void paintComponent(Graphics g) {
        //paint tiled background
        for (int i = 0; i < getWidth(); i+=16) {
            g.drawImage(tile,i,0,null);
        }
    }

    private void setSizes() {
        setMinimumSize(new Dimension(0, 54));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 54));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
    }

    private void addAudioPlayerComponent() {
        final JPanel mediaPanel = new AudioPlayerComponent().getMediaPanel(true);
        mediaPanel.setMinimumSize(new Dimension(300,45));
        mediaPanel.setPreferredSize(new Dimension(300,45));
        
        mediaPanel.setBorder(BorderFactory.createEmptyBorder(2,1,6,11));

        final Image audioPlayerBackground = GUIMediator.getThemeImage("audio_player_background").getImage();
        
        @SuppressWarnings("serial")
        final JPanel mediaPanelFrame = new JPanel() {
           @Override
           protected void paintComponent(Graphics g) {
               g.drawImage(audioPlayerBackground, 0, (getHeight()-mediaPanel.getHeight())/2,null);
               super.paintComponent(g);
           }
        };
        
        //mediaPanelFrame.setBorder(BorderFactory.createEmptyBorder(4,1,6,11));
        mediaPanelFrame.setOpaque(false);
        mediaPanelFrame.add(mediaPanel);
        
        
        add(mediaPanelFrame, BorderLayout.LINE_END);
    }

    private void addLogoPanel() {
        add(logoPanel = new LogoPanel(), BorderLayout.CENTER);
    }

    public LogoPanel getLogoPanel() {
        return logoPanel;
    }

    private void addTabButtons(Map<Tabs, Tab> tabs) {

        GridLayout gridLayout = new GridLayout(1, GUIMediator.Tabs.values().length);
        gridLayout.setHgap(8);
        
        JPanel buttonContainer = new JPanel(gridLayout);
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(8,8,8,32));
        buttonContainer.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        
        Font buttonFont = new Font("Helvetica",Font.BOLD,14);

        for (Tabs t : GUIMediator.Tabs.values()) {
            final Tabs lameFinalT = t; //java...
            AbstractButton button = createTabButton(tabs.get(t));
            button.setFont(buttonFont);

            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    GUIMediator.instance().setWindow(lameFinalT);
                }
            });

            group.add(button);
            buttonContainer.add(button);
            
            button.setSelected(t.equals(GUIMediator.Tabs.SEARCH));
        }

        add(buttonContainer, BorderLayout.LINE_START);
    }
    
    private AbstractButton createTabButton(Tab t) {
        Icon icon = t.getIcon();
        Icon disabledIcon = null;
        Icon rolloverIcon = null;

        @SuppressWarnings("serial")
        final AbstractButton button = new JRadioButton(I18n.tr(t.getTitle())) {
            protected void paintComponent(Graphics g) {
                if (isSelected()) {
                    g.drawImage(headerButtonBackgroundSelected, 0, 0, null);
                } else {
                    g.drawImage(headerButtonBackgroundUnselected, 0, 0, null);
                }
                super.paintComponent(g);
            }
        };

        button.putClientProperty(SELECTED, icon);
        if (icon != null) {
            disabledIcon = ImageManipulator.darken(icon);
            rolloverIcon = ImageManipulator.brighten(icon);
        }
        button.putClientProperty(DESELECTED, disabledIcon);
        button.setIcon(disabledIcon);
        button.setRolloverIcon(rolloverIcon);
        button.addItemListener(HIGHLIGHTER);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 7, 5 , 0));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.addMouseListener(CLICK_FORWARDER);
        button.setToolTipText(t.getToolTip());
        
        button.putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
        button.setForeground(ThemeMediator.CURRENT_THEME.getCustomUI().getTabButtonForegroundColor());
        
        Dimension buttonDim = new Dimension(107,34);
        button.setPreferredSize(buttonDim);
        button.setMinimumSize(buttonDim);
        button.setMaximumSize(buttonDim);
        button.setSelected(false);
        
        return button;
    }
    
    /*
    * Listener for ItemEvent, so that the buttons can be highlighted or not
    * when selected (or not).
    */
   private static class Highlighter implements ItemListener {
       public void itemStateChanged(ItemEvent e) {
           AbstractButton button = (AbstractButton) e.getSource();
           //DitherPanel parent = (DitherPanel) button.getParent();
           if (e.getStateChange() == ItemEvent.SELECTED) {
               button.setIcon((Icon) button.getClientProperty(SELECTED));
               //parent.setDithering(true);
           } else {
               button.setIcon((Icon) button.getClientProperty(DESELECTED));
               //parent.setDithering(false);
           }
       }
   }

   /**
    * Forwards click events from a panel to the panel's component.
    */
   private static class Clicker implements MouseListener {
       public void mouseEntered(MouseEvent e) {
           /*
           JComponent c = (JComponent) e.getSource();
           AbstractButton b;
           if (c instanceof AbstractButton) {
               b = (AbstractButton) c;
               c = (JComponent) c.getParent();
           } else {
               b = (AbstractButton) c.getComponent(0);
           }
//           if (!b.isSelected())
//               setIfNotNull(c, "TabbedPane.selected");
  */
       }

       public void mouseExited(MouseEvent e) {
           /**
           JComponent c = (JComponent) e.getSource();
           AbstractButton b;
           if (c instanceof AbstractButton) {
               b = (AbstractButton) c;
               c = (JComponent) c.getParent();
           } else {
               b = (AbstractButton) c.getComponent(0);
           }
           */
//           if (!b.isSelected())
//               setIfNotNull(c, "TabbedPane.background");
       }

       public void mouseClicked(MouseEvent e) {
           JComponent c = (JComponent) e.getSource();
           if (!(c instanceof AbstractButton)) {
               AbstractButton b = (AbstractButton) c.getComponent(0);
               b.doClick();
           }
       }

       public void mousePressed(MouseEvent e) {
       }

       public void mouseReleased(MouseEvent e) {
       }
   }
}