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
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Root panel for all PixxComponents.
 */
public class PixxPanel extends JPanel
{
  /**
     * 
     */
    private static final long serialVersionUID = 471843120347739076L;
/**
   * Black.
   */
  public static final int COLOR_BLACK=PixxColorModel.COLOR_BLACK;
  /**
   * White.
   */
  public static final int COLOR_WHITE=PixxColorModel.COLOR_WHITE;
  /**
   * Dark gray.
   */
  public static final int COLOR_DARK_GRAY=PixxColorModel.COLOR_DARK_GRAY;
  /**
   * Gray.
   */
  public static final int COLOR_GRAY=PixxColorModel.COLOR_GRAY;
  /**
   * Light gray.
   */
  public static final int COLOR_LIGHT_GRAY=PixxColorModel.COLOR_LIGHT_GRAY;
  /**
   * Front.
   */
  public static final int COLOR_FRONT=PixxColorModel.COLOR_FRONT;
  /**
   * Back.
   */
  public static final int COLOR_BACK=PixxColorModel.COLOR_BACK;
  /**
   * Selected.
   */
  public static final int COLOR_SELECTED=PixxColorModel.COLOR_SELECTED;
  /**
   * Event.
   */
  public static final int COLOR_EVENT=PixxColorModel.COLOR_EVENT;
  /**
   * Close.
   */
  public static final int COLOR_CLOSE=PixxColorModel.COLOR_CLOSE;
  /**
   * Voice.
   */
  public static final int COLOR_VOICE=PixxColorModel.COLOR_VOICE;
  /**
   * Op.
   */
  public static final int COLOR_OP=PixxColorModel.COLOR_OP;
  /**
   * Semiop.
   */
  public static final int COLOR_SEMIOP=PixxColorModel.COLOR_SEMIOP;
  /**
   * ASL male.
   */
  public static final int COLOR_MALE=PixxColorModel.COLOR_MALE;
  /**
   * ASL femeale.
   */
  public static final int COLOR_FEMEALE=PixxColorModel.COLOR_FEMEALE;
  /**
   * ASL undefined.
   */
  public static final int COLOR_UNDEF=PixxColorModel.COLOR_UNDEF;

  /**
   * Pixx Configuration.
   */
  protected PixxConfiguration _pixxConfiguration;

  /**
   * Create a new PixxPanel.
   * @param config global pixx configuration.
   */
  public PixxPanel(PixxConfiguration config)
  {
    _pixxConfiguration=config;
  }

  /**
   * Release this object. No further method call may be performed.
   */
  public void release()
  {
    _pixxConfiguration=null;
  }

  /**
   * Get the formatted text from the formatted text code.
   * @param code text code.
   * @return formatted string.
   */
  public String getText(int code)
  {
    return _pixxConfiguration.getText(code);
  }

  /**
   * Draw a 3d box at given position.
   * @param g where to draw.
   * @param x x position.
   * @param y y position.
   * @param w width.
   * @param h height.
   */
  protected void drawSeparator(Graphics g,int x,int y,int w,int h)
  {
    g.setColor(new Color(0x868686));
    g.drawLine(x+0,y+0,x+w-1,y+0);
    g.drawLine(x+0,y+0,x+0,y+1);
    g.drawLine(x+w-1,y+0,x+w-1,y+1);
    g.setColor(Color.black);
    g.drawLine(x+1,y+1,x+w-2,y+1);

    g.setColor(new Color(0x868686));
    g.drawLine(x+0,y+0,x+0,y+h-1);
    g.setColor(Color.black);
    g.drawLine(x+1,y+1,x+1,y+h-1);

    g.setColor(new Color(0xD7D3CB));
    g.drawLine(x+0,y+h-1,x+w-1,y+h-1);

    g.setColor(new Color(0xD7D3CB));
    g.drawLine(x+w-1,y+1,x+w-1,y+h-1);
  }

  /**
   * Return the i'th color from the color model.
   * @param col color index.
   * @return i'th color from color model.
   */
  public Color getColor(int col)
  {
    return _pixxConfiguration.getColor(col);
  }

  /**
   * Get the current IRC color model.
   * @return the irc color model.
   */
  public PixxColorModel getPixxColorModel()
  {
    return _pixxConfiguration.getColorModel();
  }
}

