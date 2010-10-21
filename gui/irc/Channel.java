/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2004 Philippe Detournay   */
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

package irc;

import java.util.*;

/**
 * Nickname.
 */
class Nick
{
  /**
   * Create a new Nick
   * @param n nickname.
   * @param m mode.
   * @param modes all modes.
   * @param prefix all prefixes.
   */
  public Nick(String n,String m,char[][] modes,char[] prefix)
  {
    Name=n;
    Mode=new ModeHandler(m,modes,prefix);
		Whois="";
  }

  /**
   * Nickname.
   */
  public String Name;
  /**
   * Whois information.
   */
	public String Whois;
  /**
   * Nickname mode.
   */
  public ModeHandler Mode;
}

/**
 * A channel source.
 */
public class Channel extends IRCSource implements ReplyServerListener
{
  private String _name;
  private String _topic;
  private ModeHandler _mode;
  private ListenerGroup _listeners;
  private Hashtable _nicks;

  /**
   * Create a new Channel.
   * @param config the global configuration.
   * @param name channel name.
   * @param s the source server.
   */
  public Channel(IRCConfiguration config,String name,IRCServer s)
  {
    super(config,s);
    _name=name;
    _topic="";
    _mode=new ModeHandler(s.getChannelModes(),s.getNickModes());
    _listeners=new ListenerGroup();
    _nicks=new Hashtable();
		s.addReplyServerListener(this);
		if(_ircConfiguration.getASLMaster()) getIRCServer().execute("WHO "+_name);
		setInterpretor(new ChannelInterpretor(config));
  }

  public void release()
  {
    ((IRCServer)_server).removeReplyServerListener(this);
    super.release();
  }

