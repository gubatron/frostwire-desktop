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

/**
 * A query.
 */
public class Query extends IRCSource implements ReplyServerListener
{
  private String _nick;
  private String _whois;

  private ListenerGroup _listeners;

  /**
   * Create a new Query.
   * @param config global irc configuration.
   * @param nick the remote nick.
   * @param s the server.
   */
  public Query(IRCConfiguration config,String nick,IRCServer s)
  {
    super(config,s);
    s.addReplyServerListener(this);
    _listeners=new ListenerGroup();
    _nick=nick;
    _whois="";
    if(_ircConfiguration.getASLMaster()) getIRCServer().execute("WHOIS "+_nick);
    setInterpretor(new QueryInterpretor(config));
  }

  public void release()
  {
    ((IRCServer)_server).removeReplyServerListener(this);
    super.release();
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addQueryListener(QueryListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeQueryListeners(QueryListener lis)
  {
    _listeners.removeListener(lis);
  }

  public String getType()
  {
    return "Query";
  }

  public String getName()
  {
    return _nick;
  }

  /**
   * Get whois information for remote nick.
   * @return peer whois information.
   */
  public String getWhois()
  {
    return _whois;
  }

  public boolean talkable()
  {
    return true;
  }

  public void leave()
  {
    getIRCServer().leaveQuery(getName());
  }

  /**
   * Notify this query the remote nick has changed.
   * @param newNick new remote nick.
   */
  public void changeNick(String newNick)
  {
    _nick=newNick;
    _listeners.sendEvent("nickChanged",newNick,this);
  }

  public Boolean replyReceived(String prefix,String id,String params[],IRCServer server)
  {
    if(id.equals("311"))
    {
      if(params[1].toLowerCase(java.util.Locale.ENGLISH).equals(_nick.toLowerCase(java.util.Locale.ENGLISH)))
      {
        String name=params[params.length-1];
        _whois=name;
        _listeners.sendEvent("whoisChanged",_whois,this);
      }
    }
    return Boolean.FALSE;
  }

}

