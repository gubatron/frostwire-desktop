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

/**
 * The ident server listener.
 */
public interface IdentListener
{
  /**
   * Error result.
   */
  public static final int IDENT_ERROR=-1;
  /**
   * Success result.
   */
  public static final int IDENT_OK=0;
  /**
   * Replied default user.
   */
  public static final int IDENT_DEFAULT=1;
  /**
   * Replied not found user.
   */
  public static final int IDENT_NOT_FOUND=2;

  /**
   * The ident server has received a request.
   * @param source the request source.
   * @param result the request result.
   * @param reply the replied result.
   */
  public void identRequested(String source,Integer result,String reply);

  /**
   * The ident server is running.
   * @param port port on wich the server is running.
   */
  public void identRunning(Integer port);

  /**
   * The ident server is leaving.
   * @param message leaving message.
   */
  public void identLeaving(String message);

}

