/*
 * Created on Mar 21, 2006 3:09:00 PM
 * Copyright (C) 2006 Aelitis, All Rights Reserved.
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
 */
package com.frostwire.torrent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author TuxPaper
 * @created Mar 21, 2006
 *
 */
public class UrlUtils
{
	
	
	private static final String[] prefixes = new String[] {
			"http://",
			"https://",
			"ftp://",
			"dht://",
			"magnet:?",
			"magnet://?" };

	private static int MAGNETURL_STARTS_AT = 3;	// dht:// is a form of magnet URL
	
	private static final Object[] XMLescapes = new Object[] {
		new String[] { "&", "&amp;" },
		new String[] { ">", "&gt;" },
		new String[] { "<", "&lt;" },
		new String[] { "\"", "&quot;" },
		new String[] { "'", "&apos;" },
	};

	public static String
	getMagnetURI(
		byte[]		hash )
	{
		return( "magnet:?xt=urn:btih:" + Base32.encode( hash ));
	}

	
		/**
		 * returns magnet uri if input is base 32 or base 16 encoded sha1 hash, null otherwise
		 * @param base_hash
		 * @return
		 */
	
	public static String
	normaliseMagnetURI(
		String		base_hash )
	{
		byte[]	hash = decodeSHA1Hash( base_hash );
		
		if ( hash != null ){
			
			return( getMagnetURI( hash ));
		}
		
		return( null );
	}
	
	public static byte[]
	decodeSHA1Hash(
		String	str )
	{
		if ( str == null ){
			
			return( null );
		}
		
		str = str.trim();
		
		byte[] hash = null;
		
		try{
			if ( str.length() == 40 ){
				
				hash = ByteFormatter.decodeString( str );
				
			}else if ( str.length() == 32 ){
				
				hash = Base32.decode( str );
			}
		}catch( Throwable e ){
		}
		
		if ( hash != null ){
			
			if ( hash.length != 20 ){
								
				hash = null;
			}
		}
		
		return( hash );
	}
	
	/**
	 * test string for possibility that it's an URL.  Considers 40 byte hex 
	 * strings as URLs
	 * 
	 * @param sURL
	 * @return
	 */
	public static boolean isURL(String sURL) {
		return parseTextForURL(sURL, true) != null;
	}

	public static boolean isURL(String sURL, boolean bGuess) {
		return parseTextForURL(sURL, true, bGuess) != null;
	}

	public static String parseTextForURL(String text, boolean accept_magnets) {
		return parseTextForURL(text, accept_magnets, true);
	}

	public static String
	getURL(
		String	text )
	{
		return( parseTextForURL(text, false, false ));
	}
	
