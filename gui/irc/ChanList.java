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
 * A channel list handler.
 */
public class ChanList extends IRCSource
{
  private ListenerGroup _listeners;
  private Vector _channels;
  private String _name;
  private boolean _running;
  private int _ignored;

  /**
   * Create a new ChanList.
   * @param config the global configuration.
   * @param server the IRCServer from where to retreive channel list.
   * @param name the chanlist name.
   */
  public ChanList(IRCConfiguration config,IRCServer server,String name)
  {
    super(config,server);
    _name=name;
    _server=server;
    _listeners=new ListenerGroup();
    _channels=new Vector();
    _running=false;
  }

  public String getType()
  {
    return "ChanList";
  }

  /**
   * Get the chanlist name.
   * @return the chanlist name.
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Get the channels.
   * @return array of all channels.
   */
  public ChannelInfo[] getChannels()
  {
    ChannelInfo[] ans=new ChannelInfo[_channels.size()];
    for(int i=0;i<_channels.size();i++) ans[i]=(ChannelInfo)_channels.elementAt(i);
    return ans;
  }
  
  /**
   * Get the channel count.
   * @return the number of channels.
   */
  public int getChannelCount()
  {
    return _channels.size();
  }

  /**
   * Get the ignored channel count.
   * @return the number of channels that have been ignored.
   */
  public int getIgnoredChannelCount()
  {
    return _ignored;
  }

  /**
   * Add a channel in the channel list.
   * @param nfo new channel to add.
   */
  public void addChannel(ChannelInfo nfo)
  {
    if(_channels.size()>1024 && nfo.userCount<5)
    {
      _ignored++;
      return;
    }   
    _channels.insertElementAt(nfo,_channels.size());
    _listeners.sendEvent("channelAdded",nfo,this);
  }

  /**
   * Begin a new channel listing. The channel list is cleared.
   */
  public void begin()
  {
    _ignored=0;
    _running=true;
    _channels=new Vector();
    _listeners.sendEvent("channelBegin",this);
  }

  /**
   * End the channel listing.
   */
  public void end()
  {
    _running=false;
    _listeners.sendEvent("channelEnd",this);
  }

  /**
   * Add a ChanListListener.
   * @param lis listener to add.
   */
  public void addChanListListener(ChanListListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a chanListListener.
   * @param lis listener to remove.
   */
  public void removeChanListListeners(ChanListListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Request the destruction of this chanlist.
   */
  public void leave()
  {
    if(_running) return;
    getIRCServer().leaveChanList(_name);
  }

  public boolean talkable()
  {
    return false;
  }

  public boolean mayDefault()
  {
    return false;
  }

}

