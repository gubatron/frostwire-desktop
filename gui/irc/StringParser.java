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

import java.util.*;

/**
 * String parser.
 */
public class StringParser
{
  /**
   * Trim the given string, without removing non-printable characters.
   * @param t string to trim.
   * @return trimmed string.
   */
  public static String trim(String t)
  {
    int a=0;
    while((a<t.length()) && (t.charAt(a)==' ')) a++;
    if(a==t.length()) return "";
    int b=t.length()-1;
    while((b>=0) && (t.charAt(b)==' ')) b--;
    if(b<0) return "";
    return t.substring(a,b+1);
  }

  private int indexOf(String s,char toFind)
  {
    int deep=0;
    for(int i=0;i<s.length();i++)
    {
      char c=s.charAt(i);
      if((deep==0) && (c==toFind)) return i;
      if(c=='"') deep=1-deep;
      if(c=='\'') deep=1-deep;
    }
    return -1;
  }

  /**
   * Parse the string.
   * @param line string to parse.
   * @return arrays of strings.
   */
  public String[] parseString(String line)
  {
    Vector<String> res=new Vector<String>();
    while(line.length()!=0)
    {
      int pos=indexOf(line,' ');
      if(pos==-1)
      {
        res.insertElementAt(line,res.size());
        line="";
      }
      else
      {
        String part=trim(line.substring(0,pos));
        line=trim(line.substring(pos));
        res.insertElementAt(part,res.size());
      }
    }

    String[] param=new String[res.size()];
    for(int i=0;i<res.size();i++) param[i]=(String)res.elementAt(i);
    return param;

  }

}

