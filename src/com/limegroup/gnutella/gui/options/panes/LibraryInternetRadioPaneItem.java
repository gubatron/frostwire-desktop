package com.limegroup.gnutella.gui.options.panes;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;

public final class LibraryInternetRadioPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Internet Radio Stations");

    public final static String LABEL = I18n.tr("You can restore the default internet radio stations.");

    private JLabel numRadioStationsLabel;

    private long numRadioStations = 0;

    /**
     * The constructor constructs all of the elements of this
     * <tt>AbstractPaneItem</tt>.
     * 
     * @param key
     *            the key for this <tt>AbstractPaneItem</tt> that the
     *            superclass uses to generate locale-specific keys
     */
    public LibraryInternetRadioPaneItem() {
        super(TITLE, LABEL);

        Font font = new Font("dialog", Font.BOLD, 12);
        numRadioStationsLabel = new JLabel();

        numRadioStationsLabel.setFont(font);

        LabeledComponent numRadioStationsComp = new LabeledComponent(I18n.tr("Total Radio Stations"), numRadioStationsLabel);

        add(getVerticalSeparator());

        JButton resetButton = new JButton(I18n.tr("Restore default Radio Stations"));
        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIMediator.safeInvokeLater(new Runnable() {
                    @Override
                    public void run() {
                        restoreDefaultRadioStations();
                        initOptions();
                    }
                });
            };
        });

        add(numRadioStationsComp.getComponent());

        add(getVerticalSeparator());

        add(resetButton);
    }

    protected void restoreDefaultRadioStations() {
        LibraryMediator.instance().restoreDefaultRadioStations();
    }

    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Sets the options for the fields in this <tt>PaneItem</tt> when the 
     * window is shown.
     */
    public void initOptions() {
        numRadioStations = LibraryMediator.instance().getTotalRadioStations();
        numRadioStationsLabel.setText(String.valueOf(numRadioStations));
    }

    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Applies the options currently set in this window, displaying an
     * error message to the user if a setting could not be applied.
     *
     * @throws IOException if the options could not be applied for some reason
     */
    public boolean applyOptions() throws IOException {
        return false;
    }

    public boolean isDirty() {
        return false;
    }
}
