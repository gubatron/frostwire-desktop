/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
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

package com.limegroup.gnutella.gui.search;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.bittorrent.BTDownload;
import com.frostwire.torrent.PaymentOptions;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.TableActionLabel;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class BTDownloadFileActionsRenderer extends FWAbstractJPanelTableCellRenderer {
    private final static ImageIcon bitcoin_enabled;
    private final static ImageIcon bitcoin_disabled;

    private final static ImageIcon litecoin_enabled;
    private final static ImageIcon litecoin_disabled;

    private final static ImageIcon paypal_enabled;
    private final static ImageIcon paypal_disabled;

    private final JLabel labelFileIcon;
    private final TableActionLabel labelBitcoin;
    private final TableActionLabel labelLitecoin;
    private final TableActionLabel labelPaypal;
    private final JLabel labelTitle;

    //mutable
    private BTDownload btDownload;
    private BTDownloadFileActionsHolder actionsHolder;

    static {
        bitcoin_enabled = GUIMediator.getThemeImage("bitcoin_enabled");
        bitcoin_disabled = GUIMediator.getThemeImage("bitcoin_disabled");

        litecoin_enabled = GUIMediator.getThemeImage("litecoin_enabled");
        litecoin_disabled = GUIMediator.getThemeImage("litecoin_disabled");

        paypal_enabled = GUIMediator.getThemeImage("paypal_enabled");
        paypal_disabled = GUIMediator.getThemeImage("paypal_disabled");
    }

    public BTDownloadFileActionsRenderer() {
        labelFileIcon = new JLabel();
        labelBitcoin = new TableActionLabel(bitcoin_enabled, bitcoin_disabled);
        labelLitecoin = new TableActionLabel(litecoin_enabled, litecoin_disabled);
        labelPaypal = new TableActionLabel(paypal_enabled, paypal_disabled);
        labelTitle = new JLabel();
        setupUI();
    }

    @Override
    protected void updateUIData(Object dataHolder, JTable table, int row, int column) {
        updateUIData((BTDownloadFileActionsHolder) dataHolder, table, row, column);
    }

    private void setupUI() {
        setEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(0,0,2,0));

        //We use "Bitcoin" for the protocol (upper case B), and "bitcoins" for the units of currency (lower case b)
        labelBitcoin.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation in ") + I18n.tr("bitcoins"));
        labelLitecoin.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation in ") + I18n.tr("litecoins"));
        labelPaypal.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation via Paypal"));

        initMouseListeners();
        initComponentsLayout();
    }

    private void initComponentsLayout() {
        setLayout(new MigLayout("gap 2px, fillx, left, insets 5px 5px 5px 5px","[][20px!][20px!][20px!]5px[]"));
        add(labelFileIcon, "growx 0, aligny baseline");
        add(labelBitcoin, "width 20px!, growx 0, aligny top");
        add(labelLitecoin, "width 20px!, growx 0, aligny top");
        add(labelPaypal, "width 20px!, growx 0, aligny top");
        add(labelTitle, "wmin 20lp, alignx left, growx, push, aligny center");
    }

    private void initMouseListeners() {
        labelBitcoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelBitcoin_mouseReleased(e);
            }
        });

        labelLitecoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelLitecoin_mouseReleased(e);
            }
        });

        labelPaypal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPaypal_mouseReleased(e);
            }
        });
    }

    private void updateUIData(BTDownloadFileActionsHolder value, JTable table, int row, int column) {
        try {
            boolean showSolid = mouseIsOverRow(table, row);
            actionsHolder = value;
            labelFileIcon.setIcon(actionsHolder.getFileIcon());

            btDownload = actionsHolder.getBTDownload();
            labelTitle.setText(btDownload.getDisplayName());

            PaymentOptions paymentOptions = btDownload.getPaymentOptions();
            boolean gotPaymentOptions = paymentOptions != null;

            labelBitcoin.updateActionIcon(gotPaymentOptions && paymentOptions.bitcoin != null, showSolid);
            labelPaypal.updateActionIcon(gotPaymentOptions && paymentOptions.paypalUrl != null, showSolid);
            labelLitecoin.updateActionIcon(gotPaymentOptions && paymentOptions.litecoin != null, showSolid);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void labelBitcoin_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelBitcoin.isActionEnabled()) {
            //TODO: log action UXStats.instance().log(ACTION CODE HERE);
            System.out.println("Bitcoin click! " + btDownload.getDisplayName());
        } else if (!labelBitcoin.isActionEnabled()) {
            System.out.println("Bitcoin click, but button disabled. " + btDownload.getDisplayName());
        }
    }

    private void labelLitecoin_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelLitecoin.isActionEnabled()) {
            //TODO: log action
            System.out.println("Litecoin click!");
        }
    }

    private void labelPaypal_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelPaypal.isActionEnabled()) {
            //TODO: log action
            System.out.println("Paypal click!");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        cancelEdit();
    }
}