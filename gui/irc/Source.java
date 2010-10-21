package irc;

/**
 * The root source.
 */
public abstract class Source extends IRCObject
{
  /**
   * The server.
   */
  protected Server _server;
  private ListenerGroup _listeners;
  /**
   * The interpretor.
   */
  protected Interpretor _in;

  /**
   * Create a new Source.
   * @param config the global configuration.
   * @param s the bound server.
   */
  public Source(IRCConfiguration config,Server s)
  {
    super(config);
    _listeners=new ListenerGroup();
    _in=new NullInterpretor(config);
    _server=s;
  }

  public void release()
  {
    _in=new NullInterpretor(_ircConfiguration);
    super.release();
  }

  /**
   * Get this source name.
   * @return source name.
   */
  public abstract String getName();

  /**
   * Get this source type.
   * @return source type.
   */
  public abstract String getType();

  /**
   * Test wether this source can accept messages from user.
   * @return true if this source accepts user input, false otherwise.
   */
  public abstract boolean talkable();

  /**
   * Request to leave (close) this source.
   */
  public abstract void leave();

  /**
   * Set this source's interpretor.
   * @param in new interpretor.
   */
  public void setInterpretor(Interpretor in)
  {
    _in=in;
  }

  /**
   * Send a string to this source.
   * @param str user input.
   */
  public void sendString(String str)
  {	
    _in.sendString(this,str);
  }

  /**
   * Send a string from user input to this source. The string is filtered against
   * unauthorized commands.
   * @param str user input.
   */
  public void sendUserString(String str)
  {
    if(!str.startsWith("/")) sendString(str);
    else
    {
      String cmd=str.substring(1).trim();
      int pos=cmd.indexOf(' ');
      if(pos>=0) cmd=cmd.substring(0,pos);
      if(_ircConfiguration.mayCommand(cmd)) sendString(str);
    }
  }
  
  /**
   * Get this source's interpretor.
   * @return this source's interpretor.
   */
  public Interpretor getInterpretor()
  {
    return _in;
  }

  /**
   * Request this source to clear all the history it could have.
   */
  public void clear()
  {
    _listeners.sendEvent("clear",this);
  }

  /**
   * Notify this source a new message has arrived.
   * @param source the source of the message. It can be a user nickname or a channel name.
   * @param msg the message.
   */
  public void messageReceived(String source,String msg)
  {
    if(msg.startsWith("\1"))
    {
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
        action(source,param);
      else
        getServer().sendStatusMessage("\2\3"+"4"+"["+source+" "+cmd.toUpperCase(java.util.Locale.ENGLISH)+"]");
    }
    else
    {
      _listeners.sendEvent("messageReceived",source,msg,this);
    }
  }

  /**
   * Notify this source a new notice message has arrived.
   * @param source the source of the message. It can be a user nickname or a channel name.
   * @param msg the message.
   */
  public void noticeReceived(String source,String msg)
  {
    _listeners.sendEvent("noticeReceived",source,msg,this);
  }

  /**
   * Notify this source a new action message has arrived.
   * @param nick the user who sent the action.
   * @param msg the message.
   */
  public void action(String nick,String msg)
  {
    _listeners.sendEvent("action",nick,msg,this);
  }

  /**
   * Notify this source a new report has arrived.
   * @param msg the report message.
   */
  public void report(String msg)
  {
    _listeners.sendEvent("reportReceived",msg,this);
  }

  /**
   * Add a new SourceListener.
   * @param lis listener to add.
   */
  public void addSourceListener(SourceListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a SourceListener.
   * @param lis the listener to remove.
   */
  public void removeSourceListener(SourceListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Get the source server.
   * @return the source server.
   */
  public Server getServer()
  {
    return _server;
  }
  
  /**
   * Get the connection status.
   * @return current connection status to the IRC network.
   */
  public boolean isConnected()
  {
    return getServer().isConnected();
  }
  
  /**
   * Test whether this source may be used as a default source for system event
   * output.
   * @return true if this source may be used as a default source, false otherwise.
   */
  public boolean mayDefault()
  {
    return true;
  }

}

