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

import java.io.*;
import java.applet.*;
import java.net.*;

/**
 * File handling from applet.
 */
public class AppletFileHandler implements FileHandler
{
  private Applet _app;

  /**
	 * Create a new AppletFileHandler, using the given Applet.
	 * @param app the applet to use.
	 */
  public AppletFileHandler(Applet app)
	{
	  _app=app;
	}

  public InputStream getInputStream(String fileName)
	{
	  try
		{
	    URL url=new URL(_app.getCodeBase(),fileName);
		  return url.openStream();
		}
		catch(Exception ex)
		{
		  return null;
		}
	}
}

