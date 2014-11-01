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

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.*;
import com.limegroup.gnutella.settings.SharingSettings;
import net.miginfocom.swing.MigLayout;
import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.*;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.TrackersUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gubatron
 * @author aldenml
 *
 */
@SuppressWarnings("serial")
public class CreateTorrentDialog extends JDialog implements TOTorrentProgressListener {

    /**
     * TRACKER TYPES
     */
    //static final int TT_LOCAL = 1; // I Don't Think So
    private static final int TT_EXTERNAL = 2;
    private static final int TT_DECENTRAL = 3;

    private static final String TT_EXTERNAL_DEFAULT = "http://";

    private final static String comment = I18n.tr("Torrent File Created with FrostWire http://www.frostwire.com");

    private static int tracker_type = TT_EXTERNAL;

    // false : singleMode, true: directory
    private boolean create_from_dir;
    private String singlePath = null;
    private String directoryPath = null;
    private String dotTorrentSavePath = null;

    private String trackerURL = TT_EXTERNAL_DEFAULT;
    private boolean useMultiTracker = false;
    private boolean addOtherHashes = false;

    //String multiTrackerConfig = "";
    private final List<List<String>> trackers;

    private boolean autoOpen = true;
    private File _saveDir;

    private final Container _container;
    private final JTabbedPane _tabbedPane;
    private final JPanel _basicTorrentPane;
    private final JPanel _licensesPane;
    private final JPanel _paymentsPane;
    private final CopyrightLicenseSelectorPanel _licenseSelectorPanel;
    private final PaymentOptionsPanel _paymentOptionsPanel;

    private LimeTextField _textSelectedContent;
    private JButton _buttonSelectFile;

    /** Only used on MacOSX, as FileChooser does not support both File and Directory selection. */
    private JButton _buttonSelectFolder;
    private JLabel _labelTrackers;
    private JTextArea _textTrackers;
    private JCheckBox _checkStartSeeding;
    private JCheckBox _checkUseDHT;
    private JTextArea _textWebSeeds;

    private JButton _buttonSaveAs;
    private JProgressBar _progressBar;

    private final Dimension MINIMUM_DIALOG_DIMENSIONS = new Dimension(942, 750);

    private JScrollPane _textTrackersScrollPane;
    private String _invalidTrackerURL;
    private JButton _buttonClose;

    public CreateTorrentDialog(JFrame frame) {
        super(frame);
        addOtherHashes = false;

        // they had it like this
        trackers = new ArrayList<List<String>>();
        trackers.add(new ArrayList<String>());

        _container = getContentPane();
        _tabbedPane = new JTabbedPane();

        _basicTorrentPane = new JPanel();
        _licensesPane = new JPanel();
        _paymentsPane = new JPanel();
        _licenseSelectorPanel = new CopyrightLicenseSelectorPanel();
        _paymentOptionsPanel = new PaymentOptionsPanel();

        initContainersLayouts();
        initComponents();
        setLocationRelativeTo(frame);
        pack();
    }

    private void initContainersLayouts() {
        _container.setLayout(new MigLayout("fill, insets 3px 3px 3px 3px"));
        _basicTorrentPane.setLayout(new MigLayout("fill"));
        _licensesPane.setLayout(new MigLayout("fill"));
        _paymentsPane.setLayout(new MigLayout("fill"));
    }

    private void initTabbedPane() {
        _container.add(_tabbedPane, "grow, pushy, wrap");
        _licensesPane.add(_licenseSelectorPanel, "grow");
        _paymentsPane.add(_paymentOptionsPanel, "grow");
        
        _tabbedPane.addTab("1. " + I18n.tr("Contents and Tracking"), _basicTorrentPane);
        _tabbedPane.addTab("2. " + I18n.tr("Copyright License"), _licensesPane);
        _tabbedPane.addTab("3. " + I18n.tr("Payments/Tips"), _paymentsPane);
    }

    private void initComponents() {
        initDialogSettings();
        
        initTabbedPane();        
        initProgressBar();
        initSaveCloseButtons();

        initTorrentContents();
        initTorrentTracking();
        
        buildListeners();
    }

