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
import irc.*;
import java.net.*;
import java.util.*;

/**
 * The DCCChat server. There is a distinct DCCChat server for each active DCCChat.
 */
public class DCCChatServer extends IRCObject implements Runnable,Server
{
  private Socket _socket;
  private ServerSocket _serverSocket;
  private CodingHandler _handler;
  private Thread _thread;
  private DCCChat _chat;
  private String _remoteNick;
  private String _thisNick;
  private ListenerGroup _listeners;
  private boolean _listening;
  private int _action;
  private boolean _connected;

  /**
   * Create a new DCCChat server.
   * @param config the global irc configuration.
   * @param thisNick the local nick.
   * @param remoteNick the remote nick.
   */
  public DCCChatServer(IRCConfiguration config,String thisNick,String remoteNick)
  {
    super(config);
    _action=0;
    _listeners=new ListenerGroup();
    _remoteNick=remoteNick;
    _thisNick=thisNick;
    _connected=false;
    _chat=new DCCChat(_ircConfiguration,this,_remoteNick);
  }

  public void addServerListener(ServerListener l)
  {
    _listeners.addListener(l);
  }

  public void removeServerListener(ServerListener l)
  {
    _listeners.removeListener(l);
  }

  public void connect()
  {
    //nothing here
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
    if(_chat!=null) v.insertElementAt(_chat,0);
    return v.elements();
  }

  public void enumerateSourcesAsCreated(ServerListener lis)
  {
    if(_chat!=null) lis.sourceCreated(_chat,this,new Boolean(_action==2));
  }

  public void enumerateSourcesAsRemoved(ServerListener lis)
  {
    if(_chat!=null) lis.sourceRemoved(_chat,this);
  }

  public void setDefaultSource(Source s)
  {
    //nothing here
  }

  public void release()
  {
    cleanup();
    _chat=null;
    super.release();
  }

  /**
   * Open an active connection on the given chat.
   * @param ip the ip to contact.
   * @param port the port to contact.
   */
  public void openActive(String ip,String port)
  {
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
      _handler=new CodingHandler(_ircConfiguration,_socket.getInputStream(),_socket.getOutputStream());
      _thread=new Thread(this,"DCCChat thread");
      _thread.start();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("openActive failure",e,"bugs@frostwire.com");
    }

  }

  /**
   * Open a passive connection on the given chat.
   * @return the string to send to the remote peer to begin the communication.
   */
  public String openPassive()
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

      String sip=""+high;
      _listening=false;
      _thread=new Thread(this,"DCCChat thread");
      _thread.start();
      while(!_listening) Thread.yield();
      return sip+" "+port;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }

  private void reportChat(String str)
  {
    EventDispatcher.dispatchEventAsync(_chat,"report",new Object[] {str});
  }

  private void messageChat(String src,String str)
  {
    EventDispatcher.dispatchEventAsync(_chat,"messageReceived",new Object[] {src,str});
  }

  public void run()
  {
    boolean terminated=false;
    if(_action==2)
    {
      try
      {
        _listening=true;
        reportChat(getText(IRCTextProvider.DCC_WAITING_INCOMING));
        _serverSocket.setSoTimeout(30000);
        _socket=_serverSocket.accept();
        _handler=new CodingHandler(_ircConfiguration,_socket.getInputStream(),_socket.getOutputStream());
      }
      catch(Exception e)
      {
        reportChat(getText(IRCTextProvider.DCC_UNABLE_TO_OPEN_CONNECTION,e.getMessage()));
        return;
      }
    }
    reportChat(getText(IRCTextProvider.DCC_CONNECTION_ESTABLISHED));
    _connected=true;
    _listeners.sendEventAsync("serverConnected",this);
    while(!terminated)
    {
      try
      {
        String line=_handler.read();
        if(line==null) throw new Exception(getText(IRCTextProvider.DCC_CONNECTION_CLOSED));
        try
        {
          messageChat(_remoteNick,line);
        }
        catch(Exception e)
        {
          _ircConfiguration.internalError("internal error",e,"bugs@frostwire.com");
        }
      }
      catch(Exception e)
      {
        terminated=true;
        reportChat(getText(IRCTextProvider.DCC_ERROR,e.getMessage()));
      }
    }
    _connected=false;
    _listeners.sendEventAsync("serverDisconnected",this);
    cleanup();
  }

  public void say(String destination,String str)
  {
    if(destination.equals(_remoteNick))
      sendString(str);
    else
      _chat.report(getText(IRCTextProvider.DCC_ERROR,getText(IRCTextProvider.DCC_UNABLE_TO_SEND_TO,destination)));
  }

  public void execute(String str)
  {
    _chat.report(getText(IRCTextProvider.DCC_BAD_CONTEXT));
  }

  private void sendString(String str)
  {
    try
    {
      if((_handler==null) || (!_connected)) throw new Exception(getText(IRCTextProvider.DCC_NOT_CONNECTED));
      _handler.write(str);
    }
    catch(Exception e)
    {
      _chat.report(getText(IRCTextProvider.DCC_ERROR,e.getMessage()));
    }
  }

  public void sendStatusMessage(String str)
  {
    if(_chat!=null) _chat.report(str);
  }

  private void cleanup()
  {
    try
    {
      if(_socket!=null) _socket.close();
      if(_serverSocket!=null) _serverSocket.close();
      _handler.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("cleanup failure",e,"bugs@frostwire.com");
    }
  }

  /**
   * Close this communication.
   */
  public void close()
  {
    cleanup();
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
    _listeners.sendEvent("sourceRemoved",_chat,this);
    _listeners.sendEvent("serverLeft",this);
    _chat.release();
  }

  public String getNick()
  {
    return _thisNick;
  }

  public String getUserName()
  {
    return "";
  }
  
  public String getServerName()
  {
    return getNick();
  }
}

