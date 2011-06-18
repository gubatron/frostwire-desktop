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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Separator panel.
 */
public class PixxSeparator extends JPanel
{
  /**
     * 
     */
    private static final long serialVersionUID = 6403536477182386073L;
private int _type;
  /**
   * Upside border.
   */
  public static final int BORDER_UP=0;
  /**
   * Downside border.
   */
  public static final int BORDER_DOWN=1;
  /**
   * Left border.
   */
  public static final int BORDER_LEFT=2;
  /**
   * Right border.
   */
  public static final int BORDER_RIGHT=3;

  /**
   * Create a new PixxSeparator.
   * @param type separator type.
   */
  public PixxSeparator(int type)
  {
    super();
    _type=type;
  }

  public Dimension getPreferredSize()
  {

    switch(_type)
    {
      case BORDER_UP:
        return new Dimension(16,2);
      case BORDER_DOWN:
        return new Dimension(16,1);
      case BORDER_LEFT:
        return new Dimension(2,16);
      case BORDER_RIGHT:
        return new Dimension(1,16);
      default:
        return new Dimension(16,16);

    }
  }

  public void paint(Graphics g)
  {
    update(g);
  }

  public void update(Graphics g)
  {
    int w=getSize().width;
    int h=getSize().height;
    switch(_type)
    {
      case BORDER_UP:
        g.setColor(new Color(0x868686));
        g.drawLine(0,0,w-1,0);
        g.drawLine(0,0,0,1);
        g.drawLine(w-1,0,w-1,1);
        g.setColor(Color.black);
        g.drawLine(1,1,w-2,1);
        break;
      case BORDER_LEFT:
        g.setColor(new Color(0x868686));
        g.drawLine(0,0,0,h-1);
        g.setColor(Color.black);
        g.drawLine(1,0,1,h-1);
        break;
      case BORDER_DOWN:
        g.setColor(new Color(0xD7D3CB));
        g.drawLine(0,h-1,w-1,h-1);
        break;
      case BORDER_RIGHT:
        g.setColor(new Color(0xD7D3CB));
        g.drawLine(w-1,0,w-1,h-1);
        break;
    }
  }
}

