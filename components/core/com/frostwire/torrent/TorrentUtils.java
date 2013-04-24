/*
 * File    : TorrentUtils.java
 * Created : 13-Oct-2003
 * By      : stuff
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frostwire.torrent;

/**
 * @author parg
 *
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;


public class 
TorrentUtils 
{

	
	public static final int TORRENT_FLAG_LOW_NOISE			= 0x00000001;
	public static final int TORRENT_FLAG_METADATA_TORRENT	= 0x00000002;
	
	private static final String		TORRENT_AZ_PROP_DHT_BACKUP_ENABLE		= "dht_backup_enable";
	private static final String		TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED	= "dht_backup_requested";
	private static final String		TORRENT_AZ_PROP_TORRENT_FLAGS			= "torrent_flags";
	private static final String		TORRENT_AZ_PROP_PLUGINS					= "plugins";
	
	public static final String		TORRENT_AZ_PROP_OBTAINED_FROM			= "obtained_from";
	public static final String		TORRENT_AZ_PROP_PEER_CACHE				= "peer_cache";
	public static final String		TORRENT_AZ_PROP_PEER_CACHE_VALID		= "peer_cache_valid";
	public static final String		TORRENT_AZ_PROP_INITIAL_LINKAGE			= "initial_linkage";
	public static final String		TORRENT_AZ_PROP_INITIAL_LINKAGE2		= "initial_linkage2";
	
	private static final String		MEM_ONLY_TORRENT_PATH		= "?/\\!:mem_only:!\\/?";
	
	private static ThreadLocal<Map<String,Object>>		tls	= 
		new ThreadLocal<Map<String,Object>>()
		{
			public Map<String,Object>
			initialValue()
			{
				return( new HashMap<String,Object>());
			}
		};
		
	private static volatile Set<String>		ignore_set;
	
	private static boolean bSaveTorrentBackup;
	
	private static boolean					DNS_HANDLING_ENABLE	= true;
	private static final boolean			TRACE_DNS 			= false;
	private static int						DNS_HISTORY_TIMEOUT	= 4*60*60*1000;
	
	private static volatile int				dns_mapping_seq_count;

	
	
	

	public static TOTorrent
	readFromFile(
		File		file,
		boolean		create_delegate )
		
		throws TOTorrentException
	{
		return( readFromFile( file, create_delegate, false ));
	}
	
		/**
		 * If you set "create_delegate" to true then you must understand that this results
		 * is piece hashes being discarded and then re-read from the torrent file if needed
		 * Therefore, if you delete the original torrent file you're going to get errors
		 * if you access the pieces after this (and they've been discarded)
		 * @param file
		 * @param create_delegate
		 * @param force_initial_discard - use to get rid of pieces immediately
		 * @return
		 * @throws TOTorrentException
		 */
	
	public static ExtendedTorrent
	readDelegateFromFile(
		File		file,
		boolean		force_initial_discard )
		
		throws TOTorrentException
	{
		return((ExtendedTorrent)readFromFile( file, true, force_initial_discard ));
	}
	
	public static TOTorrent
	readFromFile(
		File		file,
		boolean		create_delegate,
		boolean		force_initial_discard )
		
		throws TOTorrentException
	{
		TOTorrent torrent;
   
		try{
			torrent = TOTorrentFactory.deserialiseFromBEncodedFile(file);
			
				// make an immediate backup if requested and one doesn't exist 
			
	    	if (bSaveTorrentBackup) {
	    		
	    		File torrent_file_bak = new File(file.getParent(), file.getName() + ".bak");

	    		if ( !torrent_file_bak.exists()){
	    			
	    			try{
	    				torrent.serialiseToBEncodedFile(torrent_file_bak);
	    				
	    			}catch( Throwable e ){
	    				
	    				Debug.printStackTrace(e);
	    			}
	    		}
	    	}
	    	
		}catch (TOTorrentException e){
      
			// Debug.outNoStack( e.getMessage() );
			
			File torrentBackup = new File(file.getParent(), file.getName() + ".bak");
			
			if( torrentBackup.exists()){
				
				torrent = TOTorrentFactory.deserialiseFromBEncodedFile(torrentBackup);
				
					// use the original torrent's file name so that when this gets saved
					// it writes back to the original and backups are made as required
					// - set below
			}else{
				
				throw e;
			}
		}
				
		torrent.setAdditionalStringProperty("torrent filename", file.toString());
		
		if ( create_delegate ){
			
			//torrentDelegate	res = new torrentDelegate( torrent, file );
			
//			if ( force_initial_discard ){
//				
//				res.discardPieces( SystemTime.getCurrentTime(), true );
//			}
			
			return( null );
			
		}else{
			
			return( torrent );
		}
	}

	public static TOTorrent
	readFromBEncodedInputStream(
		InputStream		is )
		
		throws TOTorrentException
	{
		TOTorrent	torrent = TOTorrentFactory.deserialiseFromBEncodedInputStream( is );
		
			// as we've just imported this torrent we want to clear out any possible attributes that we
			// don't want such as "torrent filename"
		
		torrent.removeAdditionalProperties();
		
		return( torrent );
	}
	
	public static void
	setMemoryOnly(
		TOTorrent			torrent,
		boolean				mem_only )
	{
		if ( mem_only ){
			
			torrent.setAdditionalStringProperty("torrent filename", MEM_ONLY_TORRENT_PATH );
			
		}else{
			
			String s = torrent.getAdditionalStringProperty("torrent filename");
			
			if ( s != null && s.equals( MEM_ONLY_TORRENT_PATH )){
				
				torrent.removeAdditionalProperty( "torrent filename" );
			}
		}
	}
	
	public static void
	writeToFile(
		final TOTorrent		torrent )
	
		throws TOTorrentException 
	{
		writeToFile( torrent, false );
	}
	
	public static void
	writeToFile(
		TOTorrent		torrent,
		boolean			force_backup )
	
		throws TOTorrentException 
	{
	   try{
	   		//torrent.getMonitor().enter();
	    		   		
	    	String str = torrent.getAdditionalStringProperty("torrent filename");
	    	
	    	if ( str == null ){
	    		
	    		throw (new TOTorrentException("TorrentUtils::writeToFile: no 'torrent filename' attribute defined", TOTorrentException.RT_FILE_NOT_FOUND));
	    	}
	    	
	    	if ( str.equals( MEM_ONLY_TORRENT_PATH )){
	    		
	    		return;
	    	}
	    	
	    		// save first to temporary file as serialisation may require state to be re-read from
	    		// the existing file first and if we rename to .bak first then this aint good
	    		    	
    		File torrent_file_tmp = new File(str + "._az");

	    	torrent.serialiseToBEncodedFile( torrent_file_tmp );

	    		// now backup if required
	    	
	    	File torrent_file = new File(str);
	    	
//	    	if ( 	( force_backup ||COConfigurationManager.getBooleanParameter("Save Torrent Backup")) &&
//	    			torrent_file.exists()) {
//	    		
//	    		File torrent_file_bak = new File(str + ".bak");
//	    		
//	    		try{
//	    			
//	    				// Will return false if it cannot be deleted (including if the file doesn't exist).
//	    			
//	    			torrent_file_bak.delete();
//	    			
//	    			torrent_file.renameTo(torrent_file_bak);
//	    			
//	    		}catch( SecurityException e){
//	    			
//	    			Debug.printStackTrace( e );
//	    		}
//	    	}
	      
	    		// now rename the temp file to required one
	    	
	    	if ( torrent_file.exists()){
	    		
	    		torrent_file.delete();
	    	}
	    	
	    	torrent_file_tmp.renameTo( torrent_file );
			
	   	}finally{
	   		
	   		//torrent.getMonitor().exit();
	   	}
	}
	
	public static void
	writeToFile(
		TOTorrent		torrent,
		File			file )
	
		throws TOTorrentException 
	{
		writeToFile( torrent, file, false );
	}
	
	public static void
	writeToFile(
		TOTorrent		torrent,
		File			file,
		boolean			force_backup )
	
		throws TOTorrentException 
	{		
		torrent.setAdditionalStringProperty("torrent filename", file.toString());
		
		writeToFile( torrent, force_backup );
	}
	
	public static String
	getTorrentFileName(
		TOTorrent		torrent )
	
		throws TOTorrentException 
	{
    	String str = torrent.getAdditionalStringProperty("torrent filename");
    	
    	if ( str == null ){
    		
    		throw( new TOTorrentException("TorrentUtils::getTorrentFileName: no 'torrent filename' attribute defined", TOTorrentException.RT_FILE_NOT_FOUND));
    	}

    	if ( str.equals( MEM_ONLY_TORRENT_PATH )){
    		
    		return( null );
    	}
    	
		return( str );
	}
	
	public static void
	copyToFile(
		TOTorrent		torrent,
		File			file )

		throws TOTorrentException 
	{
	   	torrent.serialiseToBEncodedFile(file);
	}
	
	
	
	
	
	
		
	public static String
	exceptionToText(
		TOTorrentException	e )
	{
		String	errorDetail;
		
		int	reason = e.getReason();
  					
		if ( reason == TOTorrentException.RT_FILE_NOT_FOUND ){
 	     	        		 		
			errorDetail = MessageText.getString("DownloadManager.error.filenotfound" );
	        				
		}else if ( reason == TOTorrentException.RT_ZERO_LENGTH ){
	     
			errorDetail = MessageText.getString("DownloadManager.error.fileempty");
	        			
		}else if ( reason == TOTorrentException.RT_TOO_BIG ){
	 	     		
			errorDetail = MessageText.getString("DownloadManager.error.filetoobig");
			        
		}else if ( reason == TOTorrentException.RT_DECODE_FAILS ){
	 
			errorDetail = MessageText.getString("DownloadManager.error.filewithouttorrentinfo" );
	 		  			
		}else if ( reason == TOTorrentException.RT_UNSUPPORTED_ENCODING ){
	 	     		
			errorDetail = MessageText.getString("DownloadManager.error.unsupportedencoding");
							
		}else if ( reason == TOTorrentException.RT_READ_FAILS ){
	
			errorDetail = MessageText.getString("DownloadManager.error.ioerror");
					
		}else if ( reason == TOTorrentException.RT_HASH_FAILS ){
			
			errorDetail = MessageText.getString("DownloadManager.error.sha1");
					
		}else if ( reason == TOTorrentException.RT_CANCELLED ){
			
			errorDetail = MessageText.getString("DownloadManager.error.operationcancancelled");
						
		}else{
	 	     
			errorDetail = Debug.getNestedExceptionMessage(e);
		}
					
		String	msg = Debug.getNestedExceptionMessage(e);
				
		if ( errorDetail.indexOf( msg ) == -1){
				
			errorDetail += " (" + msg + ")";
		}
		
		return( errorDetail );
	}
	
	public static String
	announceGroupsToText(
		TOTorrent	torrent )
	{
		URL	announce_url = torrent.getAnnounceURL();
		
		String announce_url_str = announce_url==null?"":announce_url.toString().trim();
		
		TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();
		
		TOTorrentAnnounceURLSet[]	sets = group.getAnnounceURLSets();
		
		if ( sets.length == 0 ){
		
			return( announce_url_str );
			
		}else{
			
			StringBuffer	sb = new StringBuffer(1024);
			
			boolean	announce_found = false;
			
			for (int i=0;i<sets.length;i++){
											
				TOTorrentAnnounceURLSet	set = sets[i];
				
				URL[]	urls = set.getAnnounceURLs();
				
				if ( urls.length > 0 ){
				
					for (int j=0;j<urls.length;j++){
				
						String	str = urls[j].toString().trim();
						
						if ( str.equals( announce_url_str )){
							
							announce_found = true;
						}
						
						sb.append( str );
						sb.append( "\r\n" );
					}
					
					sb.append( "\r\n" );
				}
			}
			
			String result = sb.toString().trim();
		
			if ( !announce_found ){
				
				if ( announce_url_str.length() > 0 ){
					
					if ( result.length() == 0 ){
						
						result = announce_url_str;
						
					}else{
						
						result = "\r\n\r\n" + announce_url_str;
					}
				}
			}
			
			return( result );
		}
	}

	public static String
	announceGroupsToText(
		List<List<String>>	group )
	{
		StringBuffer	sb = new StringBuffer(1024);
			
		for ( List<String> urls: group ){
			
			if ( sb.length() > 0 ){
				
				sb.append( "\r\n" );
			}
			
			for ( String str: urls ){
				
				sb.append( str );
				sb.append( "\r\n" );
			}
		}
		
		return( sb.toString().trim());
	}
	
	public static List<List<String>>
	announceTextToGroups(
		String	text )
	{
		List<List<String>>	groups = new ArrayList<List<String>>();
		
		String[]	lines = text.split( "\n" );
		
		List<String>	current_group = new ArrayList<String>();
		
		Set<String>	hits = new HashSet<String>();
		
		for( String line: lines ){
			
			line = line.trim();
			
			if ( line.length() == 0 ){
				
				if ( current_group.size() > 0 ){
					
					groups.add( current_group );
					
					current_group = new ArrayList<String>();
				}
			}else{
				String lc_line = line.toLowerCase();
				
				if ( hits.contains( lc_line )){
					
					continue;
				}
				
				hits.add( lc_line );
				
				current_group.add( line );
			}
		}
		
		if ( current_group.size() > 0 ){
			
			groups.add( current_group );
		}
		
		return( groups );
	}
	
	public static List<List<String>>
	announceGroupsToList(
		TOTorrent	torrent )
	{
		List<List<String>>	groups = new ArrayList<List<String>>();
		
		TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();
		
		TOTorrentAnnounceURLSet[]	sets = group.getAnnounceURLSets();
		
		if ( sets.length == 0 ){
		
			List<String>	s = new ArrayList<String>();
			
			s.add( UrlUtils.getCanonicalString( torrent.getAnnounceURL()));
			
			groups.add(s);
			
		}else{
			
			Set<String>	all_urls = new HashSet<String>();
			
			for (int i=0;i<sets.length;i++){
			
				List<String>	s = new ArrayList<String>();
								
				TOTorrentAnnounceURLSet	set = sets[i];
				
				URL[]	urls = set.getAnnounceURLs();
				
				for (int j=0;j<urls.length;j++){
				
					String u = UrlUtils.getCanonicalString( urls[j] );
					
					s.add( u );
					
					all_urls.add( u );
				}
				
				if ( s.size() > 0 ){
					
					groups.add(s);
				}
			}
			
			String a = UrlUtils.getCanonicalString( torrent.getAnnounceURL());
			
			if ( !all_urls.contains( a )){
				
				List<String>	s = new ArrayList<String>();

				s.add( a );
				
				groups.add( 0, s );
			}
		}
		
		return( groups );
	}
	
	public static void
	listToAnnounceGroups(
		List<List<String>>		groups,
		TOTorrent				torrent )
	{
		try{
			TOTorrentAnnounceURLGroup tg = torrent.getAnnounceURLGroup();
			
			if ( groups.size() == 1 ){
				
				List	set = (List)groups.get(0);
				
				if ( set.size() == 1 ){
					
					torrent.setAnnounceURL( new URL((String)set.get(0)));
					
					tg.setAnnounceURLSets( new TOTorrentAnnounceURLSet[0]);
					
					return;
				}
			}
			
			
			Vector	g = new Vector();
			
			for (int i=0;i<groups.size();i++){
				
				List	set = (List)groups.get(i);
				
				URL[]	urls = new URL[set.size()];
				
				for (int j=0;j<set.size();j++){
				
					urls[j] = new URL((String)set.get(j));
				}
				
				if ( urls.length > 0 ){
					
					g.add( tg.createAnnounceURLSet( urls ));
				}
			}
			
			TOTorrentAnnounceURLSet[]	sets = new TOTorrentAnnounceURLSet[g.size()];
			
			g.copyInto( sets );
			
			tg.setAnnounceURLSets( sets );
			
			if ( sets.length == 0 ){
			
					// hmm, no valid urls at all
				
				torrent.setAnnounceURL( new URL( "http://no.valid.urls.defined/announce"));
			}
			
		}catch( MalformedURLException e ){
			
			Debug.printStackTrace( e );
		}
	}
	
	public static void
	announceGroupsInsertFirst(
		TOTorrent	torrent,
		String		first_url )
	{
		try{
			
			announceGroupsInsertFirst( torrent, new URL( first_url ));
			
		}catch( MalformedURLException e ){
			
			Debug.printStackTrace( e );
		}
	}
	
	public static void
	announceGroupsInsertFirst(
		TOTorrent	torrent,
		URL			first_url )
	{
		announceGroupsInsertFirst( torrent, new URL[]{ first_url });
	}
	
	public static void
	announceGroupsInsertFirst(
		TOTorrent	torrent,
		URL[]		first_urls )
	{
		TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();
		
		TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

		TOTorrentAnnounceURLSet set1 = group.createAnnounceURLSet( first_urls );
		
		
		if ( sets.length > 0 ){
			
			TOTorrentAnnounceURLSet[]	new_sets = new TOTorrentAnnounceURLSet[sets.length+1];
			
			new_sets[0] = set1;
			
			System.arraycopy( sets, 0, new_sets, 1, sets.length );
			
			group.setAnnounceURLSets( new_sets );
					
		}else{
			
			TOTorrentAnnounceURLSet set2 = group.createAnnounceURLSet(new URL[]{torrent.getAnnounceURL()});
			
			group.setAnnounceURLSets(
				new  TOTorrentAnnounceURLSet[]{ set1, set2 });
		}
	}
	
	public static void
	announceGroupsInsertLast(
		TOTorrent	torrent,
		URL[]		first_urls )
	{
		TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();
		
		TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

		TOTorrentAnnounceURLSet set1 = group.createAnnounceURLSet( first_urls );
		
		
		if ( sets.length > 0 ){
			
			TOTorrentAnnounceURLSet[]	new_sets = new TOTorrentAnnounceURLSet[sets.length+1];
			
			new_sets[sets.length] = set1;
			
			System.arraycopy( sets, 0, new_sets, 0, sets.length );
			
			group.setAnnounceURLSets( new_sets );
					
		}else{
			
			TOTorrentAnnounceURLSet set2 = group.createAnnounceURLSet(new URL[]{torrent.getAnnounceURL()});
			
			group.setAnnounceURLSets(
				new  TOTorrentAnnounceURLSet[]{ set2, set1 });
		}
	}
		
	public static void
	announceGroupsSetFirst(
		TOTorrent	torrent,
		String		first_url )
	{
		List	groups = announceGroupsToList( torrent );
		
		boolean	found = false;
	
		outer:
		for (int i=0;i<groups.size();i++){
			
			List	set = (List)groups.get(i);
			
			for (int j=0;j<set.size();j++){
		
				if ( first_url.equals(set.get(j))){
			
					set.remove(j);
					
					set.add(0, first_url);
					
					groups.remove(set);
					
					groups.add(0,set);
	
					found = true;
					
					break outer;
				}
			}
		}
		
		if ( !found ){
			
			System.out.println( "TorrentUtils::announceGroupsSetFirst - failed to find '" + first_url + "'" );
		}
		
		listToAnnounceGroups( groups, torrent );
	}
	
	public static boolean
	announceGroupsContainsURL(
		TOTorrent	torrent,
		String		url )
	{
		List	groups = announceGroupsToList( torrent );
		
		for (int i=0;i<groups.size();i++){
			
			List	set = (List)groups.get(i);
			
			for (int j=0;j<set.size();j++){
		
				if ( url.equals(set.get(j))){
			
					return( true );
				}
			}
		}
		
		return( false );
	}
	
	public static boolean
	mergeAnnounceURLs(
		TOTorrent 	new_torrent,
		TOTorrent	dest_torrent )
	{
		if ( new_torrent == null || dest_torrent == null ){
			
			return( false);
		}
		
		List	new_groups 	= announceGroupsToList( new_torrent );
		List 	dest_groups = announceGroupsToList( dest_torrent );
		
		List	groups_to_add = new ArrayList();
		
		for (int i=0;i<new_groups.size();i++){
			
			List new_set = (List)new_groups.get(i);
			
			boolean	match = false;
			
			for (int j=0;j<dest_groups.size();j++){
				
				List dest_set = (List)dest_groups.get(j);
				
				boolean same = new_set.size() == dest_set.size();
				
				if ( same ){
					
					for (int k=0;k<new_set.size();k++){
						
						String new_url = (String)new_set.get(k);
						
						if ( !dest_set.contains(new_url)){
							
							same = false;
							
							break;
						}
					}
				}
				
				if ( same ){
					
					match = true;
					
					break;
				}
			}
			
			if ( !match ){
		
				groups_to_add.add( new_set );
			}
		}
		
		if ( groups_to_add.size() == 0 ){
			
			return( false );
		}
		
		for (int i=0;i<groups_to_add.size();i++){
			
			dest_groups.add(i,groups_to_add.get(i));
		}
				
		listToAnnounceGroups( dest_groups, dest_torrent );
		
		return( true );
	}
	
	public static boolean
	replaceAnnounceURL(
		TOTorrent		torrent,
		URL				old_url,
		URL				new_url )
	{
		boolean	found = false;
		
		String	old_str = old_url.toString();
		String	new_str = new_url.toString();
		
		List	l = announceGroupsToList( torrent );
		
		for (int i=0;i<l.size();i++){
			
			List	set = (List)l.get(i);
			
			for (int j=0;j<set.size();j++){
		
				if (((String)set.get(j)).equals(old_str)){
					
					found	= true;
					
					set.set( j, new_str );
				}
			}
		}
		
		if ( found ){
			
			listToAnnounceGroups( l, torrent );
		}
		
		if ( torrent.getAnnounceURL().toString().equals( old_str )){
			
			torrent.setAnnounceURL( new_url );
			
			found	= true;
		}
		
		if ( found ){
			
			try{
				writeToFile( torrent );
				
			}catch( Throwable e ){
				
				Debug.printStackTrace(e);
				
				return( false );
			}
		}
		
		return( found );
	}
	
	public static String
	getLocalisedName(
		TOTorrent		torrent )
	{
		if (torrent == null) {
			return "";
		}
		try{
			String utf8Name = torrent.getUTF8Name();
			if (utf8Name != null) {
				return utf8Name;
			}
			
			LocaleUtilDecoder decoder = LocaleTorrentUtil.getTorrentEncodingIfAvailable( torrent );
			
			if ( decoder == null ){
				
				return( new String(torrent.getName(),Constants.DEFAULT_ENCODING));
			}
			
			return( decoder.decodeString(torrent.getName()));
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
			
			return( new String( torrent.getName()));
		}
	}
	
	public static void
	setTLSDescription(
		String		desc )
	{
		tls.get().put( "desc", desc );
	}
	
	public static String
	getTLSDescription()
	{
		return((String)tls.get().get( "desc" ));
	}
	
		/**
		 * get tls for cloning onto another thread
		 * @return
		 */
	
	public static Object
	getTLS()
	{
		return( new HashMap<String,Object>(tls.get()));
	}
	
	public static void
	setTLS(
		Object	obj )
	{
		Map<String,Object>	m = (Map<String,Object>)obj;
		
		Map<String,Object> tls_map = tls.get();
		
		tls_map.clear();
		
		tls_map.putAll(m);
	}
	
    public static URL getDecentralisedEmptyURL() {
        try {
            return (new URL("dht://"));

        } catch (Throwable e) {

            Debug.printStackTrace(e);

            return (null);
        }
    }
	
	public static URL
	getDecentralisedURL(
		byte[]		hash )
	{
		try{
			return( new URL( "dht://" + ByteFormatter.encodeString( hash ) + ".dht/announce" ));
			
		}catch( Throwable e ){
			
			Debug.out( e );
			
			return( getDecentralisedEmptyURL());
		}
	}
	
	public static URL
	getDecentralisedURL(
		TOTorrent	torrent )
	{
		try{
			return( new URL( "dht://" + ByteFormatter.encodeString( torrent.getHash()) + ".dht/announce" ));
			
		}catch( Throwable e ){
			
			Debug.out( e );
			
			return( getDecentralisedEmptyURL());
		}
	}

	public static void
	setDecentralised(
		TOTorrent	torrent )
	{
   		torrent.setAnnounceURL( getDecentralisedURL( torrent ));
	}
		
	public static boolean
	isDecentralised(
		TOTorrent		torrent )
	{
		if ( torrent == null ){
			
			return( false );
		}
		
		return( torrent.isDecentralised());
	}
	

	public static boolean
	isDecentralised(
		URL		url )
	{
		if ( url == null ){
			
			return( false );
		}
		
		return( url.getProtocol().equalsIgnoreCase( "dht" ));
	}
	
	private static Map
	getAzureusProperties(
		TOTorrent	torrent )
	{
		Map	m = torrent.getAdditionalMapProperty( TOTorrent.AZUREUS_PROPERTIES );
		
		if ( m == null ){
			
			m = new HashMap();
			
			torrent.setAdditionalMapProperty( TOTorrent.AZUREUS_PROPERTIES, m );
		}
		
		return( m );
	}
	
	private static Map
	getAzureusPrivateProperties(
		TOTorrent	torrent )
	{
		Map	m = torrent.getAdditionalMapProperty( TOTorrent.AZUREUS_PRIVATE_PROPERTIES );
		
		if ( m == null ){
			
			m = new HashMap();
			
			torrent.setAdditionalMapProperty( TOTorrent.AZUREUS_PRIVATE_PROPERTIES, m );
		}
		
		return( m );
	}
	
	public static String
	getObtainedFrom(
		TOTorrent		torrent )
	{
		Map	m = getAzureusPrivateProperties( torrent );

		byte[]	from = (byte[])m.get( TORRENT_AZ_PROP_OBTAINED_FROM );
		
		if ( from != null ){
			
			try{
				return( new String( from, "UTF-8" ));
				
			}catch( Throwable e ){
			
				Debug.printStackTrace(e);
			}
		}
		
		return( null );
	}
	
	public static void
	setPeerCache(
		TOTorrent		torrent,
		Map				pc )
	{
		Map	m = getAzureusPrivateProperties( torrent );
			
		try{
			m.put( TORRENT_AZ_PROP_PEER_CACHE, pc );
						
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
		}
	}
	
	public static void
	setFlag(
		TOTorrent		torrent,
		int				flag,
		boolean			value )
	{
		Map	m = getAzureusProperties( torrent );
		
		Long	flags = (Long)m.get( TORRENT_AZ_PROP_TORRENT_FLAGS );
		
		if ( flags == null ){
			
			flags = new Long(0);
		}		
		
		m.put( TORRENT_AZ_PROP_TORRENT_FLAGS, new Long(flags.intValue() | flag ));
	}
		
	public static boolean
	getFlag(
		TOTorrent		torrent,
		int				flag )
	{
		Map	m = getAzureusProperties( torrent );
		
		Long	flags = (Long)m.get( TORRENT_AZ_PROP_TORRENT_FLAGS );
		
		if ( flags == null ){
			
			return( false );
		}

		return(( flags.intValue() & flag ) != 0 );
	}
	
	public static Map<Integer,File>
	getInitialLinkage(
		TOTorrent		torrent )
	{
		Map<Integer,File>	result = new HashMap<Integer, File>();
		
		try{
			Map	pp = torrent.getAdditionalMapProperty( TOTorrent.AZUREUS_PRIVATE_PROPERTIES );
			
			if ( pp != null ){
				
				Map<String,String> links;
				
				byte[]	g_data = (byte[])pp.get( TorrentUtils.TORRENT_AZ_PROP_INITIAL_LINKAGE2 );
				
				if ( g_data == null ){
				
					links = (Map<String,String>)pp.get( TorrentUtils.TORRENT_AZ_PROP_INITIAL_LINKAGE );
					
				}else{
					
					links = (Map<String,String>)BDecoder.decode(new BufferedInputStream( new GZIPInputStream( new ByteArrayInputStream( g_data ))));

				}
				if ( links != null ){//&& TorrentUtils.isCreatedTorrent( torrent )){
					
					links = BDecoder.decodeStrings( links );
					
					for ( Map.Entry<String,String> entry: links.entrySet()){
						
						int		file_index 	= Integer.parseInt( entry.getKey());
						String	file		= entry.getValue();
					
						result.put( file_index, new File( file ));
					}
				}
			}
		}catch( Throwable e ){
			
			Debug.out( "Failed to read linkage map", e );
		}
		
		return( result );
	}
	
	public static void
	setPluginStringProperty(
		TOTorrent		torrent,
		String			name,
		String			value )
	{
		Map	m = getAzureusProperties( torrent );
		
		Object obj = m.get( TORRENT_AZ_PROP_PLUGINS );
		
		Map	p;
		
		if ( obj instanceof Map ){
			
			p = (Map)obj;
			
		}else{
			
			p = new HashMap();
			
			m.put( TORRENT_AZ_PROP_PLUGINS, p );
		}
		
		if ( value == null ){
			
			p.remove( name );
			
		}else{
			
			p.put( name, value.getBytes());
		}
	}
	
	public static String
	getPluginStringProperty(
		TOTorrent		torrent,
		String			name )
	{
		Map	m = getAzureusProperties( torrent );
		
		Object	obj = m.get( TORRENT_AZ_PROP_PLUGINS );
		
		if ( obj instanceof Map ){
		
			Map p = (Map)obj;
			
			obj = p.get( name );
			
			if ( obj instanceof byte[]){
			
				return( new String((byte[])obj));
			}
		}
		
		return( null );
	}
	
	public static void
	setPluginMapProperty(
		TOTorrent		torrent,
		String			name,
		Map				value )
	{
		Map	m = getAzureusProperties( torrent );
		
		Object obj = m.get( TORRENT_AZ_PROP_PLUGINS );
		
		Map	p;
		
		if ( obj instanceof Map ){
			
			p = (Map)obj;
			
		}else{
			
			p = new HashMap();
			
			m.put( TORRENT_AZ_PROP_PLUGINS, p );
		}
		
		if ( value == null ){
			
			p.remove( name );
			
		}else{
			
			p.put( name, value );
		}
	}
	
	public static Map
	getPluginMapProperty(
		TOTorrent		torrent,
		String			name )
	{
		Map	m = getAzureusProperties( torrent );
		
		Object	obj = m.get( TORRENT_AZ_PROP_PLUGINS );
		
		if ( obj instanceof Map ){
		
			Map p = (Map)obj;
			
			obj = p.get( name );
			
			if ( obj instanceof Map ){
		
				return((Map)obj);
			}
		}
		
		return( null );
	}
	
	public static void
	setDHTBackupEnabled(
		TOTorrent		torrent,
		boolean			enabled )
	{
		Map	m = getAzureusProperties( torrent );
		
		m.put( TORRENT_AZ_PROP_DHT_BACKUP_ENABLE, new Long(enabled?1:0));
	}
	
	public static boolean
	getDHTBackupEnabled(
		TOTorrent	torrent )
	{
			// missing -> true
		
		Map	m = getAzureusProperties( torrent );
		
		Object	obj = m.get( TORRENT_AZ_PROP_DHT_BACKUP_ENABLE );
		
		if ( obj instanceof Long ){
		
			return( ((Long)obj).longValue() == 1 );
		}
		
		return( true );
	}
	
	public static boolean
	isDHTBackupRequested(
		TOTorrent	torrent )
	{
			// missing -> false
		
		Map	m = getAzureusProperties( torrent );
		
		Object obj = m.get( TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED );
		
		if ( obj instanceof Long ){
		
			return( ((Long)obj).longValue() == 1 );
		}
		
		return( false );
	}
	
	public static void
	setDHTBackupRequested(
		TOTorrent		torrent,
		boolean			requested )
	{
		Map	m = getAzureusProperties( torrent );
		
		m.put( TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED, new Long(requested?1:0));
	}
		
	
	public static boolean isReallyPrivate(TOTorrent torrent) {
		if ( torrent == null ){
			
			return( false );
		}	
		
		if ( UrlUtils.containsPasskey( torrent.getAnnounceURL())){
				
			return torrent.getPrivate();
		}
		
		return false;
	}
	
	public static boolean
	getPrivate(
		TOTorrent		torrent )
	{
		if ( torrent == null ){
			
			return( false );
		}	
			
		return( torrent.getPrivate());
	}
	
	public static void
	setPrivate(
		TOTorrent		torrent,
		boolean			_private )
	{
		if ( torrent == null ){
			
			return;
		}
		
		try{
			torrent.setPrivate( _private );
			
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
		}
	}
	
	
	
	
	
	
		// this class exists to minimise memory requirements by discarding the piece hash values
		// when "idle" 
	
	private static final int	PIECE_HASH_TIMEOUT	= 3*60*1000;
	
	private static Map	torrent_delegates = new WeakHashMap();
	
	private static HashSet	torrentFluffKeyset = new HashSet(2);
	private static Map		fluffThombstone = new HashMap(1);
	
	/**
	 * Register keys that are used for heavyweight maps that should be discarded when the torrent is not in use
	 * Make sure these keys are only ever used for Map objects!
	 */
	public static void
	registerMapFluff(
		String[]		fluff )
	{
		synchronized (TorrentUtils.class)
		{
			for (int i = 0; i < fluff.length; i++)
				torrentFluffKeyset.add(fluff[i]);
		}
	}
	
	public interface
	ExtendedTorrent
		extends TOTorrent
	{
		public byte[][]
		peekPieces()
		    		
			throws TOTorrentException;
		
		public void
		setDiscardFluff(
			boolean	discard );
	}
	
	/**
	 * A nice string of a Torrent's hash
	 * 
	 * @param torrent Torrent to fromat hash of
	 * @return Hash string in a nice format
	 */
	public static String nicePrintTorrentHash(TOTorrent torrent) {
		return nicePrintTorrentHash(torrent, false);
	}

	/**
	 * A nice string of a Torrent's hash
	 * 
	 * @param torrent Torrent to fromat hash of
	 * @param tight No spaces between groups of numbers
	 * 
	 * @return Hash string in a nice format
	 */
	public static String nicePrintTorrentHash(TOTorrent torrent, boolean tight) {
		byte[] hash;

		if (torrent == null) {

			hash = new byte[20];
		} else {
			try {
				hash = torrent.getHash();

			} catch (TOTorrentException e) {

				Debug.printStackTrace(e);

				hash = new byte[20];
			}
		}

		return (ByteFormatter.nicePrint(hash, tight));
	}

	/**
	 * Runs a file through a series of test to verify if it is a torrent.
	 * 
	 * @param filename File to test
	 * @return true - file is a valid torrent file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean isTorrentFile(String filename) throws FileNotFoundException, IOException {
	  File check = new File(filename);
	  if (!check.exists())
	    throw new FileNotFoundException("File "+filename+" not found.");
	  if (!check.canRead())
	    throw new IOException("File "+filename+" cannot be read.");
	  if (check.isDirectory())
	    throw new FileIsADirectoryException("File "+filename+" is a directory.");
	  try {
	    TOTorrentFactory.deserialiseFromBEncodedFile(check);
	    return true;
	  } catch (Throwable e) {
	    return false;
	  }
	}
	
	private static final Pattern txt_pattern = Pattern.compile( "(UDP|TCP):([0-9]+)");
		
	public interface
	torrentAttributeListener
	{
		public void
		attributeSet(
			TOTorrent	torrent,
			String		attribute,
			Object		value );
	}
	
	public interface
	TorrentAnnounceURLChangeListener
	{
		public void
		changed();
	}
	
	private static class
	URLGroup
		implements TOTorrentAnnounceURLGroup
	{
		private TOTorrentAnnounceURLGroup		delegate;
		private TOTorrentAnnounceURLSet[]		sets;
		
		private boolean modified;
		
		private
		URLGroup(
			TOTorrentAnnounceURLGroup		_delegate,
			List<TOTorrentAnnounceURLSet>	mod_sets )
		{
			delegate	= _delegate;
			
			sets = mod_sets.toArray( new TOTorrentAnnounceURLSet[mod_sets.size()]);
		}

		public TOTorrentAnnounceURLSet[]
       	getAnnounceURLSets()
		{
			return( sets );
		}
       	
       	public void
       	setAnnounceURLSets(
       		TOTorrentAnnounceURLSet[]	_sets )
       	{
       		modified = true;
       		
       		sets = _sets;
       		
       		delegate.setAnnounceURLSets(_sets );
       	}
       		
       	public TOTorrentAnnounceURLSet
       	createAnnounceURLSet(
       		URL[]	urls )
       	{
       		return( delegate.createAnnounceURLSet( urls ));	
       	}
       	
       	protected boolean
       	hasBeenModified()
       	{
       		return( modified );
       	}
	}
	
	private static class
	DNSTXTPortInfo
	{
		private boolean	is_tcp;
		private int		port;
		
		private
		DNSTXTPortInfo(
			boolean	_is_tcp,
			int		_port )
		{
			is_tcp 	= _is_tcp;
			port	= _port;
		}
		
		private boolean
		sameAs(
			DNSTXTPortInfo		other )
		{
			return( is_tcp == other.is_tcp && port == other.port );
		}
		
		private boolean
		isTCP()
		{
			return( is_tcp );
		}
		
		private int
		getPort()
		{
			return( port );
		}
		
		private String
		getString()
		{
			return( (is_tcp?"TCP" :"UDP ") + port );
		}
	}
}