	public static String parseTextForURL(String text, boolean accept_magnets,
			boolean guess) {

		if (text == null || text.length() < 5) {
			return null;
		}

		String href = parseHTMLforURL(text);
		if (href != null) {
			return href;
		}

		try {
			text = text.trim();
			text = URLDecoder.decode(text);
		} catch (Exception e) {
			// sometimes fires a IllegalArgumentException
			// catch everything and ignore.
		}

		String textLower;
		try {
			textLower = text.toLowerCase();
		} catch (Throwable e) {
			textLower = text;
		}
		int max = accept_magnets ? prefixes.length : MAGNETURL_STARTS_AT;
		int end = -1;
		int start = textLower.length();
		String strURL = null;
		for (int i = 0; i < max; i++) {
			final int testBegin = textLower.indexOf(prefixes[i]);
			if (testBegin >= 0 && testBegin < start) {
				end = text.indexOf("\n", testBegin + prefixes[i].length());
				String strURLTest = (end >= 0) ? text.substring(testBegin, end - 1)
						: text.substring(testBegin);
				try {
					URL parsedURL = new URL(strURLTest);
					strURL = parsedURL.toExternalForm();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					if (i >= MAGNETURL_STARTS_AT) {
						strURL = strURLTest;
					}
				}
			}
		}
		if (strURL != null) {
			return strURL;
		}
		
		if (new File(text).exists()) {
			return null;
		}

		if (accept_magnets
				&& (text.startsWith("bc://") || text.startsWith("bctp://"))) {
			return parseTextForMagnets(text);
		}

		// accept raw hash of 40 hex chars
		if (accept_magnets && text.matches("^[a-fA-F0-9]{40}$")) {
			// convert from HEX to raw bytes
			byte[] infohash = ByteFormatter.decodeString(text.toUpperCase());
			// convert to BASE32
			return "magnet:?xt=urn:btih:" + Base32.encode(infohash);
		}

		// accept raw hash of 32 base-32 chars
		if (accept_magnets && text.matches("^[a-zA-Z2-7]{32}$")) {
			return "magnet:?xt=urn:btih:" + text;
		}
		
		// javascript:loadOrAlert('WVOPRHRPFSCLAW7UWHCXCH7QNQIU6TWG')

		// accept raw hash of 32 base-32 chars, with garbage around it
		if (accept_magnets && guess) {
			Pattern pattern = Pattern.compile("[^a-zA-Z2-7][a-zA-Z2-7]{32}[^a-zA-Z2-7]");
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				String hash = text.substring(matcher.start() + 1, matcher.start() + 33);
				return "magnet:?xt=urn:btih:" + hash;
			}

			pattern = Pattern.compile("[^a-fA-F0-9][a-fA-F0-9]{40}[^a-fA-F0-9]");
			matcher = pattern.matcher(text);
			if (matcher.find()) {
				String hash = text.substring(matcher.start() + 1, matcher.start() + 41);
				// convert from HEX to raw bytes
				byte[] infohash = ByteFormatter.decodeString(hash.toUpperCase());
				// convert to BASE32
				return "magnet:?xt=urn:btih:" + Base32.encode(infohash);
			}
		}
		
