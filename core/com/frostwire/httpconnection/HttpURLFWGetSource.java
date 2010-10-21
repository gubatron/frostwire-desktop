package com.frostwire.httpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.xml.sax.InputSource;


/**
 * @author FTA
 *
 */

public class HttpURLFWGetSource implements Runnable {

	
		private String _server="";
		private int _port=0;
		private String _agent="";
		private boolean _connected=false;
		private String _sourceReaded="";
		private InputSource _xmlSource= null;
		
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			// GET SOURCE TEST

		}

		// DEBUG PURPOSES ONLY
		private boolean complete = false; // Indicates if the task has been completed
		
		public HttpURLFWGetSource (String Server, int Port, String Agent) {
			this.setServer(Server);
			this.setPort(Port);
			this.setAgent(Agent);
		
		}
		
		public void run() {
			
			try {

				
				// Creates a non-blocking socket channel on specified port			
				SocketChannel sChannel = createSocketChannel(this.getServer(),this.getPort());
				String line = null;
		
		
		// Before the socket is usable, the connection need to be completed
		// by calling finishConnect(), which is non-blocking
		while (!sChannel.finishConnect()) {
			Thread.sleep(100);
			System.out.println("HttpURLFWGetSource - Waiting reply from " + this.getServer() +"...");
		}

		// Debug purposes only
		System.out.println("HttpURLFWGetSource - Connected successfully to : " +  sChannel.socket() + "!!");
		this.setConnected(true);
		sChannel.configureBlocking(true);

		Socket _socket = sChannel.socket();
		InputStream is = _socket.getInputStream();
	    OutputStream os = _socket.getOutputStream();
	    InputSource src = null;
	    src = new InputSource(is);
	    this.setXMLSource(src);
		ByteReaderFT reader = new ByteReaderFT(is);
		
		
	    // 1st: Request information from the server
		String UserAgent = "User-Agent: "+ this.getAgent() +"\r\n";
		String TheServer = "Host: " + this.getServer() +"\r\n";
		
	    os.write("GET / HTTP/1.1\r\n".getBytes()); // Might be improved in the future to handle any document not only in the root of the server
	    os.write(TheServer.getBytes());
	    os.write(UserAgent.getBytes());
	    os.write("\r\n".getBytes());
	    //
		 
	    // 2nd: Read answer from server
	    System.out.println("HttpURLFWGetSource - 2nd part. Connected, Reading answer from server...");
	    //String line = null;
		boolean saveXML = false;
	    String sourceXML = "";
	    while((line = reader.readLine()) != null && !line.equals("")) {
	    	//System.out.println("*" + line + "*");
	        
	    } // end of while
	    line = reader.readLine();
	    
	    //Step 3: Confirm everything went OK.
	    
	    while (_socket.isConnected() &&  !line.equals("0")) {
	    	os.write("HTTP/1.1 200 OK \r\n".getBytes());
		    os.write("\r\n".getBytes());		    
	    	while((line = reader.readLine()) != null && !line.equals("0")) {
	    		if (line.startsWith("<?"))
	    			saveXML = true;
	    		if (saveXML)
	    			sourceXML = sourceXML + line + "\n";
	    		if (line.equals("0"))
	    			_socket.close();	    			
	    		//System.out.println("FTA DEBUG lector *" + line + "*");
	    	}
	    } // end of while socket is connected
	    

	    this.setComplete(true);
	    
	    //System.out.println("*********************\n*************\n" + sourceXML);
	    this.setSourceReaded(sourceXML);

	    	}
	        catch (SocketException sex)
	        {
	        	//System.out.println("HttpURLFWGetSource - Remote client "+ this.getServer() +" died in the process!\n" + sex.getCause());
	        	//system.err
	        	System.out.println("HttpURLFWGetSource - Cancelled: " + sex.getMessage());
	        	this.setComplete(true);
	            //return result;
	        }
	    	catch (IOException iox) {
	            System.out.println("HttpURLFWGetSource - Some problem happened with input output!\nDetails as follow:" + iox.getMessage() );
	            //System.out.println(iox);
	            //_socket.close();
	            //return result;
	    	} catch (InterruptedException e) {
				// TODO Interrupted by timer
				System.out.println ("HttpURLFWGetSource - Request for " + this.getServer() +" timed out!");
				this.setComplete(true);
			}

		} // END OF RUN
		
		
		
		
		 // Function to get the integer value code smoothly (without producing conversion errors).
		public int IntegerValueOf(String stringValue) {
			try {
				int myinteger = new Integer(stringValue).intValue();
				return myinteger;	
			}
			catch (NumberFormatException prob) {
				return 0;
			}
		 
		} // end of integervalueOf
		
			
		

		SocketChannel createSocketChannel(String hostName, int port)
		throws IOException {
		// Create a non-blocking socket channel
		SocketChannel sChannel1 = SocketChannel.open();
		sChannel1.configureBlocking(false);
		
		// Send a connection request to the host; this method is non-blocking
		sChannel1.connect(new InetSocketAddress(hostName, port));
		return sChannel1;
		}

		// DEBUG ONLY
		// DISPOSABLE
	    public void writeMessage( SocketChannel channel, String message )
	    throws IOException {
	        ByteBuffer buf = ByteBuffer.wrap( message.getBytes()  );
	         int nbytes = channel.write( buf );
	         
	         if (nbytes == 0)
	        	 System.out.println("HttpURLFWGetSource DEBUG: No bytes written!");
	    }

	 

	    static final int BUFSIZE = 8;

	    public String decode( ByteBuffer byteBuffer )
	    throws CharacterCodingException {
	        Charset charset = Charset.forName( "us-ascii" );
	        CharsetDecoder decoder = charset.newDecoder();
	        CharBuffer charBuffer = decoder.decode( byteBuffer );
	        String result = charBuffer.toString();
	        return result;
	    }

	    
	    


	    /*************************************************
	    * Functions to get and set private variable values
	    * 
	    * 
	    **************************************************/
	    public void setPort(int _port) {
			this._port = _port;
		}

		public int getPort() {
			return _port;
		}

		public void setServer(String _server) {
			this._server = _server;
		}

		public String getServer() {
			return _server;
		}

		/**
		 * @param complete: sets the status 
		 */
		public void setComplete(boolean complete) {
			this.complete = complete;
		}

		/**
		 * @return whether the task is completed or not
		 */
		public boolean isComplete() {
			return complete;
		}


		/**
		 * @param _agent the _agent to set
		 */
		public void setAgent(String _agent) {
			this._agent = _agent;
		}

		/**
		 * @return the _agent
		 */
		public String getAgent() {
			return _agent;
		}


		/**
		 * @param _connected the _connected to set
		 */
		public void setConnected(boolean _connected) {
			this._connected = _connected;
		}

		/**
		 * @return the _connected
		 */
		public boolean isConnected() {
			return _connected;
		}


		/**
		 * @param _sourceReaded the _sourceReaded to set
		 */
		public void setSourceReaded(String _sourceReaded) {
			this._sourceReaded = _sourceReaded;
		}

		/**
		 * @return the _sourceReaded
		 */
		public String getSourceReaded() {
			return _sourceReaded;
		}


		/**
		 * @param _xmlSource the _xmlSource to set
		 */
		public void setXMLSource(InputSource _xmlSource) {
			this._xmlSource = _xmlSource;
		}

		/**
		 * @return the _xmlSource
		 */
		public InputSource getXMLSource() {
			return _xmlSource;
		}


		/**
		 * 
		 * class to handle the reading of bytes
		 */
	    class ByteReaderFT {

	        private static final byte R = '\r';
	        private static final byte N = '\n';

	        private InputStream _istream;
	        
	        public ByteReaderFT(InputStream stream) {
	            _istream = stream;
	        }

	        public void close() {
	            try {
	                _istream.close();
	            } catch (IOException ignored) {
	            }
	        }

	        public int read() {

	            int c = -1;
	        
	            if (_istream == null)
	                return c;
	        
	            try {
	                c =  _istream.read();
	            } catch(IOException ignored) {
	                // return -1
	            } catch(ArrayIndexOutOfBoundsException ignored) {
	                // return -1
	            }
	            return c;
	        }

	        public int read(byte[] buf) {
	            int c = -1;

	            if (_istream == null) {
	                return c;
	            }

	            try {
	                c = _istream.read(buf);
	            } catch(IOException ignored) {
	                // return -1
	            } catch(ArrayIndexOutOfBoundsException ignored) {
	                // return -1
	            }
	            return c;
	        }

	        public int read(byte[] buf, int offset, int length) {
	            int c = -1;

	            if (_istream == null) {
	                return c;
	            }

	            try {
	                c = _istream.read(buf, offset, length);
	            } catch(IOException ignored) {
	                // return -1
	            } catch(ArrayIndexOutOfBoundsException ignored) {
	                // happens on windows machines occasionally.
	                // return -1
	            }
	            return c;
	        }

	        /** 
	         * Reads a new line omitting EOL characters.
	         * Regarding this code a line is defined as a minimal
	         * sequence of character ending with "\n", with
	         * all "\r"'s thrown away.
	         * 
	         * Thus calling readLine on a stream containing 
	         * "abc\r\n" or "a\rbc\n" will return "abc".
	         *
	         * Throws IOException if there is an IO error.  Returns null if
	         * there are no more lines to read, i.e., EOF has been reached.
	         * Note that calling readLine on "ab<EOF>" returns null.
	         */
	        public String readLine() throws IOException {
	            if (_istream == null)
	                return "";

	            StringBuilder sBuffer = new StringBuilder();
	            int c = -1; //the character just read
	            boolean keepReading = true;
	            
	    		do {
	    		    try {
	    		    	//System.out.println("Stream currently available: " + _istream.available());
	    		    	
	    		    	//if (_istream.available() == 0) 
	    		    	//	c = -1;
	    		    	//else
	    		    		c = _istream.read();
	                } catch(ArrayIndexOutOfBoundsException aiooe) {
	                    // this is apparently thrown under strange circumstances.
	                    // interpret as an IOException.
	                    throw new IOException("aiooe.");
	                }
	    			switch(c) {
	    			    // if this was a \n character, break out of the reading loop
	    			    case  N: keepReading = false;
	    			             break;
	    			    // if this was a \r character, ignore it.
	    			    case  R: continue;
	    			    // if we reached an EOF ...
	    			    case -1: return null;			             
	                    // if it was any other character, append it to the buffer.
	    			    default: sBuffer.append((char)c);
	    			}
	            } while(keepReading);

	    		// return the string we have read.
	    		return sBuffer.toString();
	        }
	 }
	    

	      
	    

	// Class to handle de channel	
		
	public class ChannelCallback {
	    private SocketChannel channel;
	    private StringBuffer buffer;

	    public ChannelCallback( SocketChannel channel ) {
	        this.channel = channel;
	        this.buffer = new StringBuffer();
	    }

	    public void execute() throws IOException {
	        writeMessage( this.channel, this.buffer.toString() );
	        buffer = new StringBuffer();
	    }

	    public SocketChannel getChannel() {
	        return this.channel;
	    }

	    public void append( String values ) {
	        buffer.append( values );
	    }

	}


}
