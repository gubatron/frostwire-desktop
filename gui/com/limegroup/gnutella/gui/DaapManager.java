package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ThreadExecutor;
import org.limewire.io.NetworkUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.settings.DaapSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

import de.kapsi.net.daap.AutoCommitTransaction;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapServerFactory;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Database;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.Playlist;
import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.Transaction;

/**
 * This class handles the mDNS registration and acts as an
 * interface between LimeWire and DAAP.
 */
public final class DaapManager implements FinalizeListener {
    
    private static final Log LOG = LogFactory.getLog(DaapManager.class);
    
    private static final boolean USE_LIME_NIO = true;
    
    private static DaapManager instance = null;
    
    public static synchronized DaapManager instance() {
        if (instance == null) {
            instance = new DaapManager();
        }
        return instance;
    }

    private Library library;
    private Database database;
    private Playlist masterPlaylist;
    private Playlist whatsNew;
    private Playlist creativecommons;
    private Playlist videos;
        
    private DaapServer<?> server;
    
    private BonjourService bonjour;
    private AutoCommitTransaction autoCommitTxn;
    
    private boolean enabled = false;
    private int maxPlaylistSize;
    
    private Map<URN, Song> urnToSong;
    
    private DaapManager() {
        GUIMediator.addFinalizeListener(this);
    }
    
    /**
     * Initializes the Library
     */
    public synchronized void init() {
        
        if (isServerRunning()) {
            setEnabled(enabled);
        }
    }
    
    /**
     * Starts the DAAP Server
     */
    public synchronized void start() throws IOException {
        
        if (!isServerRunning()) {
            
            try {
                
                InetAddress addr = NetworkUtils.getLocalAddress();
                
                bonjour = new BonjourService(addr);
                urnToSong = new HashMap<URN, Song>();
                
                maxPlaylistSize = DaapSettings.DAAP_MAX_LIBRARY_SIZE.getValue();
                
                String name = DaapSettings.DAAP_LIBRARY_NAME.getValue();
                
                library = new Library(name);
                autoCommitTxn = new AutoCommitTransaction(library);
                
                database = new Database(name);
                whatsNew = new Playlist(I18n.tr("What\'s New"));
                creativecommons = new Playlist(I18n.tr("Creative Commons"));
                videos = new Playlist(I18n.tr("Video"));
                
                library.addDatabase(null, database);
                database.addPlaylist(null, creativecommons);
                database.addPlaylist(null, whatsNew);
                creativecommons.setSmartPlaylist(null, true);
                whatsNew.setSmartPlaylist(null, true);
                masterPlaylist = database.getMasterPlaylist();

                LimeConfig config = new LimeConfig(addr);
                
                if (DaapSettings.DAAP_REQUIRES_PASSWORD.getValue()) {
                    if (DaapSettings.DAAP_REQUIRES_USERNAME.getValue()) {
                        config.setAuthenticationMethod(DaapConfig.USERNAME_AND_PASSWORD);
                        config.setAuthenticationScheme(DaapConfig.DIGEST_SCHEME);
                    } else {
                        config.setAuthenticationMethod(DaapConfig.PASSWORD);
                        config.setAuthenticationScheme(DaapConfig.BASIC_SCHEME);
                    }
                } else {
                    config.setAuthenticationMethod(DaapConfig.NO_PASSWORD);
                    config.setAuthenticationScheme(DaapConfig.BASIC_SCHEME);
                }
                
//                if(USE_LIME_NIO)
//                    server = new LimeDaapServerNIO(library, config);
//                else
                    server = DaapServerFactory.createServer(library, config, true);

                server.setAuthenticator(new LimeAuthenticator());
                server.setStreamSource(new LimeStreamSource());
                server.setFilter(new LimeFilter());
                
                final int maxAttempts = 10;
                
                for(int i = 0; i < maxAttempts; i++) {
                    try {
                        server.bind();
                        break;
                    } catch (BindException bindErr) {
                        if (i < (maxAttempts-1)) {
                            // try next port...
                            config.nextPort();
                        } else {
                            throw bindErr;
                        }
                    }
                }

                if(USE_LIME_NIO) {
                    server.run();
                } else {
                    Thread serverThread = ThreadExecutor.newManagedThread(new Runnable() {
                        public void run() {
                            try {
                                server.run();
                            } catch (Throwable t) {
                                DaapManager.this.stop();
                                if (!handleError(t)) {
                                    GUIMediator.showError(I18n.tr("FrostWire encountered an error in the Digital Audio Access Protocol (for sharing files in iTunes). This feature will be turned off. You can turn it back on in options, under iTunes -> Sharing."));
                                    DaapSettings.DAAP_ENABLED.setValue(false);
                                    if (t instanceof RuntimeException)
                                        throw (RuntimeException) t;
                                    else
                                        throw new RuntimeException(t);
                                }
                            }
                        }
                    }, "DaapServerThread");
                    serverThread.setDaemon(true);
                    serverThread.start();
                }

                bonjour.registerService();

            } catch (IOException err) {
                stop();
                throw err;
            }
        }
    }

