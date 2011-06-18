/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2005 Philippe Detournay   */
/*                                                   */
/*         All contacts : theplouf@yahoo.com         */
/*                                                   */
/*  PJIRC is free software; you can redistribute     */
/*  it and/or modify it under the terms of the GNU   */
/*  General Public License as published by the       */
/*  Free Software Foundation; version 2 or later of  */
/*  the License.                                     */
/*                                                   */
/*  PJIRC is distributed in the hope that it will    */
/*  be useful, but WITHOUT ANY WARRANTY; without     */
/*  even the implied warranty of MERCHANTABILITY or  */
/*  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU   */
/*  General Public License for more details.         */
/*                                                   */
/*  You should have received a copy of the GNU       */
/*  General Public License along with PJIRC; if      */
/*  not, write to the Free Software Foundation,      */
/*  Inc., 59 Temple Place, Suite 330, Boston,        */
/*  MA  02111-1307  USA                              */
/*                                                   */
/*****************************************************/

package irc.dcc.prv;

import irc.dcc.*;
import java.net.*;
import java.io.*;
import irc.*;
import java.util.*;

/**
 * The dcc file handler. There is a handler for each file transfert.
 */
public class DCCFileHandler extends IRCObject implements Server,Runnable
{
  private Socket _socket;
  private ServerSocket _serverSocket;
  private Thread _thread;

  private OutputStream _os;
  private InputStream _is;
  private DCCFile _file;

  private int _action;
  private int _size;
  private boolean _listening;
  private boolean _connected;
  private ListenerGroup _listeners;


  /**
   * Create a new DCCFileHandler.
   * @param config the global configuration.
   * @param remoteNick the remote nick.
   * @param f the file to send or receive.
   */
  public DCCFileHandler(IRCConfiguration config,String remoteNick,File f)
  {
    super(config);
    _action=0;
    _size=0;
    _connected=false;
    _file=new DCCFile(config,f,this);
    _listeners=new ListenerGroup();
  }

  public void release()
  {
    cleanup();
    _file=null;
    super.release();
  }

  /**
   * Receive the file.
   * @param ip the remote ip.
   * @param port the remote port.
   * @param size the file size.
   */
  public void receive(String ip,String port,String size)
  {
    _size=new Integer(size).intValue();
    _file.prepareReceive(_size);
    _serverSocket=null;
    _action=1;
    long iip=new Long(ip).longValue();
    int b1=(int)(iip&255);
    int b2=(int)((iip>>8)&255);
    int b3=(int)((iip>>16)&255);
    int b4=(int)((iip>>24)&255);
    ip=b4+"."+b3+"."+b2+"."+b1;

    try
    {
      _socket=_ircConfiguration.getSecurityProvider().getSocket(ip,new Integer(port).intValue());
      _is=new BufferedInputStream(_socket.getInputStream());
      _os=new BufferedOutputStream(_socket.getOutputStream());
      _thread=new Thread(this,"DCCFile thread");
      _thread.start();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("receive failure",e,"bugs@frostwire.com");
    }

  }

  /**
   * Send the file in passive mode.
   * @return the string to send to the remote peer to initiate the transfert.
   */
  public String send()
  {
    _action=2;
    _socket=null;
    try
    {
      _serverSocket=_ircConfiguration.getSecurityProvider().getServerSocket(0);
      int port=_serverSocket.getLocalPort();

      InetAddress addr=_ircConfiguration.getSecurityProvider().getLocalHost();
      byte[] ip=addr.getAddress();

      int b1=ip[0];if(b1<0) b1+=256;
      int b2=ip[1];if(b2<0) b2+=256;
      int b3=ip[2];if(b3<0) b3+=256;
      int b4=ip[3];if(b4<0) b4+=256;

      long high=(b1<<24)+(b2<<16)+(b3<<8)+b4;

      _file.prepareSend();
      int size=_file.getSize();
      String sip=""+high;
      _listening=false;
      _thread=new Thread(this,"DCCFile thread");
      _thread.start();
      while(!_listening) Thread.yield();
      return sip+" "+port+" "+size;
    }
    catch(Exception e)
    {
      return "";
    }
  }

  private void writeConf(OutputStream os,int v) throws IOException
  {
    os.write((v>>24)&255);
    os.write((v>>16)&255);
    os.write((v>>8)&255);
    os.write((v)&255);
    os.flush();
  }

