package com.frostwire.bittorrent;

import java.awt.Component;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.ConfigurationDefaults;
import org.gudy.azureus2.core3.config.impl.ConfigurationParameterNotFoundException;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.logging.LogAlert;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentCreator;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.tracker.host.TRHostException;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.TrackersUtil;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledTextField;
import com.limegroup.gnutella.gui.LimeTextField;
import com.limegroup.gnutella.settings.SharingSettings;

public class CreateTorrentDialog extends JDialog {

	/**
	 * TRACKER TYPES
	 */
	static final int TT_LOCAL = 1; // I Don't Think So
	static final int TT_EXTERNAL = 2;
	static final int TT_DECENTRAL = 3;
	
	public enum TriggerInThread {
		SWT_THREAD, ANY_THREAD, NEW_THREAD
	}

	static final String TT_EXTERNAL_DEFAULT = "http://";

	/** dht:// */
	static final String TT_DECENTRAL_DEFAULT = TorrentUtils
			.getDecentralisedEmptyURL().toString();

	private static String default_open_dir = COConfigurationManager
			.getStringParameter("CreateTorrent.default.open", "");

	private static String default_save_dir = SharingSettings.TORRENTS_DIR_SETTING
			.getValueAsString();// COConfigurationManager.getStringParameter(
								// "CreateTorrent.default.save", "" );
	private static String comment = I18n
			.tr("Torrent File Created with FrostWire");
	private static int tracker_type = COConfigurationManager.getIntParameter(
			"CreateTorrent.default.trackertype", TT_EXTERNAL);

	// false : singleMode, true: directory
	boolean create_from_dir;
	String singlePath = "";
	String directoryPath = "";
	String savePath = "";

	String trackerURL = TT_EXTERNAL_DEFAULT;

	boolean computed_piece_size = true;
	long manual_piece_size;

	boolean useMultiTracker = false;
	boolean useWebSeed = false;
	private boolean addOtherHashes = false;

	String multiTrackerConfig = "";
	List trackers = new ArrayList();

	String webSeedConfig = "";
	Map webseeds = new HashMap();

	boolean autoOpen = false;
	boolean autoHost = false;
	boolean permitDHT = true;
	boolean privateTorrent = false;

	TOTorrentCreator creator = null;

	private LabeledTextField trackersTextField;
	LimeTextField tt;
	private Container _container;
	private JButton _buttonSelectFile;
	private JButton _buttonSelectFolder;
	private JTextArea _textTrackers;
	private JCheckBox _check_start_seeding;
	private JCheckBox _checkUseDHT;
	private JButton _buttonSaveAs;
	private Component _progressBar;
	private JTextField _textSelectedContent;
	private final Dimension MINIMUM_DIALOG_DIMENSIONS = new Dimension(600,570);;

	public CreateTorrentDialog() {
		try {
			addOtherHashes = ConfigurationDefaults.getInstance()
					.getBooleanParameter("CreateTorrent.default.addhashes");
		} catch (ConfigurationParameterNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//they had it like this
		trackers.add(new ArrayList());

		trackersTextField = new LabeledTextField(I18n.tr("Trackers"), 80);

		// IDEA: If user has URL(s) in clipboard, autofill trackers textfield

		// trackersTextField.setText();
		// trackerURL = Utils.getLinkFromClipboard(display,false);
		initComponents();
	}
	
	private void initComponents() {
		setTitle(I18n.tr("Create New Torrent"));
		setSize(MINIMUM_DIALOG_DIMENSIONS);
		setMinimumSize(MINIMUM_DIALOG_DIMENSIONS);
		
		_container = getContentPane();		
		_container.setLayout(new GridBagLayout());
		
		GridBagConstraints c = null;
		
		//TORRENT CONTENTS: Add file...  Add directory
		initTorrentContents();
		
		//TORRENT PROPERTIES: Trackers, Start Seeding, Trackerless
		initTorrentProperties();
		
		//CREATE AND SAVE AS
		initSaveButton();
		
		//PROGRESS BAR
		initProgressBar();
		
		buildListeners();
	}


	private void initTorrentContents() {
		GridBagConstraints c;
		JPanel torrentContentsPanel = new JPanel(new GridBagLayout());
		torrentContentsPanel.setBorder(BorderFactory.createTitledBorder(I18n.tr("Torrent Contents")));
		
		_buttonSelectFile = new JButton(I18n.tr("Select a file..."));
		_buttonSelectFolder = new JButton("Select a folder...");
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		torrentContentsPanel.add(_buttonSelectFile,c);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		torrentContentsPanel.add(_buttonSelectFolder, c);
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		_textSelectedContent = new JTextField();
		_textSelectedContent.setEnabled(false);
		_textSelectedContent.setEditable(false);
		torrentContentsPanel.add(_textSelectedContent,c);

		//add to content pane
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10,10,10,10);
		c.ipady = 50;
		c.ipadx = 50;
		_container.add(torrentContentsPanel,c);
	}