    /**
     * Stops the DAAP Server and releases all resources
     */
    public synchronized void stop() {

        if (bonjour != null)
            bonjour.close();

        if (server != null)
            server.stop();

        if (urnToSong != null)
            urnToSong.clear();

        bonjour = null;
        server = null;
        urnToSong = null;
        library = null;
        whatsNew = null;
        creativecommons = null;
        database = null;
        autoCommitTxn = null;
    }

    /**
     * Restarts the DAAP server and re-registers it via mDNS. This is equivalent
     * to:
     * <p>
     * 
     * <code>
     * stop();
     * start();
     * init();
     * </code>
     */
    public synchronized void restart() throws IOException {
        if (isServerRunning())
            stop();

        start();
        init();
    }

    /**
     * Shutdown the DAAP service properly. In this case is the main focus on
     * mDNS as in some rare cases iTunes doesn't recognize that LimeWire/DAAP 
     * is no longer online.
     */
    public void doFinalize() {
        stop();
    }

    /**
     * Updates the mDNS servive info
     */
    public synchronized void updateService() throws IOException {

        if (isServerRunning()) {
            bonjour.updateService();

            Transaction txn = library.beginTransaction();
            String name = DaapSettings.DAAP_LIBRARY_NAME.getValue();
            library.setName(txn, name);
            masterPlaylist.setName(txn, name);
            database.setName(txn, name);
            
            DaapConfig config = server.getConfig();
            if (DaapSettings.DAAP_REQUIRES_PASSWORD.getValue()) {
                if (DaapSettings.DAAP_REQUIRES_USERNAME.getValue()) {
                    config.setAuthenticationMethod(DaapConfig.USERNAME_AND_PASSWORD);
                    config.setAuthenticationScheme(DaapConfig.DIGEST_SCHEME);
                } else {
                    config.setAuthenticationMethod(DaapConfig.PASSWORD);
                    config.setAuthenticationScheme(DaapConfig.BASIC_SCHEME);
                }
            } else {
                config.setAuthenticationMethod(DaapConfig.NO_PASSWORD);
                config.setAuthenticationScheme(DaapConfig.BASIC_SCHEME);
            }
            
            txn.commit();
        }
    }

    /**
     * Disconnects all clients
     */
    public synchronized void disconnectAll() {
        if (isServerRunning()) {
            server.disconnectAll();
        }
    }

    /**
     * Returns <tt>true</tt> if server is running
     */
    public synchronized boolean isServerRunning() {
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }

    /**
     * Attempts to handle an exception. Returns true if we could handle it
     * correctly.
     */
    private boolean handleError(Throwable t) {
        if (t == null)
            return false;

        String msg = t.getMessage();
        if (msg == null
                || msg.indexOf("Unable to establish loopback connection") == -1)
            return handleError(t.getCause());

        // Problem with XP SP2. -- Loopback connections are disallowed.
        // Why? Who knows. This patch fixes it:
        // http://support.microsoft.com/default.aspx?kbid=884020
        if (OSUtils.isWindowsXP() || OSUtils.isWindowsVista()) {
            DialogOption answer = GUIMediator
                    .showYesNoCancelMessage(I18n.tr("FrostWire was unable to start the Digital Audio Access Protocol (for sharing files in iTunes) because a Microsoft patch is required or a firewall is blocking access. LimeWire can direct you to Microsoft\'s page with information if you want to use this feature. Click \'Yes\' to go to the patch, \'No\' to disable this feature, or \'Cancel\' to decide later."));
            switch (answer) {
                case YES:
                    GUIMediator
                            .openURL("http://support.microsoft.com/default.aspx?kbid=884020");
                    break;
                case NO:
                    DaapSettings.DAAP_ENABLED.setValue(false);
                    break;
            }
        } else {
            // Also a problem on non XP systems with firewalls.
            DialogOption answer = GUIMediator
                    .showYesNoMessage(I18n.tr("FrostWire was unable to start the Digital Audio Access Protocol (for sharing files in iTunes) because a firewall is blocking access. To continue using this feature, click \'Yes\' and change your firewall to allow \'LimeWire.exe\' full access to incoming and outgoing connections. To disable this feature, click \'No\'."),
                            DialogOption.YES);
            if (answer == DialogOption.NO)
                DaapSettings.DAAP_ENABLED.setValue(false);
        }

        return true;
    }

