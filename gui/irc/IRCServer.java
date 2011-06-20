package irc;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * FirstLineFilter, used to handle CTCP codes.
 */
class FirstLineFilter
{
  private IRCServer _server;

  /**
   * Create a new FirstLineFilter
   * @param serv
   * @param mgr
   * @param config
   */
  public FirstLineFilter(IRCServer serv,ServerManager mgr,IRCConfiguration config)
  {
    _server=serv;
  }

  /**
   * Release this object.
   */
  public void release()
  {
    _server=null;
  }

  /**
   * Perform any needed action from a channel message.
   * @param channel channel name.
   * @param nick nickname.
   * @param msg actual message.
   * @return true if message was handled, false otherwise.
   */
  public boolean performFromChannelMessage(String channel,String nick,String msg)
  {
    if(!msg.startsWith("\1")) return false;

    msg=msg.substring(1);
    msg=msg.substring(0,msg.length()-1);
    String cmd="";
    String param="";
    int pos=msg.indexOf(' ');
    if(pos==-1)
    {
      cmd=msg.toLowerCase(java.util.Locale.ENGLISH);
    }
    else
    {
      cmd=msg.substring(0,pos).toLowerCase(java.util.Locale.ENGLISH);
      param=msg.substring(pos+1);
    }

    if(cmd.equals("action"))
    {
      Channel c=_server.getChannel(channel,false);
      if(c!=null) c.action(nick,param);
    }
    return true;
  }

  /**
   * Perform any needed action from a nick message.
   * @param nick nickname.
   * @param msg actual message.
   * @return true if message was handled, false otherwise.
   */
  public boolean performFromNickMessage(String nick,String msg)
  {
    if(!msg.startsWith("\1")) return false;

    msg=msg.substring(1);
    msg=msg.substring(0,msg.length()-1);
    String cmd="";
    String param="";
    int pos=msg.indexOf(' ');
    if(pos==-1)
    {
      cmd=msg.toLowerCase(java.util.Locale.ENGLISH);
    }
    else
    {
      cmd=msg.substring(0,pos).toLowerCase(java.util.Locale.ENGLISH);
      param=msg.substring(pos+1);
    }

    if(cmd.equals("action"))
    {
      Query q=_server.getQuery(nick,false);
      if(q!=null) q.action(nick,param);
    }
    else if(cmd.equals("version"))
    {
      String data="FrostWire Community Chat v0.4c";
      _server.execute("NOTICE "+nick+" :\1VERSION "+data+" VKEY=46o1dkNwNXnVJ5YXttPc2NVyxtu02x8C"+"\1");
    }
    else if(cmd.equals("voidkey"))
    {
      _server.execute("NOTICE "+nick+" :\1VOIDKEY"+" RE5q8M84Ifevpp622PpNfb2Iz4FPH4AL"+"\1");
    }
    else if(cmd.equals("expirekey"))
    {
      _server.execute("NOTICE "+nick+" :\1EXPIREKEY"+" ezhw5C31hm3A0b0xnh7iG3iH1ZE17QsV"+"\1");
    }
    /*else if(cmd.equals("ping"))
    {
      _server.execute("NOTICE "+nick+" :\1PING "+param+"\1");
    }*/
  	//maybe change cmd to "date" to help prevent bot floods.
    else if(cmd.equals("time"))
    {
      /* Calendar cal=Calendar.getInstance();
      String hour=""+cal.get(Calendar.HOUR_OF_DAY);
      String min=""+cal.get(Calendar.MINUTE);
      String sec=""+cal.get(Calendar.SECOND);
      String ampm=""+cal.get(Calendar.AM_PM);
      String month=""+cal.get(Calendar.MONTH);
      String year=""+cal.get(Calendar.YEAR);
      String dowim=""+cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
      String data=month+"/"+dowim+"/"+year+", "+hour+":"+min+":"+sec+" "+ampm; */
    	String data=new Date().toString();
    	_server.execute("NOTICE "+nick+" :\1TIME "+data+"\1");
    	}
    return true;
  }

  /**
   * Perform any needed action from a notice message.
   * @param nick nickname.
   * @param msg actual message.
   * @return true if message was handled, false otherwise.
   */
  public boolean performFromNotice(String nick,String msg)
  {
    if(!msg.startsWith("\1")) return false;

    msg=msg.substring(1);
    msg=msg.substring(0,msg.length()-1);
    String cmd="";
    String param="";
    int pos=msg.indexOf(' ');
    if(pos==-1)
    {
      cmd=msg.toLowerCase(java.util.Locale.ENGLISH);
    }
    else
    {
      cmd=msg.substring(0,pos).toLowerCase(java.util.Locale.ENGLISH);
      param=msg.substring(pos+1);
    }

    Source source=_server.getDefaultSource();
    /*if(cmd.equals("ping"))
    {
      long d=(new Long(param)).longValue();
      long delta=(new Date()).getTime()-d;
      if(source!=null) source.report("\3"+"14"+"      *** "+nick+"'s reply: "+(delta/1000.0));
      return true;
    }*/
    if(cmd.equals("time"))
    {
      if(source!=null) source.report("\3"+"14"+"***"+nick+"'s local time: "+param);
      return true;
    }
    else
    {
        return true;
    }
    //else if(source!=null) source.report("\3"+"14"+"      *** "+nick+"'s reply: "+param);
  }
}

/**
 * The IRC server.
 */
public class IRCServer extends IRCObject implements Server, ServerProtocolListener
{
  private ServerProtocol _protocol;
  private Hashtable<String, Source> _channels;
  private Hashtable<String, Source> _queries;
  private Hashtable<String, Source> _chanlist;
  private Status _status;

  private Hashtable<String, String> _ignoreList;

  private ListenerGroup _listeners;
  private ListenerGroup _replylisteners;
  private ListenerGroup _messagelisteners;
  private String[] _askedNick;
  private String _nick;
  private String _userName;
  private int _tryNickIndex;
  private ModeHandler _mode;
  private String[] _host;
  private int[] _port;
  private String _passWord[];
  private int _tryServerIndex;
	private boolean _connected;
	private String _name;
	private Source _defaultSource;
	private boolean _serverLeaving;
	private boolean _registered;
	private FirstLineFilter _filter;
  //private boolean _nickWaiting=false;

