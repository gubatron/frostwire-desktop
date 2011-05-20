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

package irc.gui.prv;

import irc.ListenerGroup;
import irc.gui.common.MouseWheelPanelListener;

import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

/**
 * MouseWheelPanel. Should be compiled using java jdk1.4.
 */
public class MouseWheelPanel extends JPanel implements MouseWheelListener
{
  private ListenerGroup _listeners;

  /**
   * Create a new MouseWheelPanel.
   */
  public MouseWheelPanel()
  {
    super();
    _listeners=new ListenerGroup();
    addMouseWheelListener(this);
    setLayout(new GridLayout(1,1));
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addMouseWheelPanelListener(MouseWheelPanelListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeMouseWheelPanelListener(MouseWheelPanelListener lis)
  {
    _listeners.removeListener(lis);
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    _listeners.sendEventAsync("mouseWheelMoved",new Integer(e.getWheelRotation()));
  }
}
