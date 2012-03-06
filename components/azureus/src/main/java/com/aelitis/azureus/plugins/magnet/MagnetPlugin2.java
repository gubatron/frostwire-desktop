/*
 * Created on 03-Mar-2005
 * Created by Paul Gardner
 * Copyright (C) 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */

package com.aelitis.azureus.plugins.magnet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerCheckRequest;
import org.gudy.azureus2.core3.disk.DiskManagerCheckRequestListener;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.disk.DiskManagerListener;
import org.gudy.azureus2.core3.disk.DiskManagerPiece;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequestListener;
import org.gudy.azureus2.core3.disk.DiskManagerWriteRequest;
import org.gudy.azureus2.core3.disk.DiskManagerWriteRequestListener;
import org.gudy.azureus2.core3.disk.impl.DiskManagerFileInfoImpl;
import org.gudy.azureus2.core3.disk.impl.DiskManagerFileInfoSetImpl;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceList;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceMap;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerFactory;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerImpl;
import org.gudy.azureus2.core3.download.impl.DownloadManagerStateImpl;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.logging.LogRelation;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerListener;
import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerManagerAdapter;
import org.gudy.azureus2.core3.peer.PEPeerManagerFactory;
import org.gudy.azureus2.core3.peer.PEPeerManagerListener;
import org.gudy.azureus2.core3.peer.PEPiece;
import org.gudy.azureus2.core3.peer.impl.PEPeerControl;
import org.gudy.azureus2.core3.peer.impl.transport.PEPeerTransportProtocol;
import org.gudy.azureus2.core3.peer.util.PeerUtils;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrent.TOTorrentListener;
import org.gudy.azureus2.core3.torrent.impl.TOTorrentMetadata;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerDataProvider;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerException;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerFactory;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerListener;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponse;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponsePeer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DelayedEvent;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginListener;
import org.gudy.azureus2.plugins.clientid.ClientIDException;
import org.gudy.azureus2.plugins.clientid.ClientIDGenerator;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseContact;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseEvent;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseListener;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseProgressListener;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseTransferType;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseValue;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.config.BooleanParameter;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderAdapter;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderFactory;
import org.gudy.azureus2.pluginsimpl.local.clientid.ClientIDManagerImpl;
import org.limewire.util.OSUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.peermanager.PeerManagerRegistration;
import com.aelitis.azureus.core.peermanager.peerdb.PeerItem;
import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;
import com.aelitis.azureus.core.util.CopyOnWriteList;
import com.aelitis.net.magneturi.MagnetURIHandler;
import com.aelitis.net.magneturi.MagnetURIHandlerException;
import com.aelitis.net.magneturi.MagnetURIHandlerListener;
import com.aelitis.net.magneturi.MagnetURIHandlerProgressListener;
import com.frostwire.AzureusStarter;
import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.frostwire.gui.bittorrent.TorrentUtil;
import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;

/**
 * @author parg
 *
 */

