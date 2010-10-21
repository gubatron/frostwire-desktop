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

package irc.plugin;

import irc.*;

/**
 * The root class for all plugins.
 */
public abstract class Plugin
{

  /**
   * The global IRCConfiguration instance.
   */
  protected IRCConfiguration _ircConfiguration;
  /**
   * The running IRCApplication instance.
   */
  protected IRCApplication _ircApplication;

  /**
   * Create a new Plugin with the given IRCConfiguration instance.
   * @param ircConfiguration the global IRCConfiguration instance.
   */
  public Plugin(IRCConfiguration ircConfiguration)
  {
    _ircConfiguration=ircConfiguration;
  }

  /**
   * Set the running IRCApplication instance.
   * @param ircApplication the running IRCApplication.
   */
  public void setIRCApplication(IRCApplication ircApplication)
  {
    _ircApplication=ircApplication;
  }

  /**
   * Load the plugin.
   */
  public void load()
  {
    //default empty implementation...
  }

  /**
   * Unload any ressources used by this plugin. This should be the last
   * method call on the instance.
   */
  public void unload()
  {
    _ircConfiguration=null;
    _ircApplication=null;
  }

  /**
   * Notify this plugin that a new source has been created.
   * @param source the newly created source.
   * @param bring is true if the newly created source should gain immediate
   * focus, false is no particular action is to be taken.
   */
  public void sourceCreated(Source source,Boolean bring)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that an existing source has been removed. No further
   * call should be performed on this source.
   * @param source the removed source.
   */
  public void sourceRemoved(Source source)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that a new server has been created.
   * @param s the newly created server.
   */
  public void serverCreated(Server s)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that an existing server has acheived connection to
   * its remote host.
   * @param s connected server.
   */
  public void serverConnected(Server s)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that an existing server has disconnected from its
   * remote host.
   * @param s disconnected server.
   */
  public void serverDisconnected(Server s)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that an existing server has been removed. No further
   * call should be performed on this server.
   * @param s server removed
   */
  public void serverRemoved(Server s)
  {
    //default empty implementation...
  }

  /**
   * Notify this plugin that an external event has been triggered on it.
   * @param event the event value.
   */
  public void externalEvent(Object event)
  {
    //default empty implementation...
  }

  /**
   * Get the value from the name.
   * @param name the value name.
   * @return the plugin value for this name.
   */
  public Object getValue(Object name)
  {
    return null;
  }

} //Plugin
