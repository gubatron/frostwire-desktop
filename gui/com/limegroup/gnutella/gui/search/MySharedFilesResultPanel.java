package com.limegroup.gnutella.gui.search;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.limewire.io.IpPort;
import org.limewire.io.NetworkUtils;

import com.frostwire.gnutella.gui.skin.SkinPopupMenu;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.IncompleteFileDesc;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.Response;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.URLLabel;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.gui.tables.DataLine;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.library.SharingUtils;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.settings.ChatSettings;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.SSLSettings;
import com.limegroup.gnutella.util.LimeWireUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/**
 * Shows the files being shared by the users LimeWire in a search results like window
 * 
 */
public class MySharedFilesResultPanel extends ResultPanel {
    

    protected static final String MY_SHARED_FILES_TABLE = "MY_SHARED_FILES_TABLE";
    
    /**
     * Stops sharing a file listener when displaying shared files
     */
    Action STOP_SHARING_FILE_LISTENER;

    private JLabel filesLabel;

    private final HostData blankHostData;

	private final FileEventListener listener;
    
    
    
    /**
     *  Constructs a result panel showing your shared files. 
     */
    MySharedFilesResultPanel(String title, FileManager fileManager) {
        super(title, MY_SHARED_FILES_TABLE);

        this.TABLE.setDragEnabled(false);
        this.TABLE.setTransferHandler(null);
        // this.TABLE.setEnabled(false);
        BUTTON_ROW.setButtonsEnabled(false);

        SOUTH_PANEL.setVisible(false);
        
        blankHostData = new HostData(GuiCoreMediator.getApplicationServices().getMyGUID(), guid
                .bytes(), ConnectionSettings.CONNECTION_SPEED.getValue(), !GuiCoreMediator
                .getNetworkManager().acceptedIncomingConnection(), false, false,
                ChatSettings.CHAT_ENABLED.getValue(), false, false, NetworkUtils
                        .ip2string(GuiCoreMediator.getNetworkManager().getAddress()),
                GuiCoreMediator.getNetworkManager().getPort(), 0, LimeWireUtils.QHD_VENDOR_NAME,
                GuiCoreMediator.getConnectionManager().getPushProxies(), GuiCoreMediator
                        .getNetworkManager().canDoFWT(), GuiCoreMediator.getNetworkManager()
                        .supportsFWTVersion(), SSLSettings.isIncomingTLSEnabled());
        
        synchronized (fileManager) {
            for (int i = 0; fileManager.isValidIndex(i); i++) {
                FileDesc fd = fileManager.get(i);

                if (fd == null) {
                    continue;
                }

                if (!shouldDisplayAddedFile(fd))
                    continue;
                                
                addFile(fd);
            }
        }
        
        listener = createUpdateListener();
    }


    /**
     * @return false if this is an incomplete file and should not be added.
     */
    private boolean shouldDisplayAddedFile(FileDesc fd) {
        if (fd instanceof IncompleteFileDesc)
            return false;
        
        if (SharingUtils.isForcedShare(fd)) {
            return false;
        }
        
        return true;
        
    }
    
    private void addFile(FileDesc fd) {
        
        Response response = GuiCoreMediator.getResponseFactory().createResponse(fd);
        List<LimeXMLDocument> docs = fd.getLimeXMLDocuments();
        if (docs.size() == 1) {
            response.setDocument(docs.get(0));
        }
        
        Set<? extends IpPort> alts = Collections.emptySet();

        RemoteFileDesc rfd = response.toRemoteFileDesc(blankHostData, GuiCoreMediator.getRemoteFileDescFactory());
        SearchResult searchResult = new SharedSearchResult(fd, rfd, alts);
        add(searchResult);
    }
    
    private void removeFile(FileDesc fd) {
        for ( int i=0 ; i<DATA_MODEL.getRowCount() ; i++ ) {
            DataLine<SearchResult> line =  getLine(i);
            SearchResult sr = line.getInitializeObject();

            if (((SharedSearchResult) sr).getFileDesc().equals(fd)) {
                this.remove(sr);
            }
        }
    }
    
    @Override
    protected void buildListeners() {
        super.buildListeners();
        
        STOP_SHARING_FILE_LISTENER = new UnshareFileAction();
    }
    
    private class UnshareFileAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -7635649670855308992L;

        public UnshareFileAction() {
            putValue(Action.NAME, I18n.tr("Stop Sharing File"));
        }

