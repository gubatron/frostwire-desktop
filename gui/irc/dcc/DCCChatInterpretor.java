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

/**
 * The DCCChatInterpretor.
 */
public class DCCChatInterpretor extends BasicInterpretor
{
  /**
   * Create a new DCCChatInterpretor.
   * @param config global irc configuration.
   */
  public DCCChatInterpretor(IRCConfiguration config)
  {
    super(config);
  }

  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    if(cmd.equals("query"))
    {
      source.report(getText(IRCTextProvider.INTERPRETOR_BAD_CONTEXT,"/query"));
    }
    else if(cmd.equals("ctcp"))
    {
      source.report(getText(IRCTextProvider.INTERPRETOR_CANNOT_CTCP_IN_DCCCHAT));
    }
    else
    {
      super.handleCommand(source,cmd,parts,cumul);
    }
  }

  protected void say(Source source,String str)
  {
    Server server=source.getServer();
    if(source.talkable())
    {
      source.messageReceived(server.getNick(),str);
      server.say(source.getName(),str);
    }
    else
    {
      source.report(getText(IRCTextProvider.INTERPRETOR_NOT_ON_CHANNEL));
    }
  }
}