	private void initTorrentProperties() {
		GridBagConstraints c;
		JPanel torrentPropertiesPanel = new JPanel(new GridBagLayout());
		torrentPropertiesPanel.setBorder(BorderFactory.createTitledBorder(I18n.tr("Torrent Properties")));
		
		//Trackerless
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth=2;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		_checkUseDHT = new JCheckBox(I18n.tr("Trackerless Torrent (DHT)"));
		torrentPropertiesPanel.add(_checkUseDHT,c);

		//Start seeding checkbox
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth=2;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		_check_start_seeding = new JCheckBox(I18n.tr("Start seeding"));
		torrentPropertiesPanel.add(_check_start_seeding,c);
		
		//Trackers
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10,5,5,5);
		torrentPropertiesPanel.add(new JLabel("<html><p>Tracker Announce URLs</p><p>(Enter one per line)</p></html>"),c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.weighty = 1.0;
		c.weightx = 0.7;
		c.insets = new Insets(5,5,5,5);
		_textTrackers = new JTextArea(10,80);
		_textTrackers.setLineWrap(false);
		torrentPropertiesPanel.add(new JScrollPane(_textTrackers),c);
		
		//add to content pane
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,10,10,10);
		
		_container.add(torrentPropertiesPanel,c);
	}
	
	private void initSaveButton() {
		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0,10,10,10);
		_buttonSaveAs = new JButton(I18n.tr("Save torrent as..."));
		_container.add(_buttonSaveAs,c);
	}
	
	private void initProgressBar() {
		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0,10,10,10);
		_progressBar = new JProgressBar();
		_container.add(_progressBar,c);
	}

	private void buildListeners() {
		_buttonSelectFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("select file action performed uninplemented.");
			}
		});

		_buttonSelectFolder.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("select folder action performed uninplemented.");
			}
		});
		
		_buttonSaveAs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("save as action performed uninplemented.");
			}
		});

	}

	protected int getTrackerType() {
		return (tracker_type);
	}

	protected void setPieceSizeComputed() {
		computed_piece_size = true;
	}

	public boolean getPieceSizeComputed() {
		return (computed_piece_size);
	}

	protected void setPieceSizeManual(long _value) {
		computed_piece_size = false;
		manual_piece_size = _value;
	}

	protected long getPieceSizeManual() {
		return (manual_piece_size);
	}

	protected void setTrackerType(int type) {
		tracker_type = type;

		COConfigurationManager.setParameter(
				"CreateTorrent.default.trackertype", tracker_type);
	}

	protected String getDefaultSaveDir() {
		return (default_save_dir);
	}

	protected void setDefaultSaveDir(String d) {
		default_save_dir = d;

		COConfigurationManager.setParameter("CreateTorrent.default.save",
				default_save_dir);
	}

	protected void setAddOtherHashes(boolean o) {
		addOtherHashes = o;

		COConfigurationManager.setParameter("CreateTorrent.default.addhashes",
				addOtherHashes);

	}

	protected boolean getAddOtherHashes() {
		return (addOtherHashes);
	}

	public void makeTorrent() {

		int tracker_type = getTrackerType();

		if (tracker_type == TT_EXTERNAL) {
			TrackersUtil.getInstance().addTracker(trackerURL);
		}

		File f;

		if (create_from_dir) {
			f = new File(directoryPath);
		} else {
			f = new File(singlePath);
		}

		try {
			URL url = new URL(trackerURL);

			final TOTorrent torrent;

			if (getPieceSizeComputed()) {

				creator = TOTorrentFactory
						.createFromFileOrDirWithComputedPieceLength(f, url,
								getAddOtherHashes());

				//GUBATRON: NOT SURE IF WE NEED THIS.
				//That addListener Method comes from Wizard and the Listener class has a closed()
				//method that is called when this window is called. I guess they want
				//the torrent creator to notify this window so that it's closed.

				torrent = creator.create();

			} else {
				//GUBATRON: I THINK THIS else WILL NEVER HAPPEN
				//SINCE UI OPTIONS WILL BE A LOT SIMPLER
				TOTorrentCreator c = TOTorrentFactory
						.createFromFileOrDirWithFixedPieceLength(f, url,
								getAddOtherHashes(),
								getPieceSizeManual());

				//c.addListener(this);

				torrent = c.create();
			}

			if (tracker_type == TT_DECENTRAL) {

				TorrentUtils.setDecentralised(torrent);
			}

			torrent.setComment(comment);

			TorrentUtils.setDHTBackupEnabled(torrent, permitDHT);

			TorrentUtils.setPrivate(torrent, privateTorrent);

			LocaleTorrentUtil.setDefaultTorrentEncoding(torrent);

			// mark this newly created torrent as complete to avoid rechecking
			// on open

			final File save_dir;

			if (create_from_dir) {
				save_dir = f;
			} else {
				save_dir = f.getParentFile();
			}

			if (useMultiTracker) {
				//TODO: (MIGHT DO) Notify the UI Thread of current progress
				//this.reportCurrentTask(MessageText.getString("wizard.addingmt"));
				
				TorrentUtils.listToAnnounceGroups(trackers, torrent);
			}

			//NO WEB SEEDS FOR THIS RELEASE.
//			if (useWebSeed && webseeds.size() > 0) {
//				this.reportCurrentTask(MessageText
//						.getString("wizard.webseed.adding"));
//
//				Map ws = _wizard.webseeds;
//
//				List getright = (List) ws.get("getright");
//
//				if (getright.size() > 0) {
//
//					for (int i = 0; i < getright.size(); i++) {
//						reportCurrentTask("    GetRight: " + getright.get(i));
//					}
//					torrent.setAdditionalListProperty("url-list",
//							new ArrayList(getright));
//				}
//
//				List webseed = (List) ws.get("webseed");
//
//				if (webseed.size() > 0) {
//
//					for (int i = 0; i < webseed.size(); i++) {
//						reportCurrentTask("    WebSeed: " + webseed.get(i));
//					}
//					torrent.setAdditionalListProperty("httpseeds",
//							new ArrayList(webseed));
//				}
//
//			}

//			this.reportCurrentTask(MessageText.getString("wizard.savingfile"));

			final File torrent_file = new File(savePath);

			torrent.serialiseToBEncodedFile(torrent_file);
			//this.reportCurrentTask(MessageText.getString("wizard.filesaved"));
			switchToClose();

			//if the user wants to start seeding right away
			if (autoOpen) {
				waitForCore(TriggerInThread.NEW_THREAD,
						new AzureusCoreRunningListener() {
							public void azureusCoreRunning(AzureusCore core) {
								boolean default_start_stopped = COConfigurationManager
										.getBooleanParameter("Default Start Torrents Stopped");

								byte[] hash = null;
								try {
									hash = torrent.getHash();
								} catch (TOTorrentException e1) {
								}

								DownloadManager dm = core
										.getGlobalManager()
										.addDownloadManager(
												torrent_file.toString(),
												hash,
												save_dir.toString(),
												default_start_stopped ? DownloadManager.STATE_STOPPED
														: DownloadManager.STATE_QUEUED,
												true, // persistent
												true, // for seeding
												null); // no adapter required

								if (!default_start_stopped && dm != null) {
									// We want this to move to seeding ASAP, so
									// move it to the top
									// of the download list, where it will do
									// the quick check and
									// move to the seeding list
									// (the for seeding flag should really be
									// smarter and verify
									// it's a seeding torrent and set
									// appropriately)
									dm.getGlobalManager().moveTop(
											new DownloadManager[] { dm });
								}

								if (autoHost && getTrackerType() != TT_EXTERNAL) {

									try {
										core.getTrackerHost().hostTorrent(
												torrent, true, false);

									} catch (TRHostException e) {
										Logger.log(new LogAlert(
												LogAlert.REPEATABLE,
												"Host operation fails", e));
									}
								}

							}
						});
			}
		} catch (Exception e) {
			if (e instanceof TOTorrentException) {

				TOTorrentException te = (TOTorrentException) e;

				if (te.getReason() == TOTorrentException.RT_CANCELLED) {

					// expected failure, don't log exception
				} else {

//					reportCurrentTask(MessageText
//							.getString("wizard.operationfailed"));
//					reportCurrentTask(TorrentUtils.exceptionToText(te));
				}
			} else {
				Debug.printStackTrace(e);
//				reportCurrentTask(MessageText
//						.getString("wizard.operationfailed"));
//				reportCurrentTask(Debug.getStackTrace(e));
			}

			switchToClose();
		}
	}

	/**
	 * Not sure if we need to implement this, I suppose this changed one of the buttons of the wizard from
	 * next|cancel to close
	 */
	private void switchToClose() {
		// TODO Auto-generated method stub
		System.out.println("CreateTorrentDialog.switchToClose() UNIMPLEMENTED.");
		
	}
	
	public static void waitForCore(final TriggerInThread triggerInThread,
			final AzureusCoreRunningListener l) {
		AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
			public void azureusCoreRunning(final AzureusCore core) {
				if (triggerInThread == TriggerInThread.ANY_THREAD) {
					l.azureusCoreRunning(core);
				} else if (triggerInThread == TriggerInThread.NEW_THREAD) {
					new AEThread2("CoreWaiterInvoke", true) {
						public void run() {
							l.azureusCoreRunning(core);
						}
					}.start();
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// TODO: Need to detect cancel (can't rely on shell status since it may never open)
//						if (shell != null && !shell.isDisposed()) {
//							shell.dispose();
//							shell = null;
//						}

						if (triggerInThread == TriggerInThread.SWT_THREAD) {
							l.azureusCoreRunning(core);
						}
					}
				});
			}
		});

		if (!AzureusCoreFactory.isCoreRunning()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showWaitWindow();
				}
			});
		}

	}

	//TODO
	protected static void showWaitWindow() {
//		if (shell != null && !shell.isDisposed()) {
//			shell.forceActive();
//			return;
//		}
		
//		shell = UIFunctionsManagerSWT.getUIFunctionsSWT().showCoreWaitDlg();
	}
	
	public static void main(String[] args) {
		CreateTorrentDialog dlg = new CreateTorrentDialog();
		dlg.setVisible(true);
		dlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("End of Test");
				System.exit(0);
			}
		});
	}
}
