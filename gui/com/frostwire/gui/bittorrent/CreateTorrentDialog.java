/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

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
import org.limewire.util.OSUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.frostwire.AzureusStarter;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.CreativeCommonsLicense;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.torrent.TorrentInfoManipulator;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;
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
    static final String TT_DECENTRAL_DEFAULT = TorrentUtils.getDecentralisedEmptyURL().toString();

    private static String default_save_dir = SharingSettings.TORRENTS_DIR_SETTING.getValueAsString();

    private static String comment = I18n.tr("Torrent File Created with FrostWire http://www.frostwire.com");

    private static int tracker_type = COConfigurationManager.getIntParameter("CreateTorrent.default.trackertype", TT_EXTERNAL);

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

    //String multiTrackerConfig = "";
    private final List<List<String>> trackers;

    String webSeedConfig = "";
    //Map webseeds = new HashMap();

    boolean autoOpen = true;
    boolean autoHost = false;
    boolean permitDHT = true;
    boolean privateTorrent = false;

    TOTorrentCreator creator = null;

    private File _saveDir;

    private final Container _container;
    private final JTabbedPane _tabbedPane;
    private final JPanel _basicTorrentPane;
    private final JPanel _creativeCommonsPane;
    private final JPanel _paymentsPane;
    private final CreativeCommonsSelectorPanel _ccPanel;
    private final PaymentOptionsPanel _paymentOptionsPanel;

    private LimeTextField _textSelectedContent;
    private JButton _buttonSelectFile;
    private JLabel _labelTrackers;
    private JTextArea _textTrackers;
    private JCheckBox _checkStartSeeding;
    private JCheckBox _checkUseDHT;
    private JLabel _labelWebseeds;
    private JTextArea _textWebseeds;

    private JButton _buttonSaveAs;
    private JProgressBar _progressBar;

    private final Dimension MINIMUM_DIALOG_DIMENSIONS = new Dimension(850, 800);

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
        trackers = new ArrayList<List<String>>();
        trackers.add(new ArrayList<String>());

        _container = getContentPane();
        _tabbedPane = new JTabbedPane();

        _basicTorrentPane = new JPanel();
        _creativeCommonsPane = new JPanel();
        _paymentsPane = new JPanel();
        _ccPanel = new CreativeCommonsSelectorPanel();
        _paymentOptionsPanel = new PaymentOptionsPanel();

        initContainersLayouts();
        initComponents();
        setLocationRelativeTo(frame);
    }

    private void initContainersLayouts() {
        _container.setLayout(new MigLayout("fill"));
        _basicTorrentPane.setLayout(new MigLayout("fill"));
        _creativeCommonsPane.setLayout(new MigLayout("fill"));
        _paymentsPane.setLayout(new MigLayout("fill"));
    }

    private void initTabbedPane() {
        _container.add(_tabbedPane, "gap 5 5 5 5, grow, wrap");
        _creativeCommonsPane.add(_ccPanel, "grow");
        _paymentsPane.add(_paymentOptionsPanel, "grow");

        _tabbedPane.addTab("1. " + I18n.tr("Contents and Tracking"), _basicTorrentPane);
        _tabbedPane.addTab("2. " + I18n.tr("Copyright License"), _creativeCommonsPane);

        if (!SharingSettings.HIDE_PAYMENT_OPTIONS_PANE.getValue()) {
            _tabbedPane.addTab("3. " + I18n.tr("Payments/Tips"), _paymentsPane);
        }
    }

    private void initComponents() {
        initDialogSettings();

        // we do it from the bottom, and dock them south
        initSaveCloseButtons();
        initProgressBar();

        initTorrentContents();
        initTorrentTracking();
        initTabbedPane();

        buildListeners();
    }

    private void initDialogSettings() {
        setTitle(I18n.tr("Create New Torrent"));
        setSize(MINIMUM_DIALOG_DIMENSIONS);
        setMinimumSize(MINIMUM_DIALOG_DIMENSIONS);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        GUIUtils.addHideAction((JComponent) _container);
    }

    private void initTorrentContents() {
        JPanel torrentContentsPanel = new JPanel(new MigLayout("fillx, wrap 1", "[]"));
        GUIUtils.setTitledBorderOnPanel(torrentContentsPanel, I18n.tr("Torrent Contents"));
        _textSelectedContent = new LimeTextField();
        _textSelectedContent.setEditable(false);
        _textSelectedContent.setToolTipText(I18n.tr("These box shows the contents you've selected for your new .torrent.\nEither a file, or the contents of a folder."));
        torrentContentsPanel.add(_textSelectedContent, "north, growx, gap 5 5 0 0, wrap");

        _buttonSelectFile = new JButton(I18n.tr("Select File or Folder..."));
        _buttonSelectFile.setToolTipText(I18n.tr("Click here to select a single file or a folder as the content indexed by your new .torrent"));
        torrentContentsPanel.add(_buttonSelectFile, "width 175px, align right, gaptop 5, gapright 5, gapbottom 5");

        _basicTorrentPane.add(torrentContentsPanel, "growx, wrap");
    }

    private void initTorrentTracking() {
        JPanel torrentTrackingPanel = new JPanel(new MigLayout("fill"));
        GUIUtils.setTitledBorderOnPanel(torrentTrackingPanel, I18n.tr("Tracking"));

        _checkUseDHT = new JCheckBox(I18n.tr("Trackerless Torrent (DHT)"), true);
        _checkUseDHT.setToolTipText(I18n.tr("Select this option to create torrents that don't need trackers, completely descentralized. (Recommended)"));
        torrentTrackingPanel.add(_checkUseDHT, "align left, gapleft 5");

        _checkStartSeeding = new JCheckBox(I18n.tr("Start seeding"), true);
        _checkStartSeeding.setToolTipText(I18n
                .tr("Announce yourself as a seed for the content indexed by this torrent as soon as it's created.\nIf nobody is seeding the torrent won't work. (Recommended)"));
        torrentTrackingPanel.add(_checkStartSeeding, "align right, gapright 10, wrap");

        _labelTrackers = new JLabel("<html><p>" + I18n.tr("Tracker Announce URLs") + "</p><p>(" + I18n.tr("One tracker per line") + ")</p></html>");
        _labelTrackers.setToolTipText(I18n.tr("Enter a list of valid BitTorrent Tracker Server URLs.\nYour new torrent will be announced to these trackers if you start seeding the torrent."));
        torrentTrackingPanel.add(_labelTrackers, "aligny top, pushy, growx 40, gapleft 5, gapright 10, wmin 150px");

        _textTrackers = new JTextArea(10, 80);
        ThemeMediator.fixKeyStrokes(_textTrackers);
        _textTrackers.setToolTipText(_labelTrackers.getToolTipText());
        _textTrackers.setLineWrap(false);
        _textTrackers.setText("udp://tracker.openbittorrent.com:80/announce");
        _textTrackersScrollPane = new JScrollPane(_textTrackers);
        torrentTrackingPanel.add(_textTrackersScrollPane, "gapright 5, gapleft 80, gapbottom 5, hmin 165px, growx 60, growy, wrap");

        _labelWebseeds = new JLabel(I18n.tr("Web Seeds Mirror URLs"));
        _labelWebseeds.setToolTipText(I18n.tr("If these files can be downloaded from the web, enter the URLs of each possible mirror, one per line (GetRight style)."));
        torrentTrackingPanel.add(_labelWebseeds, "aligny top, pushy, gapleft 5, gapright 10, wmin 150px");

        _textWebseeds = new JTextArea(4, 80);
        ThemeMediator.fixKeyStrokes(_textWebseeds);
        torrentTrackingPanel.add(new JScrollPane(_textWebseeds), "gapright 5, gapleft 80, gapbottom 5, hmin 165px, growx 60, growy");

        //suggest DHT by default 
        updateTrackerRelatedControlsAvailability(true);
        _basicTorrentPane.add(torrentTrackingPanel, "grow, push");
    }

    private void initSaveCloseButtons() {
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new MigLayout("fillx"));

        //first button will dock all the way east,
        _buttonSaveAs = new JButton(I18n.tr("Save torrent as..."));
        buttonContainer.add(_buttonSaveAs, "east, gapleft 5");

        //then this one will dock east (west of) the next to the existing component
        _buttonClose = new JButton(I18n.tr("Close"));
        buttonContainer.add(_buttonClose, "east");

        _container.add(buttonContainer, "south, gapbottom 10, gapright 5");
    }

    private void initProgressBar() {
        _progressBar = new JProgressBar(0, 100);
        _progressBar.setStringPainted(true);
        _container.add(_progressBar, "south, growx, gap 5 5 0 5");
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
            _textSelectedContent.setText(I18n.tr("Unkown error. Try again please."));
        }

    }

    public void setChosenContent(File chosenFile) {
        // if we don't have read permissions on that file/folder...
        if (!chosenFile.canRead()) {
            _textSelectedContent.setText(I18n.tr("Error: You can't read on that file/folder."));
            return;
        }

        correctFileSelectionMode(chosenFile);
        setTorrentPathFromChosenFile(chosenFile);
        displayChosenContent(chosenFile);
    }

    private void displayChosenContent(File chosenFile) {
        String prefix = (chosenFile.isFile()) ? "[file] " : "[folder] ";
        _textSelectedContent.setText(prefix + chosenFile.getAbsolutePath());
    }

    private void setTorrentPathFromChosenFile(File chosenFile) {
        File canonicalFile = null;
        try {
            canonicalFile = chosenFile.getCanonicalFile();

            if (canonicalFile.isFile()) {
                directoryPath = null;
                singlePath = chosenFile.getAbsolutePath();
            } else if (canonicalFile.isDirectory()) {
                directoryPath = chosenFile.getAbsolutePath();
                singlePath = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void correctFileSelectionMode(File chosenFile) {

        //when invoked from the library, we have to init the file chooser.
        if (_fileChooser == null) {
            initFileChooser(JFileChooser.FILES_AND_DIRECTORIES);
        }

        // user chose a folder that looks like a file (aka MacOSX .app files)
        if (chosenFile.isDirectory() && _fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
            _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        create_from_dir = chosenFile.isDirectory();
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
            JOptionPane.showMessageDialog(this, I18n.tr("Please select a file or a folder.\nYour new torrent will need content to index."), I18n.tr("Something's missing"), JOptionPane.ERROR_MESSAGE);
            _tabbedPane.setSelectedIndex(0);
            return;
        }

        //if user chose a folder that's empty
        if (directoryPath != null && new File(directoryPath).listFiles().length == 0) {
            JOptionPane.showMessageDialog(this, I18n.tr("The folder you selected is empty."), I18n.tr("Invalid Folder"), JOptionPane.ERROR_MESSAGE);
            _tabbedPane.setSelectedIndex(0);
            return;
        }

        //if it's not tracker-less make sure we have valid tracker urls
        boolean useTrackers = !_checkUseDHT.isSelected();
        if (useTrackers) {
            if (!validateAndFixTrackerURLS()) {
                if (_invalidTrackerURL == null) {
                    _invalidTrackerURL = "";
                }
                JOptionPane.showMessageDialog(this, I18n.tr("Check again your tracker URL(s).\n" + _invalidTrackerURL), I18n.tr("Invalid Tracker URL\n"), JOptionPane.ERROR_MESSAGE);
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
                            public void run() {
                                GUIMediator.instance().openTorrentForSeed(new File(savePath), _saveDir);
                            }
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

        File suggestedFileName = null;
        File torrContents = (create_from_dir) ? new File(directoryPath) : new File(singlePath);

        suggestedFileName = new File(_saveAsDialog.getSelectedFile(), torrContents.getName() + ".torrent");

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
        if (trackersText == null || trackersText.length() == 0) {
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
        COConfigurationManager.setParameter("CreateTorrent.default.trackertype", tracker_type);
    }

    protected String getDefaultSaveDir() {
        return (default_save_dir);
    }

    protected void setDefaultSaveDir(String d) {
        default_save_dir = d;

        COConfigurationManager.setParameter("CreateTorrent.default.save", default_save_dir);
    }

    public boolean makeTorrent() {
        boolean result = false;
        
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

                creator = TOTorrentFactory.createFromFileOrDirWithComputedPieceLength(f, url, addOtherHashes);

                creator.addListener(this);

                torrent = creator.create();

                if (torrent != null) {
                    if (addAvailableWebSeeds(torrent,create_from_dir)) {
                        addAvailablePaymentOptions(torrent);
                        addAvailableCreativeCommonsLicense(torrent);

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

                        reportCurrentTask(MessageText.getString("wizard.savingfile"));

                        final File torrent_file = new File(savePath);

                        torrent.serialiseToBEncodedFile(torrent_file);
                        reportCurrentTask(MessageText.getString("wizard.filesaved"));
                        result = true;
                    } else {
                        result = false;
                        revertSaveCloseButtons();
                        _textWebseeds.selectAll();
                    }
                }
            }

        } catch (Throwable e) {
            result = false;
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
        }

        return result;
    }


    private boolean addAvailableWebSeeds(TOTorrent torrent, boolean isMultiFile) throws Exception {
        boolean result = true;
	    if (_textWebseeds.getText().length() > 0) {
	       List<String> mirrors = Arrays.asList(_textWebseeds.getText().split("\n"));
	       
	       if (mirrors!=null && !mirrors.isEmpty()) {
	           //check just the first file on all mirrors.
	           reportCurrentTask(I18n.tr("Checking Web seed mirror URLs..."));
               for (String mirror : mirrors) {

                   if (isMultiFile && !mirror.endsWith("/")) {
                       fixWebseedMirrorUrl(mirror);
                   }
                   
                   if (!checkWebSeedMirror(mirror, torrent, isMultiFile)) {
                       result = false;
                       showWebseedsErrorMessage(new Exception(getWebSeedTestPath(mirror, torrent, isMultiFile) + " " + I18n.tr("Web seed not reachable.")));
                       break;
                   }
               }
	           
               if (result) {
        	           torrent.setAdditionalListProperty("url-list",mirrors);
        	           result = true;
               }
	       }
	    }
	    
	    return result;
    }

    private void fixWebseedMirrorUrl(final String mirror) {
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                String text = _textWebseeds.getText();
                _textWebseeds.setText(text.replaceAll(mirror,mirror+"/"));
            }
        });
    }

    /**
     * Sends HEAD request to the mirror location along with the test path to see if the file exists.
     * Read http://getright.com/seedtorrent.html to find out how mirror urls are interpreted
     * @param mirror
     * @param testPath
     * @return
     */
    private boolean checkWebSeedMirror(String mirror, TOTorrent torrent, boolean isMultiFile) {
        String urlPath = getWebSeedTestPath(mirror, torrent, isMultiFile);
        HttpClient browser = HttpClientFactory.newDefaultInstance();

        int responseCode = 500;
        try {
            responseCode = browser.head(urlPath,2000);
            System.out.println(responseCode + ": " + urlPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode == 200;
    }

    private String getWebSeedTestPath(String mirror, TOTorrent torrent, boolean isMultiFile) {
        String urlPath = null;
        
        //fix mirror
        if (isMultiFile && !mirror.endsWith("/")) {
            mirror = mirror + "/";
        }

        if (isMultiFile) {
            //path should be <http://mirror-url/> + torrentName + "/" + relativeFilePath
            urlPath = mirror + new String(torrent.getName()) +"/" + torrent.getFiles()[0].getRelativePath();
        } else {
            //url-list should point straight to the file.
            urlPath = mirror;
        }
        return urlPath;
    }

    private void addAvailableCreativeCommonsLicense(final TOTorrent torrent) {
        if (_ccPanel.hasConfirmedRightfulUseOfLicense()) {
            CreativeCommonsLicense ccLicense = _ccPanel.getCreativeCommonsLicense();
            if (ccLicense != null) {
                TorrentInfoManipulator infoManipulator = new TorrentInfoManipulator(torrent);
                infoManipulator.addAdditionalInfoProperty("license", ccLicense.asMap());
            }
        }
    }

    private void addAvailablePaymentOptions(final TOTorrent torrent) {
        if (_paymentOptionsPanel.hasPaymentOptions()) {
            PaymentOptions paymentOptions = _paymentOptionsPanel.getPaymentOptions();
            if (paymentOptions != null) {
                TorrentInfoManipulator infoManipulator = new TorrentInfoManipulator(torrent);
                infoManipulator.addAdditionalInfoProperty("paymentOptions", paymentOptions.asMap());
            }
        }
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
    
    private void showWebseedsErrorMessage(Exception webSeedsException) {
        final Exception e = webSeedsException;
        reportCurrentTask(e.getMessage());
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                GUIMediator.showError(e.getMessage());
            }
        });
    }

    public static void waitForCore(final TriggerInThread triggerInThread, final AzureusCoreRunningListener l) {
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

    public static void main(String[] args) {
        if (OSUtils.isMacOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
        }

        AzureusStarter.start();

        CreateTorrentDialog dlg = new CreateTorrentDialog(null);
        dlg.setVisible(true);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("End of Test");
                AzureusStarter.getAzureusCore().stop();
                System.out.println("Stopped");
                System.exit(0);
            }
        });
    }

}