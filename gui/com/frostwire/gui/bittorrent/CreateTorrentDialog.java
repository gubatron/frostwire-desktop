/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.gui.bittorrent;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentCreator;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentProgressListener;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.TrackersUtil;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class CreateTorrentDialog extends JDialog implements TOTorrentProgressListener {

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

	private static String default_save_dir = SharingSettings.TORRENTS_DIR_SETTING
			.getValueAsString();
	
	private static String comment = I18n.tr("Torrent File Created with FrostWire");
	
	private static int tracker_type = COConfigurationManager.getIntParameter(
			"CreateTorrent.default.trackertype", TT_EXTERNAL);

	// false : singleMode, true: directory
	boolean create_from_dir;
	String singlePath = null;
	String directoryPath = null;
	String savePath = null;

	String trackerURL = TT_EXTERNAL_DEFAULT;

	boolean computed_piece_size = true;
	long manual_piece_size;

	boolean useMultiTracker = false;
	boolean useWebSeed = false;
	private boolean addOtherHashes = false;

	String multiTrackerConfig = "";
	List<List<String>> trackers = new ArrayList<List<String>>();

	String webSeedConfig = "";
	//Map webseeds = new HashMap();

	boolean autoOpen = true;
	boolean autoHost = false;
	boolean permitDHT = true;
	boolean privateTorrent = false;

	TOTorrentCreator creator = null;
	
	private File _saveDir;

	private Container _container;
	private JButton _buttonSelectFile;
	private JTextArea _textTrackers;
	private JCheckBox _checkStartSeeding;
	private JCheckBox _checkUseDHT;
	private JButton _buttonSaveAs;
	private JProgressBar _progressBar;
	private JTextField _textSelectedContent;
	private final Dimension MINIMUM_DIALOG_DIMENSIONS = new Dimension(600, 570);
	private JLabel _labelTrackers;
	private JScrollPane _textTrackersScrollPane;
	private JFileChooser _fileChooser;
	private String _invalidTrackerURL;
	private JFileChooser _saveAsDialog;
	private JButton _buttonClose;

	public CreateTorrentDialog(JFrame frame) {
	    super(frame);
		//don't add edonkey hashes.
		addOtherHashes = false;

		// they had it like this
		trackers.add(new ArrayList<String>());
		
		initComponents();
		setLocationRelativeTo(frame);
	}

	private void initComponents() {
		setTitle(I18n.tr("Create New Torrent"));
		setSize(MINIMUM_DIALOG_DIMENSIONS);
		setMinimumSize(MINIMUM_DIALOG_DIMENSIONS);

		_container = getContentPane();
		_container.setLayout(new GridBagLayout());

		// TORRENT CONTENTS: Add file... Add directory
		initTorrentContents();

		// TORRENT PROPERTIES: Trackers, Start Seeding, Trackerless
		initTorrentProperties();

		// CREATE AND SAVE AS
		initSaveCloseButtons();

		// PROGRESS BAR
		initProgressBar();		

		buildListeners();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
		GUIUtils.addHideAction((JComponent) getContentPane());
	}

	private void initTorrentContents() {
		GridBagConstraints c;
		JPanel torrentContentsPanel = new JPanel(new GridBagLayout());
		Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("Torrent Contents"));
		Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
		Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
		torrentContentsPanel.setBorder(border);
		torrentContentsPanel.putClientProperty(ThemeMediator.SKIN_PROPERTY_DARK_BOX_BACKGROUND, Boolean.TRUE);

		_buttonSelectFile = new JButton(I18n.tr("Select File or Folder..."));
		_buttonSelectFile.setToolTipText(I18n.tr("Click here to select a single file or a folder as the content indexed by your new .torrent"));
		
		final Insets MARGINS = new Insets(5,5,5,5);

		//text that shows what content has been selected
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = MARGINS;
		_textSelectedContent = new JTextField();
		_textSelectedContent.setEditable(false);
		_textSelectedContent.setToolTipText(I18n.tr("These box shows the contents you've selected for your new .torrent.\nEither a file, or the contents of a folder."));
		torrentContentsPanel.add(_textSelectedContent, c);
		
		
		//button to select single files
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = MARGINS;
		c.weightx = 1.0;
		torrentContentsPanel.add(_buttonSelectFile, c);
		
		// add to content pane
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		c.ipady = 50;
		c.ipadx = 50;
		_container.add(torrentContentsPanel, c);
	}

	private void initTorrentProperties() {
		GridBagConstraints c;
		JPanel torrentPropertiesPanel = new JPanel(new GridBagLayout());
		Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("Torrent Properties"));
		Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
        Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
        torrentPropertiesPanel.setBorder(border);
        torrentPropertiesPanel.putClientProperty(ThemeMediator.SKIN_PROPERTY_DARK_BOX_BACKGROUND, Boolean.TRUE);

		// Trackerless
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		_checkUseDHT = new JCheckBox(I18n.tr("Trackerless Torrent (DHT)"),true);
		_checkUseDHT.setToolTipText(I18n.tr("Select this option to create torrents that don't need trackers, completely descentralized. (Recommended)"));
		torrentPropertiesPanel.add(_checkUseDHT, c);

		// Start seeding checkbox
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		_checkStartSeeding = new JCheckBox(I18n.tr("Start seeding"),true);
		_checkStartSeeding.setToolTipText(I18n.tr("Announce yourself as a seed for the content indexed by this torrent as soon as it's created.\nIf nobody is seeding the torrent won't work. (Recommended)"));
		torrentPropertiesPanel.add(_checkStartSeeding, c);

		// Trackers
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10, 5, 5, 5);
		_labelTrackers = new JLabel(I18n.tr(
				"<html><p>Tracker Announce URLs</p><p>(One tracker per line)</p></html>"));
		_labelTrackers.setToolTipText(I18n.tr("Enter a list of valid BitTorrent Tracker Server URLs.\nYour new torrent will be announced to these trackers if you start seeding the torrent."));		
		torrentPropertiesPanel.add(_labelTrackers, c);
		

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.weighty = 1.0;
		c.weightx = 0.7;
		c.insets = new Insets(5, 5, 5, 5);
		_textTrackers = new JTextArea(10, 80);
		_textTrackers.setToolTipText(_labelTrackers.getToolTipText());
		_textTrackers.setLineWrap(false);
		_textTrackersScrollPane = new JScrollPane(_textTrackers);
		torrentPropertiesPanel.add(_textTrackersScrollPane, c);
		
		//by default suggest DHT
		updateTrackerRelatedControlsAvailability(true);

		// add to content pane
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 10, 10, 10);

		_container.add(torrentPropertiesPanel, c);
	}

	private void initSaveCloseButtons() {

		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx =0;
		c.gridy = 2;
		c.weightx = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0, 10, 10, 10);
		_buttonClose = new JButton(I18n.tr("Close"));
		_container.add(_buttonClose, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0, 10, 10, 10);
		_buttonSaveAs = new JButton(I18n.tr("Save torrent as..."));
		_container.add(_buttonSaveAs, c);
	}

	private void initProgressBar() {
		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0, 10, 10, 10);
		c.gridwidth = 2;
		_progressBar = new JProgressBar(0,100);
		_progressBar.setStringPainted(true);
		_container.add(_progressBar, c);
	}

	private void buildListeners() {
		_buttonSelectFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onButtonSelectFile();
			}
		});

		_buttonClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onButtonClose(e);
			}
		});
		
		_buttonSaveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onButtonSaveAs();
			}
		});

		_checkUseDHT.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				boolean useDHT = _checkUseDHT.isSelected();

				updateTrackerRelatedControlsAvailability(useDHT);
			}
		});
		
		_textTrackers.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (_checkUseDHT.isSelected()) {
					_checkUseDHT.setSelected(false);
				}
			}
		});

	}

	private void updateTrackerRelatedControlsAvailability(boolean useDHT) {
		_labelTrackers.setEnabled(!useDHT);
		_textTrackers.setEnabled(!useDHT);
		_textTrackersScrollPane.setEnabled(!useDHT);
		_textTrackersScrollPane.getHorizontalScrollBar().setEnabled(!useDHT);
		_textTrackersScrollPane.getVerticalScrollBar().setEnabled(!useDHT);
		_labelTrackers.setForeground(useDHT ? Color.GRAY : Color.BLACK);
	}

	
	protected void onButtonClose(ActionEvent e) {
	    GUIUtils.getDisposeAction().actionPerformed(e);
	}

	private void initFileChooser(int fileSelectionMode) {
		if (_fileChooser == null) {
			_fileChooser = new JFileChooser();
			_fileChooser.setMultiSelectionEnabled(false);
			_fileChooser.setApproveButtonText(I18n.tr("Select"));
			_fileChooser.setSelectedFile(FileChooserHandler.getLastInputDirectory());
		}

		_fileChooser.setFileSelectionMode(fileSelectionMode);
	}

	private void showFileChooser() {
		int result = _fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {

			File chosenFile = _fileChooser.getSelectedFile();
			FileChooserHandler.setLastInputDirectory(chosenFile);

			setChosenContent(chosenFile);

		} else if (result == JFileChooser.ERROR_OPTION) {
			_textSelectedContent.setText(I18n
					.tr("Unkown error. Try again please."));
		}

	}

	public void setChosenContent(File chosenFile) {
		// if we don't have read permissions on that file/folder...
		if (!chosenFile.canRead()) {
			_textSelectedContent.setText(I18n
					.tr("Error: You can't read on that file/folder."));
			return;
		}

		correctFileSelectionMode(chosenFile);
		setTorrentPathFromChosenFile(chosenFile);
		displayChosenContent(chosenFile);
	}

	private void displayChosenContent(File chosenFile) {
		String prefix = (chosenFile.isFile()) ? "[file] "
				: "[folder] ";
		_textSelectedContent.setText(prefix + chosenFile.getAbsolutePath());
	}

	private void setTorrentPathFromChosenFile(File chosenFile) {
	    File canonicalFile = null;
        try {
            canonicalFile = chosenFile.getCanonicalFile();
        } catch (IOException e) {
        }
		if (canonicalFile.isFile()) {
			directoryPath = null;
			singlePath = chosenFile.getAbsolutePath();
		} else if (canonicalFile.isDirectory()){ 
			directoryPath = chosenFile.getAbsolutePath();
			singlePath = null;
		}
	}

	private void correctFileSelectionMode(File chosenFile) {
		
		//when invoked from the library, we have to init the file chooser.
		if (_fileChooser == null) {
			initFileChooser(JFileChooser.FILES_AND_DIRECTORIES);
		}
		
		// user chose a folder that looks like a file (aka MacOSX .app files)
		if (chosenFile.isDirectory()
				&& _fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
			_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		} 
		
		create_from_dir = chosenFile.isDirectory();//_fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
	}
	
	protected void onContentSelectionButton(int onContentSelectionButton) {
		initFileChooser(onContentSelectionButton);
		showFileChooser();
		revertSaveCloseButtons();
	}

	protected void onButtonSelectFile() {
		onContentSelectionButton(JFileChooser.FILES_AND_DIRECTORIES);
	}

	protected void onButtonSaveAs() {
		//Make sure a readable file or folder has been selected.
		if (singlePath == null && directoryPath == null) {
			JOptionPane.showMessageDialog(this, I18n.tr("Please select a file or a folder.\nYour new torrent will need content to index."),I18n.tr("Something's missing"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//if user chose a folder that's empty
		if (directoryPath != null && new File(directoryPath).listFiles().length == 0) {
			JOptionPane.showMessageDialog(this, I18n.tr("The folder you selected is empty."),I18n.tr("Invalid Folder"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//if it's not tracker-less make sure we have valid tracker urls
		boolean useTrackers = !_checkUseDHT.isSelected();
		if (useTrackers) {
			if (!validateAndFixTrackerURLS()) {
				if (_invalidTrackerURL==null) {
					_invalidTrackerURL="";
				}
				JOptionPane.showMessageDialog(this, I18n.tr("Check again your tracker URL(s).\n"+_invalidTrackerURL),I18n.tr("Invalid Tracker URL\n"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			setTrackerType(TT_EXTERNAL);
		} else {
			trackers.clear();
			setTrackerType(TT_DECENTRAL);
		}
		
		//Whether or not to start seeding this torrent right away
		autoOpen = _checkStartSeeding.isSelected();
		
		//show save as dialog
		if (!showSaveAsDialog()) {
			return;
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (makeTorrent()) {
					revertSaveCloseButtons();
					_progressBar.setString(I18n.tr("Torrent Created."));
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() { 
						    CreateTorrentDialog.this.dispose();
						    UXStats.instance().log(UXAction.SHARING_TORRENT_CREATED_FORMALLY);
						}
					});
					
					
					if (autoOpen) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() { GUIMediator.instance().openTorrentForSeed(new File(savePath), _saveDir); }
						});
					}
				}
			}
		}).start();

	}

	private boolean showSaveAsDialog() {
		if (_saveAsDialog == null) {
			_saveAsDialog = new JFileChooser(SharingSettings.DEFAULT_TORRENTS_DIR);
			
			_saveAsDialog.setFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return I18n.tr("Torrent File");
				}
				
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".torrent");
				}
			});
		}
		
		File suggestedFileName =  null;
		File torrContents = (create_from_dir) ? new File(directoryPath) : new File(singlePath);
		
		//suggestedFileName = new File(torrContents.getParent(),torrContents.getName() + ".torrent");
		suggestedFileName = new File(_saveAsDialog.getSelectedFile(),torrContents.getName() + ".torrent");
		
		_saveAsDialog.setSelectedFile(suggestedFileName);
		
		int result = _saveAsDialog.showSaveDialog(this);

		if (result != JFileChooser.APPROVE_OPTION) {
			savePath = null;
			return false;
		}
		
		savePath = _saveAsDialog.getSelectedFile().getAbsolutePath();	
		
		if (!savePath.endsWith(".torrent")) {
		    savePath = savePath + ".torrent";
		}
		
		return true;
	}

	private boolean validateAndFixTrackerURLS() {
		String trackersText = _textTrackers.getText();
		if (trackersText == null || trackersText.length()==0) {
			return false;
		}
		
		String patternStr = "^(https?|udp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		Pattern pattern = Pattern.compile(patternStr);
		
		
		String[] tracker_urls = trackersText.split("\n");
		List<String> valid_tracker_urls = new ArrayList<String>();
		
		for (String tracker_url : tracker_urls) {
			
			if (tracker_url.trim().equals("")) {
				continue;
			}
			
			//asume http if the user does not specify it
			if (!tracker_url.startsWith("http://") && !tracker_url.startsWith("udp://")) {
				tracker_url = "http://" + tracker_url.trim();
			}
			
			Matcher matcher = pattern.matcher(tracker_url.trim());
			if (!matcher.matches()) {
				_invalidTrackerURL = tracker_url.trim();
				return false;
			} else {
				valid_tracker_urls.add(tracker_url.trim());
			}
		}
		
		fixValidTrackers(valid_tracker_urls);
		
		//update the trackers list of lists
		trackers.clear();
		trackers.add(valid_tracker_urls);
		trackerURL = valid_tracker_urls.get(0);

		useMultiTracker = valid_tracker_urls.size() > 1;
		
		_invalidTrackerURL = null;
		
		return true;
	}

	private void fixValidTrackers(List<String> valid_tracker_urls) {
		//re-write the tracker's text area with corrections
		StringBuilder builder = new StringBuilder();
		for (String valid_tracker_url : valid_tracker_urls) {
			builder.append(valid_tracker_url + "\n");
		}
		
		_textTrackers.setText(builder.toString());
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

	public boolean makeTorrent() {
		disableSaveCloseButtons();

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
								addOtherHashes);

				creator.addListener(this);
				
				torrent = creator.create();
				

			} else {
				// GUBATRON: I THINK THIS else WILL NEVER HAPPEN
				// SINCE UI OPTIONS WILL BE A LOT SIMPLER
				TOTorrentCreator c = TOTorrentFactory
						.createFromFileOrDirWithFixedPieceLength(f, url,
								addOtherHashes, getPieceSizeManual());

				c.addListener(this);

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
			
			_saveDir = save_dir;

			if (useMultiTracker) {
				reportCurrentTask(MessageText.getString("wizard.addingmt"));
				TorrentUtils.listToAnnounceGroups(trackers, torrent);
			}

			// NO WEB SEEDS FOR THIS RELEASE.
			// if (useWebSeed && webseeds.size() > 0) {
			// this.reportCurrentTask(MessageText
			// .getString("wizard.webseed.adding"));
			//
			// Map ws = _wizard.webseeds;
			//
			// List getright = (List) ws.get("getright");
			//
			// if (getright.size() > 0) {
			//
			// for (int i = 0; i < getright.size(); i++) {
			// reportCurrentTask("    GetRight: " + getright.get(i));
			// }
			// torrent.setAdditionalListProperty("url-list",
			// new ArrayList(getright));
			// }
			//
			// List webseed = (List) ws.get("webseed");
			//
			// if (webseed.size() > 0) {
			//
			// for (int i = 0; i < webseed.size(); i++) {
			// reportCurrentTask("    WebSeed: " + webseed.get(i));
			// }
			// torrent.setAdditionalListProperty("httpseeds",
			// new ArrayList(webseed));
			// }
			//
			// }

			reportCurrentTask(MessageText.getString("wizard.savingfile"));

			final File torrent_file = new File(savePath);

			torrent.serialiseToBEncodedFile(torrent_file);
			reportCurrentTask(MessageText.getString("wizard.filesaved"));

//			// if the user wants to start seeding right away
//			if (autoOpen) {
//				waitForCore(TriggerInThread.NEW_THREAD,
//						new AzureusCoreRunningListener() {
//							public void azureusCoreRunning(AzureusCore core) {
//								boolean default_start_stopped = COConfigurationManager
//										.getBooleanParameter("Default Start Torrents Stopped");
//
//								byte[] hash = null;
//								try {
//									hash = torrent.getHash();
//								} catch (TOTorrentException e1) {
//								}
//
//								DownloadManager dm = core
//										.getGlobalManager()
//										.addDownloadManager(
//												torrent_file.toString(),
//												hash,
//												save_dir.toString(),
//												default_start_stopped ? DownloadManager.STATE_STOPPED
//														: DownloadManager.STATE_QUEUED,
//												true, // persistent
//												true, // for seeding
//												null); // no adapter required
//
//								if (!default_start_stopped && dm != null) {
//									// We want this to move to seeding ASAP, so
//									// move it to the top
//									// of the download list, where it will do
//									// the quick check and
//									// move to the seeding list
//									// (the for seeding flag should really be
//									// smarter and verify
//									// it's a seeding torrent and set
//									// appropriately)
//									dm.getGlobalManager().moveTop(
//											new DownloadManager[] { dm });
//								}
//
//								if (autoHost && getTrackerType() != TT_EXTERNAL) {
//
//									try {
//										core.getTrackerHost().hostTorrent(
//												torrent, true, false);
//
//									} catch (TRHostException e) {
//										revertSaveCloseButtons();
//										Logger.log(new LogAlert(
//												LogAlert.REPEATABLE,
//												"Host operation fails", e));
//									}
//								}
//
//							}
//						});
//			}
		} catch (Exception e) {
			
			revertSaveCloseButtons();
			
			if (e instanceof TOTorrentException) {

				TOTorrentException te = (TOTorrentException) e;

				if (te.getReason() == TOTorrentException.RT_CANCELLED) {

					// expected failure, don't log exception
				} else {

					reportCurrentTask(MessageText.getString("wizard.operationfailed"));
					reportCurrentTask(TorrentUtils.exceptionToText(te));
				}
			} else {
				Debug.printStackTrace(e);
				reportCurrentTask(MessageText.getString("wizard.operationfailed"));
				//reportCurrentTask(Debug.getStackTrace(e));
			}

			return false;
		}
		
		return true;
	}

	private void revertSaveCloseButtons() {
		_buttonClose.setEnabled(true);

		_buttonSaveAs.setText(I18n.tr("Save torrent as..."));
		_buttonSaveAs.setEnabled(true);
	}

	/**
	 * Not sure if we need to implement this, I suppose this changed one of the
	 * buttons of the wizard from next|cancel to close
	 */
	private void disableSaveCloseButtons() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_buttonSaveAs.setText(I18n.tr("Saving Torrent..."));
				_buttonSaveAs.setEnabled(false);
				_buttonClose.setEnabled(false);
			}
		});
	}

	public static void waitForCore(final TriggerInThread triggerInThread,
			final AzureusCoreRunningListener l) {
		AzureusCoreFactory
				.addCoreRunningListener(new AzureusCoreRunningListener() {
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
								if (triggerInThread == TriggerInThread.SWT_THREAD) {
									l.azureusCoreRunning(core);
								}
							}
						});
					}
				});
	}

	@Override
	public void reportProgress(final int percent_complete) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("reportProgress: " + percent_complete);
				_progressBar.setValue(percent_complete);
			}
		});		
	}

	@Override
	public void reportCurrentTask(final String task_description) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_progressBar.setString(task_description);
			}
		});
		
	}
	
//	public static void main(String[] args) {
//		AzureusStarter.start();
//		
//		CreateTorrentDialog dlg = new CreateTorrentDialog(null);
//		dlg.setVisible(true);
//		dlg.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent e) {
//				System.out.println("End of Test");
//				AzureusStarter.getAzureusCore().stop();
//				System.out.println("Stopped");
//				System.exit(0);
//			}
//		});
//	}

}