    /**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupportedAudioFormat(String name) {
        return isSupportedFormat(DaapSettings.DAAP_SUPPORTED_AUDIO_FILE_TYPES.getValue(), name);
    }
    
    private static boolean isSupportedVideoFormat(String name) {
        return isSupportedFormat(DaapSettings.DAAP_SUPPORTED_VIDEO_FILE_TYPES.getValue(), name);
    }
    
    private static boolean isSupportedFormat(String[] types, String name) {
        for(int i = 0; i < types.length; i++) {
            if (name.endsWith(types[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a change event.
     */
    private void handleChangeEvent(FileManagerEvent evt) {
        FileDesc oldDesc = evt.getFileDescs()[0];
        Song song = urnToSong.remove(oldDesc.getSHA1Urn());

        if (song != null) {
            FileDesc newDesc = evt.getFileDescs()[1];
            urnToSong.put(newDesc.getSHA1Urn(), song);
            
            String name = newDesc.getFileName().toLowerCase(Locale.US);
            
            if (isSupportedAudioFormat(name)) {
                //updateSongAudioMeta(autoCommitTxn, song, newDesc);
            } else if (isSupportedVideoFormat(name)) {
                //updateSongVideoMeta(autoCommitTxn, song, newDesc);
            } else {
                database.removeSong(autoCommitTxn, song);
            }
            
            // auto commit
        }
    }

    /**
     * Handles an add event.
     */
    private void handleAddEvent(FileManagerEvent evt) {
        // Transactions synchronize on the Library. So if there's
        // an ongoing commit we may get a ConcurrentModificationException
        // because Database has to iterate through all Playlists and
        // count the Songs.
        synchronized (library) {
            if (database.getSongCount() >= maxPlaylistSize) {
                return;
            }
        }
        
        FileDesc file = evt.getFileDescs()[0];
        if (true) {

            String name = file.getFileName().toLowerCase(Locale.US);

            Song song = null;
            
            if (isSupportedAudioFormat(name)) {
                song = createSong(file, true);
            } else if (isSupportedVideoFormat(name)) {
                song = createSong(file, false);
            }
            
            if (song != null) {
                urnToSong.put(file.getSHA1Urn(), song);
                
                database.getMasterPlaylist().addSong(autoCommitTxn, song);
                whatsNew.addSong(autoCommitTxn, song);
                
                if (file.isLicensed()) {
                    creativecommons.addSong(autoCommitTxn, song);
                }

                if (isSupportedVideoFormat(name)) {
                    videos.addSong(autoCommitTxn, song);
                }
                
                // auto commit
            }
        }
    }

    /**
     * Handles a rename event.
     */
    private void handleRenameEvent(FileManagerEvent evt) {
        FileDesc oldDesc = evt.getFileDescs()[0];
        Song song = urnToSong.remove(oldDesc.getSHA1Urn());

        if (song != null) {
            FileDesc newDesc = evt.getFileDescs()[1];
            urnToSong.put(newDesc.getSHA1Urn(), song);
            song.setAttachment(newDesc);
        }
    }

    /**
     * Handles a remove event.
     */
    private void handleRemoveEvent(FileManagerEvent evt) {
        FileDesc file = evt.getFileDescs()[0];
        Song song = urnToSong.remove(file.getSHA1Urn());

        if (song != null) {
            database.removeSong(autoCommitTxn, song);
            song.setAttachment(null);
            
            // auto commit
        }
    }

    /**
     * Called by VisualConnectionCallback
     */
    public synchronized void handleFileManagerEvent(FileManagerEvent evt) {
        if (!enabled || !isServerRunning())
            return;

        if (evt.isChangeEvent())
            handleChangeEvent(evt);
        else if (evt.isAddEvent())
            handleAddEvent(evt);
        else if (evt.isRenameEvent())
            handleRenameEvent(evt);
        else if (evt.isRemoveEvent())
            handleRemoveEvent(evt);
    }

    /**
     * Called by VisualConnectionCallback/MetaFileManager.
     */
    public void fileManagerLoading() {
        setEnabled(false);
    }
    
    /**
     * Called by VisualConnectionCallback/MetaFileManager.
     */
    public void fileManagerLoaded() {
        setEnabled(true);
    }
    
    public synchronized boolean isEnabled() {
        return enabled;
    }
    
