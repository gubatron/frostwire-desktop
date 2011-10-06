/**
 * Copyright 2006 Felix Berger
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xnap.commons.ant.gettext;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
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
    
}