  private char[] _nickModes={'q','a','o','h','v'};
  private char[] _nickPrefixes={'~','&','@','%','+'};
  private char[] _channelPrefixes={'#','&','!','+'};
  private char[][] _globalModes={{'b'},{'k'},{'l'},{'i','m','n','p','s','t','a','q','r'}};
  /**
   * Create a new IRCServer.
   * @param config global IRCConfiguration.
   * @param mgr the server manager.
   * @param nick claimed nick.
   * @param altNick claimed alternate nick.
   * @param userName user name.
   * @param name the server name.
   */
  public IRCServer(IRCConfiguration config,ServerManager mgr,String nick,String altNick,String userName,String name)
  {
    super(config);
    _filter=new FirstLineFilter(this,mgr,config);
    _serverLeaving=false;
    _name=name;
    _userName=userName;
    _askedNick=new String[2];
    _askedNick[0]=nick;
    _askedNick[1]=altNick;
    _nick=nick;
    _connected=false;
    _ignoreList=new Hashtable<String, String>();

    _channels=new Hashtable<String, Source>();
    _queries=new Hashtable<String, Source>();
    _chanlist=new Hashtable<String, Source>();

    _listeners=new ListenerGroup();
    _replylisteners=new ListenerGroup();
    _messagelisteners=new ListenerGroup();

    _status=new Status(_ircConfiguration,this);
    _defaultSource=_status;

    _protocol=new ServerProtocol(_ircConfiguration);
    _protocol.addServerProtocolListener(this);


    _host=null;
    _mode=new ModeHandler(_globalModes,_nickModes);
  }

  /**
   * Send a special request event to all listeners.
   * @param request request string.
   * @param params request parameters.
   * @return results.
   */
  public Object[] specialRequest(String request,Object[] params)
  {
    return _listeners.sendEvent("specialServerRequest",request,this,params);
  }

  public void release()
  {
    _protocol.removeServerProtocolListener(this);
    _filter.release();
    super.release();
  }

  public Enumeration<Source> getSources()
  {
    Vector<Source> v=new Vector<Source>();
    Enumeration<Source> e;
    e=_channels.elements();
    while(e.hasMoreElements()) v.insertElementAt(e.nextElement(),v.size());
    e=_queries.elements();
    while(e.hasMoreElements()) v.insertElementAt(e.nextElement(),v.size());
    e=_chanlist.elements();
    while(e.hasMoreElements()) v.insertElementAt(e.nextElement(),v.size());
    if(_status!=null) v.insertElementAt(_status,v.size());
    return v.elements();
  }

  public void enumerateSourcesAsCreated(ServerListener lis)
  {
    Enumeration<Source> e;
    e=_channels.elements();while(e.hasMoreElements()) lis.sourceCreated((Source)e.nextElement(),this,new Boolean(false));
    e=_queries.elements();while(e.hasMoreElements()) lis.sourceCreated((Source)e.nextElement(),this,new Boolean(false));
    e=_chanlist.elements();while(e.hasMoreElements()) lis.sourceCreated((Source)e.nextElement(),this,new Boolean(false));
    if(_status!=null) lis.sourceCreated(_status,this,new Boolean(true));
  }

  public void enumerateSourcesAsRemoved(ServerListener lis)
  {
    Enumeration<Source> e;
    e=_channels.elements();while(e.hasMoreElements()) lis.sourceRemoved((Source)e.nextElement(),this);
    e=_queries.elements();while(e.hasMoreElements()) lis.sourceRemoved((Source)e.nextElement(),this);
    e=_chanlist.elements();while(e.hasMoreElements()) lis.sourceRemoved((Source)e.nextElement(),this);
    if(_status!=null) lis.sourceRemoved(_status,this);
  }

  public void setDefaultSource(Source s)
  {
    _defaultSource=s;
  }

  /**
   * Get the default server source, or null if no default source is defined.
   * @return default source.
   */
  public Source getDefaultSource()
  {
    return _defaultSource;
  }

  /**
   * Set default configuration for the next connection.
   * @param host server host.
   * @param port server port.
   * @param passWord server password.
   */
  public void setServers(String host[],int port[],String passWord[])
  {
    _tryServerIndex=0;
    _host=new String[host.length];
    for(int i=0;i<host.length;i++) _host[i]=host[i];
    _port=new int[port.length];
    for(int i=0;i<port.length;i++) _port[i]=port[i];
    _passWord=new String[passWord.length];
    for(int i=0;i<passWord.length;i++) _passWord[i]=passWord[i];
  }

  public void connect()
  {
    _tryServerIndex=0;
    if(_host!=null)
      connect(_host,_port,_passWord);
  }

  private void connect(String host[],int port[],String[] passWord)
  {
    _registered=false;
    //if(_nickWaiting) return;
    if(_tryServerIndex==_host.length) return;
    _tryNickIndex=0;
    _passWord=passWord;
    if(_protocol.connecting())
    {
      sendStatusMessage("3  Unable to connect to "+host[_tryServerIndex]+". Currently trying to connect to "+_host[_tryServerIndex]+".");
      //sendStatusMessage(getText(IRCTextProvider.SERVER_UNABLE_TO_CONNECT_STILL,host[_tryServerIndex],_host[_tryServerIndex]));
      return;
    }
    if(_protocol.connected())
    {
      //sendStatusMessage(getText(IRCTextProvider.SERVER_DISCONNECTED,_host[_tryServerIndex]));
      //disconnect();
      _protocol.disconnect();
    }
	  _connected=false;
    sendStatusMessage("3  Please wait, connecting to server...");
    //sendStatusMessage(getText(IRCTextProvider.SERVER_CONNECTING));
    _protocol.connect(host[_tryServerIndex],port[_tryServerIndex]);
  }