    private void initDialogSettings() {
        setTitle(I18n.tr("Create New Torrent"));
        setSize(MINIMUM_DIALOG_DIMENSIONS);
        setMinimumSize(MINIMUM_DIALOG_DIMENSIONS);
        setPreferredSize(MINIMUM_DIALOG_DIMENSIONS);
        //setMaximumSize(MINIMUM_DIALOG_DIMENSIONS);
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

        JPanel contentSelectionButtonsPanel = new JPanel(new MigLayout("ins 0, nogrid, fillx, wrap 1",""));
        _buttonSelectFile = new JButton(I18n.tr("Select File"));
        _buttonSelectFile.setToolTipText(I18n.tr("Click here to select a single file as the content indexed by your new .torrent"));
        _buttonSelectFolder = new JButton(I18n.tr("Select Folder"));
        _buttonSelectFolder.setToolTipText(I18n.tr("Click here to select a folder as the content indexed by your new .torrent"));

        contentSelectionButtonsPanel.add(_buttonSelectFile, "align right, width 175px, gaptop 5, gapright 5, gapbottom 5");
        contentSelectionButtonsPanel.add(_buttonSelectFolder, "align right, width 175px, gaptop 5, gapright 5, gapbottom 5");
        torrentContentsPanel.add(contentSelectionButtonsPanel,"width 380px, align right, gaptop 5, gapright 5, gapbottom 5");

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

        _textTrackers = new JTextArea(10, 70);
        ThemeMediator.fixKeyStrokes(_textTrackers);
        _textTrackers.setToolTipText(_labelTrackers.getToolTipText());
        _textTrackers.setLineWrap(false);
        _textTrackers.setText("udp://tracker.openbittorrent.com:80/announce");
        _textTrackersScrollPane = new JScrollPane(_textTrackers);
        torrentTrackingPanel.add(_textTrackersScrollPane, "gapright 5, gapleft 80, gapbottom 5, hmin 165px, growx 60, growy, wrap");

        JLabel _labelWebseeds = new JLabel(I18n.tr("Web Seeds Mirror URLs"));
        _labelWebseeds.setToolTipText(I18n.tr("If these files can be downloaded from the web, enter the URLs of each possible mirror, one per line (GetRight style)."));
        torrentTrackingPanel.add(_labelWebseeds, "aligny top, pushy, gapleft 5, gapright 10, wmin 150px");

        _textWebSeeds = new JTextArea(4, 70);
        ThemeMediator.fixKeyStrokes(_textWebSeeds);
        torrentTrackingPanel.add(new JScrollPane(_textWebSeeds), "gapright 5, gapleft 80, gapbottom 5, hmin 165px, growx 60, growy");

        //suggest DHT by default 
        updateTrackerRelatedControlsAvailability(true);
        _basicTorrentPane.add(torrentTrackingPanel, "grow, push");
    }

    private void initSaveCloseButtons() {
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new MigLayout("fillx, insets 5 5 5 5"));

        //first button will dock all the way east,
        _buttonSaveAs = new JButton(I18n.tr("Save torrent as..."));
        buttonContainer.add(_buttonSaveAs, "pushx, alignx right, gapleft 5");

        //then this one will dock east (west of) the next to the existing component
        _buttonClose = new JButton(I18n.tr("Close"));
        buttonContainer.add(_buttonClose, "alignx right, gapright 5");

