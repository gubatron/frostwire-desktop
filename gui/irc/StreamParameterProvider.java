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
import java.util.*;

/**
 * Parameter provider from a text stream.
 */
public class StreamParameterProvider implements ParameterProvider
{
  private Hashtable<String, String> _table;

  /**
   * Create a new StreamParameterProvider loading the given input stream.
   * @param is the input stream to load from. If is is null, then the
   * parameter list will be empty.
   */
  public StreamParameterProvider(InputStream is)
  {
    _table=new Hashtable<String, String>();
    if(is==null) return;

    try
    {
      BufferedReader reader=new BufferedReader(new InputStreamReader(is));
      String line=reader.readLine();
      while(line!=null)
      {
        line=line.trim();
        if(line.length()>0)
        {
          if(line.charAt(0)!='#')
          {
            parse(line);
          }
        }
        line=reader.readLine();
      }
      reader.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void parse(String str)
  {
    int pos=str.indexOf('=');
    if(pos<0) return;
    String before=str.substring(0,pos).trim().toLowerCase(java.util.Locale.ENGLISH);
    String after=str.substring(pos+1).trim();
    _table.put(before,after);
  }

  public String getParameter(String name)
  {
    return (String)_table.get(name.toLowerCase(java.util.Locale.ENGLISH));
  }
}
