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
 * Server listener.
 */
public interface ServerListener
{
  /**
   * The server has connected.
   * @param s the server.
   */
  public void serverConnected(Server s);

  /**
   * The server has disconnected.
   * @param s the server.
   */
  public void serverDisconnected(Server s);


  /**
   * The server has left.
   * @param s the server.
   */
  public void serverLeft(Server s);

  /**
   * The nickname(s) provided to this server for registration cannot be used for
   * any reason.
   * @param s the server.
   * @return an array of alternative nicknames.
   */
  public String[] cannotUseRequestedNicknames(Server s);

  /**
   * A new source has been created.
   * @param source the created source.
   * @param server the server.
   * @param bring true if the newly created source must have immediate focus.
   */
  public void sourceCreated(Source source,Server server,Boolean bring);

  /**
   * An existing source has been removed.
   * @param source the removed source.
   * @param server the server.
   */
  public void sourceRemoved(Source source,Server server);

  /**
   * Send a special request from the server. This is a generic-purpose event and
   * possible requests should be defined by server implementations.
   * @param request the request.
   * @param server the server.
   * @param params request parameters.
   * @return request result.
   */
  public Object specialServerRequest(String request,Server server,Object[] params);
}

