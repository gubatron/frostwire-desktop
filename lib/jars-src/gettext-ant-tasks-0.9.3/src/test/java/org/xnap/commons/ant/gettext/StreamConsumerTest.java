package org.xnap.commons.ant.gettext;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class StreamConsumerTest extends TestCase {

	public void testIsLogging() throws Exception {
		
		Project project = new Project();
		project.init();
		MockTask task = new MockTask();
		task.setProject(project);
		
		PipedOutputStream outputStream = new PipedOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		new StreamConsumer(new PipedInputStream(outputStream), task).start();
		writer.write("Test\n");
		writer.flush();
		
		task.latch.await(100, TimeUnit.SECONDS);
		assertEquals("Test", task.msg);
	}
	
	private class MockTask extends Task {
		
		private String msg;
		
		CountDownLatch latch = new CountDownLatch(1);
		
		public void log(String msg, int msgLevel) {
			this.msg = msg; 
			latch.countDown();
		}
		
	}
	
}
