package com.frostwire.gui.library;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class DeviceUploadDialog extends JDialog {

    private static final long serialVersionUID = 4618673762097950544L;

    private JLabel labelDetails;
    private JButton buttonCancel;

    private Container container;

    public DeviceUploadDialog(JFrame frame) {
        super(frame);

        setupUI();
        setLocationRelativeTo(frame);
    }

    protected void setupUI() {
        setupWindow();
        initLabelDetails();
        initCancelButton();
    }

    public void setupWindow() {
        String title = I18n.tr("Upload to device");
        setTitle(title);

        Dimension prefDimension = new Dimension(400, 100);

        setSize(prefDimension);
        setMinimumSize(prefDimension);
        setPreferredSize(prefDimension);
        setResizable(false);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        GUIUtils.addHideAction((JComponent) getContentPane());

        container = getContentPane();
        container.setLayout(new GridBagLayout());
    }

    private void initCancelButton() {
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(10, 0, 10, 10);
        buttonCancel = new JButton(I18n.tr("Cancel"));

        buttonCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelButton();
            }
        });

        container.add(buttonCancel, c);
    }

    protected void onCancelButton() {
        dispose();
    }

    private void initLabelDetails() {
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(10, 10, 10, 10);
        c.gridwidth = GridBagConstraints.RELATIVE;
        labelDetails = new JLabel();
        container.add(labelDetails, c);

        labelDetails.setText(I18n.tr("Waiting for device authorization..."));
    }
}
