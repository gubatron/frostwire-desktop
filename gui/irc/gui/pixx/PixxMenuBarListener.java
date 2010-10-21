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
 * The menu bar listener.
 */
public interface PixxMenuBarListener
{
  /**
   * The connection button has been clicked.
   * @param bar the menu bar.
   */
  public void connectionClicked(PixxMenuBar bar);

  /**
   * The chanlist button has been clicked.
   * @param bar the menu bar.
   */
  public void chanListClicked(PixxMenuBar bar);

  /**
   * The about button has been clicked.
   * @param bar the menu bar.
   */
  public void aboutClicked(PixxMenuBar bar);

  /**
   * The help button has been clicked.
   * @param bar the menu bar.
   */
  public void helpClicked(PixxMenuBar bar);

  /**
   * The close button has been clicked.
   * @param bar the menu bar.
   */
  public void closeClicked(PixxMenuBar bar);

  /**
   * The dock button has been clicked.
   * @param bar the menu bar.
   */
  public void dockClicked(PixxMenuBar bar);
}

