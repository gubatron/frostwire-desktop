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
 * Channel listener. Use ChannelListener2 instead.
 */
public interface ChannelListener extends SourceListener
{
  /**
   * The channel has changed all its nick list.
   * @param nicks new nicks.
   * @param modes new modes.
   * @param channel the channel.
   */
  public void nickSet(String nicks[],String modes[],Channel channel);

  /**
   * A new nick has joined.
   * @param nick the nick who joined.
   * @param mode nick mode.
   * @param channel the channel.
   */
  public void nickJoin(String nick,String mode,Channel channel);

  /**
   * A nick has quit.
   * @param nick the nick who quit.
   * @param reason reason.
   * @param channel the channel.
   */
  public void nickQuit(String nick,String reason,Channel channel);

  /**
   * A nick has part.
   * @param nick the nick who part.
   * @param reason reason.
   * @param channel the channel.
   */
  public void nickPart(String nick,String reason,Channel channel);

  /**
   * A nick has been kicked.
   * @param nick the nick who has been kicked.
   * @param by the nick who kicked.
   * @param reason kick reason.
   * @param channel the channel.
   */
  public void nickKick(String nick,String by,String reason,Channel channel);

  /**
   * The topic has been changed.
   * @param topic new topic.
   * @param by user who changed topic.
   * @param channel the channel.
   */
  public void topicChanged(String topic,String by,Channel channel);

  /**
   * Channel mode applied.
   * @param mode applied mode.
   * @param from user who applied mode.
   * @param channel the channel.
   */
  public void modeApply(String mode,String from,Channel channel);

  /**
   * Nick mode applied.
   * @param nick user on wich mode applied.
   * @param mode applied mode.
   * @param from user who applied mode.
   * @param channel the channel.
   */
  public void nickModeApply(String nick,String mode,String from,Channel channel);

  /**
   * Nick changed.
   * @param oldNick old nick.
   * @param newNick new nick.
   * @param channel the channel.
   */
  public void nickChanged(String oldNick,String newNick,Channel channel);

  /**
   * Whois bufferised information has been updated.
   * @param nick nick on wich new whois information is available.
   * @param whois whois string for nick.
   * @param channel the channel.
   */
  public void nickWhoisUpdated(String nick,String whois,Channel channel);
}

