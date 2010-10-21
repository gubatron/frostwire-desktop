package org.xnap.commons.ant.gettext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class StreamConsumer {

	private final BufferedReader bufferedReader;
	
	private final Thread thread;
	
	public StreamConsumer(InputStream inputStream, final Task task) {
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		thread = new Thread(new Runnable() {
			public void run() {
				String line = null;
				try {
					while ((line = bufferedReader.readLine()) != null) {
						if (task.getProject() != null) {
							task.log(line, Project.MSG_VERBOSE);
						}
					}
				} catch (IOException e) {
					if (task.getProject() != null) {
						task.log(e.getLocalizedMessage(), Project.MSG_ERR);
					}
					try {
						bufferedReader.close();
					} catch (IOException e1) {
					}
				}
			}
		});
		thread.setDaemon(true);
	}

	public void start() {
		thread.start();
	}
	
}
