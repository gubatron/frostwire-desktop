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

package irc.dcc;

import irc.*;
import irc.dcc.prv.*;

/**
 * The DCC Source.
 */
public abstract class DCCSource extends Source
{
  /**
   * Create a new DCCSource.
   * @param config global irc configuration.
   * @param s dcc server.
   */
  public DCCSource(IRCConfiguration config,DCCChatServer s)
  {
    super(config,s);
  }

  /**
   * Get the DCCChatServer.
   * @return the DCCChatServer associated with this source.
   */
  public DCCChatServer getDCCChatServer()
  {
    return (DCCChatServer)_server;
  }


}

