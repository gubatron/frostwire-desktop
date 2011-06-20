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
 * Handling for unbound list of items.
 */
public class ListHandler
{
  private boolean _baseAll;
  private Hashtable<String, String> _list;
  private String _orig;

  /**
   * Create a new list of items.
   * @param list item list. Syntax is : "" or "none+item1+item2+item3+..." or "all-item1-item2-item3-..."
   * If list is empty, it is equivalent to "all". Items containing + or - characters
   * can be used by replacing them by \+ or \-. The \ character may be entenred using
   * \\ syntax.
   */
  public ListHandler(String list)
  {
    _orig=list;
    list=convert(list);
    _baseAll=true;
    _list=new Hashtable<String, String>();
    StringTokenizer st=new StringTokenizer(list,"\1\2",true);
    if(!st.hasMoreTokens()) return;
    String base=st.nextToken();
    if(base.equals("none")) _baseAll=false;
    while(st.hasMoreTokens())
    {
      String mod=st.nextToken();
      if(mod.equals("\1")) mod="-";
      else if(mod.equals("\2")) mod="+";
      if(!st.hasMoreTokens()) break;
      String token=st.nextToken().toLowerCase(java.util.Locale.ENGLISH);
      _list.put(token,mod);
    }
  }

  private String convert(String txt)
  {
    String ans="";
    for(int i=0;i<txt.length();i++)
    {
      char c=txt.charAt(i);
      if(c=='-') c='\1';
      else if(c=='+') c='\2';
      else if(c=='\\' && (i!=txt.length()-1))
      {
        i++;
        c=txt.charAt(i);
      }
      ans+=c;
    }
    return ans;
  }

  /**
   * Test wether the given item is in the list.
   * @param item item to check.
   * @return true if item is in the list, false otherwise.
   */
  public boolean inList(String item)
  {
    item=item.toLowerCase(java.util.Locale.ENGLISH);
    String mod=(String)_list.get(item);
    if(mod==null) return _baseAll;
    if(mod.equals("+")) return true;
    return false;
  }

  public String toString()
  {
    return _orig;
  }
}
