/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2005 Philippe Detournay   */
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

package irc.gui.common;

import java.awt.*;
import java.lang.reflect.*;

/**
 * Wrapper for MouseWheelPanel.
 */
public class MouseWheelPanelWrapper extends Panel
{
  private Object _panel;
  private Method _add;
  private Method _remove;

  /**
   * Create a new MouseWheelPanelWrapper with the specified component inside.
   * @param c wrapper component.
   */
  public MouseWheelPanelWrapper(Component c)
  {
    setLayout(new GridLayout(1,1));
    try
    {
      Class cl=Class.forName("irc.gui.prv.MouseWheelPanel");
      _panel=cl.newInstance();
      Class[] types={MouseWheelPanelListener.class};
      _add=cl.getMethod("addMouseWheelPanelListener",types);
      _remove=cl.getMethod("removeMouseWheelPanelListener",types);
      add((Component)_panel);
      types[0]=Component.class;
      Method m=cl.getMethod("add",types);
      Object[] p={c};
      m.invoke(_panel,p);
    }
    catch(Throwable ex)
    {
      add(c);
    }
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addMouseWheelPanelListener(MouseWheelPanelListener lis)
  {
    try
    {
      Object[] p={lis};
      _add.invoke(_panel,p);
    }
    catch(Throwable ex)
    {
      //ignore...
    }
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeMouseWheelPanelListener(MouseWheelPanelListener lis)
  {
    try
    {
      Object[] p={lis};
      _remove.invoke(_panel,p);
    }
    catch(Throwable ex)
    {
      //ignore...
    }
  }
}