  /**
   * Disconnect from the irc server.
   */
  public void disconnect()
  {
    //if(_nickWaiting) return;
    if(_protocol.connected())
    {
      if(_ircConfiguration.getS("quitmessage").length()==0)
      {
        execute("QUIT");
      }
      else
      {
        execute("QUIT :"+_ircConfiguration.get("quitmessage"));
      }
    }
    else
    {
      sendStatusMessage("2  You are currently not connected...");
      //sendStatusMessage(getText(IRCTextProvider.SERVER_NOT_CONNECTED));
    }
  }

  /**
   * Return true if connected to the server, false otherwise.
   * @return connected state.
   */
  public boolean isConnected()
  {
	  return _connected;
  }

  public void connectionFailed(String message,String host)
  {
    sendStatusMessage("2  Network problems are preventing you from connecting to the chat service. Press connect to try again.");
    //sendStatusMessage("2  Error Message: "+message);
    //sendStatusMessage(getText(IRCTextProvider.SERVER_UNABLE_TO_CONNECT,message));
    _tryServerIndex++;
    if(_tryServerIndex<_host.length)
      connect(_host,_port,_passWord);
  }

	private void nickUsed()
	{
	  if(_tryNickIndex>=_askedNick.length)
	  {
      //_nickWaiting=true;
      Object[] res=_listeners.sendEvent("cannotUseRequestedNicknames",new Object[] {this});
      if(res.length>0) _askedNick=(String[])res[0];
      //_nickWaiting=false;
	    _tryNickIndex=0;
	  }
	  else
	    if(_askedNick[_tryNickIndex].indexOf("?")==-1) _tryNickIndex++;
	}

	private void register()
	{
	  String tryUseNick=_askedNick[_tryNickIndex];
	  if(tryUseNick.length()==0) tryUseNick="Guest_????";
	  String ans="";
    for(int i=0;i<tryUseNick.length();i++)
    {
      char c=tryUseNick.charAt(i);
      if(c=='?') c=(char)('0'+Math.random()*10);
      ans+=c;
    }
    if(_passWord[_tryServerIndex].length()>0) execute("pass "+_passWord[_tryServerIndex]);
    execute("nick "+ans);
    String name=_ircConfiguration.getS("userid");
    if(name.length()==0) name=ans;
    if(!_registered)
    {
      _registered=true;
      execute("user "+name+" 0 0 :"+_userName);
    }
	}

  /**
   * Get the local port of the remote connection.
   * @return the local, client-side port of the remote connection.
   */
	public int getLocalPort()
	{
	  return _protocol.getLocalPort();
	}

  public void connected(String host)
  {
    sendStatusMessage("4  Connected!");
    sendStatusMessage("");
    sendStatusMessage("  Welcome to FrostWire Chat! FrostWire does not control or endorse the content, messages or information found in this chat. FrostWire specifically disclaims any liability with regard to these areas. To review the FrostWire Chat guidelines, go to "+"\3"+"12"+"http://www.frostwire.com/chat/conduct"+"\3");
    sendStatusMessage("");
    //sendStatusMessage(getText(IRCTextProvider.SERVER_LOGIN));
		register();
  }

  private void clear(Hashtable<String, Source> l)
  {
    Enumeration<Source> e;
    e=l.elements();
    while(e.hasMoreElements()) _listeners.sendEvent("sourceRemoved",e.nextElement(),this);
    e=l.elements();
    while(e.hasMoreElements()) ((Source)e.nextElement()).release();
    l.clear();
  }

  public void disconnected(String host)
  {
    sendStatusMessage("3  You have been disconnected from the chat server.");
    //sendStatusMessage("3  Disconnected from "+host);

    clear(_channels);
    clear(_queries);
    clear(_chanlist);

    _mode.reset();
    if(_status!=null) _status.modeChanged(getMode());
    //_defaultSource=null;

    _connected=false;
    _listeners.sendEvent("serverDisconnected",this);

    if(_serverLeaving)
    {
      _listeners.sendEvent("sourceRemoved",_status,this);
      deleteStatus("");
      _listeners.sendEvent("serverLeft",this);
    }
  }

  public void sendStatusMessage(String msg)
  {
    if(_status!=null) _status.report(msg);
  }

  /**
   * Get all the channels.
   * @return an enumeration of channels.
   */
  public Enumeration<Source> getChannels()
  {
    return _channels.elements();
  }

  /**
   * Get all the queries.
   * @return an enumeration of queries.
   */
  public Enumeration<Source> getQueries()
  {
    return _queries.elements();
  }

  /**
   * Get all the chanlists.
   * @return an enumeration of chanlists.
   */
  public Enumeration<Source> getChanLists()
  {
    return _chanlist.elements();
  }

  /**
   * Get the channel from its name. If this channel doesn't exist, it is created only
   * if create boolean is set.
   * @param name channel name.
   * @param create true if channel must be created if not existing.
   * @return channel, or null.
   */
  public Channel getChannel(String name,boolean create)
  {
    Channel c=(Channel)_channels.get(name.toLowerCase(java.util.Locale.ENGLISH));
    if((c==null) && create)
    {
      c=new Channel(_ircConfiguration,name,this);
      _channels.put(name.toLowerCase(java.util.Locale.ENGLISH),c);
      _listeners.sendEvent("sourceCreated",c,this,new Boolean(true));
    }
    return c;
  }

  /**
   * Get the query from its name. If this query doesn't exist, it is created.
   * The query cannot be get if the server is not connected.
   * @param nick query name.
   * @param local true if this query has been created following a local request.
   * @return query, or null if server was not connected.
   */
  public Query getQuery(String nick,boolean local)
  {
    if(!_connected) return null;
    if(_ircConfiguration.getB("disablequeries")) return null;
    Query c=(Query)_queries.get(nick.toLowerCase(java.util.Locale.ENGLISH));
    if(c==null)
    {
      c=new Query(_ircConfiguration,nick,this);
      _queries.put(nick.toLowerCase(java.util.Locale.ENGLISH),c);
      _listeners.sendEvent("sourceCreated",c,this,new Boolean(local));

    }
    return c;
  }

