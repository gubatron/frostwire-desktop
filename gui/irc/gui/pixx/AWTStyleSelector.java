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

import irc.StyleContext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

/**
 * The style selector.
 */
public class AWTStyleSelector extends JPanel implements MouseListener
{
  private PixxConfiguration _pixxConfiguration;
  private StyleContext _ct;
  private int _color;
  private int _backColor;
  private boolean _bold;
  private boolean _underline;

  /**
   * Create a new AWTStyleSelector.
   * @param config global irc configuration.
   */
  public AWTStyleSelector(PixxConfiguration config)
  {
    super();
    _pixxConfiguration=config;
    _color=1;
    _backColor=0;
    _bold=false;
    _underline=false;
    addMouseListener(this);
    _ct=_pixxConfiguration.getIRCConfiguration().getDefaultStyleContext();
  }

  /**
   * Release this object.
   */
  public void release()
  {
    removeMouseListener(this);
    _pixxConfiguration=null;
  }

  /**
   * Set the style context.
   * @param ct style context.
   */
  public void setStyleContext(StyleContext ct)
  {
    _ct=ct;
  }

  public void paint(Graphics g)
  {
    update(g);
  }

  private void drawSelect(Graphics g,int i,int j)
  {
    int x=(int)(i*getItemWidth());
    int y=(int)(j*getItemHeight());
    int w=(int)getItemWidth();
    int h=(int)getItemHeight();
    g.setColor(Color.black);
    g.drawRect(x+1,y+1,w-2,h-2);
    g.setColor(Color.white);
    g.drawRect(x,y,w-2,h-2);
  }

  private void drawColor(Graphics g,int i,int j,Color c)
  {
    int x=(int)(i*getItemWidth());
    int y=(int)(j*getItemHeight());
    int w=(int)getItemWidth();
    int h=(int)getItemHeight();
    g.setColor(c);
    g.fillRect(x,y,w,h);
  }

  private void drawBold(Graphics g,int i,int j)
  {
    int x=(int)(i*getItemWidth());
    int y=(int)(j*getItemHeight());
    int w=(int)getItemWidth();
    int h=(int)getItemHeight();
    int tw=g.getFontMetrics().stringWidth("a");
    g.drawString("a",x+(w-tw)/2,y+h-(h-g.getFont().getSize())/2-2);
    g.drawString("a",x+(w-tw)/2+1,y+h-(h-g.getFont().getSize())/2-2);
  }

  private void drawUnderline(Graphics g,int i,int j)
  {
    int x=(int)(i*getItemWidth());
    int y=(int)(j*getItemHeight());
    int w=(int)getItemWidth();
    int h=(int)getItemHeight();
    int tw=g.getFontMetrics().stringWidth("a");
    g.drawString("a",x+(w-tw)/2,y+h-(h-g.getFont().getSize())/2-2);

    g.drawLine(x+w/2-tw/2,y+h-1,x+w/2+tw/2,y+h-1);
  }

  public void update(Graphics g)
  {
    int w=getSize().width;
    int h=getSize().height;

    Image buffer;
    Graphics gra;
    try
    {
      buffer=createImage(w,h);
      gra=buffer.getGraphics();
    }
    catch(Throwable e)
    {
      return;
    }

    Color[] cols=_pixxConfiguration.getIRCConfiguration().getStyleColors(_ct);
    int c=0;
    for(int y=0;y<2;y++)
    {
      for(int x=1;x<9;x++)
      {
        drawColor(gra,x,y,cols[c]);
        if(c==_color) drawSelect(gra,x,y);
        c++;
      }
    }

    gra.setColor(cols[_backColor]);
    gra.fillRect(0,0,(int)getItemWidth(),h);

    gra.setColor(cols[_color]);
    drawBold(gra,0,0);
    drawUnderline(gra,0,1);

    if(_bold) drawSelect(gra,0,0);
    if(_underline) drawSelect(gra,0,1);


    g.drawImage(buffer,0,0,this);
  }


  public Dimension getPreferredSize()
  {
    return new Dimension(9*10,10);
  }

  private double getItemWidth()
  {
    return getSize().width/9.0;
  }

  private double getItemHeight()
  {
    return getSize().height/2.0;
  }

  /**
   * Set the front color.
   * @param c the new front color.
   */
  public void setFrontColor(int c)
  {
    _color=c;
    repaint();
  }
  
  /**
   * Set the back color.
   * @param c the new back color.
   */
  public void setBackColor(int c)
  {
    _backColor=c;
    repaint();
  }
  
  /**
   * Set the new bold status.
   * @param bold the new bold status.
   */
  public void setBold(boolean bold)
  {
    _bold=bold;
    repaint();
  }
  
  /**
   * Set the new underline status.
   * @param underline the new underline statu.
   */
  public void setUnderline(boolean underline)
  {
    _underline=underline;
    repaint();
  }

  /**
   * Get prefix to be used for text styling.
   * @return text style prefix.
   */
  public String getPrefix()
  {
    String pre="";
    if(_color!=1 || _backColor!=0)
    {
      String c1="";
      if(_color!=1)
      {
        c1=c1+_color;
        if(_color<10) c1="0"+c1;
      }
      String c2="";
      if(_backColor!=0)
      {
        c2=""+_backColor;
        if(_backColor<10) c2="0"+c2;
        c2=","+c2;
      }
      if(c2.length()>0 && c1.length()==0) c1="1";
      pre=pre+(char)3+c1+c2;
    }
    if(_bold) pre+=(char)2;
    if(_underline) pre+=(char)31;
    return pre;
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }

  public void mousePressed(MouseEvent e)
  {
    int x=(int)(e.getX()/getItemWidth());
    int y=(int)(e.getY()/getItemHeight());
    if(x==0)
    {
      if(y==0) _bold=!_bold;
      if(y==1) _underline=!_underline;
    }
    else
    {
      x--;
      if((e.getModifiers()&InputEvent.BUTTON1_MASK)!=0)
        _color=x+y*8;
      else
        _backColor=x+y*8;
    }
    repaint();
  }

  public void mouseReleased(MouseEvent e)
  {
  }

}