        public void actionPerformed(ActionEvent e) {
            final int[] sel = TABLE.getSelectedRows();
            final FileDesc[] files = new FileDesc[sel.length];

            for (int i = 0; i < sel.length; i++) {
                DataLine<SearchResult> line =  getLine(sel[i]);
                SearchResult sr = line.getInitializeObject();

                FileDesc fd = ((SharedSearchResult) sr).getFileDesc();
                files[i] = fd;
            }

            BackgroundExecutorService.schedule(new Runnable() {
                public void run() {
                    for (int i = 0; i < files.length; i++) {
                        FileDesc fd = files[i];
                        GuiCoreMediator.getFileManager().stopSharingFile(fd.getFile());
                    }      
                }
            });
            
            // remove rows from the model seperately from the FM
            //  the FM uses synchronization for removals and can cause 
            //  unnessary repaints in the table when multiple rows are
            //  removed at the same time
            for (int i = files.length-1; i >= 0 ; i--) {
                DATA_MODEL.remove(sel[i]);
            }      
            refreshNumFiles();
        }
    }    
    
    /**
     * Creates the specialized SearchResultMenu for right-click popups.
     *
     * Upgraded access from protected to public for SearchResultDisplayer.
     */
    @Override
    public JPopupMenu createPopupMenu() {
        TableLine[] lines = getAllSelectedLines();
        if(lines.length == 0)
            return null;
        return (new SearchResultMenu(this)).addToMenu(new SkinPopupMenu(), lines, true, false);
    }
    
    /**
     * Sets the default renderers to be used in the table.
     */
    @Override
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
    }
    
    /**
     * Setup the data model 
     */
    protected void setupDataModel() {
        DATA_MODEL = new NoGroupTableRowFilter(FILTER);
    }
    /**
     * Sets the appropriate buttons to be disabled.
     */
    @Override
    public void handleNoSelection() {
       BUTTON_ROW.setButtonsEnabled(false);
    }
    
    /**
     * Sets the appropriate buttons to be enabled.
     */
    @Override
    public void handleSelection(int i)  { 
        BUTTON_ROW.setButtonsEnabled(false);
    }
    
    /**
     * Forwards the event to DOWNLOAD_LISTENER.
     */
    @Override
    public void handleActionKey() {
    }
    

    
    @Override
    protected void setupMainPanel() {
        MAIN_PANEL.add(createMyFilesInfoPanel());
                
        setupMainPanelBase();
    }

    private void refreshNumFiles() {
        String info = I18n.tr(
                "You are sharing {0} files. You can control which files FrostWire shares.",
                GuiCoreMediator.getFileManager().getNumFiles());

        filesLabel.setText("<html><font color=\"#7B5100\"><b>" + info + "</b></font></html>");
        
        
    }
    
    private JComponent createMyFilesInfoPanel() {
        JPanel panel = createWarningDitherPanel();

        
        String configure = I18n.tr("Configure");
        String library = I18n.tr("Library");
        
        panel.setLayout((new FlowLayout(FlowLayout.LEFT, 3, 3)));
        panel.add(new JLabel(GUIMediator.getThemeImage("warn-triangle")));
        
        filesLabel = new JLabel();

        refreshNumFiles();
        
        panel.add(filesLabel);
        
        panel.add(Box.createHorizontalStrut(2));
        Action configureAction = new AbstractAction() {
            /**
             * 
             */
            private static final long serialVersionUID = 8565671954910041629L;

            public void actionPerformed(ActionEvent e) {
                OptionsMediator optionsMediator = OptionsMediator.instance();
                optionsMediator.setOptionsVisible(true, OptionsConstructor.SHARED_KEY);
            }
        };
        configureAction.putValue(Action.NAME, "<b>" + configure + "</b>");
        configureAction.putValue(LimeAction.COLOR, new Color(0xAC,0x71,0x00));
        panel.add(new URLLabel(configureAction));
        
        panel.add(Box.createHorizontalStrut(2));
        Action libraryAction = new AbstractAction() {
            /**
             * 
             */
            private static final long serialVersionUID = 4838684077296068822L;

            public void actionPerformed(ActionEvent e) {
                GUIMediator.instance().getMainFrame().setSelectedTab(GUIMediator.Tabs.LIBRARY);
            }
        };
        libraryAction.putValue(Action.NAME, "<b>" + library + "</b>");
        libraryAction.putValue(LimeAction.COLOR, new Color(0xAC,0x71,0x00));
     
        panel.add(new URLLabel(libraryAction));

        Dimension ps = panel.getPreferredSize();
        ps.width = Short.MAX_VALUE;
        panel.setMaximumSize(ps);

        return panel;
    }

    @Override
    public void cleanup() {
        GuiCoreMediator.getFileManager().removeFileEventListener(listener);
    }
    
    private FileEventListener createUpdateListener() {
        
        FileEventListener listener = new FileEventListener() {
            public void handleFileEvent(final FileManagerEvent evt) {
                switch (evt.getType()) {
                    case ADD_FILE:
                    case REMOVE_FILE:

                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                               
                                if (evt.getType() == FileManagerEvent.Type.ADD_FILE) {
                                    if (shouldDisplayAddedFile(evt.getFileDescs()[0]))
                                        addFile(evt.getFileDescs()[0]);     
                                } else {
                                    removeFile(evt.getFileDescs()[0]);
                                }
                               
                                refreshNumFiles();

                            }
                        });
                    }
            }
        };
        
        GuiCoreMediator.getFileManager().addFileEventListener(listener);
        
        return listener;
    }
}
