package irc.gui.pixx;

import java.util.*;
import irc.*;
import java.awt.*;
import java.awt.event.*;
import irc.tree.*;
import irc.style.*;

/**
 * NickNameComparator.
 */
class NickNameComparator implements irc.tree.Comparator
{
  private char[] _prefixes;

  /**
   * Create a new NickNameComparator
   * @param prefixes
   */
  public NickNameComparator(char[] prefixes)
  {
    _prefixes=prefixes;
  }

  private int getPrefixCompareIndex(String nick)
  {
    if(nick.length()==0) return _prefixes.length;
    for(int i=_prefixes.length-1;i>=0;i--)
      if(nick.charAt(0)==_prefixes[i]) return i;
    return _prefixes.length;
  }

  public int compare(Object i1,Object i2)
  {
    String n1=(String)i1;
    String n2=(String)i2;
    n1=n1.toLowerCase(java.util.Locale.ENGLISH).toUpperCase(java.util.Locale.ENGLISH);
    n2=n2.toLowerCase(java.util.Locale.ENGLISH).toUpperCase(java.util.Locale.ENGLISH);
    int prefixCmp=getPrefixCompareIndex(n1)-getPrefixCompareIndex(n2);
    if(prefixCmp<0) return -1;
    if(prefixCmp>0) return 1;

    return n1.compareTo(n2);
  }
}

/**
 * The AWT Channel.
 */
public class AWTChannel extends BaseAWTSource implements ChannelListener2,PixxNickListListener
{
  private ScrollablePixxNickList _nicks;
	private Label _label;
  private SortedList _sortedList;
  private Hashtable _modeMapping;
  private NickMenuHandler _menu;

  /**
   * Create a new AWTChannel.
   * @param config the global irc configuration.
   * @param c the source channel.
   */
  public AWTChannel(PixxConfiguration config,Channel c)
  {
    super(config,c);

    _menu=new NickMenuHandler(config,this,c);
    _nicks=new ScrollablePixxNickList(_pixxConfiguration,c.getIRCServer().getNickPrefixes());
    c.addChannelListener2(this);
    _nicks.addPixxNickListListener(this);
    _sortedList=new SortedList(new NickNameComparator(c.getIRCServer().getNickPrefixes()));
    _modeMapping=new Hashtable();
		_label=new Label("");
    _label.setBackground(_pixxConfiguration.getColor(PixxColorModel.COLOR_BACK));
    _label.setForeground(_pixxConfiguration.getColor(PixxColorModel.COLOR_WHITE));
    if(_pixxConfiguration.getIRCConfiguration().getB("asl"))
		{
		  Panel right=new Panel();
			right.setLayout(new BorderLayout());
			right.add(_nicks,BorderLayout.CENTER);
			Panel outlabel=new Panel();
      outlabel.setLayout(new BorderLayout());
      outlabel.add(new PixxSeparator(PixxSeparator.BORDER_LEFT),BorderLayout.WEST);
      outlabel.add(new PixxSeparator(PixxSeparator.BORDER_RIGHT),BorderLayout.EAST);
      outlabel.add(new PixxSeparator(PixxSeparator.BORDER_UP),BorderLayout.NORTH);
      outlabel.add(new PixxSeparator(PixxSeparator.BORDER_DOWN),BorderLayout.SOUTH);
			outlabel.add(_label,BorderLayout.CENTER);
			right.add(outlabel,BorderLayout.SOUTH);
			add(right,BorderLayout.EAST);
		}
		else
		{
      add(_nicks,BorderLayout.EAST);
		}
    doLayout();
    title();
    //connection
      print("  Please wait, joining chat room...",3);
      print("  Connected!",4);
      //motd
      print("  Welcome to FrostWire Chat! FrostWire does not control or endorse the content, messages or information found in this chat. FrostWire specifically disclaims any liability with regard to these areas. To review the FrostWire Chat guidelines, go to "+"\3"+"12"+"http://www.frostwire.com/chat/conduct"+"\3",1);
      print("");
      //open channel
      //_source.sendString("/play sounds/ChatJoin.au");
      //show you join
      if(_pixxConfiguration.getB("showchannelyoujoin"))
      {
      print("      *** Now chatting in room "+c.getName()+" as "+c.getServer().getNick()+".",3);
      print("");
      }
  }

