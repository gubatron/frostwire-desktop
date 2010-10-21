package irc.gui.pixx;
import irc.*;

import irc.style.*;
import java.awt.event.*;

/**
 * The AWTQuery.
 */
public class AWTQuery extends BaseAWTSource implements QueryListener,ReplyServerListener
{
  private NickMenuHandler _menu;

  /**
   * Create a new AWTQuery.
   * @param config the global irc configuration.
   * @param query the source query.
   */
  public AWTQuery(PixxConfiguration config,Query query)
  {
    super(config,query);
    _menu=new NickMenuHandler(config,this,query);
    query.addQueryListener(this);
    query.getIRCServer().addReplyServerListener(this);
    update();
    print("  This is a private conversation with "+_source.getName()+".",3);
    print("  We caution you against giving out any personally identifiable information (such as your social security number, credit card number, name, address, telephone number, driver's license number, password(s), etc.) online. This information can easily be used for illegal or harmful purposes.",4);
    print("");
  }

  public void release()
  {
    ((Query)_source).removeQueryListeners(this);
    ((Query)_source).getIRCServer().removeReplyServerListener(this);
    _menu.release();
    _menu=null;
    super.release();
  }

  private void update()
  {
    String whois=((Query)_source).getWhois();
    String[] nick=new String[2];
    nick[0]=_source.getName()+":"+_pixxConfiguration.getIRCConfiguration().formatASL(whois);
    nick[1]=_source.getServer().getNick()+":"+_pixxConfiguration.getIRCConfiguration().formatASL(_source.getServer().getUserName());
    _list.setNickList(nick);
    title();
  }

  private void title()
  {
    String whois=((Query)_source).getWhois();
    if(whois.length()>0)
      setTitle(_source.getName()+" ("+_pixxConfiguration.getIRCConfiguration().formatASL(whois)+")");
    else
      setTitle(" FrostWire Chat: Private conversation with "+_source.getName()+".");

  }

  public void nickChanged(String newNick,Query query)
  {
    update();
  }

  public void whoisChanged(String whois,Query query)
  {
    update();
  }

  private String whois(String nick)
  {
    nick=nick.toLowerCase(java.util.Locale.ENGLISH);
    if(nick.equals(_source.getName().toLowerCase(java.util.Locale.ENGLISH))) return ((Query)_source).getWhois();
    if(nick.equals(_source.getServer().getNick().toLowerCase(java.util.Locale.ENGLISH))) return _source.getServer().getUserName();
    return "";
  }

  public void nickEvent(StyledList lis,String nick,MouseEvent e)
  {
    if(_pixxConfiguration.matchMouseConfiguration("nickpopup",e))
    {
      _menu.popup(nick,whois(nick),_list,e.getX(),e.getY());
    }
    else
    {
      super.nickEvent(lis,nick,e);
    }
  }

  public Boolean replyReceived(String prefix,String id,String params[],IRCServer server)
  {
    if(id.equals("301")) //away
    {
      if(params[1].toLowerCase(java.util.Locale.ENGLISH).equals(_source.getName().toLowerCase(java.util.Locale.ENGLISH)))
      {
    	  String toSend="14      *** "+params[1]+" is currently away.";
        for(int i=2;i<params.length;i++) toSend+=" Away message: "+params[i];
        _source.report(toSend);
      }
    }
    return Boolean.FALSE;
  }

}

