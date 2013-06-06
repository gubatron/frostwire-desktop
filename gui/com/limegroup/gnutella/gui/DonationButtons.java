package com.limegroup.gnutella.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class DonationButtons extends JPanel {

    private class DonationButton extends JButton {

        public DonationButton(String text, String donationURL, String tipText) {
            initComponent(text, donationURL, tipText);
        }

        private void initComponent(String text, final String donationURL, String tipText) {
            Font buttonFont = new Font("Helvetica", Font.BOLD, 10);
            setMinimumSize(new Dimension(26,18));
            setPreferredSize(new Dimension(26,18));
            setMaximumSize(new Dimension(36,18));
            setFont(buttonFont);
            setForeground(new Color(0x1f3545));
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
