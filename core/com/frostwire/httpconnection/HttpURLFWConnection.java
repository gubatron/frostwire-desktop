package com.frostwire.httpconnection;
import java.util.Timer;
import java.util.TimerTask;

import org.xml.sax.InputSource;

public class HttpURLFWConnection extends java.lang.Object {
	
	/**
	 * 
	 */
	static int SERVER_TIMEOUT = 5000;
	private String _sourceXML = "";
	private InputSource _myXMLSource = null;
	
	public boolean runTask(String server, int port, int timeout, String agent) {
		if (timeout == 0)
			timeout = SERVER_TIMEOUT; // set default timeout
		else 
			SERVER_TIMEOUT = timeout;
		
		HttpURLFWGetSource theTask = new HttpURLFWGetSource(server, port, agent);
		Thread task = new Thread(theTask);
			
		
		HttpFWTOut to = new HttpFWTOut(task);
		new Timer(true).schedule((TimerTask)to,SERVER_TIMEOUT);
		System.out.println(SERVER_TIMEOUT / 1000 + " seconds remaining before timeout for "+ server +"...");
		task.start();


		 while (task.isAlive())
		 {
			 //System.out.println("Task still alive...."); // debug only
			 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		 }
		 
		 return true;
	}

	public boolean updateHttpTask (String Server, String Agent) {
		HttpURLFWGetSource theTask = new HttpURLFWGetSource(Server, 80,Agent);
		Thread task = new Thread(theTask);

		HttpFWTOut to = new HttpFWTOut(task);
		new Timer(true).schedule((TimerTask)to,SERVER_TIMEOUT);
		System.out.println("HttpURLFWConnection - FTA DEBUG: " + SERVER_TIMEOUT + " milliseconds remaining before timeout...");
		task.start();


		 while (task.isAlive())
		 {
			 //System.out.println("Task still alive...."); // until it finishes
		 }
		 // Might be useful in the future to get documents from server
		 this.setSourceXML(theTask.getSourceReaded()); // Save source read from server
		 this.setmyXMLSource(theTask.getXMLSource()); // get XML source for being read with SAX
		 return theTask.isConnected();
	}
	
	public void testTask () {

		// Just to test a particular server
		HttpURLFWGetSource theTask = new HttpURLFWGetSource("update2.frostwire.com", 80,"fer test");
		Thread task = new Thread(theTask);


		HttpFWTOut to = new HttpFWTOut(task);
		new Timer(true).schedule((TimerTask)to,SERVER_TIMEOUT);
		System.out.println("DEBUG MODE " + SERVER_TIMEOUT + " milliseconds remaining before timeout...");
		task.start();


		 while (task.isAlive())
		 {
			 //System.out.println("Task still alive....");
		 }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpURLFWConnection task1 = new HttpURLFWConnection();
		task1.testTask();
	} // end of main
	
	
/**
	 * @param _sourceXML the _sourceXML to set
	 */
	public void setSourceXML(String _sourceXML) {
		this._sourceXML = _sourceXML;
	}

	/**
	 * @return the _sourceXML
	 */
	public String getSourceXML() {
		return _sourceXML;
	}

	/**
	 * @param myXMLSource the myXMLSource to set
	 */
	public void setmyXMLSource (InputSource _myXMLSource) {
		this._myXMLSource = _myXMLSource;
	}

	/**
	 * @return the _sourceXML
	 */
	public InputSource getmyXMLSource() {
		return  _myXMLSource;
	}
	

/**
 * 
 * Class to handle timeout
 * @author FTA
 *
 */
	
public class HttpFWTOut extends TimerTask {

		Thread threadToTimeOut;

		public HttpFWTOut(Thread threadToTimeOut) {
		this.threadToTimeOut = threadToTimeOut;
		}

		public void run() {
		System.out.println("HttpFWTOut - Server seems to be not responding within a reasonable time...");
		threadToTimeOut.interrupt();
		}
		
	}	
	
} // end of main class
