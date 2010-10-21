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
 * Server irc protocol event listener.
 */
public interface ServerProtocolListener
{
  /**
   * A new server numeric reply has been received.
   * @param prefix reply prefix.
   * @param id reply id.
   * @param params reply parameters.
   */
  public void replyReceived(String prefix,String id,String params[]);

  /**
   * A new server message has been received.
   * @param prefix message prefix.
   * @param command message command.
   * @param params message parameters.
   */
  public void messageReceived(String prefix,String command,String params[]);

  /**
   * The server is now connected.
   * @param host connected host.
   */
  public void connected(String host);

  /**
   * The connection coulnd't be established.
   * @param message error message.
   * @param host remote host.
   */
  public void connectionFailed(String message,String host);

  /**
   * Connection to server has been lost.
   * @param host lost host.
   */
  public void disconnected(String host);
}

