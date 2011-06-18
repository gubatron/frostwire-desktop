package irc.gui.pixx;

import irc.*;

/**
 * The AWTStatus.
 */
public class AWTStatus extends BaseAWTSource implements StatusListener,ReplyServerListener
{

  /**
     * 
     */
    private static final long serialVersionUID = 4322157273662520645L;

/**
   * Create a new AWTStatus.
   * @param config global irc configuration.
   * @param s source status.
   */
  public AWTStatus(PixxConfiguration config,Status s)
  {
    super(config,s);
    s.addStatusListener(this);
    s.getIRCServer().addReplyServerListener(this);
    title();
    print("  Press the \"Connect\" button to join the community...",3);
  }

  public void release()
  {
    ((Status)_source).removeStatusListener(this);
    ((Status)_source).getIRCServer().removeReplyServerListener(this);
    super.release();
  }

  private String getSourceName()
  {
    if(!_pixxConfiguration.getIRCConfiguration().getB("multiserver"))
    {
      if(_pixxConfiguration.getIRCConfiguration().getB("useinfo"))
        return "Information";
      return "Status";
    }

    return ((Status)_source).getServerName();
  }

  private void title()
  {
    setTitle(" Welcome to The FrostWire Chat Network "+((Status)_source).getNick()+"!");
//    setTitle(getSourceName()+": "+ ((Status)_source).getNick()+" ["+((Status)_source).getMode()+"]");
  }

  public String getShortTitle()
  {
    return getSourceName();
  }

  /**
   * A notice has been received.
   * @param from source nickname.
   * @param msg notice string.
   */
  public void noticeReceived(String from,String msg)
  {
    print("-"+from+"- "+msg,6);
  }

  public void nickChanged(String nick,Status status)
  {
	  status.getIRCServer().getDefaultSource().report("03      *** You are now known as "+nick+".");
	  //print("03      *** You are now known as "+nick+".");
	  title();
  }

  public void modeChanged(String mode,Status status)
  {
    //if(mode.length()>0) print("      *** Your mode changed to: "+mode,3);
    //title();
  }

  public void invited(String channel,String who,Status status)
  {
    if(status.getIRCServer().getDefaultSource()!=null)
    {
        //status.getIRCServer().getDefaultSource().report("      *** "+who+" invites you to join "+channel);
        //_source.sendString("/play sounds/ChatInvt.au");
    }
  }

  public Boolean replyReceived(String prefix,String id,String params[],IRCServer server)
  {
    /*if(id.equals("301")) //away
    {
    	String toSend="14      *** "+params[1]+" is currently away.";
      //String toSend=getText(PixxTextProvider.SOURCE_AWAY,params[1])+" :";
      for(int i=2;i<params.length;i++) toSend+=" Away message: "+params[i];
      _source.report(toSend);
    }*/
    return Boolean.FALSE;
  }

}

