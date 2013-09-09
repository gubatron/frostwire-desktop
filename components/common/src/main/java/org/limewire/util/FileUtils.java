/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.limewire.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides file manipulation methods; ensures a file exists, makes a file 
 * writable, renames, saves and deletes a file. 
 */
public class FileUtils {
    
    private static final Log LOG = LogFactory.getLog(FileUtils.class);
    
    public static void writeObject(String fileName, Object obj) 
    throws IOException{
        writeObject(new File(fileName),obj);
    }
    
    /**
     * Writes the passed Object to corresponding file
     * @param file The file to which to write 
     * @param map The Object to be stored
     */
    public static void writeObject(File f, Object obj)
        throws IOException {
        
        try {
            f = getCanonicalFile(f);
        } catch (IOException tryAnyway){}
        
        ObjectOutputStream out = null;
        try {
            //open the file
            out = new ObjectOutputStream(
            		new BufferedOutputStream(
            				new FileOutputStream(f)));
            //write to the file
            out.writeObject(obj);	
            out.flush();
        } finally {
            close(out);
        }
    }
    
    public static Object readObject(String fileName)
    throws IOException, ClassNotFoundException {
        return readObject(new File(fileName));
    }
    
    /**
     * @param file The file from where to read the serialized Object
     * @return The Object that was read
     */
    public static Object readObject(File file)
        throws IOException, ClassNotFoundException {
        
        try {
            file = getCanonicalFile(file);
        } catch (IOException tryAnyway){}
        
        ObjectInputStream in = null;
        try {
            //open the file
            in = new ObjectInputStream(
            		new BufferedInputStream(
            				new FileInputStream(file)));
            //read and return the object
            return in.readObject();	
        } finally {
            close(in);
        }    
    }

    /**
     * Gets the canonical path, catching buggy Windows errors
     */
    public static String getCanonicalPath(File f) throws IOException {
        try {
            return f.getCanonicalPath();
        } catch(IOException ioe) {
            String msg = ioe.getMessage();
            // windows bugs out :(
            if(OSUtils.isWindows() && msg != null && msg.indexOf("There are no more files") != -1)
                return f.getAbsolutePath();
            else
                throw ioe;
        }
    }
    
    /** Same as f.getCanonicalFile() in JDK1.3. */
    public static File getCanonicalFile(File f) throws IOException {
        try {
            return f.getCanonicalFile();
        } catch(IOException ioe) {
            String msg = ioe.getMessage();
            // windows bugs out :(
            if(OSUtils.isWindows() && msg != null && msg.indexOf("There are no more files") != -1)
                return f.getAbsoluteFile();
            else
                throw ioe;
        }
    }
    
    /**
     * Determines if file 'a' is an ancestor of file 'b'.
     */
    public static final boolean isAncestor(File a, File b) {
        while(b != null) {
            if(b.equals(a))
                return true;
            b = b.getParentFile();
        }
        return false;
    }

    /** 
     * Detects attempts at directory traversal by testing if testDirectory 
     * really is the parent of testPath.  This method should be used to make
     * sure directory traversal tricks aren't being used to trick
     * LimeWire into reading or writing to unexpected places.
     * 
     * Directory traversal security problems occur when software doesn't 
     * check if input paths contain characters (such as "../") that cause the
     * OS to go up a directory.  This function will ignore benign cases where
     * the path goes up one directory and then back down into the original directory.
     * 
     * @return false if testParent is not the parent of testChild.
     * @throws IOException if getCanonicalPath throws IOException for either input file
     */
    public static final boolean isReallyParent(File testParent, File testChild) throws IOException {
        // Don't check testDirectory.isDirectory... 
        // If it's not a directory, it won't be the parent anyway.
        // This makes the tests more simple.
        
        String testParentName = getCanonicalPath(testParent);
        String testChildParentName = getCanonicalPath(testChild.getAbsoluteFile().getParentFile());
        if (! testParentName.equals(testChildParentName))
            return false;
        
        return true;
    }
    
