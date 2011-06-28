package com.frostwire.gui.bittorrent;

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

public class SendFileProgressDialog extends JDialog {

	private JProgressBar _progressBar;
	private JButton _cancelButton;
	
    private static final long serialVersionUID = 8298555975449164242L;
    private final boolean _singleFileMode;
	private Container _container;
	private TOTorrentCreator _torrentCreator;
	private int _percent_complete;

    public SendFileProgressDialog(JFrame frame, boolean singleFileMode) {
        super(frame);

        _singleFileMode = singleFileMode;

        setupUI();
        setLocationRelativeTo(frame);
    }

    public boolean isSingleFileMode() {
        return _singleFileMode;
    }

    protected void setupUI() {
        setupWindow();
        initProgressBar();      
        initCancelButton();
    }

	public void setupWindow() {
		String itemType = isSingleFileMode() ? I18n.tr("Preparing selected file") : I18n.tr("Preparing selected folder");
		setTitle(itemType+", "+I18n.tr("please wait..."));
		
		Dimension prefDimension = new Dimension(512, 100);
        
		setSize(prefDimension);
        setMinimumSize(prefDimension);
        setPreferredSize(prefDimension);
        setResizable(false);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                this_windowOpened(e);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
            	if (_torrentCreator != null) {
            		_torrentCreator.cancel();
            	}
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
		c.insets = new Insets(10,0,10,10);
		_cancelButton = new JButton(I18n.tr("Cancel"));
		
		_cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onCancelButton();
			}
		});
		
		_container.add(_cancelButton,c);
	}

	protected void onCancelButton() {
		if (_torrentCreator != null) {
			_torrentCreator.cancel();
		}
		
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
		_progressBar = new JProgressBar(0,100);
		_progressBar.setStringPainted(true);
		_container.add(_progressBar, c);		
	}

	protected void this_windowOpened(WindowEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(_singleFileMode ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle((_singleFileMode) ? I18n.tr("Select the file you want to send") : I18n.tr("Select the folder you want to send"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = fileChooser.getSelectedFile();
            new Thread(new Runnable() {

				@Override
				public void run() {
					makeTorrentAndDownload(selectedFile.getAbsoluteFile());					
				}}).start();
        } else if (result == JFileChooser.CANCEL_OPTION) {
            onCancelButton();
        } else if (result == JFileChooser.ERROR_OPTION) {
            System.out.println("Error");
        }
    }

	private void updateProgressBarText() {
		GUIMediator.safeInvokeLater(new Runnable() {
			@Override
			public void run() {
				_progressBar.setString("Preparing files (" + _percent_complete + " %)");
				_progressBar.setValue(_percent_complete);
			}
		});
	}
	
    protected void torrentCreator_reportProgress(int percent_complete) {
    	_percent_complete = percent_complete;
    	updateProgressBarText();
    }

    private void makeTorrentAndDownload(final File file) {
        try {

            final TOTorrent torrent;

            _torrentCreator = TOTorrentFactory.createFromFileOrDirWithComputedPieceLength(file, new URL("http://"), false);

            _torrentCreator.addListener(new TOTorrentProgressListener() {
                public void reportProgress(int percent_complete) {
                    torrentCreator_reportProgress(percent_complete);
                }

                public void reportCurrentTask(String task_description) {
                }
            });
            
            

            torrent = _torrentCreator.create();

            TorrentUtils.setDecentralised(torrent);
            TorrentUtils.setDHTBackupEnabled(torrent, true);
            TorrentUtils.setPrivate(torrent, false);
            LocaleTorrentUtil.setDefaultTorrentEncoding(torrent);

            final File torrentFile = new File(SharingSettings.TORRENTS_DIR_SETTING.getValue(), file.getName() + ".torrent");

            torrent.serialiseToBEncodedFile(torrentFile.getAbsoluteFile());

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	dispose();
                    GUIMediator.instance().openTorrentForSeed(torrentFile, file.getParentFile());
                    new ShareTorrentDialog(torrent).setVisible(true);
                }
            });

        } catch (final Exception e) {
            e.printStackTrace();
            
            GUIMediator.safeInvokeLater(new Runnable() {

				@Override
				public void run() {
		            _progressBar.setString("There was an error. Make sure the file/folder is not empty.");
				}            	
            });

        }
    }
}
