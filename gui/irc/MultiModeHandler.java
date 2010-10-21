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
 * Combined IRC mode handler.
 */
public class MultiModeHandler
{
  private String[] _parameters;
  private char[][] _modes;
  private char[] _prefix;
  private int _paramIndex;
  private int _codeIndex;
  private boolean _positive;

  private String _currentMode;
  private char _currentModeChar;
  private boolean _currentHasParameter;
  private String _currentParameter;
  private boolean _currentPrefix;

  /**
   * Create a new ModeHandler with empty mode.
   * @param code initial code.
   * @param modes A,B,C,D know modes. This is an array of four arrays of chars.
   * @param prefix prefix modes.
   */
  public MultiModeHandler(String code,char[][] modes,char prefix[])
  {
    _modes=modes;
    _prefix=prefix;
    _parameters=(new StringParser()).parseString(code);
    _paramIndex=1;
    _codeIndex=0;
    _positive=true;
  }

  /**
   * Check whether there is more modes to handle.
   * @return true if there is more modes to handle, false otherwise.
   */
  public boolean terminated()
  {
    return (_codeIndex==_parameters[0].length());
  }

  /**
   * Handle the next mode. This method must be called before first call to any
   * other getter's method.
   */
  public void next()
  {
    if(terminated()) return;
    char c=_parameters[0].charAt(_codeIndex++);
    if(c=='-')
    {
      _positive=false;
      next();
      return;
    }
    if(c=='+')
    {
      _positive=true;
      next();
      return;
    }
    _currentMode="-";
    if(_positive) _currentMode="+";
    _currentMode+=c;
    _currentModeChar=c;
    if(hasParameter(_positive,c) && _paramIndex<_parameters.length)
    {
      _currentHasParameter=true;
      _currentParameter=_parameters[_paramIndex++];
    }
    else
    {
      _currentHasParameter=false;
      _currentParameter="";
    }
    _currentPrefix=inside(_prefix,c);
  }

  private boolean inside(char[] list,char c)
  {
    for(int i=0;i<list.length;i++) if(list[i]==c) return true;
    return false;
  }

  /**
   * Get current mode to handle.
   * @return mode.
   */
  public String getMode()
  {
    return _currentMode;
  }

  /**
   * Check whether the current mode has a parameter.
   * @return true if the mode has a parameter, false otherwise.
   */
  public boolean hasParameter()
  {
    return _currentHasParameter;
  }

  /**
   * Get the current parameter, if defined.
   * @return the current parameter, if defined.
   */
  public String getParameter()
  {
    return _currentParameter;
  }

  /**
   * Check wether the current mode comes from the prefix list instead of the A,B,C,D
   * list.
   * @return true if the mode is a prefix mode.
   */
  public boolean isPrefix()
  {
    return _currentPrefix;
  }

  /**
   * Check wether the current mode comes from the A list.
   * @return true if the mode is a A code.
   */
  public boolean isModeA()
  {
    return inside(_modes[0],_currentModeChar);
  }

  private boolean hasParameter(boolean positive,char c)
  {
    if(inside(_prefix,c)) return true;
    else if(inside(_modes[0],c)) return true;
    else if(inside(_modes[1],c)) return true;
    else if(inside(_modes[2],c)) return positive;
    else return false;
  }
}