    /**
     * Detects attempts at directory traversal by testing if testDirectory 
     * really is a parent of testPath.
     * @see isReallyParent
     */
    public static final boolean isReallyInParentPath(File testParent, File testChild) throws IOException {

    	String testParentName = getCanonicalPath(testParent);
        File testChildParentFile = testChild.getAbsoluteFile().getParentFile();
        if (testChildParentFile == null) 
            testChildParentFile = testChild.getAbsoluteFile();
    	String testChildParentName = getCanonicalPath(testChildParentFile);
    	return testChildParentName.startsWith(testParentName);
    }
    
    /**
     * Utility method that returns the file extension of the given file.
     * 
     * @param f the <tt>File</tt> instance from which the extension 
     *   should be extracted
     * @return the file extension string, or <tt>null</tt> if the extension
     *   could not be extracted
     */
    public static String getFileExtension(File f) {
        String name = f.getName();
        return getFileExtension(name);
    }
     
    /**
     * Utility method that returns the file extension of the given file.
     * 
     * @param name the file name <tt>String</tt> from which the extension
     *  should be extracted
     * @return the file extension string (without the dot), or <tt>null</tt> if the extension
     *   could not be extracted
     */
    public static String getFileExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return null;
        }

        // the file must have a name other than the extension
        if (index == 0) {
            return null;
        }

        // if the last character of the string is the ".", then there's
        // no extension
        if (index == (name.length() - 1)) {
            return null;
        }

        return name.substring(index + 1);
    }
    
    /**
     * Utility method to set a file as non read only.
     * If the file is already writable, does nothing.
     *
     * @param f the <tt>File</tt> instance whose read only flag should
     *  be unset.
     * 
     * @return whether or not <tt>f</tt> is writable after trying to make it
     *  writeable -- note that if the file doesn't exist, then this returns
     *  <tt>true</tt> 
     */
    public static boolean setWriteable(File f) {
        if(!f.exists())
            return true;

        // non Windows-based systems return the wrong value
        // for canWrite when the argument is a directory --
        // writing is based on the 'x' attribute, not the 'w'
        // attribute for directories.
        if(f.canWrite()) {
            if(OSUtils.isWindows())
                return true;
            else if(!f.isDirectory())
                return true;
        }
            
        String fName;
        try {
            fName = f.getCanonicalPath();
        } catch(IOException ioe) {
            fName = f.getPath();
        }
            
        String cmds[] = null;
        if( OSUtils.isWindows() || OSUtils.isMacOSX() )
            SystemUtils.setWriteable(fName);
        else if ( OSUtils.isOS2() )
            ;//cmds = null; // Find the right command for OS/2 and fill in
        else {
            if(f.isDirectory())
                cmds = new String[] { "chmod", "u+w+x", fName };
            else
                cmds = new String[] { "chmod", "u+w", fName};
        }
        
        if( cmds != null ) {
            try { 
                Process p = Runtime.getRuntime().exec(cmds);
                p.waitFor();
            }
            catch(SecurityException ignored) { }
            catch(IOException ignored) { }
            catch(InterruptedException ignored) { }
        }
        
		return f.canWrite();
    }
    
    /**
     * @param directory Gets all files under this directory RECURSIVELY.
     * @param filter If null, then returns all files.  Else, only returns files
     * extensions in the filter array.
     * @return An array of Files recursively obtained from the directory,
     * according to the filter.
     * 
     */
    public static File[] getFilesRecursive(File directory,
                                           String[] filter) {
        List<File> dirs = new ArrayList<File>();
        // the return array of files...
        List<File> retFileArray = new ArrayList<File>();
        File[] retArray = new File[0];

        // bootstrap the process
        if (directory.exists() && directory.isDirectory())
            dirs.add(directory);

        // while i have dirs to process
        while (dirs.size() > 0) {
            File currDir = dirs.remove(0);
            String[] listedFiles = currDir.list();
            for (int i = 0; (listedFiles != null) && (i < listedFiles.length); i++) {
                File currFile = new File(currDir,listedFiles[i]);
                if (currFile.isDirectory()) // to be dealt with later
                    dirs.add(currFile);
                else if (currFile.isFile()) { // we have a 'file'....
                    boolean shouldAdd = false;
                    if (filter == null)
                        shouldAdd = true;
                    else {
                        String ext = FileUtils.getFileExtension(currFile);
                        for (int j = 0; (j < filter.length) && (ext != null); j++) {
                            if (ext.equalsIgnoreCase(filter[j]))  {
                                shouldAdd = true;
                                
                                // don't keep looping through all filters --
                                // one match is good enough
                                break;
                            }
                        }
                    }
                    if (shouldAdd)
                        retFileArray.add(currFile);
                }
            }
        }        

        if (!retFileArray.isEmpty()) {
            retArray = new File[retFileArray.size()];
            for (int i = 0; i < retArray.length; i++)
                retArray[i] = retFileArray.get(i);
        }

        return retArray;
    }
    
    /** Given a folder path it'll return all the files contained within it and it's subfolders
     * as a flat set of Files.
     * 
     * Non-recursive implementation, up to 20% faster in tests than recursive implementation. :)
     * 
     * @author gubatron
     * @param folder
     * @param extensions If you only need certain files filtered by their extensions, use this string array (without the "."). or set to null if you want all files. e.g. ["txt","jpg"] if you only want text files and jpegs.
     * 
     * @return The set of files.
     */
    public static Collection<File> getAllFolderFiles(File folder, String[] extensions) {
        Set<File> results = new HashSet<File>();
        Stack<File> subFolders = new Stack<File>();
        File currentFolder = folder;
        while (currentFolder != null && currentFolder.isDirectory() && currentFolder.canRead()) {
            File[] fs = null;
            try {
                fs = currentFolder.listFiles();
            } catch (SecurityException e) {
            }
            
            if (fs != null && fs.length > 0) {
                for (File f : fs) {
                    if (!f.isDirectory()) {
                        if (extensions == null || FilenameUtils.isExtension(f.getName(), extensions)) {
                            results.add(f);
                        }
                    } else {
                        subFolders.push(f);
                    }
                }
            }
            
            if (!subFolders.isEmpty()) {
                currentFolder = subFolders.pop();
            } else {
                currentFolder = null;
            }
        }
        return results;
    }    

    /**
     * Deletes the given file or directory, moving it to the trash can or 
     * recycle bin if the platform has one and <code>moveToTrash</code> is
     * true.
     * 
     * @param file The file or directory to trash or delete
     * @param moveToTrash whether the file should be moved to the trash bin 
     * or permanently deleted
     * @return     true on success
     * 
     * @throws IllegalArgumentException if the OS does not support moving files
     * to a trash bin, check with {@link OSUtils#supportsTrash()}.
     */
    public static boolean delete(File file, boolean moveToTrash) {
    	if (!file.exists()) {
    		return false;
    	}
    	if (moveToTrash) {
    	    if (OSUtils.isMacOSX()) {
    	        return moveToTrashOSX(file);
    	    } else if (OSUtils.isWindows()) {
    	        return SystemUtils.recycle(file);
    	    }
    	    else {
    	        throw new IllegalArgumentException("OS does not support trash");
    	    }
    	} 
    	else {
    	    return deleteRecursive(file);
    	}
    }

    /**
     * Moves the given file or directory to Trash.
     * 
     * @param file The file or directory to move to Trash
     * @throws IOException if the canonical path cannot be resolved
     *          or if the move process is interrupted
     * @return true on success
     */
    private static boolean moveToTrashOSX(File file) {
    	try {
    		String[] command = moveToTrashCommand(file);
    		ProcessBuilder builder = new ProcessBuilder(command);
    		builder.redirectErrorStream();
    		Process process = builder.start();
    		ProcessUtils.consumeAllInput(process);
    		process.waitFor();
    	} catch (InterruptedException err) {
    		LOG.error("InterruptedException", err);
    	} catch (IOException err) {
    		LOG.error("IOException", err);
    	}
    	return !file.exists();
    }

    /**
     * Creates and returns the the osascript command to move
     * a file or directory to the Trash
     * 
     * @param file The file or directory to move to Trash
     * @throws IOException if the canonical path cannot be resolved
     * @return OSAScript command
     */
    private static String[] moveToTrashCommand(File file) {
    	String path = null;
    	try {
    		path = file.getCanonicalPath();
    	} catch (IOException err) {
    		LOG.error("IOException", err);
    		path = file.getAbsolutePath();
    	}
    	
    	String fileOrFolder = (file.isFile() ? "file" : "folder");
    	
    	String[] command = new String[] { 
    			"osascript", 
    			"-e", "set unixPath to \"" + path + "\"",
    			"-e", "set hfsPath to POSIX file unixPath",
    			"-e", "tell application \"Finder\"", 
    			"-e",    "if " + fileOrFolder + " hfsPath exists then", 
    			"-e",        "move " + fileOrFolder + " hfsPath to trash",
    			"-e",    "end if",
    			"-e", "end tell" 
    	};
    	
    	return command;
    }
    
    
    /**
     * Deletes all files in 'directory'.
     * Returns true if this succesfully deleted every file recursively, including itself.
     * 
     * @param directory
     * @return
     */
    public static boolean deleteRecursive(File directory) {
		// make sure we only delete canonical children of the parent file we
		// wish to delete. I have a hunch this might be an issue on OSX and
		// Linux under certain circumstances.
		// If anyone can test whether this really happens (possibly related to
		// symlinks), I would much appreciate it.
		String canonicalParent;
		try {
			canonicalParent = getCanonicalPath(directory);
		} catch (IOException ioe) {
			return false;
		}

		if (!directory.isDirectory())
			return directory.delete();

		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!getCanonicalPath(files[i]).startsWith(canonicalParent))
					continue;
			} catch (IOException ioe) {
				return false;
			}
            
			if (!deleteRecursive(files[i]))
				return false;
		}

		return directory.delete();
	}
    
    /**
     * @return true if the two files are the same.  If they are both
     * directories returns true if there is at least one file that 
     * conflicts.
     */
    public static boolean conflictsAny(File a, File b) {
    	if (a.equals(b))
    		return true;
    	Set<File> unique = new HashSet<File>();
    	unique.add(a);
    	for (File recursive: getFilesRecursive(a,null))
    		unique.add(recursive);
    	
    	if (unique.contains(b))
    		return true;
    	for (File recursive: getFilesRecursive(b,null)) {
    		if (unique.contains(recursive))
    			return true;
    	}
    	
    	return false;
    	
    }
    
    /**
     * Returns total length of all files by going through
     * the given directory (if it's a directory).
     */
    public static long getLengthRecursive(File f) {
    	if (!f.isDirectory())
    		return f.length();
    	long ret = 0;
    	for (File file : getFilesRecursive(f,null))
    		ret += file.length();
    	return ret;
    }

    /**
     * A utility method to close Closeable objects (Readers, Writers, 
     * Input- and OutputStreams and RandomAccessFiles).
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {}
        }
    }
    
    /**
     * A utility method to flush Flushable objects (Readers, Writers, 
     * Input- and OutputStreams and RandomAccessFiles).
     */
    public static void flush(Flushable flushable) {
        if (flushable != null) {
            try {
                flushable.flush();
            } catch (IOException ignored) {}
        }
    }
    
    /** 
     * Attempts to copy the first 'amount' bytes of file 'src' to 'dst',
     * returning the number of bytes actually copied.  If 'dst' already exists,
     * the copy may or may not succeed.
     * 
     * @param src the source file to copy
     * @param amount the amount of src to copy, in bytes
     * @param dst the place to copy the file
     * @return the number of bytes actually copied.  Returns 'amount' if the
     *  entire requested range was copied.
     */
    public static long copy(File src, long amount, File dst) {
        final int BUFFER_SIZE=1024;
        long amountToRead=amount;
        InputStream in=null;
        OutputStream out=null;
        try {
            //I'm not sure whether buffering is needed here.  It can't hurt.
            in=new BufferedInputStream(new FileInputStream(src));
            out=new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buf=new byte[BUFFER_SIZE];
            while (amountToRead>0) {
                int read=in.read(buf, 0, (int)Math.min(BUFFER_SIZE, amountToRead));
                if (read==-1)
                    break;
                amountToRead-=read;
                out.write(buf, 0, read);
            }
        } catch (IOException e) {
        } finally {
            close(in);
            flush(out);
            close(out);
        }
        return amount-amountToRead;
    }

    /** 
     * Copies the file 'src' to 'dst', returning true iff the copy succeeded.
     * If 'dst' already exists, the copy may or may not succeed.  May also
     * fail for VERY large source files.
     */
    public static boolean copy(File src, File dst) {
        //Downcasting length can result in a sign change, causing
        //copy(File,int,File) to terminate immediately.
        long length=src.length();
        return copy(src, (int)length, dst)==length;
    }
    
    /**
     * Creates a temporary file using
     * {@link File#createTempFile(String, String, File)}, trying a few times.
     * This is a workaround for Sun Bug: 6325169: createTempFile occasionally
     * fails (throwing an IOException).
     */
    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        IOException iox = null;
        
        for(int i = 0; i < 10; i++) {
            try {
                return File.createTempFile(prefix, suffix, directory);
            } catch(IOException x) {
                iox = x;
            }
        }
        
        throw iox;
    }
    
    /**
     * Creates a temporary file using
     * {@link File#createTempFile(String, String)}, trying a few times.
     * This is a workaround for Sun Bug: 6325169: createTempFile occasionally
     * fails (throwing an IOException).
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        IOException iox = null;
        
        for(int i = 0; i < 10; i++) {
            try {
                return File.createTempFile(prefix, suffix);
            } catch(IOException x) {
                iox = x;
            }
        }
        
        throw iox;
    }

    public static File getJarFromClasspath(String markerFile) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            classLoader = FileUtils.class.getClassLoader();
        }
        if (classLoader == null) {
            return null;
        }
        return getJarFromClasspath(classLoader, markerFile);
    }
    
    public static File getJarFromClasspath(ClassLoader classLoader, String markerFile) {
        if (classLoader == null) {
            throw new IllegalArgumentException();
        }
        
        URL messagesURL = classLoader.getResource(markerFile);
        if (messagesURL != null) {
            String url = CommonUtils.decode(messagesURL.toExternalForm());
            if (url != null && url.startsWith("jar:file:")) {
                url = url.substring("jar:file:".length(), url.length());
                url = url.substring(0, url.length() - markerFile.length() - "!/".length());
                return new File(url);
            }
        }

        return null;
    }

    public static void copyDirectoryRecursively(File srcDir, File targetDir) {
    	if (!targetDir.exists()) {
    		targetDir.mkdir();
    		targetDir.setWritable(true);
    	}
    	
    	for (File srcElement : srcDir.listFiles()) {
    		if (srcElement.isFile()) {
    			FileUtils.copy(srcElement, 
    					new File(targetDir + File.separator + srcElement.getName()));
    		} else if (srcElement.isDirectory()) {
    			FileUtils.copyDirectoryRecursively(srcElement, new File(targetDir,srcElement.getName()));
    		}
    	}
    }
    
    public static boolean deleteEmptyDirectoryRecursive(File directory) {
        // make sure we only delete canonical children of the parent file we
        // wish to delete. I have a hunch this might be an issue on OSX and
        // Linux under certain circumstances.
        // If anyone can test whether this really happens (possibly related to
        // symlinks), I would much appreciate it.
        String canonicalParent;
        try {
            canonicalParent = getCanonicalPath(directory);
        } catch (IOException ioe) {
            return false;
        }

        if (!directory.isDirectory())
            return false;
        
        boolean canDelete = true;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                if (!getCanonicalPath(files[i]).startsWith(canonicalParent))
                    continue;
            } catch (IOException ioe) {
                canDelete = false;
            }
            
            if (!deleteEmptyDirectoryRecursive(files[i])) {
                canDelete = false;
            }
        }

        return canDelete ? directory.delete() : false;
    }
    
    public static String getValidFileName(String fileName) {
        String newFileName = fileName.replaceAll("[\\\\/:*?\"<>|\\[\\]]+", "_");
        return newFileName;
    }
    
    public static File[] listFiles(File directoryFile) {
        List<File> files = new LinkedList<>();
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(directoryFile.toPath())) {
            for (Path child : dir) {
                files.add(child.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files.toArray(new File[0]);
    }
}