  private int readConf(InputStream is) throws IOException
  {
    int b1=is.read();if(b1<0) b1+=256;
    int b2=is.read();if(b2<0) b2+=256;
    int b3=is.read();if(b3<0) b3+=256;
    int b4=is.read();if(b4<0) b4+=256;
    return (b1<<24)+(b2<<16)+(b3<<8)+b4;
  }

  private void connected()
  {
    _connected=true;
    _listeners.sendEventAsync("serverConnected",this);
  }

  private void disconnected()
  {
    _connected=false;
    _listeners.sendEventAsync("serverDisconnected",this);
  }

  public void run()
  {
    byte[] buffer=new byte[4096];
    if(_action==1) //receive
    {
      try
      {
        connected();
        int read=0;
        while(_size-read>0)
        {
          int r=_is.read(buffer,0,buffer.length);
          if(r==-1) throw new Exception(getText(IRCTextProvider.DCC_STREAM_CLOSED));
          read+=r;
          _file.bytesReceived(buffer,0,r);
          Thread.yield();
          writeConf(_os,read);
        }
        writeConf(_os,_size);
        _file.fileReceived();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        _file.fileReceiveFailed();
      }
      disconnected();
      cleanup();
    }
    else if(_action==2) //send
    {
      _listening=true;
      try
      {
        _serverSocket.setSoTimeout(30000);
        _socket=_serverSocket.accept();
        _os=new BufferedOutputStream(_socket.getOutputStream());
        _is=new BufferedInputStream(_socket.getInputStream());
        connected();
        int size=_file.getSize();
        int toread=size;
        int rec=0;
        while(toread>0)
        {
          int r=_file.readBytes(buffer,0,buffer.length);
          if(r<0) throw new Exception(getText(IRCTextProvider.DCC_STREAM_CLOSED));
          _os.write(buffer,0,r);
          toread-=r;

          if(_is.available()>0) rec=readConf(_is);
        }
        _os.flush();
        while(rec!=size)
        {
          rec=readConf(_is);
        }
        _os.close();
        _file.fileSent();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        _file.fileSentFailed();
      }
      disconnected();
      cleanup();

    }
  }

  private void cleanup()
  {
    try
    {
      if(_socket!=null) _socket.close();
      if(_serverSocket!=null) _serverSocket.close();
      _is.close();
      _os.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("cleanup failure",e,"bugs@frostwire.com");
    }
  }

  /**
   * Close the current transfert.
   */
  public void close()
  {
    cleanup();
  }

  public void say(String destination,String str)
  {
    //nothing here
  }

  public void execute(String str)
  {
    //nothing here
  }

  public void sendStatusMessage(String str)
  {
    //nothing here
  }

  public String getNick()
  {
    return "";
  }

  public String getUserName()
  {
    return "";
  }

  public void connect()
  {
    //nothing here...
  }

  public void disconnect()
  {
    close();
  }

  public boolean isConnected()
  {
    return _connected;
  }

  public Enumeration<Source> getSources()
  {
    Vector<Source> v=new Vector<Source>();
    if(_file!=null) v.insertElementAt(_file,v.size());
    return v.elements();
  }

  public void enumerateSourcesAsCreated(ServerListener l)
  {
    if(_file!=null) l.sourceCreated(_file,this,new Boolean(true));
  }

  public void enumerateSourcesAsRemoved(ServerListener l)
  {
    if(_file!=null) l.sourceRemoved(_file,this);
  }

  public void setDefaultSource(Source source)
  {
    //nothing here...
  }

  public void addServerListener(ServerListener l)
  {
    _listeners.addListener(l);
  }

  public void removeServerListener(ServerListener l)
  {
    _listeners.removeListener(l);
  }

  public void leave()
  {
    disconnect();
    long time=System.currentTimeMillis();
    while(isConnected())
    {
      try
      {
        Thread.sleep(100);
        if(System.currentTimeMillis()-time>10000) break;
      }
      catch(InterruptedException ex)
      {
        //ignore it...
      }
    }
    _listeners.sendEvent("sourceRemoved",_file,this);
    _listeners.sendEvent("serverLeft",this);
    _file.release();
  }

  public String getServerName()
  {
    return getNick();
  }
}

