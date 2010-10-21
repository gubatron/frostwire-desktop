package org.xnap.commons.ant.gettext;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;

public class AbstractGettextTask extends Task {

    /**
     * PO directory.
     */
    protected File poDirectory;
    public void setPoDirectory(String poDirectory) {
    	 this.poDirectory = new File(this.getOwningTarget().getProject().getBaseDir(), poDirectory);
    	 try {
    		 // Make the path prettier
    		 this.poDirectory = this.poDirectory.getCanonicalFile();
    	 } catch (IOException e) {
    	 }
    }

    /**
     * Filename of the .pot file
     */
    protected String keysFile = "keys.pot";
    public void setKeysFile(String keysFile) {
        this.keysFile = keysFile;
    }
    
    protected void runCommandLineAndWait(Commandline cl) {
    	try {
    		Process p = Runtime.getRuntime().exec(cl.getCommandline(), null, getOwningTarget().getProject().getBaseDir());
    		new StreamConsumer(p.getInputStream(), this).start();
    		new StreamConsumer(p.getErrorStream(), this).start();
    		int exitCode = p.waitFor();
    		if (exitCode != 0) {
            	log(cl.getExecutable() + " returned " + exitCode);
            	throw new BuildException("Build failed");
            }
    	} catch (IOException e) {
    		log("Could not execute " + cl.getExecutable() + ": " + e.getMessage(), Project.MSG_ERR);
    	} catch (InterruptedException e) {
    		log("Process was interrupted: " + e.getMessage(), Project.MSG_ERR);
    	}
    }

    protected String getParentPath(File parent, Location location) {
    	String locationPath = new File(location.getFileName()).getParent();
    	if (parent.getAbsolutePath().startsWith(locationPath)) {
        	// + 1 for path separator
			return parent.getAbsolutePath().substring(locationPath.length() + 1);
		}
		return parent.getAbsolutePath();
	}
    
    protected String getAbsolutePath(String path, String parentPath) {
        return parentPath + File.separator + path;
    }
    
}
