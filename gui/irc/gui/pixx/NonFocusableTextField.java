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

package irc.gui.pixx;

import java.awt.*;

/**
 * A NonFocusableTextField is a TextField that cannot receive focus via focus cycle.
 */
public class NonFocusableTextField extends TextField
{
  /**
   * Create a new NonFocusableTextField.
   */
  public NonFocusableTextField()
  {
    super();
  }

  /**
   * Create a new NonFocusableTextField.
   * @param text initial field text.
   */
  public NonFocusableTextField(String text)
  {
    super(text);
  }

  public boolean isFocusable()
  {
    try
    {
      String version=System.getProperty("java.version");
      int pos=version.indexOf('.');
      String major=version.substring(0,pos);
      version=version.substring(pos+1);
      pos=version.indexOf('.');
      String med=version.substring(0,pos);
      int ma=Integer.parseInt(major);
      int me=Integer.parseInt(med);
      if(ma>1) return true;
      if(me>1) return true;
      return false;
    }
    catch(Exception ex)
    {
      return true;
    }
  }
}
