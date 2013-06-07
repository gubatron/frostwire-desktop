package com.limegroup.gnutella.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
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
        //MigLayout lesson: Using px explicitly as the unit does make a big difference.
        add(new DonationButton("$1","https://gumroad.com/l/COllj",I18n.tr("Support FrostWire development with a USD $1/mo donation")),"w 26px!, h 18px!");
        add(new DonationButton("$5","https://gumroad.com/l/Ffdv",I18n.tr("Support FrostWire development with a USD $5/mo donation")),"w 26px!, h 18px!");
        add(new DonationButton("$10","https://gumroad.com/l/moCT",I18n.tr("Support FrostWire development with a USD $10/mo donation")),"w 30px!, h 18px!");
        add(new DonationButton("$25","https://gumroad.com/l/DrUTE",I18n.tr("Support FrostWire development with a USD $25/mo donation")),"w 30px!, h 18px!");
        add(new DonationButton("฿","bitcoin:14F6JPXK2fR5b4gZp3134qLRGgYtvabMWL",I18n.tr("Support FrostWire development with a ฿itcoin donation")),"w 26px!, h 18px!");
        add(new DonationButton("Ł","litecoin:LLW2rNAXbAt41SGjk8GZjbi3uYT2snjbq1",I18n.tr("Support FrostWire development with a Łitecoin donation")),"w 26px!, h 18px!");
    }

    private JLabel createDonateLabel() {
        Font labelFont = new Font("Helvetica", Font.BOLD, 12);
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
            Font buttonFont = new Font("Helvetica", Font.BOLD, 12);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(null);
            setContentAreaFilled(false);
            setOpaque(false);
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
            g.setColor(new Color(0xe4e8ea));
            g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 2, 2);
            super.paintComponent(g);
        }
        
        
    }
    
    
}
