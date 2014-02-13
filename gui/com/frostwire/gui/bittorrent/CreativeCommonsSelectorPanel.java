package com.frostwire.gui.bittorrent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.frostwire.torrent.CreativeCommonsLicense;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;

public class CreativeCommonsSelectorPanel extends JPanel {
    
    private final JCheckBox confirmRightfulUseOfLicense;
    
    private final JLabel authorsNameLabel;
    private final LimeTextField authorsName;
    private final JLabel titleLabel;
    private final LimeTextField title;
    private final JLabel attributionUrlLabel;
    private final LimeTextField attributionUrl;

    
    
    
    public CreativeCommonsSelectorPanel() {
        setLayout(new MigLayout("fill"));
        GUIUtils.setTitledBorderOnPanel(this, I18n
                .tr("Choose a Creative Commons License for this work"));
        
        confirmRightfulUseOfLicense = new JCheckBox("<html><strong>" + I18n.tr("I am the Content Creator of this work or I have been granted the rights to share this content under the following license by the Content Creator(s).") + "</strong></html>");

        authorsNameLabel = new JLabel("<html><b>" + I18n.tr("Author's Name") + "</b></html>");
        authorsName = new LimeTextField();
        authorsName.setToolTipText(I18n.tr("The name of the creator or creators of this work."));
        
        titleLabel = new JLabel("<html><b>" + I18n.tr("Work's Title") + "</b></html>");
        title = new LimeTextField();
        title.setToolTipText(I18n.tr("The name of this work, i.e. the title of a music album, the title of a book, the title of a movie, etc."));
        
        attributionUrlLabel = new JLabel("<html><b>" + I18n.tr("Attribution URL") + "</b></html>");
        attributionUrl = new LimeTextField();
        attributionUrl.setToolTipText(I18n.tr("The Content Creator's website to give attribution about this work if shared by others."));
        
        initListeners();
        initComponents();
    }

    private void initListeners() {
        confirmRightfulUseOfLicense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               onConfirmRightfulUseOfLicenseAction(); 
            }
        });
    }

    private void initComponents() {
        confirmRightfulUseOfLicense.setSelected(false);
        
        add(confirmRightfulUseOfLicense,"growx, north, gapbottom 8, wrap");

        add(authorsNameLabel,"gapbottom 5px, wmin 215px");
        add(titleLabel,"gapbottom 5px, wrap");
        
        add(authorsName, "gapbottom 5px, growx 50, wmin 215px, span 1");
        add(title,"gapbottom 5px, growx 50, span 1, wrap");
        
        JPanel attribPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        attribPanel.add(attributionUrlLabel,"width 110px!, alignx left");
        attribPanel.add(attributionUrl, "alignx left, growx, pushx");
        add(attribPanel,"aligny top, growx, pushy, gapbottom 5px, span 2, wrap");
        
        confirmRightfulUseOfLicense.setSelected(false);
        onConfirmRightfulUseOfLicenseAction();
    }

    protected void onConfirmRightfulUseOfLicenseAction() {
        boolean creativeCommonsEnabled = confirmRightfulUseOfLicense.isSelected();
        authorsNameLabel.setEnabled(creativeCommonsEnabled);
        authorsName.setEnabled(creativeCommonsEnabled);
        titleLabel.setEnabled(creativeCommonsEnabled);
        title.setEnabled(creativeCommonsEnabled);
        attributionUrlLabel.setEnabled(creativeCommonsEnabled);
        attributionUrl.setEnabled(creativeCommonsEnabled);
    }

    public boolean hasCreativeCommonsLicense() {
        return confirmRightfulUseOfLicense.isSelected(); //&&;
    }

    public CreativeCommonsLicense getCreativeCommonsLicense() {
        return null;
    }
}