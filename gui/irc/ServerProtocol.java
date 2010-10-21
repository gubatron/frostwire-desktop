package irc;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The raw irc server protocol handler.
 */
public class ServerProtocol extends IRCObject implements Runnable
{
  private ListenerGroup _listeners;
  private String _host;
  private int _port;
  private Socket _socket;
  private CodingHandler _handler;
  private Thread _thread;
  private boolean _connected;
  private boolean _connecting;

  /**
   * Create a new ServerProtocol.
   * @param config global irc configuration.
   */
  public ServerProtocol(IRCConfiguration config)
  {
    super(config);
    _connected=false;
    _connecting=false;
    _listeners=new ListenerGroup();
  }

  /**
   * Connect to the server.
   * @param host server host.
   * @param port server port.
   */
  public void connect(String host,int port)
  {
    if(_connected) disconnect();
    _connecting=true;
    _host=host;
    _port=port;
    _thread=new Thread(this,"Read thread");
    _thread.start();
  }

  /**
   * Return connected status.
   * @return true if connected, false otherwise.
   */
  public boolean connected()
  {
    return _connected;
  }

  /**
   * Return connecting status.
   * @return true if connecting, false otherwise.
   */
  public boolean connecting()
  {
    return _connecting;
  }

  /**
   * Disconnect from server.
   */
  public synchronized void disconnect()
  {
    if(!_connected) return;
    if(_connecting) return;
    try
    {
      _socket.close();
      _handler.close();
    }
    catch(Exception e)
    {
   //   System.out.println("disconnection");
   //   System.out.println(e);
    }
    _connected=false;
    _listeners.sendEvent("disconnected",_host);
  }

  /**
   * Get the local, client-side tcp port for the current connection.
   * @return the local port.
   */
  public int getLocalPort()
  {
    return _socket.getLocalPort();
  }

  private void decodeLine(String line)
  {
    Vector res=new Vector();
    while(line.length()!=0)
    {
      if(line.startsWith(":") && (res.size()!=0))
      {
        res.insertElementAt(line.substring(1),res.size());
        line="";
      }
      else
      {
        int pos=line.indexOf(' ');
        if(pos==-1)
        {
          res.insertElementAt(StringParser.trim(line),res.size());
          line="";
        }
        else
        {
          String part=StringParser.trim(line.substring(0,pos));
          line=StringParser.trim(line.substring(pos+1));
          res.insertElementAt(part,res.size());
        }
      }
    }
    if(res.size()==0) return;

    String source="";
    if(((String)(res.elementAt(0))).startsWith(":"))
    {
      source=(String)res.elementAt(0);
      source=source.substring(1);
      res.removeElementAt(0);
    }
    if(res.size()==0) return;

    String cmd=(String)res.elementAt(0);
    res.removeElementAt(0);

    String[] param=new String[res.size()];
    for(int i=0;i<res.size();i++) param[i]=(String)res.elementAt(i);

    if((cmd.charAt(0)>='0') && (cmd.charAt(0)<='9'))
    {

      _listeners.sendEventAsync("replyReceived",source,cmd,param);
    }
    else
    {

      _listeners.sendEventAsync("messageReceived",source,cmd,param);
    }
  }

  public void run()
  {
    try
    {
      _socket=_ircConfiguration.getSecurityProvider().getSocket(_host,_port);

      _handler=new CodingHandler(_ircConfiguration,new BufferedInputStream(_socket.getInputStream()),new BufferedOutputStream(_socket.getOutputStream()));
      _connected=true;
      _connecting=false;
      _listeners.sendEventAsync("connected",_host);
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      _connecting=false;

      if(e.getMessage()!=null)
        _listeners.sendEventAsync("connectionFailed",e.getMessage(),_host);
      else
        _listeners.sendEventAsync("connectionFailed",e.getClass().getName(),_host);
        
      return;
    }
    boolean terminated=false;
    while(!terminated)
    {
      try
      {
        String line=_handler.read();
        if(line==null) throw new Exception();
        //System.out.println("--> "+line);
        try
        {
          if(line!=null) decodeLine(line);
        }
        catch(Exception e)
        {
          _ircConfiguration.internalError("Internal Error",e,"chat@frostwire.com");
        }
      }
      catch(Exception e)
      {
        terminated=true;
      }
    }
    EventDispatcher.dispatchEventAsync(this,"disconnect",new Object[0]);
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addServerProtocolListener(ServerProtocolListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeServerProtocolListener(ServerProtocolListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Send a raw string to the server.
   * @param str string to send.
   * @throws Exception if something went wrong.
   */
  public void sendString(String str) throws Exception
  {
    if(!connected()) throw new Exception("2  You are currently not connected...");
    _handler.write(str);
    //System.out.println("<-- "+str);
  }

  /**
   * Send a formatted string to the server.
   * @param command comment to send.
   * @param params parameters list. Only the last parameter can contain blanks.
   * @throws Exception if something went wrong.
   */
  public void sendCommand(String command,String params[]) throws Exception
  {
    String toSend=command;

    for(int i=0;i<params.length;i++)
    {
      toSend+=" ";
      if(params[i].indexOf(' ')!=-1) toSend+=":";
      toSend+=params[i];
    }
    sendString(toSend);
  }


}

