package com.limegroup.gnutella.gui.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.ResourceManager;
import com.limegroup.gnutella.settings.ApplicationSettings;

public class FrostAssociations {
	
    /** 
     * A constant for the current associations "level"
     * increment this when adding new associations 
     */
    public static final int CURRENT_ASSOCIATIONS = 2;
    
	private static final String PROGRAM;
	private static final String UNSUPPORTED_PLATFORM = "";
	
	private static Collection<LimeAssociationOption> options;
	
	static {
		if (OSUtils.isWindows())
			PROGRAM = "FrostWire";
		else if (OSUtils.isUnix()) 
			PROGRAM = System.getProperty("unix.executable", UNSUPPORTED_PLATFORM);
		else
			PROGRAM = UNSUPPORTED_PLATFORM;
	}
	
	public synchronized static Collection<LimeAssociationOption> getSupportedAssociations() {
		if (options == null)
			options = getSupportedAssociationsImpl();
		return options;
	}
	
	public synchronized static boolean anyAssociationsSupported() {
		return !getSupportedAssociations().isEmpty();
	}
	
	private static Collection<LimeAssociationOption> getSupportedAssociationsImpl() {
		if (!ResourceManager.instance().isJdicLibraryLoaded())
			return Collections.emptyList();
		
		Collection<LimeAssociationOption> ret = new ArrayList<LimeAssociationOption>();
		
		// strings that the shell will understand 
		String fileOpener = null;
		String fileIcon = null;
		String protocolOpener = null;
		
		if (OSUtils.isWindows()) {
			String runningPath = SystemUtils.getRunningPath();
			
			if (runningPath != null && runningPath.endsWith(PROGRAM+".exe")) {
				protocolOpener = runningPath;
				fileOpener = "\""+runningPath+"\" \"%1\"";
				
				fileIcon = runningPath.replace(".exe", ".ico");
				//fileIcon = runningPath+",1";				
			}
		} 
		
		// if we have a string that opens a file, register torrents
		if (fileOpener != null) {			
			
			if (OSUtils.isWindows()) { // Windows users
				ShellAssociation tor = new FileTypeAssociation("torrent",
						"application/x-bittorrent", 
						fileOpener, "open",
						"FrostWire Torrent", 
						fileIcon);				
				LimeAssociationOption torrentwin = 
					new LimeAssociationOption(tor,
							ApplicationSettings.HANDLE_TORRENTS,
							".torrent",
							I18n.tr("\".torrent\" files"));
				ret.add(torrentwin);
			}
			else //Mac, Linux
			{
				ShellAssociation file = new FileTypeAssociation("torrent",
					"Application/x-bittorrent", fileOpener, "open",
					"FrostWire Torrent", fileIcon);
				LimeAssociationOption torrent = new LimeAssociationOption(
						file,
						ApplicationSettings.HANDLE_TORRENTS,
						".torrent",
						I18n.tr("\".torrent\" files"));
				ret.add(torrent);
			}
		}
		
		// if we have a string that opens a protocol, register magnets
		if (protocolOpener != null) {
			// Note: MagnetAssociation will only work on windows
			MagnetAssociation mag = new MagnetAssociation(PROGRAM, protocolOpener);
			LimeAssociationOption magOption = new LimeAssociationOption(
					mag,
					ApplicationSettings.HANDLE_MAGNETS,
					"magnet:",
                    I18n.tr("\"magnet:\" links"));
			ret.add(magOption);
		}
		
		return Collections.unmodifiableCollection(ret);
	}
}
