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

package irc;

import java.util.*;

/**
 * The null-server class.
 */
public class NullServer implements Server
{
  /**
   * Create a new NullServer.
   */
  public NullServer()
  {
    //empty default implementation
  }

  public void addServerListener(ServerListener l)
  {
    //empty default implementation
  }

  public void removeServerListener(ServerListener l)
  {
    //empty default implementation
  }

  public void say(String destination,String str)
  {
    //empty default implementation
  }

  public void execute(String str)
  {
    //empty default implementation
  }

  public void sendStatusMessage(String str)
  {
    //empty default implementation
  }

  public String getNick()
  {
    return "";
  }

  public String getUserName()
  {
    return "";
  }
  
  public String getServerName()
  {
    return "Null";
  }

  public void connect()
  {
    //empty default implementation
  }

  public void disconnect()
  {
    //empty default implementation
  }

  public boolean isConnected()
  {
    return false;
  }

  public void setDefaultSource(Source def)
  {
    //empty default implementation
  }

  public void leave()
  {
    //empty default implementation
  }

  public void enumerateSourcesAsCreated(ServerListener lis)
  {
    //empty default implementation
  }

  public void enumerateSourcesAsRemoved(ServerListener lis)
  {
    //empty default implementation
  }

  public Enumeration<Source> getSources()
  {
    return new Vector<Source>().elements();
  }

}

