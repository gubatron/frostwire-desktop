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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

public class GettextExtractKeysTask extends AbstractGettextTask {

    protected Vector filesets = new Vector();
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }
    
    private String xgettextCommand = "xgettext";
    public void setXgettextCommand(String xgettextCommand) {
        this.xgettextCommand = xgettextCommand;
    }
    
    private String encoding = "utf-8";
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    private String keywords = "-ktrc:1c,2 -ktrnc:1c,2,3 -ktr -kmarktr -ktrn:1,2 -k";
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    private void checkPreconditions() throws BuildException {
        if (poDirectory == null) {
            throw new BuildException("poDirectory must be set for xgettext");
        }
        if (filesets.isEmpty()) {
            throw new BuildException("at least one fileset must be specified to search for .java files in");
        }
    }
    
    public void execute() {
     
        checkPreconditions();
        
        ArrayList files = new ArrayList();
        for (Iterator i = filesets.iterator(); i.hasNext();) {
        	FileSet fileSet = (FileSet)i.next();
            DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
            String names[] = scanner.getIncludedFiles();
            File parent = fileSet.getDir(getProject());
            String parentPath = GettextUtils.getRelativePath(parent, getLocation());
            log(parentPath);
            for (int j = 0; j < names.length; j++) {
            	files.add(GettextUtils.createAbsolutePath(parentPath, names[j]));
            }
        }
        File file = createListFile(files);
        
        Commandline cl = new Commandline();
        cl.setExecutable(xgettextCommand);
        cl.createArgument().setValue("-c");
        cl.createArgument().setValue("--from-code=" + encoding);
        cl.createArgument().setValue("--output=" + new File(poDirectory, keysFile).getAbsolutePath());
        cl.createArgument().setValue("--language=Java");
        cl.createArgument().setLine(keywords);
        
        cl.createArgument().setValue("--files-from=" + file.getAbsolutePath());
        
        log("Executing: " + cl.toString());
        
        runCommandLineAndWait(cl);
    }
    
    private File createListFile(List files) {
        try {
            File listFile = File.createTempFile("srcfiles", null);
            log(listFile.getAbsolutePath());
            listFile.deleteOnExit();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(listFile));
            try {
                for (Iterator iterator = files.iterator(); iterator.hasNext();) {
					String file = (String) iterator.next();
                    writer.write(file);
                    writer.newLine();
                }                
            } finally {
                writer.close();
            }
            
            return listFile;
        } catch (IOException e) {
            log(e.getMessage());
            return null;
        }
    }
    
}
