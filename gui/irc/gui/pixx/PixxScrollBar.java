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

/**
 * The pixx scroll bar.
 */
public interface PixxScrollBar
{
  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addPixxScrollBarListener(PixxScrollBarListener lis);

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removePixxScrollBarListener(PixxScrollBarListener lis);

  /**
   * Set minimum position.
   * @param v new minimum position.
   */
  public void setMinimum(int v);

  /**
   * Set maximum position.
   * @param v new maximum position.
   */
  public void setMaximum(int v);

  /**
   * Set value.
   * @param v new value.
   */
  public void setValue(int v);

  /**
   * Get current value.
   * @return value.
   */
  public int getValue();
}