  /**
   * Add a channel listener.
   * @param lis listener to add.
   * @deprecated use addChannelListener2 instead.
   */
  public void addChannelListener(ChannelListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a channel listener.
   * @param lis listener to remove.
   * @deprecated use removeChannelListener2 instead.
   */
  public void removeChannelListener(ChannelListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Add a channel listener.
   * @param lis listener to add.
   */
  public void addChannelListener2(ChannelListener2 lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a channel listener.
   * @param lis listener to remove.
   */
  public void removeChannelListener2(ChannelListener2 lis)
  {
    _listeners.removeListener(lis);
  }
  
  public String getType()
  {
    return "Channel";
  }

  public String getName()
  {
    return _name;
  }

  public boolean talkable()
  {
    return true;
  }

  public void leave()
  {
    getIRCServer().leaveChannel(getName());
  }

  /**
   * Test wether this channel has the given nickname.
   * @param nick nickname to test.
   * @return true if nick is present on this channel, false otherwise.
   */
  public boolean hasNick(String nick)
  {
    return _nicks.get(nick.toLowerCase(java.util.Locale.ENGLISH))!=null;
  }

  /**
   * Notify this channel the given nick has joined, with the given options.
   * @param nick new nick.
   * @param mode nick mode.
   */
  public void joinNick(String nick,String mode)
  {
    _nicks.put(nick.toLowerCase(java.util.Locale.ENGLISH),new Nick(nick,mode,getIRCServer().getChannelModes(),getIRCServer().getNickModes()));
		if(_ircConfiguration.getASLMaster()) getIRCServer().execute("WHO "+nick);
    _listeners.sendEvent("nickJoin",nick,mode,this);
  }
  
  /**
   * Notify this channel it should reset its nick list. 
   */
  public void resetNicks()
  {
    _nicks.clear();
    _listeners.sendEvent("nickReset",this);
  }

  /**
   * Notify this channel it should change its hole nick list.
   * @param nicks new nicks.
   * @param modes new modes. There is a one to one mapping between nicks and modes.
   */
  public void setNicks(String[] nicks,String[] modes)
  {
    for(int i=0;i<nicks.length;i++) _nicks.put(nicks[i].toLowerCase(java.util.Locale.ENGLISH),new Nick(nicks[i],modes[i],getIRCServer().getChannelModes(),getIRCServer().getNickModes()));
    _listeners.sendEvent("nickSet",nicks,modes,this);
  }

  /**
   * Notify this channel the given nick has left the channel.
   * @param nick the nick.
   * @param reason the reason.
   */
  public void partNick(String nick,String reason)
  {
    _nicks.remove(nick.toLowerCase(java.util.Locale.ENGLISH));
    _listeners.sendEvent("nickPart",nick,reason,this);
  }

  /**
   * Notify this channel the given nick has been kicked.
   * @param nick the kicked nick.
   * @param by the nick who kicked nick.
   * @param reason the kick reason.
   */
  public void kickNick(String nick,String by,String reason)
  {
    _nicks.remove(nick.toLowerCase(java.util.Locale.ENGLISH));
    _listeners.sendEvent("nickKick",new Object[] {nick,by,reason,this});
  }

  /**
   * Notify this channel the given nick has quit.
   * @param nick the nick who quit.
   * @param reason reason.
   */
  public void quitNick(String nick,String reason)
  {
    _nicks.remove(nick.toLowerCase(java.util.Locale.ENGLISH));
    _listeners.sendEvent("nickQuit",nick,reason,this);
  }

  /**
   * Get all the nicks in this channel.
   * @return array of all nick names.
   */
  public String[] getNicks()
  {
    String[] ans=new String[_nicks.size()];
    Enumeration e=_nicks.elements();
    int i=0;
    while(e.hasMoreElements())
      ans[i++]=((Nick)e.nextElement()).Name;
    return ans;
  }

  /**
   * Get the nick mode associated with the given nick.
   * @param nick nickname to get mode.
   * @return nick mode or null if nick not found.
   */
  public String getNickMode(String nick)
  {
    Nick n=(Nick)_nicks.get(nick.toLowerCase(java.util.Locale.ENGLISH));
    if(n==null) return null;
    return n.Mode.getMode();
  }

  /**
   * Notify this channel its topic has changed.
   * @param topic new topic.
   * @param by nickname who changed topic.
   */
  public void setTopic(String topic,String by)
  {
    _topic=topic;
    _listeners.sendEvent("topicChanged",topic,by,this);
  }

  /**
   * Notify this channel a nick mode has changed.
   * @param nick the nick.
   * @param mode the applied mode.
   * @param from the nick who changed mode.
   */
  public void applyUserMode(String nick,String mode,String from)
  {
    Nick n=(Nick)_nicks.get(nick.toLowerCase(java.util.Locale.ENGLISH));
    if(n!=null) n.Mode.apply(mode);
    _listeners.sendEvent("nickModeApply",new Object[] {nick,mode,from,this});
  }

  /**
   * Notify this channel its mode has changed.
   * @param mode applied mode.
   * @param from user who changed mode.
   */
  public void applyMode(String mode,String from)
  {
    _mode.apply(mode);
    _listeners.sendEvent("modeApply",mode,from,this);
  }

  /**
   * Get this channel mode.
   * @return channel mode.
   */
  public String getMode()
  {
    return _mode.getMode();
  }

  /**
   * Get this channel topic.
   * @return channel topic.
   */
  public String getTopic()
  {
    return _topic;
  }

  /**
   * Notify this channel a nick has been renamed.
   * @param oldNick old nickname.
   * @param newNick new nickname.
   */
  public void changeNick(String oldNick,String newNick)
  {
    Nick n=(Nick)_nicks.get(oldNick.toLowerCase(java.util.Locale.ENGLISH));
    _nicks.remove(oldNick.toLowerCase(java.util.Locale.ENGLISH));
    n.Name=newNick;
    _nicks.put(newNick.toLowerCase(java.util.Locale.ENGLISH),n);

    _listeners.sendEvent("nickChanged",oldNick,newNick,this);
  }

	private void learn(String nick,String whois)
	{
	  Nick n=(Nick)_nicks.get(nick.toLowerCase(java.util.Locale.ENGLISH));
		if(n==null) return;
		n.Whois=whois;
    _listeners.sendEvent("nickWhoisUpdated",nick,whois,this);
	}

  /**
   * Get bufferised whois data for the given nick.
   * @param nick nickname.
   * @return whois data for nick, or "" if not found.
   */
	public String whois(String nick)
	{
	  Nick n=(Nick)_nicks.get(nick.toLowerCase(java.util.Locale.ENGLISH));
	  if(n==null) return "";
		return n.Whois;
	}

  public Boolean replyReceived(String prefix,String id,String params[],IRCServer server)
	{
	  if(id.equals("352"))
  	{
		  String name=params[params.length-1];
			int pos=name.indexOf(" ");
			if(pos!=-1) name=name.substring(pos+1);
			String nick=params[5];
			learn(nick,name);
		}
    return Boolean.FALSE;
  }
}

