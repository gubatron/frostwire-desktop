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
 * The Source Listener.
 */
public interface SourceListener
{
  /**
   * A new message has been received.
   * @param nick source nick.
   * @param msg message.
   * @param source source.
   */
  public void messageReceived(String nick,String msg,Source source);

  /**
   * A new report has been received.
   * @param message report.
   * @param source source.
   */
  public void reportReceived(String message,Source source);

  /**
   * A new notice has been received.
   * @param nick source nick.
   * @param message notice.
   * @param source source.
   */
  public void noticeReceived(String nick,String message,Source source);

  /**
   * A new action has been received.
   * @param nick source nick.
   * @param msg message.
   * @param source source.
   */
  public void action(String nick,String msg,Source source);

  /**
   * A new clear request has been received.
   * @param source source.
   */
  public void clear(Source source);
}

