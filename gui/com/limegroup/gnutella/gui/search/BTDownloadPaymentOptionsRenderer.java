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
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.bittorrent.BTDownload;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.util.StringUtils;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.TableActionLabel;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class BTDownloadPaymentOptionsRenderer extends FWAbstractJPanelTableCellRenderer {
    private final static ImageIcon bitcoin_enabled;
    private final static ImageIcon bitcoin_disabled;

    private final static ImageIcon litecoin_enabled;
    private final static ImageIcon litecoin_disabled;

    private final static ImageIcon dogecoin_enabled;
    private final static ImageIcon dogecoin_disabled;

    private final static ImageIcon paypal_enabled;
    private final static ImageIcon paypal_disabled;

    private final TableActionLabel labelBitcoin;
    private final TableActionLabel labelLitecoin;
    private final TableActionLabel labelDogecoin;
    private final TableActionLabel labelPaypal;

    //mutable
    private BTDownload btDownload;
    private BTDownloadPaymentOptionsHolder actionsHolder;

    static {
        bitcoin_enabled = GUIMediator.getThemeImage("bitcoin_enabled");
        bitcoin_disabled = GUIMediator.getThemeImage("bitcoin_disabled");

        litecoin_enabled = GUIMediator.getThemeImage("litecoin_enabled");
        litecoin_disabled = GUIMediator.getThemeImage("litecoin_disabled");

        dogecoin_enabled = GUIMediator.getThemeImage("dogecoin_enabled");
        dogecoin_disabled = GUIMediator.getThemeImage("dogecoin_disabled");
        
        paypal_enabled = GUIMediator.getThemeImage("paypal_enabled");
        paypal_disabled = GUIMediator.getThemeImage("paypal_disabled");
    }

    public BTDownloadPaymentOptionsRenderer() {
        labelBitcoin = new TableActionLabel(bitcoin_enabled, bitcoin_disabled);
        labelLitecoin = new TableActionLabel(litecoin_enabled, litecoin_disabled);
        labelDogecoin = new TableActionLabel(dogecoin_enabled, dogecoin_disabled);
        labelPaypal = new TableActionLabel(paypal_enabled, paypal_disabled);
        setupUI();
    }

    @Override
    protected void updateUIData(Object dataHolder, JTable table, int row, int column) {
        updateUIData((BTDownloadPaymentOptionsHolder) dataHolder, table, row, column);
    }

    private void setupUI() {
        setEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(0,0,2,0));

        //We use "Bitcoin" for the protocol (upper case B), and "bitcoins" for the units of currency (lower case b)
        labelBitcoin.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation in ") + I18n.tr("bitcoins"));
        labelLitecoin.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation in ") + I18n.tr("litecoins"));
        labelDogecoin.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation in ") + I18n.tr("dogecoins"));
        labelPaypal.setToolTipText(I18n.tr("Name your price, Send a Tip or Donation via Paypal"));

        initMouseListeners();
        initComponentsLayout();
    }

    private void initComponentsLayout() {
        setLayout(new MigLayout("gap 2px, fillx, center, insets 5px 5px 5px 5px","[20px!][20px!][20px!][20px!]"));
        add(labelBitcoin, "width 20px!, growx 0, aligny top, push");
        add(labelLitecoin, "width 20px!, growx 0, aligny top");
        add(labelDogecoin, "width 20px!, growx 0, aligny top");
        add(labelPaypal, "width 20px!, growx 0, aligny top, push");
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

        labelDogecoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelDogecoin_mouseReleased(e);
            }
        });

        labelPaypal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                labelPaypal_mouseReleased(e);
            }
        });
    }

    private void updateUIData(BTDownloadPaymentOptionsHolder value, JTable table, int row, int column) {
        try {
            boolean showSolid = mouseIsOverRow(table, row);
            actionsHolder = value;

            btDownload = actionsHolder.getBTDownload();

            PaymentOptions paymentOptions = btDownload.getPaymentOptions();
            boolean gotPaymentOptions = paymentOptions != null;

            labelBitcoin.updateActionIcon(gotPaymentOptions && !StringUtils.isNullOrEmpty(paymentOptions.bitcoin), showSolid);
            labelLitecoin.updateActionIcon(gotPaymentOptions && !StringUtils.isNullOrEmpty(paymentOptions.litecoin), showSolid);
            labelDogecoin.updateActionIcon(gotPaymentOptions && !StringUtils.isNullOrEmpty(paymentOptions.dogecoin), showSolid);
            labelPaypal.updateActionIcon(gotPaymentOptions && !StringUtils.isNullOrEmpty(paymentOptions.paypalUrl), showSolid);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void labelBitcoin_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelBitcoin.isActionEnabled()) {
            //TODO: uxlog action UXStats.instance().log(ACTION CODE HERE);
            System.out.println("Bitcoin click! " + btDownload.getDisplayName());
        } else if (!labelBitcoin.isActionEnabled()) {
            System.out.println("Bitcoin click, but button disabled. " + btDownload.getDisplayName());
        }
    }

    private void labelLitecoin_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelLitecoin.isActionEnabled()) {
            //TODO: uxlog action
            System.out.println("Litecoin click!");
        }
    }

    private void labelDogecoin_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelLitecoin.isActionEnabled()) {
            //TODO: uxlog action
            System.out.println("Dogecoin click!");
        }
    }
    
    private void labelPaypal_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && labelPaypal.isActionEnabled()) {
            //TODO: uxlog action
            System.out.println("Paypal click!");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        cancelEdit();
    }
}