  public void release()
  {
    _menu.release();
    ((Channel)_source).removeChannelListener2(this);
    _nicks.removePixxNickListListener(this);
    _nicks.release();
    _menu=null;
    super.release();
  }

	public void doLayout()
	{
	  _label.setText("");
		super.doLayout();
	}

  public void setVisible(boolean b)
  {
    super.setVisible(b);
    if(!b) _nicks.dispose();
  }
  
	/*
	 * Override BaseAWTSource.messageReceived()
	 */
	public void messageReceived(String nick,String str,Source source)
	{
		checkSound(str); 
		String full=(String)_modeMapping.get(nick);
		if((full!=null) && (!full.equals(nick)))
		{
			if((full.startsWith("~")))
			{
			nick="4"+nick+"";
			}
			else if((full.startsWith("&")))
			{
			nick="4"+nick+"";
			}
			else if((full.startsWith("@")))
			{
			nick="12"+nick+"";
			}
			else if((full.startsWith("%")))
			{
			nick="12"+nick+"";
			}
			else if((full.startsWith("+")))
			{
			nick="10"+nick+"";
			}
		}
	super.messageReceived(nick,str,source);
}


  private String getFullModeNick(String nick,String mode)
  {
    Channel c=(Channel)getSource();
    char[] _prefixes=c.getIRCServer().getNickPrefixes();
    char[] _modes=c.getIRCServer().getNickModes();
    char[][] chanmodes=c.getIRCServer().getChannelModes();
    ModeHandler h=new ModeHandler(mode,chanmodes,_modes);
    for(int i=0;i<_modes.length;i++)
    {
      if(h.hasMode(_modes[i])) return _prefixes[i]+nick;
    }

    return nick;
  }

  private String getUnprefixedNick(String nick)
  {
    if(nick.length()==0) return nick;
    Channel c=(Channel)getSource();
    char[] _prefixes=c.getIRCServer().getNickPrefixes();
    for(int i=0;i<_prefixes.length;i++)
      if(nick.charAt(0)==_prefixes[i]) return nick.substring(1);
    return nick;
  }


  private void setNicks(String[] nicks)
  {
    for(int i=0;i<nicks.length;i++) addNick(nicks[i]);
  }

  private void addNick(String nick)
  {
    String mode=((Channel)_source).getNickMode(nick);
    if(mode!=null)
    {
      String full=getFullModeNick(nick,mode);
      _sortedList.add(full);
      _modeMapping.put(nick,full);
    }
  }

  private void removeNick(String nick)
  {
    String full=(String)_modeMapping.get(nick);
    if(full!=null)
    {
      _sortedList.remove(full);
      _modeMapping.remove(nick);
    }
  }

  private void updateNick(String nick)
  {
    removeNick(nick);
    addNick(nick);
  }

  private void update()
  {
    String[] n=new String[_modeMapping.size()];
    Enumeration e=_modeMapping.keys();
    int i=0;
    while(e.hasMoreElements()) n[i++]=(String)e.nextElement();
    _textField.setCompleteList(n);

    n=new String[_modeMapping.size()];
    e=_modeMapping.keys();
    i=0;
    while(e.hasMoreElements())
    {
      String nick=(String)e.nextElement();
      n[i++]=nick+":"+((Channel)_source).whois(getUnprefixedNick(nick));
    }
    _list.setNickList(n);

    n=new String[_sortedList.getSize()];
    e=_sortedList.getItems();
    i=0;
    while(e.hasMoreElements())
		{
      String nick=(String)e.nextElement();
      String whois=((Channel)_source).whois(getUnprefixedNick(nick));
		  n[i++]=nick+":"+whois;
		}

    _nicks.set(n);
    title();
  }

