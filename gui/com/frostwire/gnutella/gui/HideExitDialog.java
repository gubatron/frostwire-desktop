package com.frostwire.gnutella.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.ApplicationSettings;

public class HideExitDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 2944990636303224030L;

    private JLabel _label;
    private JCheckBox _checkBox;
    private JButton _buttonYes;
    private JButton _buttonNo;
    
    private boolean _minimizeToTray;

    public HideExitDialog(JFrame frame) {
        super(frame, I18n.tr("Do you want to hide FrostWire?"));
        setupUI();
        setLocationRelativeTo(frame);
    }

    protected void setupUI() {
        setResizable(false);

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c;

        _label = new JLabel(I18n.tr("Do you want to hide FrostWire? Note:[More text]"));
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(_label, c);
        
        _checkBox = new JCheckBox(I18n.tr("Remember my decision"));
        _checkBox.setSelected(true);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(_checkBox, c);

        // yes button
        _buttonYes = new JButton(I18n.tr("Yes"));
        _buttonYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonYes_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 430, 8, 4);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 20;
        getContentPane().add(_buttonYes, c);

        // no button
        _buttonNo = new JButton(I18n.tr("No"));
        _buttonNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonNo_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 8, 6);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 18;
        getContentPane().add(_buttonNo, c);

        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(_buttonYes);
        GUIUtils.addHideAction((JComponent) getContentPane());
    }

    protected void buttonYes_actionPerformed(ActionEvent e) {
        _minimizeToTray = true;
        if (_checkBox.isSelected()) {
            ApplicationSettings.MINIMIZE_TO_TRAY.setValue(true);
            ApplicationSettings.SHOW_HIDE_EXIT_DIALOG.setValue(false);
        }
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    protected void buttonNo_actionPerformed(ActionEvent e) {
        _minimizeToTray = false;
        if (_checkBox.isSelected()) {
            ApplicationSettings.MINIMIZE_TO_TRAY.setValue(false);
            ApplicationSettings.SHOW_HIDE_EXIT_DIALOG.setValue(false);
        }
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    public boolean getMinimizeToTray() {
        return _minimizeToTray;
    }
}
