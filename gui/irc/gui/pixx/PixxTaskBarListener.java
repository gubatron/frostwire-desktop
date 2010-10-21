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

import java.awt.event.*;

/**
 * Task bar listener.
 */
public interface PixxTaskBarListener
{
  /**
   * The active awt source has been desactivated.
   * @param bar the taskbar.
   * @param source the desactivated source.
   */
  public void AWTSourceDesactivated(PixxTaskBar bar,BaseAWTSource source);

  /**
   * A new awt source has been activated.
   * @param bar the taskbar.
   * @param source the activated source.
   */
  public void AWTSourceActivated(PixxTaskBar bar,BaseAWTSource source);

  /**
   * A new awt source has been added in the taskbar.
   * @param bar the taskbar.
   * @param source the added source.
   */
  public void AWTSourceAdded(PixxTaskBar bar,BaseAWTSource source);

  /**
   * An awt source has been removed from the taskbar.
   * @param bar the taskbar.
   * @param source the removed source.
   */
  public void AWTSourceRemoved(PixxTaskBar bar,BaseAWTSource source);

  /**
   * An mouse event occured on an awt source icon.
   * @param bar the taskbar.
   * @param source the awt source.
   * @param e the mouse event.
   */
  public void eventOccured(PixxTaskBar bar,BaseAWTSource source,MouseEvent e);
}