public class 
MagnetPlugin2
	implements Plugin
{	
	private static final String	SECONDARY_LOOKUP 			= "http://magnet.vuze.com/";
	private static final int	SECONDARY_LOOKUP_DELAY		= 20*1000;
	private static final int	SECONDARY_LOOKUP_MAX_TIME	= 2*60*1000;
	
	private static final String	PLUGIN_NAME				= "Magnet URI Handler";
	private static final String PLUGIN_CONFIGSECTION_ID = "plugins.magnetplugin";

	private PluginInterface		plugin_interface;
		
	private CopyOnWriteList		listeners = new CopyOnWriteList();
	
	private boolean			first_download	= true;
	
	private BooleanParameter secondary_lookup;
	
	public static void
	load(
		PluginInterface		plugin_interface )
	{
		plugin_interface.getPluginProperties().setProperty( "plugin.version", 	"1.0" );
		plugin_interface.getPluginProperties().setProperty( "plugin.name", PLUGIN_NAME );
	}
	
	public void
	initialize(
		PluginInterface	_plugin_interface )
	{
		plugin_interface	= _plugin_interface;
		
		BasicPluginConfigModel	config = 
			plugin_interface.getUIManager().createBasicPluginConfigModel( ConfigSection.SECTION_PLUGINS, 
					PLUGIN_CONFIGSECTION_ID);
		
		secondary_lookup = config.addBooleanParameter2( "MagnetPlugin.use.lookup.service", "MagnetPlugin.use.lookup.service", true );

		MagnetURIHandler.getSingleton().addListener(
			new MagnetURIHandlerListener()
			{
				public byte[]
				badge()
				{
					InputStream is = getClass().getClassLoader().getResourceAsStream( "com/aelitis/azureus/plugins/magnet/Magnet.gif" );
					
					if ( is == null ){
						
						return( null );
					}
					
					try{
						ByteArrayOutputStream	baos = new ByteArrayOutputStream();
						
						try{
							byte[]	buffer = new byte[8192];
							
							while( true ){
	
								int	len = is.read( buffer );
				
								if ( len <= 0 ){
									
									break;
								}
		
								baos.write( buffer, 0, len );
							}
						}finally{
							
							is.close();
						}
						
						return( baos.toByteArray());
						
					}catch( Throwable e ){
						
						Debug.printStackTrace(e);
						
						return( null );
					}
				}
							
				public byte[]
				download(
					final MagnetURIHandlerProgressListener		muh_listener,
					final byte[]								hash,
					final String								args,
					final InetSocketAddress[]					sources,
					final long									timeout )
				
					throws MagnetURIHandlerException
				{
						// see if we've already got it!
					
					try{
						Download	dl = plugin_interface.getDownloadManager().getDownload( hash );
					
						if ( dl != null ){
							
							Torrent	torrent = dl.getTorrent();
							
							if ( torrent != null ){
								
								return( torrent.writeToBEncodedData());
							}
						}
					}catch( Throwable e ){
					
						Debug.printStackTrace(e);
					}
					
					return( MagnetPlugin2.this.download(
							new MagnetPluginProgressListener()
							{
								public void
								reportSize(
									long	size )
								{
									muh_listener.reportSize( size );
								}
								
								public void
								reportActivity(
									String	str )
								{
									muh_listener.reportActivity( str );
								}
								
								public void
								reportCompleteness(
									int		percent )
								{
									muh_listener.reportCompleteness( percent );
								}
								
								public void
								reportContributor(
									InetSocketAddress	address )
								{
								}
								
								public boolean 
								verbose() 
								{
									return( muh_listener.verbose());
								}
							},
							hash,
							args,
							sources,
							timeout ));
				}
				
				public boolean
				download(
					URL		url )
				
					throws MagnetURIHandlerException
				{
					try{
						
						plugin_interface.getDownloadManager().addDownload( url, false );
						
						return( true );
						
					}catch( DownloadException e ){
						
						throw( new MagnetURIHandlerException( "Operation failed", e ));
					}
				}
				
				public boolean
				set(
					String		name,
					Map		values )
				{
					List	l = listeners.getList();
					
					for (int i=0;i<l.size();i++){
						
						if (((MagnetPluginListener)l.get(i)).set( name, values )){
							
							return( true );
						}
					}
					
					return( false );
				}
				
				public int
				get(
					String		name,
					Map			values )
				{
					List	l = listeners.getList();
					
					for (int i=0;i<l.size();i++){
						
						int res = ((MagnetPluginListener)l.get(i)).get( name, values );
						
						if ( res != Integer.MIN_VALUE ){
							
							return( res );
						}
					}
					
					return( Integer.MIN_VALUE );
				}
			});
		
		plugin_interface.addListener(
			new PluginListener()
			{
				public void
				initializationComplete()
				{
						// make sure DDB is initialised as we need it to register its
						// transfer types
					
					AEThread2 t = 
						new AEThread2( "MagnetPlugin:init", true )
						{
							public void
							run()
							{
								plugin_interface.getDistributedDatabase();
							}
						};
										
					t.start();
				}
				
				public void
				closedownInitiated(){}
				
				public void
				closedownComplete(){}			
			});
		
		plugin_interface.getUIManager().addUIListener(
				new UIManagerListener()
				{
					public void
					UIAttached(
						UIInstance		instance )
					{
//						if ( instance instanceof UISWTInstance ){
//							
////							UISWTInstance	swt = (UISWTInstance)instance;
////							
////							Image	image = swt.loadImage( "com/aelitis/azureus/plugins/magnet/icons/magnet.gif" );
////
////							menu1.setGraphic( swt.createGraphic( image ));
////							menu2.setGraphic( swt.createGraphic( image ));							
//						}
					}
					
					public void
					UIDetached(
						UIInstance		instance )
					{
						
					}
				});
	}
	
	public URL
	getMagnetURL(
		Download		d )
	{
		Torrent	torrent = d.getTorrent();
		
		if ( torrent == null ){
			
			return( null );
		}
		
		return( getMagnetURL( torrent.getHash()));
	}
	
	public URL
	getMagnetURL(
		byte[]		hash )
	{
		try{
			return( new URL( "magnet:?xt=urn:btih:" + Base32.encode(hash)));
		
		}catch( Throwable e ){
		
			Debug.printStackTrace(e);
		
			return( null );
		}
	}
	
	public byte[]
	badge()
	{
		return( null );
	}
	
	public byte[]
	download(
		MagnetPluginProgressListener		listener,
		byte[]								hash,
		String								args,
		InetSocketAddress[]					sources,
		long								timeout )
	
		throws MagnetURIHandlerException
	{
	    try {
            main(null);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	    if (true) {
	        return null;
	    }
		byte[]	torrent_data = downloadSupport( listener, hash, args, sources, timeout );
		
		if ( args != null ){
			
			String[] bits = args.split( "&" );
			
			List<String>	new_web_seeds = new ArrayList<String>();

			for ( String bit: bits ){
				
				String[] x = bit.split( "=" );
				
				if ( x.length == 2 ){
					
					if ( x[0].equalsIgnoreCase( "ws" )){
						
						try{
							new_web_seeds.add( new URL( UrlUtils.decode( x[1] )).toExternalForm());
							
						}catch( Throwable e ){							
						}
					}
				}
			}
			
			if ( new_web_seeds.size() > 0 ){
				
				try{
					TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedByteArray( torrent_data );
	
					Object obj = torrent.getAdditionalProperty( "url-list" );
					
					List<String> existing = new ArrayList<String>();
					
					if ( obj instanceof byte[] ){
		                
						try{
							new_web_seeds.remove( new URL( new String((byte[])obj, "UTF-8" )).toExternalForm());
							
						}catch( Throwable e ){							
						}
					}else if ( obj instanceof List ){
						
						List<byte[]> l = (List<byte[]>)obj;
						
						for ( byte[] b: l ){
							
							try{
								existing.add( new URL( new String((byte[])b, "UTF-8" )).toExternalForm());
								
							}catch( Throwable e ){							
							}
						}
					}
					
					boolean update = false;
					
					for ( String e: new_web_seeds ){
						
						if ( !existing.contains( e )){
							
							existing.add( e );
							
							update = true;
						}
					}
					
					if ( update ){
					
						List<byte[]>	l = new ArrayList<byte[]>();
						
						for ( String s: existing ){
							
							l.add( s.getBytes( "UTF-8" ));
						}
						
						torrent.setAdditionalProperty( "url-list", l );
						
						torrent_data = BEncoder.encode( torrent.serialiseToMap());
					}
					
				}catch( Throwable e ){
				}
			}
		}
		
		return( torrent_data );
	}
	
	private byte[]
	downloadSupport(
		final MagnetPluginProgressListener		listener,
		final byte[]							hash,
		final String							args,
		final InetSocketAddress[]				sources,
		final long								timeout )
	
		throws MagnetURIHandlerException
	{
		try{
			if ( first_download ){
			
				listener.reportActivity( getMessageText( "report.waiting_ddb" ));
				
				first_download = false;
			}
			
			final DistributedDatabase db = plugin_interface.getDistributedDatabase();
			
			final List			potential_contacts 		= new ArrayList();
			final AESemaphore	potential_contacts_sem 	= new AESemaphore( "MagnetPlugin:liveones" );
			final AEMonitor		potential_contacts_mon	= new AEMonitor( "MagnetPlugin:liveones" );
			
			final int[]			outstanding		= {0};
			final boolean[]		lookup_complete	= {false};
			
			listener.reportActivity(  getMessageText( "report.searching" ));
			
			DistributedDatabaseListener	ddb_listener = 
				new DistributedDatabaseListener()
				{
					private Set	found_set = new HashSet();
					
					public void
					event(
						DistributedDatabaseEvent 		event )
					{
						int	type = event.getType();
	
						if ( type == DistributedDatabaseEvent.ET_OPERATION_STARTS ){

								// give live results a chance before kicking in explicit ones
							
							if ( sources.length > 0 ){
								
								new DelayedEvent(
									"MP:sourceAdd",
									10*1000,
									new AERunnable()
									{
										public void
										runSupport()
										{
											addExplicitSources();
										}
									});
							}
							
						}else if ( type == DistributedDatabaseEvent.ET_VALUE_READ ){
													
							contactFound( event.getValue().getContact());
			
						}else if (	type == DistributedDatabaseEvent.ET_OPERATION_COMPLETE ||
									type == DistributedDatabaseEvent.ET_OPERATION_TIMEOUT ){
								
							listener.reportActivity( getMessageText( "report.found", String.valueOf( found_set.size())));
							
								// now inject any explicit sources

							addExplicitSources();
							
							try{
								potential_contacts_mon.enter();													

								lookup_complete[0] = true;
								
							}finally{
								
								potential_contacts_mon.exit();
							}
							
							potential_contacts_sem.release();
						}
					}
					
					protected void
					addExplicitSources()
					{	
						for (int i=0;i<sources.length;i++){
							
							try{
								contactFound( db.importContact(sources[i]));
								
							}catch( Throwable e ){
								
								Debug.printStackTrace(e);
							}
						}
					}
					
					public void
					contactFound(
						final DistributedDatabaseContact	contact )
					{
						String	key = contact.getAddress().toString();
						
						synchronized( found_set ){
							
							if ( found_set.contains( key )){
								
								return;
							}
							
							found_set.add( key );
						}
						
						if ( listener.verbose()){
						
							listener.reportActivity( getMessageText( "report.found", contact.getName()));
						}
						
						try{
							potential_contacts_mon.enter();													

							outstanding[0]++;
							
						}finally{
							
							potential_contacts_mon.exit();
						}
						
						contact.isAlive(
							20*1000,
							new DistributedDatabaseListener()
							{
								public void 
								event(
									DistributedDatabaseEvent event) 
								{
									try{
										boolean	alive = event.getType() == DistributedDatabaseEvent.ET_OPERATION_COMPLETE;
											
										if ( listener.verbose()){
										
											listener.reportActivity( 
												getMessageText( alive?"report.alive":"report.dead",	contact.getName()));
										}
										
										try{
											potential_contacts_mon.enter();
											
											Object[]	entry = new Object[]{ new Boolean( alive ), contact};
											
											boolean	added = false;
											
											if ( alive ){
												
													// try and place before first dead entry 
										
												for (int i=0;i<potential_contacts.size();i++){
													
													if (!((Boolean)((Object[])potential_contacts.get(i))[0]).booleanValue()){
														
														potential_contacts.add(i, entry );
														
														added = true;
														
														break;
													}
												}
											}
											
											if ( !added ){
												
												potential_contacts.add( entry );	// dead at end
											}
												
										}finally{
												
											potential_contacts_mon.exit();
										}
									}finally{
										
										try{
											potential_contacts_mon.enter();													

											outstanding[0]--;
											
										}finally{
											
											potential_contacts_mon.exit();
										}
										
										potential_contacts_sem.release();
									}
								}
							});
					}
				};
				
			db.read(
				ddb_listener,
				db.createKey( hash, "Torrent download lookup for '" + ByteFormatter.encodeString( hash ) + "'" ),
				timeout,
				DistributedDatabase.OP_EXHAUSTIVE_READ | DistributedDatabase.OP_PRIORITY_HIGH );
			
			long	remaining	= timeout;
			
			long 	overall_start 			= SystemTime.getMonotonousTime();
			boolean	sl_enabled				= true; //secondary_lookup.getValue() && FeatureAvailability.isMagnetSLEnabled();

			long	secondary_lookup_time 	= -1;
			
			long last_found = -1;
			
			final Object[] secondary_result = { null };
			
			while( remaining > 0 ){
					
				try{
					potential_contacts_mon.enter();

					if ( 	lookup_complete[0] && 
							potential_contacts.size() == 0 &&
							outstanding[0] == 0 ){
						
						break;
					}
				}finally{
					
					potential_contacts_mon.exit();
				}
								
				
				while( remaining > 0 ){
				
					long wait_start = SystemTime.getMonotonousTime();

					boolean got_sem = potential_contacts_sem.reserve( 1000 );
		
					long now = SystemTime.getMonotonousTime();
					
					remaining -= ( now - wait_start );
				
					if ( got_sem ){
					
						last_found = now;
						
						break;
						
					}else{
						
						if ( sl_enabled ){
							
							if ( secondary_lookup_time == -1 ){
							
								long	base_time;
								
								if ( last_found == -1 || now - overall_start > 60*1000 ){
									
									base_time = overall_start;
									
								}else{
									
									base_time = last_found;
								}
								
								long	time_so_far = now - base_time;
								
								if ( time_so_far > SECONDARY_LOOKUP_DELAY ){
									
									secondary_lookup_time = SystemTime.getMonotonousTime();
									
									doSecondaryLookup( listener, secondary_result, hash, args );
								}
							}else{
								
								try{
									byte[] torrent = getSecondaryLookupResult( secondary_result );
									
									if ( torrent != null ){
										
										return( torrent );
									}
								}catch( ResourceDownloaderException e ){
									
									// ignore, we just continue processing
								}
							}
						}

						continue;
					}
				}
				
				DistributedDatabaseContact	contact;
				boolean						live_contact;
				
				try{
					potential_contacts_mon.enter();
					
					// System.out.println( "rem=" + remaining + ",pot=" + potential_contacts.size() + ",out=" + outstanding[0] );
					
					if ( potential_contacts.size() == 0 ){
						
						if ( outstanding[0] == 0 ){
						
							break;
							
						}else{
							
							continue;
						}
					}else{
					
						Object[]	entry = (Object[])potential_contacts.remove(0);
						
						live_contact 	= ((Boolean)entry[0]).booleanValue(); 
						contact 		= (DistributedDatabaseContact)entry[1];
					}
					
				}finally{
					
					potential_contacts_mon.exit();
				}
					
				// System.out.println( "magnetDownload: " + contact.getName() + ", live = " + live_contact );
				
				if ( !live_contact ){
					
					listener.reportActivity( getMessageText( "report.tunnel", contact.getName()));

					contact.openTunnel();
				}
				
				try{
					listener.reportActivity( getMessageText( "report.downloading", contact.getName()));
					
					DistributedDatabaseValue	value = 
						contact.read( 
								new DistributedDatabaseProgressListener()
								{
									public void
									reportSize(
										long	size )
									{
										listener.reportSize( size );
									}
									public void
									reportActivity(
										String	str )
									{
										listener.reportActivity( str );
									}
									
									public void
									reportCompleteness(
										int		percent )
									{
										listener.reportCompleteness( percent );
									}
								},
								db.getStandardTransferType( DistributedDatabaseTransferType.ST_TORRENT ),
								db.createKey ( hash , "Torrent download content for '" + ByteFormatter.encodeString( hash ) + "'"),
								timeout );
										
					if ( value != null ){
						
							// let's verify the torrent
						
						byte[]	data = (byte[])value.getValue(byte[].class);

						try{
							TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedByteArray( data );
							
							if ( Arrays.equals( hash, torrent.getHash())){
							
								listener.reportContributor( contact.getAddress());
						
								return( data );
								
							}else{
								
								listener.reportActivity( getMessageText( "report.error", "torrent invalid (hash mismatch)" ));
							}
						}catch( Throwable e ){
							
							listener.reportActivity( getMessageText( "report.error", "torrent invalid (decode failed)" ));
						}
					}
				}catch( Throwable e ){
					
					listener.reportActivity( getMessageText( "report.error", Debug.getNestedExceptionMessage(e)));
					
					Debug.printStackTrace(e);
				}
			}
		
			if ( sl_enabled ){
				
				if ( secondary_lookup_time == -1 ){
					
					secondary_lookup_time = SystemTime.getMonotonousTime();
					
					doSecondaryLookup(listener, secondary_result, hash, args );
				}
				
				while( SystemTime.getMonotonousTime() - secondary_lookup_time < SECONDARY_LOOKUP_MAX_TIME ){
					
					try{
						byte[] torrent = getSecondaryLookupResult( secondary_result );
						
						if ( torrent != null ){
							
							return( torrent );
						}
						
						Thread.sleep( 500 );
						
					}catch( ResourceDownloaderException e ){
						
						break;
					}
				}
			}
			
			return( null );		// nothing found
			
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
			
			listener.reportActivity( getMessageText( "report.error", Debug.getNestedExceptionMessage(e)));

			throw( new MagnetURIHandlerException( "MagnetURIHandler failed", e ));
		}
	}
	
	protected void
	doSecondaryLookup(
		final MagnetPluginProgressListener		listener,
		final Object[]							result,
		byte[]									hash,
		String									args )
	{
		listener.reportActivity( getMessageText( "report.secondarylookup", null ));
		
		try{
			ResourceDownloaderFactory rdf = plugin_interface.getUtilities().getResourceDownloaderFactory();
		
			URL sl_url = new URL( SECONDARY_LOOKUP + "magnetLookup?hash=" + Base32.encode( hash ) + (args.length()==0?"":("&args=" + UrlUtils.encode( args ))));
			
			ResourceDownloader rd = rdf.create( sl_url );
			
			rd.addListener(
				new ResourceDownloaderAdapter()
				{
					public boolean
					completed(
						ResourceDownloader	downloader,
						InputStream			data )
					{
						listener.reportActivity( getMessageText( "report.secondarylookup.ok", null ));

						synchronized( result ){
						
							result[0] = data;
						}
						
						return( true );
					}
					
					public void
					failed(
						ResourceDownloader			downloader,
						ResourceDownloaderException e )
					{
						synchronized( result ){
							
							result[0] = e;
						}
						
						listener.reportActivity( getMessageText( "report.secondarylookup.fail" ));
					}
				});
			
			rd.asyncDownload();
			
		}catch( Throwable e ){
			
			listener.reportActivity( getMessageText( "report.secondarylookup.fail", Debug.getNestedExceptionMessage( e ) ));
		}
	}
	
	protected byte[]
	getSecondaryLookupResult(
		final Object[]	result )
	
		throws ResourceDownloaderException
	{
		Object x;
		
		synchronized( result ){
			
			x = result[0];
			
			result[0] = null;
		}
			
		if ( x instanceof InputStream ){
			
			InputStream is = (InputStream)x;
				
			try{
				TOTorrent t = TOTorrentFactory.deserialiseFromBEncodedInputStream( is );
				
				TorrentUtils.setPeerCacheValid( t );
		
				return( BEncoder.encode( t.serialiseToMap()));
				
			}catch( Throwable e ){							
			}
		}else if ( x instanceof ResourceDownloaderException ){
			
			throw((ResourceDownloaderException)x);
		}
		
		return( null );
	}
	
	protected String
	getMessageText(
		String	resource )
	{
		return( plugin_interface.getUtilities().getLocaleUtilities().getLocalisedMessageText( "MagnetPlugin." + resource ));
	}
	
	protected String
	getMessageText(
		String	resource,
		String	param )
	{
		return( plugin_interface.getUtilities().getLocaleUtilities().getLocalisedMessageText( 
				"MagnetPlugin." + resource, new String[]{ param }));
	}
	
	public void
	addListener(
		MagnetPluginListener		listener )
	{
		listeners.add( listener );
	}
	
	public void
	removeListener(
		MagnetPluginListener		listener )
	{
		listeners.remove( listener );
	}
	
	public static void main(String[] args) throws Exception {
	    AzureusStarter.start();
	    URL[] trackers = new URL[] { new URL("udp://tracker.openbittorrent.com:80") };//, new URL("udp://tracker.publicbt.com:80"), new URL("udp://tracker.ccc.de:80") };

        final TOTorrentMetadata torrent = new TOTorrentMetadata(ByteFormatter.decodeString("ebae8cd0dda88a82d4e495eb49f78942b3463446"), "4502608c5d83fbe179d11deee851f727d267bd47", trackers) {
          @Override
        public void notifySaved() {
            System.out.println("Torrent saved to: " + getSaveLocation());
        }  
        };

        
	     TRTrackerAnnouncer                 tracker_client;
	     MetadataTrackerAnnouncerListener      tracker_client_listener = new MetadataTrackerAnnouncerListener() 
	            ;
	            
	            
	            tracker_client = TRTrackerAnnouncerFactory.create( torrent, null);
	            tracker_client.addListener( tracker_client_listener );
	            tracker_client.setAnnounceDataProvider(new MetadataTrackerAnnouncerDataProvider());
	            
	            PEPeerManager peerManager = PEPeerManagerFactory.create( tracker_client.getPeerId(), new MetadataPeerManagerAdapter() , new MetadataDiskManager(torrent));
	           tracker_client_listener.setPeerManager(peerManager);
	           
	            peerManager.start();
	            peerManager.setSuperSeedMode(true);
	            
	            tracker_client.update(true);
	            
	            System.in.read();
	}

    public static byte[] decodeHex(String str) {
        str = str.toLowerCase();
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }
    
    static boolean metatada_requested = false;
    
    private static void tryMetadata(TOTorrent torrent, final PEPeerTransportProtocol peer) {
        if (metatada_requested) {
            return;
        }
        
        if (peer.supportsUTMETADATA()) {
            metatada_requested = true;
            peer.sendMetadataRequest(0);
        }
        
    }
    
    public static class MetadataDiskManager implements DiskManager {
        
        private final TOTorrentMetadata torrent;
        
        public MetadataDiskManager(TOTorrentMetadata torrent) {
            this.torrent = torrent;
        }
        
        @Override
        public boolean stop(boolean closing) {
            return false;
        }
        
        @Override
        public void start() {
        }
        
        @Override
        public void setPieceCheckingEnabled(boolean enabled) {
        }
        
        @Override
        public void saveState() {
        }
        
        @Override
        public void saveResumeData(boolean interim_save) throws Exception {
        }
        
        @Override
        public void removeListener(DiskManagerListener l) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public DirectByteBuffer readBlock(int pieceNumber, int offset, int length) {
            return null;
        }
        
        @Override
        public void moveDataFiles(File new_parent_dir, String dl_name) {
        }
        
        @Override
        public boolean isStopped() {
            return false;
        }
        
        @Override
        public boolean isInteresting(int pieceNumber) {
            return true;
        }
        
        @Override
        public boolean isDone(int pieceNumber) {
            return false;
        }
        
        @Override
        public boolean hasOutstandingWriteRequestForPiece(int piece_number) {
            return false;
        }
        
        @Override
        public boolean hasOutstandingReadRequestForPiece(int piece_number) {
            return false;
        }
        
        @Override
        public boolean hasOutstandingCheckRequestForPiece(int piece_number) {
            return false;
        }
        
        @Override
        public boolean hasListener(DiskManagerListener l) {
            return false;
        }
        
        @Override
        public long getTotalLength() {
            return 0;
        }
        
        @Override
        public TOTorrent getTorrent() {
            return torrent;
        }
        
        @Override
        public int getState() {
            return 0;
        }
        
        @Override
        public File getSaveLocation() {
            return null;
        }
        
        @Override
        public long getRemainingExcludingDND() {
            return 0;
        }
        
        @Override
        public long getRemaining() {
            return 0;
        }
        
        @Override
        public long[] getReadStats() {
            return null;
        }
        
        @Override
        public DiskManagerPiece[] getPieces() {
            return new DiskManagerPiece[0];
        }
        
        @Override
        public DMPieceMap getPieceMap() {
            return null;
        }
        
        @Override
        public DMPieceList getPieceList(int pieceNumber) {
            return null;
        }
        
        @Override
        public int getPieceLength(int piece_number) {
            return 0;
        }
        
        @Override
        public int getPieceLength() {
            return 0;
        }
        
        @Override
        public DiskManagerPiece getPiece(int PieceNumber) {
            return null;
        }
        
        @Override
        public int getPercentDone() {
            return 0;
        }
        
        @Override
        public int getNbPieces() {
            return 0;
        }
        
        @Override
        public DiskManagerFileInfo[] getFiles() {
            return new DiskManagerFileInfo[0];
        }
        
        @Override
        public DiskManagerFileInfoSet getFileSet() {
            return new DiskManagerFileInfoSetImpl(new DiskManagerFileInfoImpl[0],null );
        }
        
        @Override
        public String getErrorMessage() {
            return null;
        }
        
        @Override
        public int getCompleteRecheckStatus() {
            return 0;
        }
        
        @Override
        public int getCacheMode() {
            return 0;
        }
        
        @Override
        public void generateEvidence(IndentWriter writer) {
        }
        
        @Override
        public boolean filesExist() {
            return false;
        }
        
        @Override
        public void enqueueWriteRequest(DiskManagerWriteRequest request, DiskManagerWriteRequestListener listener) {
        }
        
        @Override
        public void enqueueReadRequest(DiskManagerReadRequest request, DiskManagerReadRequestListener listener) {
        }
        
        @Override
        public void enqueueCompleteRecheckRequest(DiskManagerCheckRequest request, DiskManagerCheckRequestListener listener) {
        }
        
        @Override
        public void enqueueCheckRequest(DiskManagerCheckRequest request, DiskManagerCheckRequestListener listener) {
        }
        
        @Override
        public void downloadRemoved() {
        }
        
        @Override
        public void downloadEnded() {
        }
        
        @Override
        public DiskManagerWriteRequest createWriteRequest(int pieceNumber, int offset, DirectByteBuffer data, Object user_data) {
            return null;
        }
        
        @Override
        public DiskManagerReadRequest createReadRequest(int pieceNumber, int offset, int length) {
            return null;
        }
        
        @Override
        public DiskManagerCheckRequest createCheckRequest(int pieceNumber, Object user_data) {
            return null;
        }
        
        @Override
        public boolean checkBlockConsistencyForWrite(String originator, int pieceNumber, int offset, DirectByteBuffer data) {
            return false;
        }
        
        @Override
        public boolean checkBlockConsistencyForRead(String originator, boolean peer_request, int pieceNumber, int offset, int length) {
            return false;
        }
        
        @Override
        public boolean checkBlockConsistencyForHint(String originator, int pieceNumber, int offset, int length) {
            return false;
        }
        
        @Override
        public void addListener(DiskManagerListener l) {
        }
    }
    
    public static class MetadataPeerManagerAdapter implements PEPeerManagerAdapter {
        
        @Override
        public void statsRequest(PEPeer originator, Map request, Map reply) {            
        }
        
        @Override
        public void setTrackerRefreshDelayOverrides(int percent) {            
        }
        
        @Override
        public void setStateSeeding(boolean never_downloaded) {
        }
        
        @Override
        public void setStateFinishing() {
        }
        
        @Override
        public void restartDownload(boolean forceRecheck) {
        }
        
        @Override
        public void removePiece(PEPiece piece) {
        }
        
        @Override
        public void removePeer(PEPeer peer) {
        }
        
        @Override
        public void protocolBytesSent(PEPeer peer, int bytes) {
        }
        
        @Override
        public void protocolBytesReceived(PEPeer peer, int bytes) {
        }
        
        @Override
        public void priorityConnectionChanged(boolean added) {
        }
        
        @Override
        public void permittedSendBytesUsed(int bytes) {
        }
        
        @Override
        public void permittedReceiveBytesUsed(int bytes) {
        }
        
        @Override
        public boolean isPeriodicRescanEnabled() {
            return false;
        }
        
        @Override
        public boolean isPeerSourceEnabled(String peer_source) {
            return false;
        }
        
        @Override
        public boolean isPeerExchangeEnabled() {
           return true;
        }
        
        @Override
        public boolean isNATHealthy() {
            return false;
        }
        
        @Override
        public boolean isExtendedMessagingEnabled() {
            return true;
        }
        
        @Override
        public boolean hasPriorityConnection() {
            return false;
        }
        
        @Override
        public int getUploadRateLimitBytesPerSecond() {
            return 0;
        }
        
        @Override
        public TRTrackerScraperResponse getTrackerScrapeResponse() {
            return null;
        }
        
        @Override
        public String getTrackerClientExtensions() {
            return null;
        }
        
        @Override
        public byte[][] getSecrets(int crypto_level) {
            return null;
        }
        
        @Override
        public long getRandomSeed() {
            return 0;
        }
        
        @Override
        public int getPosition() {
            return 0;
        }
        
        @Override
        public int getPermittedBytesToSend() {
            return 0;
        }
        
        @Override
        public int getPermittedBytesToReceive() {
            return 0;
        }
        
        @Override
        public PeerManagerRegistration getPeerManagerRegistration() {
            return new PeerManagerRegistration() {
                
                @Override
                public void unregister() {
                }
                
                @Override
                public void removeLink(String link) {
                }
                
                @Override
                public TOTorrentFile getLink(String link) {
                    return null;
                }
                
                @Override
                public String getDescription() {
                    return null;
                }
                
                @Override
                public void deactivate() {
                }
                
                @Override
                public void addLink(String link, TOTorrentFile target) throws Exception {
                }
                
                @Override
                public void activate(PEPeerControl peer_control) {
                }
            };
        }
        
        @Override
        public int getMaxUploads() {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public int getMaxSeedConnections() {
            return 0;
        }
        
        @Override
        public int getMaxConnections() {
            return 0;
        }
        
        @Override
        public LogRelation getLogRelation() {
            return null;
        }
        
        @Override
        public int getDownloadRateLimitBytesPerSecond() {
            return 0;
        }
        
        @Override
        public String getDisplayName() {
            return null;
        }
        
        @Override
        public int getCryptoLevel() {
            return 0;
        }
        
        @Override
        public void enqueueReadRequest(PEPeer peer, DiskManagerReadRequest request, DiskManagerReadRequestListener listener) {
        }
        
        @Override
        public void discarded(PEPeer peer, int bytes) {
        }
        
        @Override
        public void dataBytesSent(PEPeer peer, int bytes) {
        }
        
        @Override
        public void dataBytesReceived(PEPeer peer, int bytes) {
        }
        
        @Override
        public void addPiece(PEPiece piece) {
        }
        
        @Override
        public void addPeer(PEPeer peer) {
            peer.addListener(new MetadataPeerListener());
        }
        
        @Override
        public void addHTTPSeed(String address, int port) {
        }
    }
    
    public static class MetadataPeerListener implements PEPeerListener {
        
        @Override
        public void stateChanged(PEPeer peer, int new_state) {
            if (new_state == PEPeer.READY_TO_ASK_FOR_METADATA) {
                tryMetadata(null, (PEPeerTransportProtocol)peer);
            }
        }

        @Override
        public void sentBadChunk(PEPeer peer, int piece_num, int total_bad_chunks) {
        }
        
        @Override
        public void removeAvailability(PEPeer peer, BitFlags peerHavePieces) {
        }
        
        @Override
        public void addAvailability(PEPeer peer, BitFlags peerHavePieces) {
        }
    }
    
    public static class MetadataTrackerAnnouncerListener implements TRTrackerAnnouncerListener
    {
        private PEPeerManager peerManager;
        
        public PEPeerManager getPeerManager() {
            return peerManager;
        }
        
        public void setPeerManager(PEPeerManager peerManager) {
            this.peerManager = peerManager;
        }
        
        public void 
        receivedTrackerResponse(
            TRTrackerAnnouncerResponse  response) 
        {
            if (peerManager != null) {
            peerManager.processTrackerResponse(response);
            }
        }

        public void 
        urlChanged(
            final TRTrackerAnnouncer    announcer,  
            final URL                   old_url,
            URL                         new_url,
            boolean                     explicit ) 
        {
        }

        public void 
        urlRefresh() 
        {
        }
    }
    
    public static class MetadataTrackerAnnouncerDataProvider implements TRTrackerAnnouncerDataProvider {
        
        @Override
        public void setPeerSources(String[] allowed_sources) {
        }
        
        @Override
        public boolean isPeerSourceEnabled(String peer_source) {
            return true;
        }
        
        @Override
        public int getUploadSpeedKBSec(boolean estimate) {
            return 0;
        }
        
        @Override
        public long getTotalSent() {
            return 0;
        }
        
        @Override
        public long getTotalReceived() {
            return 0;
        }
        
        @Override
        public long getRemaining() {
            return 0;
        }
        
        @Override
        public int getPendingConnectionCount() {
            return 0;
        }
        
        @Override
        public String getName() {
            return "MetadataTrackerAnnouncerDataProvider";
        }
        
        @Override
        public int getMaxNewConnectionsAllowed() {
            return 20;
        }
        
        @Override
        public long getFailedHashCheck() {
            return 0;
        }
        
        @Override
        public String getExtensions() {
            return null;
        }
        
        @Override
        public int getCryptoLevel() {
            return 0;
        }
        
        @Override
        public int getConnectedConnectionCount() {
            return 0;
        }
    }
}
