package com.frostwire.gui.library;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.torrent.TOTorrentCreator;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentProgressListener;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class DeviceUploadProgressDialog extends JDialog {

    private static final long serialVersionUID = 4618673762097950544L;

    private static final Log LOG = LogFactory.getLog(DeviceUploadProgressDialog.class);

    private JProgressBar _progressBar;
    private JButton _cancelButton;

    private Container _container;

    public DeviceUploadProgressDialog(JFrame frame) {
        super(frame);

        setupUI();
        setLocationRelativeTo(frame);
    }

    protected void setupUI() {
        setupWindow();
        initProgressBar();
        initCancelButton();
    }

    public void setupWindow() {
        String itemType = I18n.tr("Preparing selection");
        setTitle(itemType + ", " + I18n.tr("please wait..."));

        Dimension prefDimension = new Dimension(512, 100);

        setSize(prefDimension);
        setMinimumSize(prefDimension);
        setPreferredSize(prefDimension);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // TODO
            }
        });

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        GUIUtils.addHideAction((JComponent) getContentPane());

        _container = getContentPane();
        _container.setLayout(new GridBagLayout());
    }

    private void initCancelButton() {
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(10, 0, 10, 10);
        _cancelButton = new JButton(I18n.tr("Cancel"));

        _cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelButton();
            }
        });

        _container.add(_cancelButton, c);
    }

    protected void onCancelButton() {
        // TODO

        dispose();
    }

    private void initProgressBar() {
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(10, 10, 10, 10);
        c.gridwidth = GridBagConstraints.RELATIVE;
        _progressBar = new JProgressBar(0, 100);
        _progressBar.setStringPainted(true);
        _container.add(_progressBar, c);
    }

    private void updateProgressBarText() {
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                _progressBar.setString("Preparing files (" + 50 + " %)");
                _progressBar.setValue(50);
            }
        });
    }
}
