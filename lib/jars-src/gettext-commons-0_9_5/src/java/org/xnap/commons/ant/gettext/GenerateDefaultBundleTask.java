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
import org.apache.tools.ant.types.Commandline;

public class GenerateDefaultBundleTask extends AbstractGettextGenerateTask {

	protected String msginitCmd = "msginit";
    public void setMsgenCmd(String msginitCmd) {
		this.msginitCmd = msginitCmd;
	}
    
    protected String potFile = null;
    public void setPotFile(String potFile) {
		this.potFile = potFile;
	}
	
    protected void checkPreconditions() {
    	super.checkPreconditions();
    	if (potFile == null) {
    		throw new BuildException("potFile must be specified: <... potFile=\"po/keys.pot\"");
    	}
    }
    
    public void execute() {
        
    	checkPreconditions();
    	
    	File defaultPo = createDefaultBundle();
    	CommandlineFactory cf = getCommandlineFactory();
        Commandline cl = cf.createCommandline(defaultPo, null);
    	
    	log("Executing: " + cl.toString(), Project.MSG_DEBUG);
        runCommandLineAndWait(cl);
        
        defaultPo.delete();
    }


    private File createDefaultBundle() {
    	try {
    		File defaultPo = File.createTempFile("default", ".po");
    		defaultPo.delete();
    		
    		Commandline cl = new Commandline();
    		cl.setExecutable(msginitCmd);
    		cl.createArgument().setValue("--no-translator");
    		cl.createArgument().setValue("-i");
    		cl.createArgument().setValue(potFile);
    		cl.createArgument().setValue("-o");
    		cl.createArgument().setFile(defaultPo);
    		
    		log("Generating default po file: " + cl.toString(), Project.MSG_INFO);
    		runCommandLineAndWait(cl);
    		
    		return defaultPo;
    	} catch (IOException e) {
    		throw new BuildException(e);
    	}
    }
}