  public synchronized void nickSet(String[] nicks,String[] modes,Channel channel)
  {
    setNicks(nicks);
    update();
  }
  
  public synchronized void nickReset(Channel channel)
  {
    _sortedList.clear();
    _modeMapping.clear();
    _nicks.removeAll();
    update();
  }

  public synchronized void nickJoin(String nick,String mode,Channel channel)
  {
    addNick(nick);
    update();
    /* if(_pixxConfiguration.getB("showchannelnickjoin")) */
    if(_sortedList.getSize()<=100) print("      *** "+nick+" has joined the conversation.",14);
	  //_source.sendString("/play sounds/ChatJoin.au");
  }

  public synchronized void nickPart(String nick,String reason,Channel channel)
  {
    removeNick(nick);
    update();
    /* if(_pixxConfiguration.getB("showchannelnickpart")) */
    if(_sortedList.getSize()<=100) print("      *** "+nick+" has left the conversation.",14);
  }

  public synchronized void nickKick(String nick,String by,String reason,Channel channel)
  {
    removeNick(nick);
    update();
	//_source.sendString("/play sounds/ChatKick.au");
	
    if(_pixxConfiguration.getB("showchannelnickkick"))
    {
      if(nick.equals(_source.getServer().getNick()))
      {
        /* _source.getServer().sendStatusMessage(getText(PixxTextProvider.SOURCE_YOU_KICKED,channel.getName(),by)+": "+reason); */
        print("\2"+" You have been kicked out of the chat room by Host "+by+": "+reason,4);
    	_nicks.removeAll();
    	kicktitle();
      }
      else if(reason.length()>0)
        print("\2"+" Host "+by+" kicked "+nick+" out of the chat room"+": "+reason,4);
      else
    	print("\2"+" Host "+by+" kicked "+nick+" out of the chat room.",4);
    }
  }

  public synchronized void nickQuit(String nick,String reason,Channel channel)
  {
    removeNick(nick);
    update();
    /* if(_pixxConfiguration.getB("showchannelnickquit")) */
    if(_sortedList.getSize()<=100) print("      *** "+nick+" has left the conversation.",14);
  }

  private void title()
  {
	  int count=_sortedList.getSize();
	  String title=" Welcome to FrostWire Live Chat!";
	  if(_pixxConfiguration.getB("displaychannelcount"))
    	{
    	if(_sortedList.getSize()==1) title+=" There is "+count+" person chatting in "+_source.getName()+".";
    	else if(_sortedList.getSize()<=0) title+=" There is nobody chatting in "+_source.getName()+".";
    	else if(_sortedList.getSize()>=2) title+=" There are "+count+" people chatting in "+_source.getName()+".";
    	}
	  if(_pixxConfiguration.getB("displaychannelname")) title+=" Chat Room: "+_source.getName();
	  if(_pixxConfiguration.getB("displaychannelmode")) title+=" Room Modes: "+((Channel)_source).getMode();
	  if(_pixxConfiguration.getB("displaychanneltopic"))
	  {
		  if(title.length()!=0)
			  title+=": "+((Channel)_source).getTopic();
		  else
			  title=((Channel)_source).getTopic();
		  }
	  setTitle(title.trim());
	  }
  
  private void kicktitle()
  {
    String title=" You were kicked out of the chat room.";
    setTitle(title.trim());
  }

  public synchronized void topicChanged(String topic,String by,Channel channel)
  {
    if(_pixxConfiguration.getB("showchanneltopicchanged"))
    {
      if(by.length()==0)
      {
        print("10 The chat's topic is: "+topic);
        print("");
      }
      else
      {
        print("10 The chat's topic has changed to: "+topic);
      }
      title();
    }
  }

