 /*
 * Created on Oct 10, 2003
 * Modified Apr 14, 2004 by Alon Rohter
 * Copyright (C) 2003, 2004, 2005, 2006 Aelitis, All Rights Reserved.
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

package com.frostwire.torrent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.istack.internal.logging.Logger;

/**
 * File utility class.
 */
public class FileUtil {
	
  public static final String DIR_SEP = System.getProperty("file.separator");
  
  private static final int	RESERVED_FILE_HANDLE_COUNT	= 4;
	
  private static List		reserved_file_handles 	= new ArrayList();
  
  private static Method reflectOnUsableSpace;
  
  static {

	  try
	  {
		  reflectOnUsableSpace = File.class.getMethod("getUsableSpace", (Class[])null);
	  } catch (NoSuchMethodException e)
	  {
		  reflectOnUsableSpace = null;
	  }
  }

  public static boolean isAncestorOf(File parent, File child) {
	  parent = canonise(parent);
	  child = canonise(child);
	  if (parent.equals(child)) {return true;}
	  String parent_s = parent.getPath();
	  String child_s = child.getPath();
	  if (parent_s.charAt(parent_s.length()-1) != File.separatorChar) {
		  parent_s += File.separatorChar;
	  }
	  return child_s.startsWith(parent_s);
  }
  
  public static File canonise(File file) {
	  try {return file.getCanonicalFile();}
	  catch (IOException ioe) {return file;}
  }
  
  public static String getCanonicalFileName(String filename) {
    // Sometimes Windows use filename in 8.3 form and cannot
    // match .torrent extension. To solve this, canonical path
    // is used to get back the long form

    String canonicalFileName = filename;
    try {
      canonicalFileName = new File(filename).getCanonicalPath();
    }
    catch (IOException ignore) {}
    return canonicalFileName;
  }

  
  public static File getUserFile(String filename) {
    return new File(System.getProperty("user.home"), filename);
  }
  
  /**
   * Deletes the given dir and all files/dirs underneath
   */
  public static boolean recursiveDelete(File f) {
//    String defSaveDir = COConfigurationManager.getStringParameter("Default save path");
//    String moveToDir = COConfigurationManager.getStringParameter("Completed Files Directory", "");
//    
//    try{
//  	  moveToDir = new File(moveToDir).getCanonicalPath();
//    }catch( Throwable e ){
//    }
//    try{
//    	defSaveDir = new File(defSaveDir).getCanonicalPath();
//    }catch( Throwable e ){
//    }
    
    try {

//      if (f.getCanonicalPath().equals(moveToDir)) {
//        System.out.println("FileUtil::recursiveDelete:: not allowed to delete the MoveTo dir !");
//        return( false );
//      }
//      if (f.getCanonicalPath().equals(defSaveDir)) {
//        System.out.println("FileUtil::recursiveDelete:: not allowed to delete the default data dir !");
//        return( false );
//      }
      
      if (f.isDirectory()) {
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
          if ( !recursiveDelete(files[i])){
        	  
        	  return( false );
          }
        }
        if ( !f.delete()){
        	
        	return( false );
        }
      }
      else {
        if ( !f.delete()){
        	
        	return( false );
        }
      }
    } catch (Exception ignore) {/*ignore*/}
    
