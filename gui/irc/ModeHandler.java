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
 * IRC mode handler.
 */
public class ModeHandler
{
  private String _mode;
  private Vector<String> _parameters;
  private char[][] _modes;
  private char[] _prefix;

  /**
   * Create a new ModeHandler with empty mode.
   * @param modes A,B,C,D known modes. This is an array of four arrays of chars.
   * @param prefix prefix modes.
   */
  public ModeHandler(char[][] modes,char[] prefix)
  {
    this("",modes,prefix);
  }

  /**
   * Create a new ModeHandler with given mode.
   * @param mode initial mode.
   * @param modes A,B,C,D know modes.
   * @param prefix prefix modes.
   */
  public ModeHandler(String mode,char[][] modes,char[] prefix)
  {
    _modes=modes;
    _prefix=prefix;
    _mode="";
    _parameters=new Vector<String>();
    if(mode.startsWith("+")) mode=mode.substring(1);
    apply("+"+mode);
  }

  /**
   * Reset current mode.
   */
  public void reset()
  {
    _mode="";
  }

  /**
   * Get password (+k), if defined.
   * @return password.
   */
  public String getPassword()
  {
    if(hasMode('k')) return findParameter('k');
    return "";
  }

  /**
   * Get user limit (+l), if defined.
   * @return the user limit.
   */
  public int getLimit()
  {
    if(hasMode('l')) return new Integer(findParameter('l')).intValue();
    return 0;
  }

  private boolean inside(char[] list,char c)
  {
    for(int i=0;i<list.length;i++) if(list[i]==c) return true;
    return false;
  }

  private String findParameter(char k)
  {
    int index=getParameterIndex(k);
    if(index>=0) return (String)_parameters.elementAt(index);
    return "";
  }


  private boolean hasParameter(boolean positive,char c)
  {
    if(inside(_prefix,c)) return false;
    else if(inside(_modes[0],c)) return true;
    else if(inside(_modes[1],c)) return true;
    else if(inside(_modes[2],c)) return positive;
    else return false;
  }

  private int getParameterIndex(char k)
  {
    if(_parameters.size()==0) return -1;
    int index=0;
    for(int i=0;i<_mode.length();i++)
    {
      char c=_mode.charAt(i);
      if(hasParameter(true,c))
      {
        if(c==k) return index;
        index++;
        if(index>=_parameters.size()) return -1;
      }
      else
      {
        if(c==k) return -1;
      }
    }
    return -1;
  }

  private void addMode(char mode,String param)
  {
    if(hasMode(mode)) removeMode(mode,param);
    _mode+=mode;
    if(hasParameter(true,mode)) _parameters.insertElementAt(param,_parameters.size());
  }

  private void removeMode(char mode,String param)
  {
    if(!hasMode(mode)) return;
    if(hasParameter(true,mode)) _parameters.removeElementAt(getParameterIndex(mode));
    int pos=_mode.indexOf(mode);
    _mode=_mode.substring(0,pos)+_mode.substring(pos+1);
  }

  private void applyOne(boolean positive,char mode,String param)
  {
    if(positive) addMode(mode,param);
    else removeMode(mode,param);
  }

  /**
   * Apply a new mode on the current mode.
   * @param mode new mode to apply.
   */
  public void apply(String mode)
  {
    String[] params=(new StringParser()).parseString(mode);
    boolean positive=true;
    int index=0;

    for(int i=0;i<params[0].length();i++)
    {
      char c=params[0].charAt(i);
      if(c=='+')
      {
        positive=true;
      }
      else if(c=='-')
      {
        positive=false;
      }
      else
      {
        String param="";
        if(hasParameter(positive,c))
        {
          index++;
          if(index>=params.length) return;
          param=params[index];
        }
        applyOne(positive,c,param);
      }
    }
  }

  /**
   * Test wether the given mode is set.
   * @param mode mode to test.
   * @return true if mode is set, false otherwise.
   */
  public boolean hasMode(char mode)
  {
    return _mode.indexOf(mode)!=-1;
  }

  /**
   * Get a string representation of the current mode.
   * @return string representation of current mode.
   */
  public String getMode()
  {
    String ans=_mode;
    for(int i=0;i<_parameters.size();i++) ans+=" "+_parameters.elementAt(i);
    return ans;
  }
}

