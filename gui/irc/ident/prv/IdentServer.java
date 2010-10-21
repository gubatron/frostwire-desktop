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

package irc.ident.prv;

import irc.ident.*;
import java.io.*;
import java.net.*;
import java.util.*;
import irc.*;

/**
 * LocalInfo.
 */
class LocalInfo
{
  /**
   * Create a new LocalInfo
   * @param alocalPort local port.
   * @param asystem system name.
   * @param aid user id.
   */
  public LocalInfo(int alocalPort,String asystem,String aid)
  {
    this.localPort=alocalPort;
    this.system=asystem;
    this.id=aid;
  }

  /**
   * Local port.
   */
  public int localPort;
  /**
   * System name.
   */
  public String system;
  /**
   * User id.
   */
  public String id;
}

/**
 * The built-in pjirc ident server.
 */
public class IdentServer extends IRCObject implements Runnable
{
  private Thread _thread;
  private boolean _running;
  private Hashtable _table;
  private ServerSocket _serverSocket;
  private boolean _defaultUser;
  private String _system;
  private String _id;
  private ListenerGroup _listeners;
  private int _port;

  /**
   * Create a new IdentServer.
   * @param config global irc configuration.
   */
  public IdentServer(IRCConfiguration config)
  {
    super(config);
    resetDefaultUser();
    _table=new Hashtable();
    _listeners=new ListenerGroup();
    _thread=null;
  }

	/**
	 * Start the ident server.
	 * @throws Exception
	 */
  public void start() throws Exception
  {
    start(113);
  }

  /**
   * Erase the default user configuration.
   */
  public void resetDefaultUser()
  {
    _defaultUser=false;
  }

  /**
   * Set the default user configuration.
   * @param system user system.
   * @param id user id.
   */
  public void setDefaultUser(String system,String id)
  {
    _defaultUser=true;
    _system=system;
    _id=id;
  }

  /**
   * Start the ident server on the given port.
   * @param port ident server port.
   * @throws Exception
   */
  public void start(int port) throws Exception
  {
    _port=port;
    _running=false;
    _serverSocket=_ircConfiguration.getSecurityProvider().getServerSocket(_port);
    _thread=new Thread(this,"IDENT server");
    _thread.start();
    while(!_running) Thread.yield();
  }

  /**
   * Stop the execution of the ident server.
   */
  public void stop()
  {
    if(_thread==null) return;
    try
    {
      _serverSocket.close();
      _thread.join();
      _thread=null;
    }
    catch(Exception e)
    {
     // e.printStackTrace();
    }
  }

  /**
   * Register a new local connection.
   * @param localPort the local port of this connection.
   * @param system user system on this connection.
   * @param id user id on this connection.
   */
  public synchronized void registerLocalConnection(int localPort,String system,String id)
  {
    _table.put(new Integer(localPort),new LocalInfo(localPort,system,id));
  }

  /**
   * Unregister a local connection.
   * @param localPort local port.
   */
  public synchronized void unregisterLocalConnection(int localPort)
  {
    _table.remove(new Integer(localPort));
  }

  private synchronized LocalInfo processRequest(int localPort)
  {
    return (LocalInfo)_table.get(new Integer(localPort));
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public synchronized void addIdentListener(IdentListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public synchronized void removeIdentListener(IdentListener lis)
  {
    _listeners.removeListener(lis);
  }

  public void run()
  {
    boolean terminated=false;

    _running=true;
    _listeners.sendEventAsync("identRunning",new Integer(_port));
    while(!terminated)
    {
      try
      {
        Socket s=_serverSocket.accept();
        String from=getText(IRCTextProvider.IDENT_UNKNOWN);
        int result=IdentListener.IDENT_ERROR;
        String reply=getText(IRCTextProvider.IDENT_NONE);
        try
        {
          try
          {
            from=_ircConfiguration.getSecurityProvider().resolve(s.getInetAddress());
          }
          catch(Exception e)
          {
            from=s.getInetAddress().getHostAddress() ;
          }
          BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
          BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
          String request=reader.readLine();
          int pos=request.indexOf(',');
          String serverSide=request.substring(0,pos).trim();
          String clientSide=request.substring(pos+1).trim();

          LocalInfo info=processRequest(new Integer(serverSide).intValue());
          reply=serverSide+" , "+clientSide+" : ";
          if(info==null)
          {
            if(!_defaultUser)
            {
              result=IdentListener.IDENT_NOT_FOUND;
              reply+="ERROR : NO-USER";
            }
            else
            {
              result=IdentListener.IDENT_DEFAULT;
              reply+="USERID : "+_system+" : "+_id;
            }
          }
          else
          {
            result=IdentListener.IDENT_OK;
            reply+="USERID : "+info.system+" : "+info.id;
          }

          writer.write(reply+"\n");
          writer.flush();
          reader.close();
          writer.close();
          s.close();
          _listeners.sendEventAsync("identRequested",from,new Integer(result),reply);

        }
        catch(Exception e)
        {
          _listeners.sendEventAsync("identRequested",from,new Integer(IdentListener.IDENT_ERROR),e.getMessage());
        }
      }
      catch(Exception e)
      {
        _listeners.sendEventAsync("identLeaving",e.getMessage());
        terminated=true;
      }
    }
  }

}

