package com.limegroup.gnutella.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;


public class DonationButtons extends JPanel {
    
    private static final Color FONT_COLOR = new Color(0x1f3545);
    
    public DonationButtons() {
        setLayout(new MigLayout("insets 0, nogrid, ltr, gapx 6","","[align center]"));
        add(createDonateLabel());
        add(new DonationButton("$1","http://www.frostwire.com/?donate1",I18n.tr("Support FrostWire development with a USD $1 donation")));
        add(new DonationButton("$5","http://www.frostwire.com/?donate5",I18n.tr("Support FrostWire development with a USD $5 donation")));
        add(new DonationButton("$10","http://www.frostwire.com/?donate10",I18n.tr("Support FrostWire development with a USD $10 donation")));
        add(new DonationButton("$25","http://www.frostwire.com/?donate25",I18n.tr("Support FrostWire development with a USD $25 donation")));
    }

    private JLabel createDonateLabel() {
        Font labelFont = new Font("Helvetica", Font.BOLD, 10);
        JLabel donateLabel = new JLabel(I18n.tr("Donate")+":");
        donateLabel.setForeground(FONT_COLOR);
        donateLabel.setFont(labelFont);
        return donateLabel;
    }

    private class DonationButton extends JButton {

        public DonationButton(String text, String donationURL, String tipText) {
            initComponent(text, donationURL, tipText);
        }

        private void initComponent(String text, final String donationURL, String tipText) {
            Font buttonFont = new Font("Helvetica", Font.BOLD, 9);
            setMinimumSize(new Dimension(38,22));
            setMargin(new Insets(0,0,0,0));
            setOpaque(false);
            setPreferredSize(new Dimension(40,22));
            setMaximumSize(new Dimension(40,22));
            setFont(buttonFont);
            setForeground(FONT_COLOR);
            setBackground(new Color(0xedf1f4));
            setText(text);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);
            setToolTipText(tipText);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUIMediator.openURL(donationURL);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            // TODO Move this code to a UI if necessary, for now KIFSS
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 2, 2);
            super.paintComponent(g);
        }
        
        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(new Color(0xe4e8ea));
            g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 2, 2);
        }
        
    }
    
    
}