    private synchronized void setEnabled(boolean enabled) {
        
        this.enabled = enabled;
        
        if (!enabled || !isServerRunning())
            return;
        
        Map<URN, Song> tmpUrnToSong = new HashMap<URN, Song>();
        
        int size = masterPlaylist.getSongCount();        
        Transaction txn = library.beginTransaction();    
   
        FileDesc[] files = GuiCoreMediator.getFileManager().getAllSharedFileDescriptors();
        
        for(int i = 0; i < files.length; i++) {
            FileDesc file = files[i];
//            if(file instanceof IncompleteFileDesc) {
//                continue;
//            }
            
            String name = file.getFileName().toLowerCase(Locale.US);
            boolean audio = isSupportedAudioFormat(name);
            
            if(!audio && !isSupportedVideoFormat(name)) {
                continue;
            }
            
            URN urn = file.getSHA1Urn();
            
            // 1)
            // _Remove_ URN from the current 'map'...
            Song song = urnToSong.remove(urn);
                
            // Check if URN is already in the tmpMap.
            // If so do nothing as we don't want add 
            // the same file multible times...
            if(tmpUrnToSong.containsKey(urn)) {
                continue;
            }
            
            // This URN was already mapped with a Song.
            // Save the Song (again) and update the meta
            // data if necessary
            if (song != null) {
                tmpUrnToSong.put(urn, song);
                
//                if (audio) {
//                    updateSongAudioMeta(txn, song, file);
//                } else {
//                    updateSongVideoMeta(txn, song, file);
//                }
                
            } else if (size < maxPlaylistSize) {

                song = createSong(file, audio);
                tmpUrnToSong.put(urn, song);
                database.getMasterPlaylist().addSong(txn, song);
                
                if (file.isLicensed()) {
                    creativecommons.addSong(txn, song);
                }
                
                if (isSupportedVideoFormat(name)) {
                    videos.addSong(txn, song);
                }
                
                size++;
            }
        }
        
        // See 1)
        // As all known URNs were removed from 'map' only
        // deleted FileDesc URNs can be leftover! We must 
        // remove the associated Songs from the Library now
        for(Song song : urnToSong.values()) {
            database.removeSong(txn, song);
            song.setAttachment(null);
        }
        
        urnToSong.clear();
        urnToSong = tmpUrnToSong; // tempMap is the new 'map'

        txn.commit();
    }
    
    /**
     * Create a Song and sets its meta data with
     * the data which is retrieved from the FileDesc
     */
    private Song createSong(FileDesc desc, boolean audio) {
        
        Song song = new Song(desc.getFileName());
        
        song.setSize(null, desc.getFileSize() & 0xFFFFFFFFL);
        song.setDateAdded(null, System.currentTimeMillis()/1000L);
        
        File file = desc.getFile();
        String ext = FileUtils.getFileExtension(file);
        
        if (!audio) {
            song.setHasVideo(null, true);
        }
        
        if (ext != null) {
            // Note: This is required for formats other than MP3
            // For example AAC (.m4a) files won't play if no
            // format is set. As far as I can tell from the iTunes
            // 'Get Info' dialog are Songs assumed as MP3 until
            // a format is set explicit.
            ext = ext.toLowerCase(Locale.US);
            if (!ext.endsWith("mp3"))
                song.setFormat(null, ext);

//            if (audio) {
//                updateSongAudioMeta(null, song, desc);
//            } else {
//                updateSongVideoMeta(null, song, desc);
//            }
            
        } else {
            song.setAttachment(desc);
        }

        return song;
    }
    
    /**
     * Handles the audio stream
     */
    private final class LimeStreamSource implements DaapStreamSource {
        
        public Object getSource(Song song) throws IOException {
            FileDesc fileDesc = (FileDesc)song.getAttachment();

            if(fileDesc != null)
                return new FileInputStream(fileDesc.getFile());
            
            return null;
        }
    }
    
    /**
     * Implements the DaapAuthenticator
     */
    private final class LimeAuthenticator implements DaapAuthenticator {
        
        public boolean authenticate(String username, String password, String uri, String nonce) {
            
            if (uri == null && nonce == null) {
                // BASIC
                return DaapSettings.DAAP_PASSWORD.equals(password);
            } else if (uri != null && nonce != null) {
                // DIGEST
                String ha1 = DaapSettings.DAAP_PASSWORD.getValue();
                if (ha1.startsWith("MD5/")) {
                    ha1 = ha1.substring(4);
                }
                String ha2 = DaapUtil.calculateHA2(uri);
                String digest = DaapUtil.digest(ha1, ha2, nonce);
                return digest.equalsIgnoreCase(password);
            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unknown scheme!");
                }
            }
            