  /**
   * Get the chanlist from its name. If this chanlist doesn't exist, it is created.
   * @param name chanlist name.
   * @return channel.
   */
  private ChanList getChanList(String name)
  {
    ChanList c=(ChanList)_chanlist.get(name.toLowerCase(java.util.Locale.ENGLISH));
    if(c==null)
    {
      c=new ChanList(_ircConfiguration,this,name);
      _chanlist.put(name.toLowerCase(java.util.Locale.ENGLISH),c);
      _listeners.sendEvent("sourceCreated",c,this,new Boolean(true));
    }
    return c;
  }

  /**
   * Request to leave the given channel.
   * @param name channel name.
   */
  public void leaveChannel(String name)
  {
    execute("part "+name);
  }

  /**
   * Request to leave the given query.
   * @param name query name.
   */
  public void leaveQuery(String name)
  {
    Query q=getQuery(name,false);
    if(q==null) return;
    _listeners.sendEvent("sourceRemoved",q,this);
    deleteQuery(name);
  }

  public void leave()
  {
    leaveStatus("");
  }

  /**
   * Request to leave the status. This will cause server leaving.
   * @param name Status name. Unused.
   */
  public void leaveStatus(String name)
  {
    if(_status==null) return;
    if(isConnected())
    {
      _serverLeaving=true;
      disconnect();
    }
    else
    {
      _listeners.sendEvent("sourceRemoved",_status,this);
      deleteStatus("");
      _listeners.sendEvent("serverLeft",this);
    }
  }

  /**
   * Request to leave the given channel list.
   * @param name chanlist name.
   */
  public void leaveChanList(String name)
  {
    _listeners.sendEvent("sourceRemoved",getChanList(name),this);
    deleteChanList(name);
  }

  private void deleteSource(Source src)
  {
    if(src==_defaultSource) _defaultSource=null;
    src.release();
  }

  private void deleteChannel(String name)
  {
    deleteSource((Source)_channels.remove(name.toLowerCase(java.util.Locale.ENGLISH)));
  }

  private void deleteQuery(String name)
  {
    deleteSource((Source)_queries.remove(name.toLowerCase(java.util.Locale.ENGLISH)));
  }

  private void deleteChanList(String name)
  {
    deleteSource((Source)_chanlist.remove(name.toLowerCase(java.util.Locale.ENGLISH)));
  }

  private void deleteStatus(String name)
  {
    deleteSource(_status);
    _status=null;
  }

  public String getServerName()
  {
    if(_name.length()==0)
    {
      if(_tryServerIndex<_host.length)
        return _host[_tryServerIndex];
      return _host[0];
    }
       
    return _name;
  }

  /**
   * Get this server's status, or null if this server has no status.
   * @return the status, or null if the server hasno status.
   */
  public Status getStatus()
  {
    return _status;
  }

  /**
   * Add a server listener.
   * @param l listener to add.
   */
  public void addServerListener(ServerListener l)
  {
    _listeners.addListener(l);
  }

  /**
   * Remove a listener.
   * @param l listener to remove.
   */
  public void removeServerListener(ServerListener l)
  {
    _listeners.removeListener(l);
  }

  /**
   * Add a reply listener.
   * @param l listener to add.
   */
  public void addReplyServerListener(ReplyServerListener l)
  {
    _replylisteners.addListener(l);
  }

  /**
   * Add a message listener.
   * @param l listener to add.
   */
  public void addMessageServerListener(MessageServerListener l)
  {
    _messagelisteners.addListener(l);
  }

  /**
   * Remove a reply listener.
   * @param l listener to remove.
   */
  public void removeReplyServerListener(ReplyServerListener l)
  {
    _replylisteners.removeListener(l);
  }

  /**
   * Remove a message listener.
   * @param l listener to remove.
   */
  public void removeMessageServerListener(MessageServerListener l)
  {
    _messagelisteners.removeListener(l);
  }

  /**
   * Get an array of all known channel prefixes.
   * @return an array of all channel prefixes.
   */
  public char[] getChannelPrefixes()
  {
    return _channelPrefixes;
  }

  /**
   * Get an array of all known nickname prefixes.
   * @return array of all nickname prefixes.
   */
  public char[] getNickPrefixes()
  {
    return _nickPrefixes;
  }

  /**
   * Get an array of all known nickname modes.
   * @return array of all nickname modes.
   */
  public char[] getNickModes()
  {
    return _nickModes;
  }

  /**
   * Get an array of all known A,B,C,D channel modes.
   * @return array of all channel modes. This is an array of four char arrays.
   */
  public char[][] getChannelModes()
  {
    return _globalModes;
  }

  /**
   * Get the nick prefix associated with the given nick mode.
   * @param mode nick mode.
   * @return nick prefix for this mode.
   */
  public String getNickPrefix(String mode)
  {
    if(mode.length()==0) return "";
    char cmode=mode.charAt(0);
    for(int i=0;i<_nickModes.length;i++) if(_nickModes[i]==cmode) return ""+_nickPrefixes[i];
    return "";
  }

  /**
   * Get the nick mode associated with the given nick prefix.
   * @param prefix nick prefix.
   * @return nick mode for this prefix.
   */
  public String getNickMode(String prefix)
  {
    if(prefix.length()==0) return "";
    char cprefix=prefix.charAt(0);
    for(int i=0;i<_nickPrefixes.length;i++) if(_nickPrefixes[i]==cprefix) return ""+_nickModes[i];
    return "";
  }

  private void setNicks(Channel c,Vector<String> nicks)
  {
    String[] n=new String[nicks.size()];
    String[] modes=new String[nicks.size()];

    for(int i=0;i<nicks.size();i++)
    {
      n[i]=(String)nicks.elementAt(i);
      modes[i]="";
      if(n[i].length()>0)
      {
        modes[i]=getNickMode(""+n[i].charAt(0));
        if(modes[i].length()!=0) n[i]=n[i].substring(1);
      }
    }
    c.setNicks(n,modes);
  }

