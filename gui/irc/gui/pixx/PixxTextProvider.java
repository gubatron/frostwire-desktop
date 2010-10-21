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

package irc.gui.pixx;

import irc.*;

/**
 * PixxTextProvider.
 */
public interface PixxTextProvider
{
  /**
   * You've been kicked out of %1 by %2
   */
  public static final int SOURCE_YOU_KICKED=TextProvider.USER_BASE+0x601;
  /**
   * Status
   */
  public static final int SOURCE_STATUS=TextProvider.USER_BASE+0x602;
  /**
   * Channels for %1
   */
  public static final int SOURCE_CHANLIST=TextProvider.USER_BASE+0x603;
  /**
   * Retrieving channels...
   */
  public static final int SOURCE_CHANLIST_RETREIVING=TextProvider.USER_BASE+0x604;
  /**
   * %1 has joined %2
   */
  public static final int SOURCE_HAS_JOINED=TextProvider.USER_BASE+0x605;
  /**
   * %1 has left %2
   */
  public static final int SOURCE_HAS_LEFT=TextProvider.USER_BASE+0x606;
  /**
   * %1 has been kicked by %2
   */
  public static final int SOURCE_HAS_BEEN_KICKED_BY=TextProvider.USER_BASE+0x607;
  /**
   * %1 has quit
   */
  public static final int SOURCE_HAS_QUIT=TextProvider.USER_BASE+0x608;
  /**
   * Topic is %1
   */
  public static final int SOURCE_TOPIC_IS=TextProvider.USER_BASE+0x609;
  /**
   * %1 changed topic to %2
   */
  public static final int SOURCE_CHANGED_TOPIC=TextProvider.USER_BASE+0x60A;
  /**
   * %1 sets channel mode to %2
   */
  public static final int SOURCE_CHANNEL_MODE=TextProvider.USER_BASE+0x60B;
  /**
   * Channel mode is %1
   */
  public static final int SOURCE_CHANNEL_MODE_IS=TextProvider.USER_BASE+0x60C;
  /**
   * %1 sets mode %2 on %3
   */
  public static final int SOURCE_USER_MODE=TextProvider.USER_BASE+0x60D;
  /**
   * %1 is now known as %2
   */
  public static final int SOURCE_KNOWN_AS=TextProvider.USER_BASE+0x60E;
  /**
   * Mode changed to %1
   */
  public static final int SOURCE_YOUR_MODE=TextProvider.USER_BASE+0x60F;
  /**
   * Your nick is now %1
   */
  public static final int SOURCE_YOUR_NICK=TextProvider.USER_BASE+0x610;
  /**
   * Infos
   */
  public static final int SOURCE_INFO=TextProvider.USER_BASE+0x611;
  /**
   * %1 is away
   */
  public static final int SOURCE_AWAY=TextProvider.USER_BASE+0x612;
  /**
   * %1 invites you to join %2
   */
  public static final int SOURCE_YOU_INVITED=TextProvider.USER_BASE+0x613;
  /**
   * You're talking in %1 as %2
   */
  public static final int SOURCE_YOU_JOINED_AS=TextProvider.USER_BASE+0x614;

  /**
   * Whois
   */
  public static final int GUI_WHOIS=TextProvider.USER_BASE+0x701;
  /**
   * Query
   */
  public static final int GUI_QUERY=TextProvider.USER_BASE+0x702;
  /**
   * Kick
   */
  public static final int GUI_KICK=TextProvider.USER_BASE+0x703;
  /**
   * Ban
   */
  public static final int GUI_BAN=TextProvider.USER_BASE+0x704;
  /**
   * Kick + Ban
   */
  public static final int GUI_KICKBAN=TextProvider.USER_BASE+0x705;
  /**
   * Op
   */
  public static final int GUI_OP=TextProvider.USER_BASE+0x706;
  /**
   * DeOp
   */
  public static final int GUI_DEOP=TextProvider.USER_BASE+0x707;
  /**
   * Voice
   */
  public static final int GUI_VOICE=TextProvider.USER_BASE+0x708;
  /**
   * DeVoice
   */
  public static final int GUI_DEVOICE=TextProvider.USER_BASE+0x709;
  /**
   * Ping
   */
  public static final int GUI_PING=TextProvider.USER_BASE+0x70A;
  /**
   * Version
   */
  public static final int GUI_VERSION=TextProvider.USER_BASE+0x70B;
  /**
   * Time
   */
  public static final int GUI_TIME=TextProvider.USER_BASE+0x70C;
  /**
   * Finger
   */
  public static final int GUI_FINGER=TextProvider.USER_BASE+0x70D;
  /**
   * Receiving file (%1 bytes)
   */
  public static final int GUI_RETREIVING_FILE=TextProvider.USER_BASE+0x70E;
  /**
   * Sending file (%1 bytes)
   */
  public static final int GUI_SENDING_FILE=TextProvider.USER_BASE+0x70F;
  /**
   * %1 terminated
   */
  public static final int GUI_TERMINATED=TextProvider.USER_BASE+0x710;
  /**
   * %1 failed
   */
  public static final int GUI_FAILED=TextProvider.USER_BASE+0x711;
  /**
   * Close
   */
  public static final int GUI_CLOSE=TextProvider.USER_BASE+0x712;
  /**
   * Disconnect
   */
  public static final int GUI_DISCONNECT=TextProvider.USER_BASE+0x713;
  /**
   * Channels
   */
  public static final int GUI_CHANNELS=TextProvider.USER_BASE+0x714;
  /**
   * Help
   */
  public static final int GUI_HELP=TextProvider.USER_BASE+0x715;
  /**
   * private
   */
  public static final int GUI_PRIVATE=TextProvider.USER_BASE+0x716;
  /**
   * public
   */
  public static final int GUI_PUBLIC=TextProvider.USER_BASE+0x717;
  /**
   * Connect
   */
  public static final int GUI_CONNECT=TextProvider.USER_BASE+0x718;
  /**
   * About
   */
  public static final int GUI_ABOUT=TextProvider.USER_BASE+0x719;
  /**
   * Change nick to
   */
  public static final int GUI_CHANGE_NICK=TextProvider.USER_BASE+0x71A;
  /**
   * Font
   */
  public static final int GUI_FONT=TextProvider.USER_BASE+0x71C;
  /**
   * Select font
   */
  public static final int GUI_FONT_WINDOW=TextProvider.USER_BASE+0x71D;
  /**
   * Ok
   */
  public static final int GUI_FONT_WINDOW_OK=TextProvider.USER_BASE+0x71E;
  /**
   * Enter text here... 
   */
  public static final int GUI_ENTER_TEXT_HERE=TextProvider.USER_BASE+0x71F;
}
