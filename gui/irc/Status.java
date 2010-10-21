package irc;

/**
 * The status.
 */
public class Status extends IRCSource implements ReplyServerListener
{
  private ListenerGroup _listeners;

  /**
   * Create a new Status.
   * @param config global irc configuration.
   * @param s server.
   */
  public Status(IRCConfiguration config,IRCServer s)
  {
    super(config,s);
    s.addReplyServerListener(this);
    _listeners=new ListenerGroup();
    setInterpretor(new StatusInterpretor(config));
  }

  public void release()
  {
    ((IRCServer)_server).removeReplyServerListener(this);
    super.release();
  }

  public String getType()
  {
    return "Status";
  }

  public String getName()
  {
    return getServerName();
  }

  /**
   * Get the server name.
   * @return the server name.
   */
  public String getServerName()
  {
    return getIRCServer().getServerName();
  }

  public boolean talkable()
  {
    return false;
  }

  public void leave()
  {
    if(!_ircConfiguration.getB("multiserver")) return;

    //sendString("/quit");
    getIRCServer().leaveStatus(getName());
  }

  /**
   * Get this status nick.
   * @return status nick.
   */
  public String getNick()
  {
    return _server.getNick();
  }

  /**
   * Get this status mode.
   * @return status mode.
   */
  public String getMode()
  {
    return getIRCServer().getMode();
  }

  /**
   * Add listener.
   * @param lis listener to add.
   */
  public void addStatusListener(StatusListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove listener.
   * @param lis listener to remove.
   */
  public void removeStatusListener(StatusListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Notify this status that the nick has changed.
   * @param nick new nick.
   */
  public void nickChanged(String nick)
  {
    _listeners.sendEvent("nickChanged",nick,this);
  }

  /**
   * Notify this status that the mode has changed.
   * @param mode new mode.
   */
  public void modeChanged(String mode)
  {
    _listeners.sendEvent("modeChanged",mode,this);
  }

  /**
   * We've been invited on a channel.
   * @param channel channel we're invited to.
   * @param who nickname who invited us.
   */
  public void invited(String channel,String who)
  {
    _listeners.sendEvent("invited",channel,who,this);
  }

  public Boolean replyReceived(String prefix,String id,String params[],IRCServer server)
	{
    if(id.equals("322")) return Boolean.FALSE; //chanlist
    if(_ircConfiguration.getB("useinfo"))
    {
      int i=new Integer(id).intValue();
      if((i>=300) && (i!=372)) return Boolean.FALSE;
    }
/*	  if(id.equals("401")) //no such nick/channel
	  {
      Source src=getIRCServer().getDefaultSource();
      String toSend="";
      for(int i=1;i<params.length;i++) toSend+=" "+params[i];
      toSend=toSend.substring(1);
      if(src!=null) src.report(toSend);
	  }
	  else if(id.equals("317")) //idle time
	  {
	    if(params.length>3)
	    {
	      String time=params[2];
	      long tme=Long.parseLong(time);
        long seconds=(tme)%60;
        long mins=(tme/(60))%60;
        long hours=(tme/(60*60))%24;
        long days=(tme/(60*60*24))%7;
        long weeks=(tme/(60*60*24*7));

	      String res="";

	      if(tme>(60*60*24*7)) res+=weeks+" weeks ";
        if(tme>(60*60*24)) res+=days+" days ";
        if(tme>(60*60)) res+=hours+" hours ";
        if(tme>(60)) res+=mins+" minutes ";
        res+=seconds+" seconds";
        res=res.trim();

        String signon=new java.util.Date(1000*Long.parseLong(params[3])).toString();

        report(_ircConfiguration.getText(IRCTextProvider.REPLY_IDLE,params[1],res));
        report(_ircConfiguration.getText(IRCTextProvider.REPLY_SIGNON,params[1],signon));
      }

	  }*/
	  else
	  {
      /*//Source src=getIRCServer().getDefaultSource();
      String toSend="";
      for(int i=1;i<params.length;i++) toSend+=" "+params[i];
      toSend=toSend.substring(1);
      if(src!=null) src.report(toSend);*/
    }
    return Boolean.FALSE;
	}
}