  private void decodeVariable(String key,String val)
  {
    if(key.toLowerCase(java.util.Locale.ENGLISH).equals("prefix"))
    {
      if(!val.startsWith("(")) return;
      int pos=val.indexOf(")");
      if(pos<0) return;
      String modes=val.substring(1,pos);
      String prefixes=val.substring(pos+1);
      if(prefixes.length()!=modes.length()) return;

      _nickModes=new char[modes.length()];
      for(int i=0;i<modes.length();i++) _nickModes[i]=modes.charAt(i);
      _nickPrefixes=new char[modes.length()];
      for(int i=0;i<prefixes.length();i++) _nickPrefixes[i]=prefixes.charAt(i);
    }
    else if(key.toLowerCase(java.util.Locale.ENGLISH).equals("chantypes"))
    {
      _channelPrefixes=new char[val.length()];
      for(int i=0;i<_channelPrefixes.length;i++) _channelPrefixes[i]=val.charAt(i);
    }
    else if(key.toLowerCase(java.util.Locale.ENGLISH).equals("chanmodes"))
    {
      int pos=val.indexOf(',');
      if(pos<0) return;
      String a=val.substring(0,pos);
      val=val.substring(pos+1);
      pos=val.indexOf(',');
      if(pos<0) return;
      String b=val.substring(0,pos);
      val=val.substring(pos+1);
      pos=val.indexOf(',');
      if(pos<0) return;
      String c=val.substring(0,pos);
      String d=val.substring(pos+1);
      _globalModes=new char[4][];
      _globalModes[0]=new char[a.length()];for(int i=0;i<a.length();i++) _globalModes[0][i]=a.charAt(i);
      _globalModes[1]=new char[b.length()];for(int i=0;i<b.length();i++) _globalModes[1][i]=b.charAt(i);
      _globalModes[2]=new char[c.length()];for(int i=0;i<c.length();i++) _globalModes[2][i]=c.charAt(i);
      _globalModes[3]=new char[d.length()];for(int i=0;i<d.length();i++) _globalModes[3][i]=d.charAt(i);
    }
  }

  private void learnServerVariables(String var[])
  {
    for(int i=1;i<var.length;i++)
    {
      String v=var[i];
      int pos=v.indexOf('=');
      String key;
      String val;
      if(pos<0)
      {
        key=v;
        val="";
      }
      else
      {
        key=v.substring(0,pos);
        val=v.substring(pos+1);
      }
      decodeVariable(key,val);
    }
    _mode=new ModeHandler(_globalModes,_nickModes);
  }