    return( true );
  }

  public static boolean recursiveDeleteNoCheck(File f) {
    try {
      if (f.isDirectory()) {
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
          if ( !recursiveDeleteNoCheck(files[i])){
        	  
        	  return( false );
          }
        }
        if ( !f.delete()){
        	
        	return( false );
        }
      }
      else {
        if ( !f.delete()){
        	
        	return( false );
        }
      }
    } catch (Exception ignore) {/*ignore*/}
    
    return( true );
  }  
 
  public static long
  getFileOrDirectorySize(
  	File		file )
  {
  	if ( file.isFile()){
  		
  		return( file.length());
  		
  	}else{
  		
  		long	res = 0; 
  			
  		File[] files = file.listFiles();
  		
  		if ( files != null ){
  			
  			for (int i=0;i<files.length;i++){
  				
  				res += getFileOrDirectorySize( files[i] );
  			}
  		}
  		
  		return( res );
  	}
  }
  
  public static String
  convertOSSpecificChars(
  	String		file_name_in,
  	boolean		is_folder )
  {
  		// this rule originally from DiskManager
 
  	char[]	chars = file_name_in.toCharArray();
  	
  	for (int i=0;i<chars.length;i++){
  		
  		if ( chars[i] == '"' ){
  			
  			chars[i] = '\'';
  		}
  	}
  	
  	if ( !Constants.isOSX ){
  		
  		if ( Constants.isWindows ){
  			
  				//  this rule originally from DiskManager

	 		// The definitive list of characters permitted for Windows is defined here:
	 		// http://support.microsoft.com/kb/q120138/
  			String not_allowed = "\\/:?*<>|"; 
  		 	for (int i=0;i<chars.length;i++){
  		 		if (not_allowed.indexOf(chars[i]) != -1) {
  		  			chars[i] = '_';
  		  		}
  		  	}
  		 	
  		 	// windows doesn't like trailing dots and whitespaces in folders, replace them
   		 	
  		 	if ( is_folder ){
  		 	
  		 		for(int i = chars.length-1;i >= 0 && (chars[i] == '.' || chars[i] == ' ');chars[i] = '_',i--);
  		 	}
  		}
  		
  			// '/' is valid in mac file names, replace with space
  			// so it seems are cr/lf
  		
	 	for (int i=0;i<chars.length;i++){
		 		
			char	c = chars[i];
				
			if ( c == '/' || c == '\r' || c == '\n'  ){
		  			
				chars[i] = ' ';
			}
		}
  	}

  	String	file_name_out = new String(chars);
  	
	try{
		
			// mac file names can end in space - fix this up by getting
			// the canonical form which removes this on Windows
		
			// however, for soem reason getCanonicalFile can generate high CPU usage on some user's systems
			// in  java.io.Win32FileSystem.canonicalize
			// so changing this to only be used on non-windows
		
		if ( Constants.isWindows ){
			
			while( file_name_out.endsWith( " " )){
				
				file_name_out = file_name_out.substring(0,file_name_out.length()-1);
			}
			
		}else{
			
			String str = new File(file_name_out).getCanonicalFile().toString();
		
			int	p = str.lastIndexOf( File.separator );
			
			file_name_out = str.substring(p+1);
		}
		
	}catch( Throwable e ){
		// ho hum, carry on, it'll fail later
		//e.printStackTrace();
	}
	
	//System.out.println( "convertOSSpecificChars: " + file_name_in + " ->" + file_name_out );
	
	return( file_name_out );
  }
  
  
  
  	// synchronise it to prevent concurrent attempts to write the same file
  
  
  
  	

  		// synchronised against writes to make sure we get a consistent view
  
  	public static void
	deleteResilientFile(
		File		file )
	{
		file.delete();
		new File( file.getParentFile(), file.getName() + ".bak" ).delete();
	}
	
	/**
     * Backup the given file to filename.bak, removing the old .bak file if necessary.
     * If _make_copy is true, the original file will copied to backup, rather than moved.
     * @param _filename name of file to backup
     * @param _make_copy copy instead of move
     */
    public static void backupFile( final String _filename, final boolean _make_copy ) {
      backupFile( new File( _filename ), _make_copy );
    }
      
    /**
     * Backup the given file to filename.bak, removing the old .bak file if necessary.
     * If _make_copy is true, the original file will copied to backup, rather than moved.
     * @param _file file to backup
     * @param _make_copy copy instead of move
     */
    public static void backupFile( final File _file, final boolean _make_copy ) {
      if ( _file.length() > 0L ) {
        File bakfile = new File( _file.getAbsolutePath() + ".bak" );
        if ( bakfile.exists() ) bakfile.delete();
        if ( _make_copy ) {
          copyFile( _file, bakfile );
        }
        else {
          _file.renameTo( bakfile );
        }
      }
    }
    
    
    /**
     * Copy the given source file to the given destination file.
     * Returns file copy success or not.
     * @param _source_name source file name
     * @param _dest_name destination file name
     * @return true if file copy successful, false if copy failed
     */
    public static boolean copyFile( final String _source_name, final String _dest_name ) {
      return copyFile( new File(_source_name), new File(_dest_name));
    }
    
    /**
     * Copy the given source file to the given destination file.
     * Returns file copy success or not.
     * @param _source source file
     * @param _dest destination file
     * @return true if file copy successful, false if copy failed
     */
    /*
    // FileChannel.transferTo() seems to fail under certain linux configurations.
    public static boolean copyFile( final File _source, final File _dest ) {
      FileChannel source = null;
      FileChannel dest = null;
      try {
        if( _source.length() < 1L ) {
          throw new IOException( _source.getAbsolutePath() + " does not exist or is 0-sized" );
        }
        source = new FileInputStream( _source ).getChannel();
        dest = new FileOutputStream( _dest ).getChannel();
      
        source.transferTo(0, source.size(), dest);
        return true;
      }
      catch (Exception e) {
        Debug.out( e );
        return false;
      }
      finally {
        try {
          if (source != null) source.close();
          if (dest != null) dest.close();
        }
        catch (Exception ignore) {}
      }
    }
    */
    
    public static boolean copyFile( final File _source, final File _dest ) {
      try {
        copyFile( new FileInputStream( _source ), new FileOutputStream( _dest ) );
        return true;
      }
      catch( Throwable e ) {
      	Debug.printStackTrace( e );
        return false;
      }
    }
    
    public static void copyFileWithException( final File _source, final File _dest ) throws IOException{
         copyFile( new FileInputStream( _source ), new FileOutputStream( _dest ) );
    }
    
    public static boolean copyFile( final File _source, final OutputStream _dest, boolean closeInputStream ) {
        try {
          copyFile( new FileInputStream( _source ), _dest, closeInputStream );
          return true;
        }
        catch( Throwable e ) {
        	Debug.printStackTrace( e );
          return false;
        }
      }
    
    	/**
    	 * copys the input stream to the file. always closes the input stream
    	 * @param _source
    	 * @param _dest
    	 * @throws IOException
    	 */
    
    public static void 
    copyFile( 
    	final InputStream 	_source, 
    	final File 			_dest )
    
    	throws IOException
    {
    	FileOutputStream	dest = null;
    
    	boolean	close_input = true;
    	
    	try{
    		dest = new FileOutputStream(_dest);
   
    			// copyFile will close from now on, we don't need to
    		
    		close_input = false;
    		
    		copyFile( _source, dest, true );
    		
    	}finally{
    		
       		try{
    			if(close_input){
    				_source.close();
    			}
    		}catch( IOException e ){
     		}
    		
    		if ( dest != null ){
    			
    			dest.close();
    		}
    	}
    }
    
    public static void 
    copyFile( 
    	final InputStream 	_source, 
    	final File 			_dest,
    	boolean				_close_input_stream )
    
    	throws IOException
    {
    	FileOutputStream	dest = null;
        	
    	boolean	close_input = _close_input_stream;
    	
    	try{
    		dest = new FileOutputStream(_dest);
       		
    		close_input = false;
    		
    		copyFile( _source, dest, close_input );
    		
    	}finally{
    		
       		try{
    			if( close_input ){
    				
    				_source.close();
    			}
    		}catch( IOException e ){
     		}
    		
    		if ( dest != null ){
    			
    			dest.close();
    		}
    	}
    }
    
    public static void 
    copyFile( 
      InputStream   is,
      OutputStream  os ) 
    	
    	throws IOException 
    {
      copyFile(is,os,true);
    }
    
    public static void 
	copyFile( 
		InputStream		is,
		OutputStream	os,
		boolean 		closeInputStream )
	
		throws IOException
	{
    	try{
    		
    		if ( !(is instanceof BufferedInputStream )){
    			
    			is = new BufferedInputStream(is);
    		}
    		
    		byte[]	buffer = new byte[65536*2];
			
    		while(true){
    			
    			int	len = is.read(buffer);
    			
    			if ( len == -1 ){
    				
    				break;
    			}
    			
    			os.write( buffer, 0, len );
    		}
    	}finally{
    		try{
    			if(closeInputStream){
    			  is.close();
    			}
    		}catch( IOException e ){
    			
    		}
    		
    		os.close();
    	}
	}
    
    public static void
    copyFileOrDirectory(
    	File	from_file_or_dir,
    	File	to_parent_dir )
    
    	throws IOException
    {
    	if ( !from_file_or_dir.exists()){
    		
    		throw( new IOException( "File '" + from_file_or_dir.toString() + "' doesn't exist" ));
    	}
    	
    	if ( !to_parent_dir.exists()){
    		
    		throw( new IOException( "File '" + to_parent_dir.toString() + "' doesn't exist" ));
    	}
    	
    	if ( !to_parent_dir.isDirectory()){
    		
    		throw( new IOException( "File '" + to_parent_dir.toString() + "' is not a directory" ));
    	}
    	
    	if ( from_file_or_dir.isDirectory()){
    		
    		File[]	files = from_file_or_dir.listFiles();
    		
    		File	new_parent = new File( to_parent_dir, from_file_or_dir.getName());
    		
    		FileUtil.mkdirs(new_parent);
    		
    		for (int i=0;i<files.length;i++){
    			
    			File	from_file	= files[i];
    			
    			copyFileOrDirectory( from_file, new_parent );
    		}
    	}else{
    		
    		File target = new File( to_parent_dir, from_file_or_dir.getName());
    		
    		if ( !copyFile(  from_file_or_dir, target )){
    			
    			throw( new IOException( "File copy from " + from_file_or_dir + " to " + target + " failed" ));
    		}
    	}
    }
    
    /**
     * Returns the file handle for the given filename or it's
     * equivalent .bak backup file if the original doesn't exist
     * or is 0-sized.  If neither the original nor the backup are
     * available, a null handle is returned.
     * @param _filename root name of file
     * @return file if successful, null if failed
     */
    public static File getFileOrBackup( final String _filename ) {
      try {
        File file = new File( _filename );
        //make sure the file exists and isn't zero-length
        if ( file.length() <= 1L ) {
          //if so, try using the backup file
          File bakfile = new File( _filename + ".bak" );
          if ( bakfile.length() <= 1L ) {
            return null;
          }
          else return bakfile;
        }
        else return file;
      }
      catch (Exception e) {
        Debug.out( e );
        return null;
      }
    }

    public static File
    getJarFileFromClass(
    	Class		cla )
    {
    	try{
	    	String str = cla.getName();
	    	
	    	str = str.replace( '.', '/' ) + ".class";
	    	
	        URL url = cla.getClassLoader().getResource( str );
	        
	        if ( url != null ){
	        	
	        	String	url_str = url.toExternalForm();
	
	        	if ( url_str.startsWith("jar:file:")){
	
	        		File jar_file = FileUtil.getJarFileFromURL(url_str);
	        		
	        		if ( jar_file != null && jar_file.exists()){
	        			
	        			return( jar_file );
	        		}
	        	}
	        }
    	}catch( Throwable e ){
    		
    		Debug.printStackTrace(e);
    	}

        return( null );
    }
    
    public static File
	getJarFileFromURL(
		String		url_str )
    {
    	if (url_str.startsWith("jar:file:")) {
        	
        	// java web start returns a url like "jar:file:c:/sdsd" which then fails as the file
        	// part doesn't start with a "/". Add it in!
    		// here's an example 
    		// jar:file:C:/Documents%20and%20Settings/stuff/.javaws/cache/http/Dparg.homeip.net/P9090/DMazureus-jnlp/DMlib/XMAzureus2.jar1070487037531!/org/gudy/azureus2/internat/MessagesBundle.properties
    			
        	// also on Mac we don't get the spaces escaped
        	
    		url_str = url_str.replaceAll(" ", "%20" );
        	
        	if ( !url_str.startsWith("jar:file:/")){
        		
       
        		url_str = "jar:file:/".concat(url_str.substring(9));
        	}
        	
        	try{
        			// 	you can see that the '!' must be present and that we can safely use the last occurrence of it
          	
        		int posPling = url_str.lastIndexOf('!');
            
        		String jarName = url_str.substring(4, posPling);
        		
        			//        System.out.println("jarName: " + jarName);
        		
        		URI uri;
        		
        		try{
        			uri = URI.create(jarName);
        			
        			if ( !new File(uri).exists()){
        				
        				throw( new FileNotFoundException());
        			}
        		}catch( Throwable e ){

        			jarName = "file:/" + UrlUtils.encode( jarName.substring( 6 ));
        		
        			uri = URI.create(jarName);
        		}
        		
        		File jar = new File(uri);
        		
        		return( jar );
        		
        	}catch( Throwable e ){
        	
        		Debug.printStackTrace( e );
        	}
    	}
    	
    	return( null );
    }

    public static boolean
    writeStringAsFile(
    	File		file,
    	String		text )
    {
    	try{
    		return( writeBytesAsFile2( file.getAbsolutePath(), text.getBytes( "UTF-8" )));
    		
    	}catch( Throwable e ){
    		
    		Debug.out( e );
    		
    		return( false );
    	}
    }
    
    public static void 
    writeBytesAsFile( 
    	String filename, 
    	byte[] file_data ) 
    {
    		// pftt, this is used by emp so can't fix signature to make more useful
    	
    	writeBytesAsFile2( filename, file_data );
    }
    
    public static boolean 
    writeBytesAsFile2( 
    	String filename, 
    	byte[] file_data ) 
    {
    	try{
    		File file = new File( filename );

    		if ( !file.getParentFile().exists()){
    			
    			file.getParentFile().mkdirs();
    		}
    		
    		FileOutputStream out = new FileOutputStream( file );

    		try{
    			out.write( file_data );

     		}finally{
     			
       			out.close();
    		}
     		
    		return( true );
    		
    	}catch( Throwable t ){
    		
    		Debug.out( "writeBytesAsFile:: error: ", t );
    	
    		return( false );
    	}
    }
    
	public static String 
	translateMoveFilePath(
		String old_root, 
		String new_root, 
		String file_to_move )
	{
			// we're trying to get the bit from the file_to_move beyond the old_root and append it to the new_root
		
		if ( !file_to_move.startsWith(old_root)){
			
			return null;
		}
	
		if ( old_root.equals( new_root )){
			
				// roots are the same -> nothings gonna change
			
			return( file_to_move );
		}
		
		if ( new_root.equals( file_to_move )){
		
				// new root already the same as the from file, nothing to change
			
			return( file_to_move );
		}
		
		String file_suffix = file_to_move.substring(old_root.length());
		
		if ( file_suffix.startsWith(File.separator )){
			
			file_suffix = file_suffix.substring(1);
			
		}else{
				// hack to deal with special known case of this
				// old_root:  c:\fred\jim.dat
				// new_root:  c:\temp\egor\grtaaaa
				// old_file:  c:\fred\jim.dat.az!
				
			if ( new_root.endsWith( File.separator )){
			
				Debug.out( "Hmm, this is not going to work out well... " + old_root + ", " + new_root + ", " + file_to_move );

			}else{
				
					// deal with case where new root already has the right suffix
				
				if ( new_root.endsWith( file_suffix )){
					
					return( new_root );
				}
				
				return( new_root + file_suffix );
			}
		}
		
		if ( new_root.endsWith(File.separator)){
		
			new_root = new_root.substring( 0, new_root.length()-1 );
		}
	
		return new_root + File.separator + file_suffix;
	}
	
	/**
	 * Makes Directories as long as the directory isn't directly in Volumes (OSX)
	 * @param f
	 * @return
	 */
	public static boolean mkdirs(File f) {
		if (Constants.isOSX) {
			Pattern pat = Pattern.compile("^(/Volumes/[^/]+)");
			Matcher matcher = pat.matcher(f.getParent());
			if (matcher.find()) {
				String sVolume = matcher.group();
				File fVolume = new File(sVolume);
				if (!fVolume.isDirectory()) {
					System.out.println(sVolume
							+ " is not mounted or not available.");
					return false;
				}
			}
		}
		return f.mkdirs();
	}
	
	public static String getExtension(String fName) {
		final int fileSepIndex = fName.lastIndexOf(File.separator);
		final int fileDotIndex = fName.lastIndexOf('.');
		if (fileSepIndex == fName.length() - 1 || fileDotIndex == -1
				|| fileSepIndex > fileDotIndex) {
			return "";
		}
		
		return fName.substring(fileDotIndex);
	}

	public static String
	readFileAsString(
		File	file,
		int		size_limit,
		String charset)
	
		throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		try {
			return readInputStreamAsString(fis, size_limit, charset);
		} finally {

			fis.close();
		}
	}

	public static String
	readFileAsString(
		File	file,
		int		size_limit )
	
		throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		try {
			return readInputStreamAsString(fis, size_limit);
		} finally {

			fis.close();
		}
	}
		
	public static String
	readInputStreamAsString(
		InputStream is,
		int		size_limit )
	
		throws IOException
	{
		return readInputStreamAsString(is, size_limit, "ISO-8859-1");
	}

	public static String
	readInputStreamAsString(
		InputStream is,
		int		size_limit,
		String charSet)
	
		throws IOException
	{
		StringBuffer result = new StringBuffer(1024);

		byte[] buffer = new byte[1024];

		while (true) {

			int len = is.read(buffer);

			if (len <= 0) {

				break;
			}

			result.append(new String(buffer, 0, len, charSet));

			if (size_limit >= 0 && result.length() > size_limit) {

				result.setLength(size_limit);

				break;
			}
		}

		return (result.toString());
	}

	public static String
	readInputStreamAsStringWithTruncation(
		InputStream 	is,
		int				size_limit )
	
		throws IOException
	{
		StringBuffer result = new StringBuffer(1024);

		byte[] buffer = new byte[1024];

		try{
			while (true) {
	
				int len = is.read(buffer);
	
				if (len <= 0) {
	
					break;
				}
	
				result.append(new String(buffer, 0, len, "ISO-8859-1"));
	
				if (size_limit >= 0 && result.length() > size_limit) {
	
					result.setLength(size_limit);
	
					break;
				}
			}
		}catch( SocketTimeoutException e ){
		}

		return (result.toString());
	}
	
	public static String
	readFileEndAsString(
		File	file,
		int		size_limit )
	
		throws IOException
	{
		FileInputStream	fis = new FileInputStream( file );
		
		try{
			if (file.length() > size_limit) {
				fis.skip(file.length() - size_limit);
			}
			
			StringBuffer	result = new StringBuffer(1024);
			
			byte[]	buffer = new byte[1024];
			
			while( true ){
			
				int	len = fis.read( buffer );
			
				if ( len <= 0 ){
					
					break;
				}
			
				result.append( new String( buffer, 0, len, "ISO-8859-1" ));
				
				if ( result.length() > size_limit ){
					
					result.setLength( size_limit );
					
					break;
				}
			}
			
			return( result.toString());
			
		}finally{
			
			fis.close();
		}
	}
	
	public static byte[]
	readInputStreamAsByteArray(
		InputStream		is )
	   	
	   	throws IOException
	{
		return( readInputStreamAsByteArray( is, Integer.MAX_VALUE ));
	}
	
	public static byte[]
	readInputStreamAsByteArray(
		InputStream		is,
		int				size_limit )
	
		throws IOException
	{
		ByteArrayOutputStream	baos = new ByteArrayOutputStream(32*1024);
		
		byte[]	buffer = new byte[32*1024];
		
		while( true ){
			
			int	len = is.read( buffer );
			
			if ( len <= 0 ){
				
				break;
			}
			
			baos.write( buffer, 0, len );
			
			if ( baos.size() > size_limit ){
				
				throw( new IOException( "size limit exceeded" ));
			}
		}
		
		return( baos.toByteArray());
	}
	
	public static byte[]
   	readFileAsByteArray(
   		File		file )
   	
   		throws IOException
   	{
   		ByteArrayOutputStream	baos = new ByteArrayOutputStream((int)file.length());
   		
   		byte[]	buffer = new byte[32*1024];
   		
   		InputStream is = new FileInputStream( file );
   		
   		try{
	   		while( true ){
	   			
	   			int	len = is.read( buffer );
	   			
	   			if ( len <= 0 ){
	   				
	   				break;
	   			}
	   			
	   			baos.write( buffer, 0, len );
	   		}
	   		
	   		return( baos.toByteArray());
	   		
   		}finally{
   			
   			is.close();
   		}
   	}
	
	public final static boolean getUsableSpaceSupported()
	{
		return reflectOnUsableSpace != null;
	}
	
	public final static long getUsableSpace(File f)
	{
		try
		{
			return ((Long)reflectOnUsableSpace.invoke(f)).longValue();
		} catch (Exception e)
		{
			return -1;
		}		
	}
	
	/**
		 * Gets the encoding that should be used when writing script files (currently only
		 * tested for windows as this is where an issue can arise...)
		 * We also only test based on the user-data directory name to see if an explicit
		 * encoding switch is requried...
		 * @return null - use default
		 */
	
	private static boolean 	sce_checked;
	private static String	script_encoding;
}