  public synchronized void modeApply(String mode,String from,Channel channel)
  {
	  if(from.length()>0)
	  {
		  if(mode.equals("+X")) //+X == Official Channel? Could already be reserved.
				  {
			  		print("      *** "+channel+" is now an official room.",14);
				  }
		  else if(mode.equals("+G")) //+G == Filtered Room/Rated G
				  {
			  		print("      *** This room is now Rated G for all audiences.",14);
				  }
		  else
		  {
			//do nothing  
		  }
	  }
	  else
	  {
/*		  if(mode.equals("+X")) //+X == Official Channel? Could already be reserved.
				  {
			  		print("      *** This room was created by a network staff member.",14);
				  }
		  else if(mode.equals("+G")) //+G == Filtered Room/Rated G
				  {
			  		print("      *** This room is Rated G for all audiences.",14);
				  }
		  else
		  {
			//do nothing  
		  }*/
	  }
    /*if(_pixxConfiguration.getB("showchannelmodeapply"))
    {
      if(from.length()>0)
        print("      *** Room mode has changed to: "+mode,14);
      else
        print("      *** Room mode is: "+mode,14);
    }*/
    title();
  }

  public synchronized void nickModeApply(String nick,String mode,String from,Channel channel)
  {
 //if(_pixxConfiguration.getB("showchannelnickmodeapply"))
		if(!nick.equals(_source.getServer().getNick()))
		{
			if(mode.equals("+q"))
				print("      *** "+from+" has made "+nick+" an Owner.",14);
			else if(mode.equals("+a"))
				print("      *** "+from+" has made "+nick+" an Admin.",14);
			else if(mode.equals("+o"))
				print("      *** "+from+" has made "+nick+" a Host.",14);
			else if(mode.equals("+h"))
				print("      *** "+from+" has made "+nick+" a Helper.",14);
			else if(mode.equals("-v"))
				print("      *** "+from+" has made "+nick+" a Spectator.",14);
		}
		else if(nick.equals(_source.getServer().getNick()))
		{
			if(mode.equals("+q"))
				print("      *** "+from+" has made you an Owner.",3);
			else if(mode.equals("+a"))
				print("      *** "+from+" has made you an Admin.",3);
			else if(mode.equals("+o"))
				print("      *** "+from+" has made you a Host.",3);
			else if(mode.equals("+h"))
				print("      *** "+from+" has made you a Helper.",3);
			else if(mode.equals("+v"))
				print("      *** "+from+" has made you a Participant.",3);
			else if(mode.equals("-v"))
				print("      *** "+from+" has made you a Spectator.",3);
		}
    updateNick(nick);
    update();
  }

  public synchronized void nickChanged(String oldNick,String newNick,Channel channel)
  {
    //if(_pixxConfiguration.getB("showchannelnickchanged"))
	  
	if(!newNick.equals(_source.getServer().getNick()))
	{
    print("      *** "+oldNick+" is now known as "+newNick+".",14);
	}
    removeNick(oldNick);
    addNick(newNick);
    update();
  }

  public void nickWhoisUpdated(String nick,String whois,Channel channel)
	{
    update();
	}

  public void eventOccured(String nick,MouseEvent e)
  {
    if(_pixxConfiguration.matchMouseConfiguration("nickpopup",e))
    {
      _menu.popup(nick,((Channel)_source).whois(nick),_nicks,e.getX(),e.getY());
    }
    else if(_pixxConfiguration.matchMouseConfiguration("nickquery",e))
    {
      if(_pixxConfiguration.getB("automaticqueries"))
      {
        if(!nick.equals(_source.getServer().getNick()))
        {
          _source.sendString("/focus Query "+nick);
          _source.sendString("/query "+nick);
        }
      }
    }
  }

	public void ASLEventOccured(String nick,String info)
	{
    _label.setText(_pixxConfiguration.getIRCConfiguration().formatASL(info));
	}

  public void nickEvent(StyledList lis,String nick,MouseEvent e)
  {
    if(_pixxConfiguration.matchMouseConfiguration("nickpopup",e))
    {
      _menu.popup(nick,((Channel)_source).whois(nick),_list,e.getX(),e.getY());
    }
    else
    {
      super.nickEvent(lis,nick,e);
    }
  }
}
