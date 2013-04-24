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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File utility class.
 */
public class FileUtil {
	
  public static final String DIR_SEP = System.getProperty("file.separator");
  
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
}
