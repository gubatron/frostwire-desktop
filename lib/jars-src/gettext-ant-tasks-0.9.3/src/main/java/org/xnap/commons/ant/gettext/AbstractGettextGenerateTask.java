package org.xnap.commons.ant.gettext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

public abstract class AbstractGettextGenerateTask extends AbstractGettextTask {

    /**
     * msgcat command.
     */
    protected String msgcatCmd = "msgcat";
    public void setMsgcatCmd(String msgcatCmd) {
        this.msgcatCmd = msgcatCmd;
    }

    /**
     * @description msgfmt command.
     * @parameter expression="${msgfmtCmd}" default-value="msgfmt"
     * @required 
     */
    protected String msgfmtCmd = "msgfmt";
    public void setMsgfmtCmd(String msgfmtCmd) {
        this.msgfmtCmd = msgfmtCmd;
    }
    
    /**
     * @description target package.
     * @parameter expression="${targetBundle}"
     * @required 
     */
    protected String targetBundle = "Messages";
    public void setTargetBundle(String targetBundle) {
        this.targetBundle = targetBundle;
    }
    
    /**
     * @description Output format ("class" or "properties")
     * @parameter expression="${outputFormat}" default-value="class"
     * @required 
     */
    protected String outputFormat = "class";
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    /**
     * Java version.
     * Can be "1" or "2".
     * @parameter expression="${javaVersion}" default-value="2"
     * @required
     */
    protected String javaVersion = "2";
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    protected String outputDirectory;
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    protected Vector poFiles = new Vector();
    public void addFileSet(FileSet fileset) {
    	poFiles.add(fileset);
    }

    
    protected String[] getPoFiles() {
    	List files = new ArrayList();
    	if (!poFiles.isEmpty()) {
    		for (Iterator i = poFiles.iterator(); i.hasNext();) {
    			FileSet fileSet = (FileSet) i.next();
    			DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
                String names[] = scanner.getIncludedFiles();
                File parent = fileSet.getDir(getProject());
                String parentPath = getParentPath(parent, getLocation());
                for (int j = 0; j < names.length; j++) {
                	files.add(getAbsolutePath(names[j], parentPath));
                }
    		}
    		return (String[]) files.toArray(new String[0]);
    	}
    	else {
    		DirectoryScanner ds = new DirectoryScanner();
    		ds.setBasedir(poDirectory);
    		ds.setIncludes(new String[] {"**/*.po"});
    		ds.scan();
    		return ds.getIncludedFiles();
    	}
    }
    

    
    protected void checkPreconditions() {
    	if (outputDirectory == null) {
    		throw new BuildException("outputDirectory must be specified: <... outputDirectory=\"po\"");
    	}
    }
    
    protected CommandlineFactory getCommandlineFactory() {
    	if ("class".equals(outputFormat)) {
    		return new MsgFmtCommandlineFactory();
    	} else if ("properties".equals(outputFormat)) {
    		return new MsgCatCommandlineFactory();
    	} else {
    		throw new BuildException("Unknown output format: " 
    				+ outputFormat + ". Should be 'class' or 'properties'.");
    	}
    }
    
    protected abstract class CommandlineFactory {

        public abstract Commandline createCommandline(File file, String locale);
        
        public Commandline createCommandline(File file) {
            String locale = file.getName().substring(0, file.getName().lastIndexOf('.'));
            return createCommandline(file, locale);
        }

    }
    	
    protected class MsgFmtCommandlineFactory extends CommandlineFactory {
        
    	public Commandline createCommandline(File file, String locale) {
            Commandline cl = new Commandline();
            cl.setExecutable(msgfmtCmd);
            
            if ("2".equals(javaVersion)) {
                cl.createArgument().setValue("--java2");
            } else {
                cl.createArgument().setValue("--java");
            }
            
            cl.createArgument().setValue("-d");
            cl.createArgument().setValue(outputDirectory);
            cl.createArgument().setValue("-r");
            cl.createArgument().setValue(targetBundle);
            if (locale != null) {
            	cl.createArgument().setValue("-l");
            	cl.createArgument().setValue(GettextUtils.getJavaLocale(locale));
            }
            cl.createArgument().setFile(file);
            log(cl.toString(), Project.MSG_WARN);
            return cl;
        }
    }

    protected class MsgCatCommandlineFactory extends CommandlineFactory {
        
    	public Commandline createCommandline(File file, String locale) {
            String basepath = targetBundle.replace('.', File.separatorChar);
            if (locale != null) {
                basepath += "_" + GettextUtils.getJavaLocale(locale);
            }
            
            File target = new File(outputDirectory, basepath + ".properties");
            Commandline cl = new Commandline();
        
            cl.setExecutable(msgcatCmd);
        
            cl.createArgument().setValue("--no-location");
            cl.createArgument().setValue("-p");
            cl.createArgument().setFile(file);
            cl.createArgument().setValue("-o");
            cl.createArgument().setFile(target);

            return cl;
        }
     }
    
}
