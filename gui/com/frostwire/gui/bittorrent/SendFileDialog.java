package com.frostwire.gui.bittorrent;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentCreator;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentProgressListener;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class SendFileDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 8298555975449164242L;

    private final boolean _singleFileMode;

    public SendFileDialog(JFrame frame, boolean singleFileMode) {
        super(frame);

        _singleFileMode = singleFileMode;

        setupUI();
        setLocationRelativeTo(frame);
    }

    public boolean isSingleFileMode() {
        return _singleFileMode;
    }

    protected void setupUI() {
        setTitle(I18n.tr("Create New Torrent"));
        setSize(new Dimension(400, 400));
        setMinimumSize(new Dimension(400, 400));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                this_windowOpened(e);
            }
        });

        //        _container = getContentPane();
        //        _container.setLayout(new GridBagLayout());
        //
        //        // TORRENT CONTENTS: Add file... Add directory
        //        initTorrentContents();
        //
        //        // TORRENT PROPERTIES: Trackers, Start Seeding, Trackerless
        //        initTorrentProperties();
        //
        //        // CREATE AND SAVE AS
        //        initSaveCloseButtons();
        //
        //        // PROGRESS BAR
        //        initProgressBar();      
        //
        //        buildListeners();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        GUIUtils.addHideAction((JComponent) getContentPane());
    }

    protected void this_windowOpened(WindowEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(_singleFileMode ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            makeTorrentAndDownload(selectedFile.getAbsoluteFile());
        } else if (result == JFileChooser.CANCEL_OPTION) {
            System.out.println("Canceled");
        } else if (result == JFileChooser.ERROR_OPTION) {
            System.out.println("Error");
        }
    }

    protected void torrentCreator_reportCurrentTask(String task_description) {
        System.out.println("CurrentTask:" + task_description);
    }

    protected void torrentCreator_reportProgress(int percent_complete) {
        System.out.println("Progress:" + percent_complete);
    }

    private void makeTorrentAndDownload(final File file) {
        try {

            TOTorrent torrent;

            TOTorrentCreator torrentCreator = TOTorrentFactory.createFromFileOrDirWithComputedPieceLength(file, new URL("http://"), false);

            torrentCreator.addListener(new TOTorrentProgressListener() {
                public void reportProgress(int percent_complete) {
                    torrentCreator_reportProgress(percent_complete);
                }

                public void reportCurrentTask(String task_description) {
                    torrentCreator_reportCurrentTask(task_description);
                }
            });

            torrent = torrentCreator.create();

            TorrentUtils.setDecentralised(torrent);
            TorrentUtils.setDHTBackupEnabled(torrent, true);
            TorrentUtils.setPrivate(torrent, false);
            LocaleTorrentUtil.setDefaultTorrentEncoding(torrent);

            final File torrentFile = new File(SharingSettings.TORRENTS_DIR_SETTING.getValue(), file.getName() + ".torrent");

            torrent.serialiseToBEncodedFile(torrentFile.getAbsoluteFile());

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GUIMediator.instance().openTorrentForSeed(torrentFile, file.getParentFile());
                }
            });

        } catch (Exception e) {
            System.out.println("Error creating torrent");
            e.printStackTrace();
        }
    }
}
