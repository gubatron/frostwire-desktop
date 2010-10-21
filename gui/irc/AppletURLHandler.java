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

import java.applet.*;
import java.net.*;

/**
 * URL handling via applet.
 */
public class AppletURLHandler implements URLHandler
{

  private AppletContext _ctx;

  /**
   * Create a new AppletURLHandler using the given AppletContext.
   * @param ctx the applet context to use.
   */
  public AppletURLHandler(AppletContext ctx)
  {
    _ctx=ctx;
  }

  private String replace(String on,String what,String with)
  {
    int pos=on.indexOf(what);
    while(pos>=0)
    {
      String before=on.substring(0,pos);
      String after=on.substring(pos+what.length());
      on=before+with+after;
      pos=on.indexOf(what);
    }
    return on;
  }

  private URL decodeURL(String u) throws MalformedURLException
  {
    if(u.indexOf("://")==-1) u="http://"+u;
    replace(u," ","%20");
    return new URL(u);
  }

  public void stateURL(String url)
  {
    try
    {
      _ctx.showStatus(decodeURL(url).toString());
    }
    catch(Exception e)
    {
      throw new RuntimeException(e.toString());
    }
  }

  public void openURL(String url)
  {
    openURL(url,"_blank");
  }

  public void openURL(String url,String target)
  {
    try
    {
      _ctx.showDocument(decodeURL(url),target);
    }
    catch(Exception e)
    {
      throw new RuntimeException(e.toString());
    }
  }
}

