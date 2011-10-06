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
				} finally {
					try {
						bufferedReader.close();
					} catch (IOException e) {
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
