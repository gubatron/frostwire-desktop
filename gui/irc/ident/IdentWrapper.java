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

package irc.ident;

import com.limegroup.gnutella.util.FrostWireUtils;

import irc.*;
import irc.ident.prv.*;

/**
 * Ident wrapper.
 */
public class IdentWrapper extends IRCObject
{
  private IdentServer _ident;
  private IdentListener _lis;

  /**
   * Create a new IdentWrapper.
   * @param config the global configuration.
   */
  public IdentWrapper(IRCConfiguration config)
	{
	  super(config);
	}

  /**
   * Start the execution of the ident server, using default configuration.
   * @param userName ident user name.
   * @param lis the listener to use.
   * @return any occured exception if failed, or null if everything went well.
   */
	public Exception start(String userName,IdentListener lis)
	{
	  _lis=lis;
    _ident=new IdentServer(_ircConfiguration);
    _ident.addIdentListener(lis);
    String name=_ircConfiguration.getS("userid");
    if(name.length()==0) name=userName;
    _ident.setDefaultUser("FrostWire " + FrostWireUtils.getFrostWireVersion() ,name);
    try
    {
      int port=113;
      _ident.start(port);
			return null;
    }
    catch(Exception e)
    {
		  return e;
    }
	}

	/**
	 * Stop the execution of the ident server.
	 */
	public void stop()
	{
    _ident.removeIdentListener(_lis);
    _ident.stop();
    _lis=null;
  }
}