            return false;
        }
    }
    
    /**
     * The DAAP Library should be only accessable from the LAN
     * as we can not guarantee for the required bandwidth and it
     * could be used to bypass Gnutella etc. Note: iTunes can't
     * connect to DAAP Libraries outside of the LAN but certain
     * iTunes download tools can.
     */
    private final class LimeFilter implements DaapFilter {

        /**
         * Returns true if <tt>address</tt> is a private address
         */
        public boolean accept(InetAddress address) {
            
            try {
                // not private & not close, not allowed.
                if (!GuiCoreMediator.getNetworkInstanceUtils().isVeryCloseIP(address)
                        && !!GuiCoreMediator.getNetworkInstanceUtils().isPrivateAddress(address))
                    return false;
            } catch (IllegalArgumentException err) {
                LOG.error(err);
                return false;
            }

            // Is it a annoying fellow? >:-)
            return GuiCoreMediator.getIpFilter().allow(address.getAddress());
        }
    }

    /**
     * A LimeWire specific implementation of DaapConfig
     */
    private final class LimeConfig extends DaapConfig {

        private InetAddress addr;

        public LimeConfig(InetAddress addr) {
            this.addr = addr;

            // Reset PORT to default value to prevent increasing
            // it to infinity
            DaapSettings.DAAP_PORT.revertToDefault();
        }

        public String getServerName() {
            return FrostWireUtils.getHttpServer();
        }

        public void nextPort() {
            int port = DaapSettings.DAAP_PORT.getValue();
            DaapSettings.DAAP_PORT.setValue(port + 1);
        }

        public int getBacklog() {
            return 0;
        }

        public InetSocketAddress getInetSocketAddress() {
            int port = DaapSettings.DAAP_PORT.getValue();
            return new InetSocketAddress(addr, port);
        }

        public int getMaxConnections() {
            return DaapSettings.DAAP_MAX_CONNECTIONS.getValue();
        }
    }

    /**
     * Helps us to publicize and update the DAAP Service via mDNS
     */
    private final class BonjourService {

        private static final String VERSION = "Version";

        private static final String MACHINE_NAME = "Machine Name";

        private static final String PASSWORD = "Password";

        private final JmDNS zeroConf;

        private ServiceInfo serviceInfo;

        public BonjourService(InetAddress addr) throws IOException {
            zeroConf = new JmDNS(addr);
        }

        public boolean isRegistered() {
            return (serviceInfo != null);
        }

        private ServiceInfo createServiceInfo() {

            String type = DaapSettings.DAAP_TYPE_NAME.getValue();
            String name = DaapSettings.DAAP_SERVICE_NAME.getValue();

            int port = DaapSettings.DAAP_PORT.getValue();
            int weight = DaapSettings.DAAP_WEIGHT.getValue();
            int priority = DaapSettings.DAAP_PRIORITY.getValue();

            boolean password = DaapSettings.DAAP_REQUIRES_PASSWORD.getValue();

            Hashtable<String, String> props = new Hashtable<String, String>();

            // Greys the share and the playlist names when iTunes's
            // protocol version is different from this version. It's
            // only a nice visual effect and has no impact to the
            // ability to connect this server! Disabled because
            // iTunes 4.2 is still widespread...
            props.put(VERSION, Integer.toString(DaapUtil.DAAP_VERSION_3));

            // This is the inital share name
            props.put(MACHINE_NAME, name);

            // shows the small lock if Service is protected
            // by a password!
            props.put(PASSWORD, Boolean.toString(password));

            String qualifiedName = null;

            // This isn't really required but as iTunes
            // does it in this way I'm doing it too...
            if (password) {
                qualifiedName = name + "_PW." + type;
            } else {
                qualifiedName = name + "." + type;
            }

            ServiceInfo serviceInfo = new ServiceInfo(type, qualifiedName, port,
                    weight, priority, props);

            return serviceInfo;
        }

        public void registerService() throws IOException {

            if (isRegistered())
                throw new IOException();

            ServiceInfo serviceInfo = createServiceInfo();
            zeroConf.registerService(serviceInfo);
            this.serviceInfo = serviceInfo;
        }

        public void unregisterService() {
            if (!isRegistered())
                return;

            zeroConf.unregisterService(serviceInfo);
            serviceInfo = null;
        }

        public void updateService() throws IOException {
            if (!isRegistered())
                throw new IOException();

            if (serviceInfo.getPort() != DaapSettings.DAAP_PORT.getValue())
                unregisterService();

            ServiceInfo serviceInfo = createServiceInfo();
            zeroConf.registerService(serviceInfo);

            this.serviceInfo = serviceInfo;
        }

        public void close() {
            unregisterService();
            zeroConf.close();
        }
    }
}