        _container.add(buttonContainer, "alignx right, pushx");
    }

    private void initProgressBar() {
        _progressBar = new JProgressBar(0, 100);
        _progressBar.setStringPainted(true);
        _container.add(_progressBar, "growx, gap 5px 0, wrap");
    }

    private void buildListeners() {
        _buttonSelectFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                onButtonSelectFile();
            }
        });

        _buttonSelectFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onButtonSelectFolder();
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

    void onButtonClose(ActionEvent e) {
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    private void showFileChooser(final int fileFolderChoosingMode) {
        FileFilter directoryFilesAllowedFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.isFile();
            }

            @Override
            public String getDescription() {
                String desc = I18n.tr("Select a single file or one directory");
                if (fileFolderChoosingMode == JFileChooser.FILES_ONLY) {
                    desc = I18n.tr("Select a single file");
                } else if (fileFolderChoosingMode == JFileChooser.DIRECTORIES_ONLY) {
                    desc = I18n.tr("Select a single directory");
                }
                return desc;
            }
        };

        File directory = FileChooserHandler.getLastInputDirectory();
        File chosenFile = null;

        if (fileFolderChoosingMode == JFileChooser.FILES_ONLY) {
            chosenFile = FileChooserHandler.getInputFile(GUIMediator.getAppFrame(),
                    I18n.tr("Select a single file"),
                    directory,
                    directoryFilesAllowedFilter);
        } else if (fileFolderChoosingMode == JFileChooser.DIRECTORIES_ONLY) {
            chosenFile = FileChooserHandler.getInputDirectory(GUIMediator.getAppFrame(),
                    I18n.tr("Select a single directory"),
                    I18n.tr("Select folder"),
                    directory,
                    directoryFilesAllowedFilter);
        }

        if (chosenFile != null) {
            FileChooserHandler.setLastInputDirectory(chosenFile); //might not be necessary.
            setChosenContent(chosenFile, fileFolderChoosingMode);
        }
    }

    public void setChosenContent(File chosenFile, int fileChoosingMode) {
        // if we don't have read permissions on that file/folder...
        if (!chosenFile.canRead()) {
            _textSelectedContent.setText(I18n.tr("Error: You can't read on that file/folder."));
            return;
        }

        chosenFile = correctFileSelectionMode(chosenFile, fileChoosingMode);
        setTorrentPathFromChosenFile(chosenFile);
        displayChosenContent(chosenFile);
    }

    private void displayChosenContent(File chosenFile) {
        String prefix = (chosenFile.isFile()) ? "[file] " : "[folder] ";
        _textSelectedContent.setText(prefix + chosenFile.getAbsolutePath());
    }

    private void setTorrentPathFromChosenFile(File chosenFile) {
        File canonicalFile;
        try {
            canonicalFile = chosenFile.getCanonicalFile();

            if (canonicalFile.isFile()) {
                create_from_dir = false;
                directoryPath = null;
                singlePath = chosenFile.getAbsolutePath();
            } else if (canonicalFile.isDirectory()) {
                create_from_dir = true;
                directoryPath = chosenFile.getAbsolutePath();
                singlePath = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private File correctFileSelectionMode(File chosenFile, int fileSelectionMode) {
        if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY && chosenFile.isFile()) {
            chosenFile = chosenFile.getParentFile();
        }

        create_from_dir = chosenFile.isDirectory();

        return chosenFile;
    }


    void onContentSelectionButton(int onContentSelectionButton) {
        showFileChooser(onContentSelectionButton);
        revertSaveCloseButtons();
    }

    void onButtonSelectFile() {
            onContentSelectionButton(JFileChooser.FILES_ONLY);
    }

    void onButtonSelectFolder() {
        onContentSelectionButton(JFileChooser.DIRECTORIES_ONLY);
    }

    void onButtonSaveAs() {
        //Make sure a readable file or folder has been selected.
        if (singlePath == null && directoryPath == null) {
            JOptionPane.showMessageDialog(this, I18n.tr("Please select a file or a folder.\nYour new torrent will need content to index."), I18n.tr("Something's missing"), JOptionPane.ERROR_MESSAGE);
            _tabbedPane.setSelectedIndex(0);
            return;
        }

        //if user chose a folder that's empty
        File[] fileArray;
        if (directoryPath != null &&
            ((fileArray = new File(directoryPath).listFiles()) != null) &&
            fileArray.length == 0) {
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
                                GUIMediator.instance().openTorrentForSeed(new File(dotTorrentSavePath), _saveDir);
                            }
                        });
                    }
                }
            }
        }).start();

    }

    private boolean showSaveAsDialog() {
        FileFilter saveAsFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return I18n.tr("Torrent File");
            }

            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".torrent");
            }
        };

        File torrContents = (create_from_dir) ? new File(directoryPath) : new File(singlePath);
        File suggestedFile = new File(SharingSettings.DEFAULT_TORRENTS_DIR, torrContents.getName() + ".torrent");

        File chosenFile = FileChooserHandler.getSaveAsFile(GUIMediator.getAppFrame(),
                I18n.tr("Save .torrent"),
                suggestedFile,
                saveAsFilter);

        if (chosenFile == null) {
            dotTorrentSavePath = null;
            return false;
        }

        dotTorrentSavePath = chosenFile.getAbsolutePath();

        if (!dotTorrentSavePath.endsWith(".torrent")) {
            dotTorrentSavePath = dotTorrentSavePath + ".torrent";
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
            builder.append(valid_tracker_url);
            builder.append("\n");
        }

        _textTrackers.setText(builder.toString());
    }

    int getTrackerType() {
        return (tracker_type);
    }

    void setTrackerType(int type) {
        tracker_type = type;
    }

    boolean makeTorrent() {
        boolean result = false;
        
        disableSaveCloseButtons();

        int tracker_type = getTrackerType();

        if (tracker_type == TT_EXTERNAL) {
            TrackersUtil.getInstance().addTracker(trackerURL);
        }

        File f = new File((create_from_dir) ? directoryPath : singlePath);

        try {
            URL url = new URL(trackerURL);
            final TOTorrent torrent;

            TOTorrentCreator creator = TOTorrentFactory.createFromFileOrDirWithComputedPieceLength(f, url, addOtherHashes);
            creator.addListener(this);
            torrent = creator.create();

            if (torrent != null) {
                if (addAvailableWebSeeds(torrent, create_from_dir)) {
                    _saveDir = f.getParentFile();

                    addAvailablePaymentOptions(torrent);
                    addAvailableCopyrightLicense(torrent);

                    if (tracker_type == TT_DECENTRAL) {
                        TorrentUtils.setDecentralised(torrent);
                    }

                    torrent.setComment(comment);
                    boolean permitDHT = true; //TODO: If available on jlibtorrent, put in advanced settings.
                    TorrentUtils.setDHTBackupEnabled(torrent, permitDHT);
                    boolean privateTorrent = false;  //TODO: If available on jlibtorrent, put in advanced settings.
                    TorrentUtils.setPrivate(torrent, privateTorrent);
                    LocaleTorrentUtil.setDefaultTorrentEncoding(torrent);

                    if (useMultiTracker) {
                        reportCurrentTask(MessageText.getString("wizard.addingmt"));
                        TorrentUtils.listToAnnounceGroups(trackers, torrent);
                    }

                    reportCurrentTask(MessageText.getString("wizard.savingfile"));
                    final File torrent_file = new File(dotTorrentSavePath);
                    torrent.serialiseToBEncodedFile(torrent_file);
                    reportCurrentTask(MessageText.getString("wizard.filesaved"));
                    result = true;
                } else {
                    result = false;
                    revertSaveCloseButtons();
                    _textWebSeeds.selectAll();
                }
            }
        } catch (Throwable e) {
            result = false;
            revertSaveCloseButtons();
            if (e instanceof TOTorrentException) {
                TOTorrentException te = (TOTorrentException) e;
                if (te.getReason() != TOTorrentException.RT_CANCELLED) {
                    reportCurrentTask(MessageText.getString("wizard.operationfailed"));
                    reportCurrentTask(TorrentUtils.exceptionToText(te));
                }
            } else {
                Debug.printStackTrace(e);
                reportCurrentTask(MessageText.getString("wizard.operationfailed"));
            }
        }

        return result;
    }


    private boolean addAvailableWebSeeds(TOTorrent torrent, boolean isMultiFile) {
        boolean result = true;
	    if (_textWebSeeds.getText().length() > 0) {
	       List<String> mirrors = Arrays.asList(_textWebSeeds.getText().split("\n"));
	       
	       if (!mirrors.isEmpty()) {
	           //check just the first file on all mirrors.
	           reportCurrentTask(I18n.tr("Checking Web seed mirror URLs..."));
               for (String mirror : mirrors) {

                   if (isMultiFile && !mirror.endsWith("/")) {
                       fixWebSeedMirrorUrl(mirror);
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

    private void fixWebSeedMirrorUrl(final String mirror) {
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                String text = _textWebSeeds.getText();
                _textWebSeeds.setText(text.replaceAll(mirror, mirror + "/"));
            }
        });
    }

    /**
     * Sends HEAD request to the mirror location along with the test path to see if the file exists.
     * Read http://getright.com/seedtorrent.html to find out how mirror urls are interpreted
     */
    private boolean checkWebSeedMirror(String mirror, TOTorrent torrent, boolean isMultiFile) {
        String urlPath = getWebSeedTestPath(mirror, torrent, isMultiFile);
        HttpClient browser = HttpClientFactory.newInstance();

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
        String urlPath;
        
        //fix mirror
        if (isMultiFile && !mirror.endsWith("/")) {
            mirror = mirror + "/";
        }

        if (isMultiFile) {
            //path should be <http://mirror-url/> + torrentName + "/" + relativeFilePath
            urlPath = mirror + new String(torrent.getName()) + "/" + torrent.getFiles()[0].getRelativePath();
        } else {
            //url-list should point straight to the file.
            urlPath = mirror;
        }
        return urlPath;
    }

    private void addAvailableCopyrightLicense(final TOTorrent torrent) {
        if (_licenseSelectorPanel.hasConfirmedRightfulUseOfLicense()) {
            CopyrightLicenseBroker license = _licenseSelectorPanel.getLicenseBroker();
            if (license != null) {
                TorrentInfoManipulator infoManipulator = new TorrentInfoManipulator(torrent);
                infoManipulator.addAdditionalInfoProperty("license", license.asMap());
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
        _buttonSaveAs.setText(I18n.tr("Save torrent as..."));
        _buttonSaveAs.setEnabled(true);
        _buttonClose.setEnabled(true);
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

    /*
    public static void main(String[] args) {
        
        if (OSUtils.isMacOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
        }

       // ThemeMediator.changeTheme();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception ex) {
            ex.printStackTrace();
        }

        CreateTorrentDialog dlg = new CreateTorrentDialog(null);
        dlg.setVisible(true);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("End of Test");
                System.out.println("Stopped");
                System.exit(0);
            }
        });
    }
    */
}