  public void replyReceived(String prefix,String id,String params[])
  {
    Object[] b=_replylisteners.sendEvent("replyReceived",new Object[] {prefix,id,params,this});
    for(int i=0;i<b.length;i++) if(((Boolean)b[i]).booleanValue()) return;
    
    if(id.equals("232")) //RPL_RULES (Unreal Only)
    {
      //String cname=params[1];
          String toSend="";
          for(int i=1;i<params.length;i++) toSend+=" "+params[i];
          toSend=toSend.substring(1);
          sendStatusMessage(toSend);
          /* if(_defaultSource!=null) _defaultSource.report("\3 *** See the Information window for a listing of the rules."); */
    }
    else if(id.equals("324")) //mode : RPL_CHANNELMODEIS
    {
      Channel c=getChannel(params[1],false);
      if(c!=null)
      {
        String mode="";
        for(int i=2;i<params.length;i++) mode+=" "+params[i];
        mode=mode.substring(1);
        c.applyMode(mode,"");
      }
    }
    else if(id.equals("332")) //topic : RPL_TOPIC
    {
      Channel c=getChannel(params[1],false);
      if(c!=null) c.setTopic(params[2],"");
    }
    else if(id.equals("353")) //names : RPL_NAMREPLY
    {
      int first=1;
      if(params[1].length()==1) first++;
      Channel c=getChannel(params[first],false);
      if(c!=null)
      {
        String nick="";
        Vector<String> nicks=new Vector<String>();
        for(int i=0;i<params[first+1].length();i++)
        {
          char u=params[first+1].charAt(i);
          if(u==' ')
          {
            if(nick.length()>0) nicks.insertElementAt(nick,nicks.size());
            nick="";
          }
          else
          {
            nick+=u;
          }
        }
        if(nick.length()>0) nicks.insertElementAt(nick,nicks.size());
        setNicks(c,nicks);
      }
    }
    else if(id.equals("001")) //RPL_WELCOME
    {
      String nick=params[0];
      if(!(nick.equals(_nick)))
      {
        _nick=nick;
        if(_status!=null) _status.nickChanged(nick);
      }
			_connected=true;
      _listeners.sendEvent("serverConnected",this);
    }
    else if(id.equals("005")) //RPL_ISUPPORT
    {
      learnServerVariables(params);
    }
    else if(id.equals("321")) ///list begin : RPL_LISTSTART
    {
      getChanList(_host[_tryServerIndex]).begin();
    }
    else if(id.equals("322")) ///list : RPL_LIST
    {
      String name=params[1];
      int count=new Integer(params[2]).intValue();
			if((count<32767) && (isChannel(name)))
			{
        String topic=params[3];
        getChanList(_host[_tryServerIndex]).addChannel(new ChannelInfo(name,topic,count));
			}
    }
    else if(id.equals("323")) ///list end : RPL_LISTEND
    {
      getChanList(_host[_tryServerIndex]).end();
    }
	else if(id.equals("433")) //nick used : ERR_NICKNAMEINUSE
	{
	  if(!_connected)
	  {
	    nickUsed();
	    register();
	  }
	  else if(_defaultSource!=null) _defaultSource.report("3      *** That nickname is already in use, please try another one.");
	}
    else if(id.equals("400")) //ERR_UNKNOWNERROR
    {
    	  if(_defaultSource!=null) _defaultSource.report("2      *** The message could not be sent.");
    }
    else if(id.equals("401")) //ERR_NOSUCHNICK
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** That room or person does not exist.");
    }
    else if(id.equals("402")) //ERR_NOSUCHSERVER
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The server you specified does not exist.");
    }
    else if(id.equals("403")) //ERR_NOSUCHCHANNEL
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The room you are trying to set does not exist");
    }
    else if(id.equals("404")) //ERR_CANNOTSENDTOCHAN
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** The message could not be sent. The message was either blocked or you may not be allowed to communicate in this room.");
    }
    else if(id.equals("405")) //ERR_TOOMANYCHANNELS
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Unable to join because you have too many rooms open.");
    }
    else if(id.equals("412")) //ERR_NOTEXTTOSEND
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** You must first type a message before sending it.");
    }
    else if(id.equals("421")) //ERR_UNKNOWNCOMMAND
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** The command you typed is not supported.");
    }
    else if(id.equals("432")) //ERR_ERRONEUSNICKNAME
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** That nickname is not allowed. Please choose another nickname.");
    }
    else if(id.equals("433")) //ERR_NICKNAMEINUSE
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** That nickname is already in use, please try another one.");
    }
    else if(id.equals("438")) //ERR_NCHANGETOOFAST
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** You are trying to change your nickname too quickly, try again later.");
    }
    else if(id.equals("440")) //ERR_SERVICESDOWN
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** Services are currently down. Please try again later.");
    }
    //  We're performing an action on a channel we're not into
    else if(id.equals("442")) //ERR_NOTONCHANNEL
    {
      Channel chan=getChannel(params[1],false);
      if(chan!=null)
      {
        _listeners.sendEvent("sourceRemoved",chan,this);
        deleteChannel(chan.getName());
      }
      else if(_defaultSource!=null) _defaultSource.report("\2\33      *** You must first join the room before doing that.");
    }
    else if(id.equals("443")) //ERR_USERONCHANNEL
    {
      String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\33      *** Your invitation to "+cname+" could not be sent because that user is already in the room.");
    }
    else if(id.equals("460")) //ERR_NOTFORHALFOPS
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("\2\34      *** You don't have permission to do that in this chat room.");
    }
    else if(id.equals("461")) //ERR_NEEDMOREPARAMS
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** You must specify more parameters for this command.");
    }
    else if(id.equals("462")) //ERR_ALREADYREGISTRED
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("4      *** You have already registered, you may not reregister.");
    }
    else if(id.equals("463")) //ERR_NOPERMFORHOST
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("4      *** You are not among the privileged.");
    }
    else if(id.equals("464")) //ERR_PASSWDMISMATCH
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("4      *** Your authentication password has failed.");
    }
    else if(id.equals("465")) //ERR_YOUREBANNEDCREEP
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("4      *** You are currently banned completely from the FrostWire Chat service due to violations of the Code of Conduct. This ban is permanent.");
    }
    else if(id.equals("447")) //ERR_NONICKCHANGE
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Nickname changes are not permitted at this time, try again later.");
    }
    else if(id.equals("467")) //ERR_KEYSET
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** The room key has already been set.");
    }
    else if(id.equals("468")) //ERR_ONLYSERVERSCANCHANGE
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** You must be a Sysop to change that mode.");
    }
    else if(id.equals("469")) //ERR_LINKSET
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** The link is already set for this room.");
    }
    else if(id.equals("470")) //ERR_LINKCHANNEL
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** The room you are trying to join is full. Trying next available room...");
    }
    else if(id.equals("471")) //ERR_CHANNELISFULL
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** The room you are trying to join is full. You cannot join it.");
    }
    else if(id.equals("472")) //ERR_UNKNOWNMODE
    {
      //String cname=params[1];
        	if(_defaultSource!=null) _defaultSource.report("3      *** The mode you are trying to set does not exist.");
    }
    else if(id.equals("473")) //ERR_INVITEONLYCHAN
    {
      //String cname=params[1];
        if(_defaultSource!=null) _defaultSource.report("3      *** Only those who have been invited may enter that room.");
    }
    else if(id.equals("474")) //ERR_BANNEDFROMCHAN
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("4      *** You are currently banned by a host in that chat room due to violations of the Code of Conduct. You may be able to rejoin at a later time.");
    }
    else if(id.equals("475")) //ERR_BADCHANNELKEY
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The room you are trying to join is protected with a password. You cannot join it.");
    }
    else if(id.equals("476")) //ERR_BADCHANMASK
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The room mode you are trying to set does not exist");
    }
    else if(id.equals("477")) //ERR_NEEDREGGEDNICK
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Only authenticated users can join that room.");
    }
    else if(id.equals("478")) //ERR_BANLISTFULL
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The ban list is full. You must remove a current ban before setting a new one.");
    }
    else if(id.equals("479")) //ERR_LINKFAIL
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The room you are trying to join has an invalid link set and can't be joined.");
    }
    else if(id.equals("480")) //ERR_CANNOTKNOCK (Unreal Only)
    {
      String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** "+cname+" does not allow knocks.");
    }
    else if(id.equals("481")) //ERR_NOPRIVILEGES
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Permission Denied. You are not a System Operator.");
    }
    else if(id.equals("482")) //ERR_CHANOPRIVSNEEDED
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You don't have permission to do that in this chat room.");
    }
    else if(id.equals("483")) //ERR_CANTKILLSERVER
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Permission Denied. You are not a System Operator.");
    }
    else if(id.equals("484")) //ERR_ATTACKDENY
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The user is protected. You don't have permission to do that on this network.");
    }
    else if(id.equals("485")) //ERR_KILLDENY
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You don't have permission to do that on this network.");
    }
    else if(id.equals("486")) //ERR_NONONREG
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be authenticated to message that user.");
    }
    else if(id.equals("487")) //ERR_NOTFORUSERS
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be a System Operator to execute that command.");
    }
    else if(id.equals("488")) //ERR_HTMDISABLED
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Waiting: This option is temporarily disabled.");
    }
    else if(id.equals("489")) //ERR_SECUREONLYCHAN (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be using a secure connection to join that room.");
    }
    else if(id.equals("490")) //ERR_NOSWEAR (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** This user has blocked private messages that are not suitable for all audiences.");
    }
    else if(id.equals("491")) //ERR_NOOPERHOST
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Permission Denied. You are not authorized to access network controls.");
    }
    else if(id.equals("492")) //ERR_NOSERVICEHOST CTCP? (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Your request could not be sent because that user does not allow it.");
    }
    else if(id.equals("499")) //ERR_CHANOWNPRIVNEEDED
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be an Owner in the chat room to complete the action you just attempted.");
    }
    else if(id.equals("500")) //ERR_TOOMANYJOINS
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Too many join requests. Please wait a while and try again.");
    }
    else if(id.equals("501")) //ERR_UMODEUNKNOWNFLAG
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** Unknown user mode.");
    }
    else if(id.equals("502")) //ERR_USERSDONTMATCH
    {
      //String cname=params[1];
    	if(_defaultSource!=null) _defaultSource.report("3      *** Permission Denied. You are not a System Operator.");
    }
    else if(id.equals("511")) //ERR_SILELISTFULL
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** You have reached the maximum amount of entries allowed in your silence list.");
    }
    else if(id.equals("512")) //ERR_TOOMANYWATCH
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** You have reached the maximum amount of entries allowed in your watch list.");
    }
    else if(id.equals("513")) //ERR_NEEDPONG
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** To connect, type /QOUTE PONG %lX");
    }
    else if(id.equals("514")) //ERR_TOOMANYDCC
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("2      *** You have reached the maximum amount of entries allowed in your DCC list.");
    }
    else if(id.equals("518")) //ERR_NOINVITE (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** This room does not allow invitations.");
    }
    else if(id.equals("519")) //ERR_ADMONLY +A (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be a System Operator to join that room.");
    }
    else if(id.equals("520")) //ERR_OPERONLY +O (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be an Operator to join that room.");
    }
    else if(id.equals("524")) //ERR_OPERSPVERIFY
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You must be a System Operator to join that room.");
    }
    else if(id.equals("972")) //ERR_CANNOTDOCOMMAND (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You don't have permission to do that in this chat room.");
    }
    else if(id.equals("974")) //ERR_CANNOTCHANGECHANMODE (Unreal Only)
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** You don't have permission to do that in this chat room.");
    }
    else if(id.equals("951")) //Authentication is unable to process
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** The server is too busy to process your authentication at this time.");
    }
    else if(id.equals("952")) //Authentication returns bad username
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Username and/or password do not match.");
    }
    else if(id.equals("953")) //Authentication returns bad password
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Username and/or password do not match.");
    }
    else if(id.equals("954")) //Authentication returns no password
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Username and/or password do not match.");
    }
    else if(id.equals("955")) //Authentication returns no post-connect password
    {
      //String cname=params[1];
    	  if(_defaultSource!=null) _defaultSource.report("3      *** Username and/or password do not match.");
    }
    else
    {
    	/*
      String toSend="";
      for(int i=1;i<params.length;i++) toSend+=" "+params[i];
      toSend=toSend.substring(1);
      sendStatusMessage(toSend);
      */
    }

  }

  private String extractNick(String full)
  {
    int pos=full.indexOf('!');
    if(pos==-1) return full;
    return full.substring(0,pos);
  }

  private boolean isChannel(String name)
  {
    if(name.length()==0) return false;
    for(int i=0;i<_channelPrefixes.length;i++) if(name.charAt(0)==_channelPrefixes[i]) return true;
    return false;
  }

  private void globalNickRemove(String nick,String reason)
  {
    Enumeration<Source> e=_channels.elements();
    while(e.hasMoreElements())
    {
      Channel c=(Channel)e.nextElement();
      if(c.hasNick(nick)) c.quitNick(nick,reason);
    }
  }

  private void globalNickChange(String oldNick,String newNick)
  {
    //When user issues /nick command, the new nick is saved on the settings
    //if the old nickname was the one on settings.
    String myNick = com.limegroup.gnutella.settings.ChatSettings.CHAT_IRC_NICK.getValue();
    if (myNick != null && oldNick.equals(myNick) && !oldNick.equals(newNick)) {
      com.limegroup.gnutella.settings.ChatSettings.CHAT_IRC_NICK.setValue(newNick);
    }
    
    Enumeration<Source> e;
    e=_channels.elements();
    while(e.hasMoreElements())
    {
      Channel c=(Channel)e.nextElement();
      if(c.hasNick(oldNick)) c.changeNick(oldNick,newNick);
    }

    Query q=(Query)_queries.get(oldNick.toLowerCase(java.util.Locale.ENGLISH));
    if(q!=null)
    {
      _queries.remove(oldNick.toLowerCase(java.util.Locale.ENGLISH));
      q.changeNick(newNick);
      Query existing=(Query)_queries.get(newNick.toLowerCase(java.util.Locale.ENGLISH));
      if(existing!=null) existing.leave();
      _queries.put(newNick.toLowerCase(java.util.Locale.ENGLISH),q);
    }
  }

  /**
   * Return true if this server is ignoring the given nick, false otherwise.
   * @param nick nick to test.
   * @return the ignore status of the given nick.
   */
	public synchronized boolean ignore(String nick)
	{
	  return _ignoreList.get(nick)!=null;
	}

  /**
   * Ignore the given nick.
   * @param nick nick to ignore.
   */
	public synchronized void addIgnore(String nick)
	{
	  _ignoreList.put(nick,nick);
	}

  /**
   * Remove the given list from the ignore list.
   * @param nick nick to remove from ignore list.
   */
	public synchronized void removeIgnore(String nick)
	{
	  _ignoreList.remove(nick);
	}

  public void messageReceived(String prefix,String command,String params[])
  {
    Object[] b=_messagelisteners.sendEvent("messageReceived",new Object[] {prefix,command,params,this});
    for(int i=0;i<b.length;i++) if(((Boolean)b[i]).booleanValue()) return;


    String toSend="";
    for(int i=0;i<params.length;i++) toSend+=" "+params[i];


    command=command.toLowerCase(java.util.Locale.ENGLISH);

    String nick=extractNick(prefix);

    if(command.equals("notice"))
    {
      if(!ignore(nick))
      {
        if(!_filter.performFromNotice(nick,params[1]))
          if(_defaultSource!=null) _defaultSource.noticeReceived(nick,params[1]);
      }
    }
    else if(command.equals("privmsg"))
    {
		  if(!ignore(nick))
			{
        if(isChannel(params[0]))
        {
          if(!_filter.performFromChannelMessage(params[0],nick,params[1]))
          {
            Channel c=getChannel(params[0],false);
            if(c!=null) c.messageReceived(nick,params[1]);
          }
        }
        else
        {
          if(!_filter.performFromNickMessage(nick,params[1]))
          {
            Query q=getQuery(nick,false);
            if(q!=null) q.messageReceived(nick,params[1]);
          }
        }
			}
    }
    else if(command.equals("join"))
    {
      if(!nick.equals(getNick()))
      {
        Channel c=getChannel(params[0],false);
        if(c!=null) c.joinNick(nick,"");
      }
      else
      {
        Channel c=getChannel(params[0],true);
        if(c!=null)
        {
          c.resetNicks();
          execute("mode "+params[0]);
        }
      }
    }
    else if(command.equals("part"))
    {
      Channel c=getChannel(params[0],false);
      if(c!=null)
      {
        if(params.length>1)
        {
          c.partNick(nick,params[1]);
        }
        else
        {
          c.partNick(nick,"");
        }
        if(nick.equals(getNick()))
        {
          _listeners.sendEvent("sourceRemoved",c,this);
          deleteChannel(c.getName());
        }
      }
    }
    else if(command.equals("kick"))
    {
      Channel c=getChannel(params[0],false);
      if(c!=null)
      {
        String target=params[1];
        String reason="";
        if(params.length>2) reason=params[2];
        c.kickNick(target,nick,reason);
        if(target.equals(getNick()))
        {
          if(_ircConfiguration.getB("autorejoin"))
          {
            c.report("Attempting to rejoin room "+c.getName()+"...");
            execute("join "+params[0]);
          }
          else
          {
            // _listeners.sendEvent("sourceRemoved",c,this);
            // deleteChannel(c.getName());
          }
        }
      }
    }
    else if(command.equals("topic"))
    {
      Channel c=getChannel(params[0],false);
      if(c!=null) c.setTopic(params[1],nick);
    }
    else if(command.equals("mode"))
    {
      String full="";
      for(int i=1;i<params.length;i++) full+=params[i]+" ";
      if(isChannel(params[0]))
      {
        Channel c=getChannel(params[0],false);
        if(c!=null)
        {
          MultiModeHandler h=new MultiModeHandler(full,_globalModes,_nickModes);
          while(!h.terminated())
          {
            h.next();
            if(h.isPrefix() || h.isModeA())
            {
              c.applyUserMode(h.getParameter(),h.getMode(),nick);
            }
            else
            {
              if(h.hasParameter())
                c.applyMode(h.getMode()+" "+h.getParameter(),nick);
              else
                c.applyMode(h.getMode(),nick);
            }
          }
        }
      }
      else if(nick.equals(getNick()))
      {
        _mode.apply(full);
        if(_status!=null) _status.modeChanged(getMode());
      }
    }
    else if(command.equals("nick"))
    {
      if(nick.equals(getNick()))
      {
        _nick=params[0];
        if(_status!=null) _status.nickChanged(getNick());
      }
      globalNickChange(nick,params[0]);
    }
    else if(command.equals("quit"))
    {
      if(params.length>0)
        globalNickRemove(nick,params[0]);
      else
        globalNickRemove(nick,"");
    }
    else if(command.equals("ping"))
    {
      execute("pong :"+params[0]);
   //   sendStatusMessage("\3"+"3"+"PING? PONG!");
    }
    else if(command.equals("invite"))
    {
      String invited=params[0];
      String channel=params[1];
      if(invited.equals(getNick()))
      {
        //if(_status!=null) _status.invited(channel,nick);
        if(_defaultSource!=null) _defaultSource.report("3      *** "+nick+" has invited you to join "+channel+". Type /join "+channel+" to accept this invitation.");
        //_ircConfiguration.getAudioConfiguration().play("sounds/ChatInvt.au");
      }
    }
    else if(command.equals("error"))
    {
      //sendStatusMessage("4  Server Message: "+params[0]);
    }
    else
    {
      //   System.out.println("("+command+") "+prefix+" -> "+toSend);
    }

  }

  public String getNick()
  {
    return _nick;
  }

  public String getUserName()
  {
    return _userName;
  }

  /**
   * Get the current status mode.
   * @return status mode.
   */
  public String getMode()
  {
    return _mode.getMode();
  }

  public void say(String destination,String str)
  {
    execute("PRIVMSG "+destination+" :"+str);
  }

  public void execute(String str)
  {
    int pos=str.indexOf(' ');
    if(pos>=0)
    {
      String cmd=str.substring(0,pos).toLowerCase(java.util.Locale.ENGLISH);
      if(cmd.equals("join"))
      {
        String rem=str.substring(pos+1);
        pos=rem.indexOf(' ');
        if(pos>=0) rem=rem.substring(0,pos);
        if(!_ircConfiguration.mayJoin(rem)) return;
      }
      else if(cmd.equals("part"))
      {
        String rem=str.substring(pos+1);
        pos=rem.indexOf(' ');
        if(pos>=0) rem=rem.substring(0,pos);
        if(!_ircConfiguration.mayLeave(rem)) return;
      }
    }

    pos=str.indexOf(' ');
    if(pos>0)
    {
      String cmd=str.substring(0,pos).toUpperCase(java.util.Locale.ENGLISH);
      String param=str.substring(pos+1);
      str=cmd+" "+param;
    }
    else
    {
      str=str.toUpperCase(java.util.Locale.ENGLISH);
    }
    sendString(str);
  }

  private void sendString(String str)
  {
    try
    {
      _protocol.sendString(str);
    }
    catch(Exception e)
    {
      //sendStatusMessage("4  Server Message: "+e.getMessage());
    }
  }

}

