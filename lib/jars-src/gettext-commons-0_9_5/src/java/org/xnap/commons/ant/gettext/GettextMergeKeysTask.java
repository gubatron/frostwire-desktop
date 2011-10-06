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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;

public class GettextMergeKeysTask extends AbstractGettextTask { 
    
    /**
     * @description msgcat command.
     * @parameter expression="${msgmergeCmd}" default-value="msgmerge"
     * @required 
     */
    protected String msgmergeCmd = "msgmerge";
    public void setMsgmergeCmd(String msgmergeCmd) {
        this.msgmergeCmd = msgmergeCmd;
    }
    
    private void checkPreconditions() throws BuildException {
        if (poDirectory == null) {
            throw new BuildException("poDirectory must be set for msgmerge");
        }
    }
    
    public void execute() {
        checkPreconditions();
        log("Invoking msgmerge for po files in '" 
                + poDirectory + "'.");
        
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(poDirectory);
        ds.setIncludes(new String[] {"**/*.po"});
        ds.scan();
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            log("Processing " + files[i]);
            Commandline cl = new Commandline();
            cl.setExecutable(msgmergeCmd);
            cl.createArgument().setValue("-q");
            cl.createArgument().setValue("--backup=numbered");
            cl.createArgument().setValue("-U");
            cl.createArgument().setFile(new File(poDirectory, files[i]));
            cl.createArgument().setValue(new File(poDirectory, keysFile).getAbsolutePath());
            
            log("Executing: " + cl.toString(), Project.MSG_INFO);
            
            runCommandLineAndWait(cl);
        }
    }
}
