package org.xnap.commons.ant.gettext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;

public class GettextDistTask extends AbstractGettextGenerateTask {

	protected int percentage = 0;
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}
	
	protected String moreOrLess = "greaterOrEqual";
	public void setMoreOrLess(String moreOrLess) {
		this.moreOrLess = moreOrLess;
	}
		
    public void execute() {
        
    	checkPreconditions();
    	
        CommandlineFactory cf = getCommandlineFactory();
        String[] files = getPoFiles();
        
        for (int i = 0; i < files.length; i++) {
            log("Processing " + files[i]);
            if (percentage > 0) {
            	if (!fileMatchesPercentage(new File(poDirectory, files[i]))) {
            		log("Skipping " + files[i]);
            		continue;
            	}
            }
            Commandline cl = cf.createCommandline(new File(poDirectory, files[i]));
            log("Executing: " + cl.toString(), Project.MSG_DEBUG);
            runCommandLineAndWait(cl);
        }
    }
    
    boolean fileMatchesPercentage(File file) {
    	
    	Commandline cl = new Commandline();
    	cl.setExecutable(msgfmtCmd);
    	cl.createArgument().setValue("--statistics");
    	cl.createArgument().setFile(file);
    	
    	try {
			Process process = Runtime.getRuntime().exec(cl.getCommandline());
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			StringBuilder builder = new StringBuilder();
			char buffer[] = new char[1024];
			int count;
			while ((count = reader.read(buffer, 0, buffer.length)) != -1) {
				builder.append(buffer, 0, count);
			}
			
			String output = builder.toString();
			// zero translations
			if (output.startsWith("0")) {
				if (getProject() != null) {
					log(MessageFormat.format("{0} has {1}% translated", new Object[] { file.getName(), Integer.valueOf(0) }), Project.MSG_INFO);
				}
				return evaluatePercentage(0);
			}
			
			StringTokenizer st = new StringTokenizer(output);

			int total = 0;
			int translated = Integer.parseInt(st.nextToken());
			total = translated;

			while (st.hasMoreTokens()) {
				st.nextToken();
				if (!st.hasMoreTokens()) {
					break;
				}
				st.nextToken();
				if (!st.hasMoreTokens()) {
					break;
				}
				total += Integer.parseInt(st.nextToken());
			}

			int translatedPercentage =  (int)(100.0 * (double)translated / (double)total);

			if (getProject() != null) {
				log(MessageFormat.format("{0} has {1}% translated", new Object[] { file.getName(), Integer.valueOf(translatedPercentage) }), Project.MSG_INFO);
			}
			
			return evaluatePercentage(translatedPercentage);
		
		} catch (IOException e) {
			throw new BuildException(e.getMessage());
		}
		catch (NumberFormatException nfe) {
			throw new BuildException(nfe.getMessage());
		}
    }
    
    private boolean evaluatePercentage(int translatedPercentage) {
    	
		if (moreOrLess.equals("greaterOrEqual")) {
			return translatedPercentage >= percentage;
		}
		else {
			return translatedPercentage < percentage;
		}
    }
}