		return null;
	}

	public static String
	parseTextForMagnets(
		String		text )
	{
		if (text.startsWith("magnet:")) {
			return text;
		}

		// accept raw hash of 40 hex chars
		if (text.matches("^[a-fA-F0-9]{40}$")) {
			// convert from HEX to raw bytes
			byte[] infohash = ByteFormatter.decodeString(text.toUpperCase());
			// convert to BASE32
			return "magnet:?xt=urn:btih:" + Base32.encode(infohash);
		}

		// accept raw hash of 32 base-32 chars
		if (text.matches("^[a-zA-Z2-7]{32}$")) {
			return "magnet:?xt=urn:btih:" + text;
		}
		
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile("magnet:\\?[a-z%0-9=_:&.]+", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group();
		}

		pattern = Pattern.compile("bc://bt/([a-z0-9=\\+/]+)", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(text.replaceAll(" ", "+"));
		if (matcher.find()) {
			String base64 = matcher.group(1);
			byte[] decode = Base64.decode(base64);
			if (decode != null && decode.length > 0) {
				// Format is AA/<name>/<size>/<hash>/ZZ
				try {
					String decodeString = new String(decode, "utf8");
					pattern = Pattern.compile("AA.*/(.*)/ZZ", Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(decodeString);
					if (matcher.find()) {
						String hash = matcher.group(1);
						String magnet = parseTextForMagnets(hash);
						if (magnet != null) {
							pattern = Pattern.compile("AA/(.*)/[0-9]+", Pattern.CASE_INSENSITIVE);
							matcher = pattern.matcher(decodeString);
							if (matcher.find()) {
								String name = matcher.group(1);
								return magnet + "&dn=" + encode(name);
							}
							return magnet;
						}
					}
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

		pattern = Pattern.compile("bctp://task/(.*)", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(text);
		if (matcher.find()) {
			// Format is <name>/<size>/<hash>
			String decodeString = matcher.group(1);
			String magnet = parseTextForMagnets(decodeString);
			if (magnet != null) {
				pattern = Pattern.compile("(.*)/[0-9]+", Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(decodeString);
				if (matcher.find()) {
					String name = matcher.group(1);
					return magnet + "&dn=" + encode(name);
				}
				return magnet;
			}
		}

		// accept raw hash of 32 base-32 chars, with garbage around it
		if (true) {
			text = "!" + text + "!";
			pattern = Pattern.compile("[^a-zA-Z2-7][a-zA-Z2-7]{32}[^a-zA-Z2-7]");
			matcher = pattern.matcher(text);
			if (matcher.find()) {
				String hash = text.substring(matcher.start() + 1, matcher.start() + 33);
				return "magnet:?xt=urn:btih:" + hash;
			}

			pattern = Pattern.compile("[^a-fA-F0-9][a-fA-F0-9]{40}[^a-fA-F0-9]");
			matcher = pattern.matcher(text);
			if (matcher.find()) {
				String hash = text.substring(matcher.start() + 1, matcher.start() + 41);
				// convert from HEX to raw bytes
				byte[] infohash = ByteFormatter.decodeString(hash.toUpperCase());
				// convert to BASE32
				return "magnet:?xt=urn:btih:" + Base32.encode(infohash);
			}
		}
		
		return( null );
	}
	
	private static String parseHTMLforURL(String text) {
		if (text == null) {
			return null;
		}

		// examples:
		// <A HREF=http://abc.om/moo>test</a>
		// <A style=cow HREF="http://abc.om/moo">test</a>
		// <a href="http://www.gnu.org/licenses/fdl.html" target="_top">moo</a>

		Pattern pat = Pattern.compile("<.*a\\s++.*href=\"?([^\\'\"\\s>]++).*",
				Pattern.CASE_INSENSITIVE);
		Matcher m = pat.matcher(text);
		if (m.find()) {
			String sURL = m.group(1);
			try {
				sURL = URLDecoder.decode(sURL);
			} catch (Exception e) {
				// sometimes fires a IllegalArgumentException
				// catch everything and ignore.
			}
			return sURL;
		}

		return null;
	}

	/**
	 * Like URLEncoder.encode, except translates spaces into %20 instead of +
	 * @param s
	 * @return
	 */
	public static String encode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(s).replaceAll("\\+", "%20");
		}
	}
	
	public static String decode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return( URLDecoder.decode(s, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return( URLDecoder.decode(s));
		}
	}
	
	public static String escapeXML(String s) {
		if (s == null) {
			return "";
		}
		String ret = s;
		for (int i = 0; i < XMLescapes.length; i++) {
			String[] escapeEntry = (String[])XMLescapes[i];
			ret = ret.replaceAll(escapeEntry[0], escapeEntry[1]);
		}
		return ret;
	}

	public static String unescapeXML(String s) {
		if (s == null) {
			return "";
		}
		String ret = s;
		for (int i = 0; i < XMLescapes.length; i++) {
			String[] escapeEntry = (String[])XMLescapes[i];
			ret = ret.replaceAll(escapeEntry[1], escapeEntry[0]);
		}
		return ret;
	}
	
	public static String
	convertIPV6Host(
		String	host )
	{
		if ( host.indexOf(':') != -1 ){
			
			return( "[" + host + "]" );
		}
		
		return( host );
	}
	
	public static String
	expandIPV6Host(
		String	host )
	{
		if ( host.indexOf(':') != -1 ){
			
			try{
				return( InetAddress.getByAddress(InetAddress.getByName( host ).getAddress()).getHostAddress());
				
			}catch( Throwable e ){
				
				Debug.printStackTrace(e);
			}
		}
		
		return( host );
	}
	
	public static void
	connectWithTimeout(
		final URLConnection		connection,
		long					connect_timeout )
	
		throws IOException
	{
		connectWithTimeouts( connection, connect_timeout, -1 );
	}
	
	public static void
	connectWithTimeouts(
		final URLConnection		connection,
		long					connect_timeout,
		long					read_timeout )
	
		throws IOException
	{
		if ( connect_timeout != -1 ){
				
			connection.setConnectTimeout( (int)connect_timeout );	
		}
			
		if ( read_timeout != -1 ){
				
			connection.setReadTimeout( (int)read_timeout );	
		}
			
		connection.connect();
	}
	
	
	// private static final String default_headers = "SG9zdDogbG9jYWxob3N0OjQ1MTAwClVzZXItQWdlbnQ6IE1vemlsbGEvNS4wIChXaW5kb3dzOyBVOyBXaW5kb3dzIE5UIDUuMTsgZW4tVVM7IHJ2OjEuOC4xLjE0KSBHZWNrby8yMDA4MDQwNCBGaXJlZm94LzIuMC4wLjE0CkFjY2VwdDogdGV4dC94bWwsYXBwbGljYXRpb24veG1sLGFwcGxpY2F0aW9uL3hodG1sK3htbCx0ZXh0L2h0bWw7cT0wLjksdGV4dC9wbGFpbjtxPTAuOCxpbWFnZS9wbmcsKi8qO3E9MC41CkFjY2VwdC1MYW5ndWFnZTogZW4tdXMsZW47cT0wLjUKQWNjZXB0LUVuY29kaW5nOiBnemlwLGRlZmxhdGUKQWNjZXB0LUNoYXJzZXQ6IElTTy04ODU5LTEsdXRmLTg7cT0wLjcsKjtxPTAuNwpLZWVwLUFsaXZlOiAzMDAKQ29ubmVjdGlvbjoga2VlcC1hbGl2ZQ==";
	private static final String default_headers = "QWNjZXB0OiB0ZXh0L2h0bWwsYXBwbGljYXRpb24veGh0bWwreG1sLGFwcGxpY2F0aW9uL3htbDtxPTAuOSwqLyo7cT0wLjgKQWNjZXB0LUNoYXJzZXQ6IElTTy04ODU5LTEsdXRmLTg7cT0wLjcsKjtxPTAuMwpBY2NlcHQtRW5jb2Rpbmc6IGd6aXAsZGVmbGF0ZQpBY2NlcHQtTGFuZ3VhZ2U6IGVuLVVTLGVuO3E9MC44CkNhY2hlLUNvbnRyb2w6IG1heC1hZ2U9MApDb25uZWN0aW9uOiBrZWVwLWFsaXZlClVzZXItQWdlbnQ6IE1vemlsbGEvNS4wIChXaW5kb3dzIE5UIDYuMTsgV09XNjQpIEFwcGxlV2ViS2l0LzUzNi4xMSAoS0hUTUwsIGxpa2UgR2Vja28pIENocm9tZS8yMC4wLjExMzIuNDcgU2FmYXJpLzUzNi4xMQ==";

	
	
	
	
	public static boolean queryHasParameter(String query_string, String param_name, boolean case_sensitive) {
		if (!case_sensitive) {
			query_string = query_string.toLowerCase();
			param_name = param_name.toLowerCase();
		}
		if (query_string.charAt(0) == '?') {
			query_string = '&' + query_string.substring(1);
		}
		else if (query_string.charAt(0) != '&') {
			query_string = '&' + query_string;
		}
		
		return query_string.indexOf("&" + param_name + "=") != -1;
	}
	
	public static boolean 
	containsPasskey(
		URL		url )
	{
		String url_str = url.toExternalForm();
		
		return( url_str.matches(".*[0-9a-z]{20,40}.*"));
	}
	
	public static URL
	setPort(
		URL		u,
		int		port )
	{
		if ( port == -1 ){
			port = u.getDefaultPort();
		}
		StringBuffer result = new StringBuffer();
		result.append(u.getProtocol());
		result.append(":");
		String authority=u.getAuthority();
		if (authority != null && authority.length() > 0) {
			result.append("//");
			int pos = authority.indexOf( '@' );
			if ( pos != -1 ){
				result.append(authority.substring(0,pos+1));
				authority = authority.substring(pos+1);
			}
			pos = authority.lastIndexOf(':');
			if ( pos == -1 ){
				if ( port > 0 ){
					result.append(authority + ":" + port );
				}else{
					result.append(authority);
				}
			}else{
				if ( port > 0 ){
					result.append(authority.substring(0,pos+1) + port );
				}else{
					result.append(authority.substring(0,pos));
				}
			}
		}
		if (u.getPath() != null) {
			result.append(u.getPath());
		}
		if (u.getQuery() != null) {
			result.append('?');
			result.append(u.getQuery());
		}
		if (u.getRef() != null) {
			result.append("#");
			result.append(u.getRef());
		}
		try{
			return( new URL( result.toString()));
		}catch( Throwable e ){
			Debug.out(e);
			return(u);
		}
	}
	
	public static URL
	setHost(
		URL			u,
		String		host )
	{
		StringBuffer result = new StringBuffer();
		result.append(u.getProtocol());
		result.append(":");
		String authority=u.getAuthority();
		if (authority != null && authority.length() > 0) {
			result.append("//");
			int pos = authority.indexOf( '@' );
			if ( pos != -1 ){
				result.append(authority.substring(0,pos+1));
				authority = authority.substring(pos+1);
			}
			pos = authority.lastIndexOf(':');
			if ( pos == -1 ){
				result.append(host );
			}else{
				result.append(host + authority.substring(pos));				
			}
		}
		if (u.getPath() != null) {
			result.append(u.getPath());
		}
		if (u.getQuery() != null) {
			result.append('?');
			result.append(u.getQuery());
		}
		if (u.getRef() != null) {
			result.append("#");
			result.append(u.getRef());
		}
		try{
			return( new URL( result.toString()));
		}catch( Throwable e ){
			Debug.out(e);
			return(u);
		}
	}
	
	public static URL
	setProtocol(
		URL			u,
		String		protocol )
	{
		String str = u.toExternalForm();
		
		int pos = str.indexOf( ":" );
		
		try{
			return( new URL( protocol + str.substring( pos )));
			
		}catch( Throwable e ){
			
			Debug.out( e );
			
			return( u );
		}
	}
	
	public static URL
	getBaseURL(
		URL		u )
	{
		StringBuffer result = new StringBuffer();
		result.append(u.getProtocol());
		result.append(":");
		String authority=u.getAuthority();
		if (authority != null && authority.length() > 0) {
			result.append("//");
			int pos = authority.indexOf( '@' );
			if ( pos != -1 ){
				result.append(authority.substring(0,pos+1));
				authority = authority.substring(pos+1);
			}
			pos = authority.lastIndexOf(':');
			int	port = u.getPort();
			if ( port == -1 ){
				port = u.getDefaultPort();
			}
			if ( pos == -1 ){
				result.append(authority + ":" + port );
			}else{
				result.append(authority.substring(0,pos+1) + port );				
			}
		}

		try{
			return( new URL( result.toString()));
		}catch( Throwable e ){
			Debug.out(e);
			return(u);
		}
	}
	
	public static String
	getCanonicalString(
		URL		url )
	{
		String protocol = url.getProtocol();
		
		if ( !protocol.equals( protocol.toLowerCase( Locale.US ))){
			
			protocol = protocol.toLowerCase( Locale.US );
			
			url = UrlUtils.setProtocol( url, protocol ); 
		}
		
		int	port = url.getPort();
		
		if ( protocol.equals( "http" ) || protocol.equals( "https" )){
		
			if ( port == url.getDefaultPort()){
				
				url = UrlUtils.setPort( url, 0 );
			}
		}else{
			
			if ( port == -1 ){
				
				url = UrlUtils.setPort( url, url.getDefaultPort());
			}
		}
		
		return( url.toString());
	}
	
		/**
		 * Returns an explicit IPv4 url if the supplied one has both IPv6 and IPv4 addresses
		 * @param url
		 * @return
		 */
	
	public static URL
	getIPV4Fallback(
		URL	url )
	{
		try{
			InetAddress[] addresses = InetAddress.getAllByName( url.getHost());
			
			if ( addresses.length > 0 ){
				
				InetAddress	ipv4	= null;
				InetAddress	ipv6	= null;
				
				for ( InetAddress a: addresses ){
					
					if ( a instanceof Inet4Address ){
						
						ipv4 = a;
						
					}else{
						
						ipv6 = a;
					}
				}
				
				if ( ipv4 != null && ipv6 != null ){
					
					url = UrlUtils.setHost( url, ipv4.getHostAddress());
					
					return( url );
				}
			}
		}catch( Throwable f ){
		}	
		
		return( null );
	}
	
	public static long
	getContentLength(
		URLConnection	con )
	{
		long res = con.getContentLength();
		
		if ( res == -1 ){
			
			try{
				String	str = con.getHeaderField( "content-length" );
				
				if ( str != null ){
					
					res = Long.parseLong( str );
				}
			}catch( Throwable e ){
				
			}
		}
		
		return( res );
	}
}
