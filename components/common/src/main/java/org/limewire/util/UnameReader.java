package org.limewire.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class UnameReader {
	
	public static String read()  {
		
		String output = "";
		
		try {
		
			ProcessBuilder pb = new ProcessBuilder("uname", "-a");
			pb.redirectErrorStream(true);
			Process proc = pb.start();

			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String line;
			int exit = -1;

			while ((line = br.readLine()) != null) {
			    
				output = line;
			    
			    try {
			        exit = proc.exitValue();
			        if (exit == 0)  {
			            // Process finished
			        }
			    } catch (IllegalThreadStateException t) {
			        proc.destroy();
			    }
			}
		} catch (Exception e) {
		    // ignore
		}
		
		return output;
	}
}
