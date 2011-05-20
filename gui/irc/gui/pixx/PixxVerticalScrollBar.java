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

import irc.ListenerGroup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The vertical pixx scroll bar.
 */
public class PixxVerticalScrollBar extends PixxPanel implements PixxScrollBar,MouseListener,MouseMotionListener,Runnable
{
  private double _min;
  private double _max;
  private double _val;
  private boolean _mouseDown;
  private boolean _mouseDownUp;
  private boolean _mouseDownDown;
  private int _base;

  private final int _arrow=10;
  private double _view;

	private MouseEvent _repeatEvent;
	private int _repeatEventCount;
  private Thread _repeatThread;

  private ListenerGroup _listeners;

  /**
   * Create a new PixxVerticalScrollBar.
   * @param config global irc configuration.
   * @param min minimum value.
   * @param max maximum value.
   * @param view width of the display.
   */
  public PixxVerticalScrollBar(PixxConfiguration config,int min,int max,double view)
  {
    super(config);
    _mouseDown=false;
    _view=view;
    _listeners=new ListenerGroup();
    setMinimum(min);
    setMaximum(max);
    setValue(min);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

	public void run()
	{
	  boolean terminated=false;
		_repeatEventCount=0;
		while(!terminated)
		{
		  try
			{
			  if(_repeatEventCount++==0)
				  Thread.sleep(500);
				else
				  Thread.sleep(50);
				mousePressed(_repeatEvent);
			}
			catch(InterruptedException ex)
			{
			  terminated=true;
			}
		}
	}

  public void release()
  {
    removeMouseListener(this);
    removeMouseMotionListener(this);
    super.release();
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addPixxScrollBarListener(PixxScrollBarListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removePixxScrollBarListener(PixxScrollBarListener lis)
  {
    _listeners.removeListener(lis);
  }

  private Color[] getColors(boolean invert)
  {
    Color[] c=new Color[5];
    if(!invert)
    {
      c[0]=getColor(COLOR_FRONT);
      c[1]=getColor(COLOR_BLACK);
      c[2]=getColor(COLOR_GRAY);
      c[3]=getColor(COLOR_LIGHT_GRAY);
      c[4]=getColor(COLOR_WHITE);
    }
    else
    {
      c[0]=getColor(COLOR_SELECTED);
      c[1]=getColor(COLOR_BLACK);
      c[2]=getColor(COLOR_GRAY);
      c[3]=getColor(COLOR_LIGHT_GRAY);
      c[4]=getColor(COLOR_WHITE);
    }

    return c;
  }

  private void drawA(Graphics g,int pos,boolean invert)
  {
    int w=getSize().width;
    //int h=getSize().height;
    int y=pos;

    Color c[]=getColors(invert);

    g.setColor(c[0]);
    for(int i=0;i<w-5;i++)
      g.drawLine(i+3,y-1,i+3,y-1-i);

    g.setColor(c[1]);
    g.drawLine(0,y-1,w-2,y-w+1);
    g.setColor(c[2]);
    g.drawLine(1,y-1,w-2,y-w+2);
    g.setColor(c[4]);
    g.drawLine(2,y-1,w-2,y-w+3);

    g.setColor(c[1]);
    g.drawLine(w-1,y-1,w-1,y-w);
    g.setColor(c[4]);
    g.drawLine(w-2,y-1,w-2,y+3-w);

  }

  private void drawB(Graphics g,int pos,boolean invert)
  {
    int w=getSize().width;
    //int h=getSize().height;
    int y=pos;

    Color c[]=getColors(invert);

    g.setColor(c[0]);
    for(int i=0;i<w-5;i++)
      g.drawLine(w-1-i-3,y,w-1-i-3,y+i);

    g.setColor(c[1]);
    g.drawLine(0,y+w-1,w-1,y);
    g.setColor(c[2]);
    g.drawLine(1,y+w-3,w-2,y);
    g.setColor(c[4]);
    g.drawLine(1,y+w-4,w-3,y);

    g.setColor(c[3]);
    g.drawLine(0,y,0,y+w-2);
    g.setColor(c[4]);
    g.drawLine(1,y,1,y+w-4);


  }

  private void drawInside(Graphics g,int pos,int lng,boolean invert)
  {
    int w=getSize().width;
    //int h=getSize().height;
    Color c[]=getColors(invert);
    int y=pos;

    g.setColor(c[3]);
    g.drawLine(0,y,0,y+lng-1);
    g.setColor(c[4]);
    g.drawLine(1,y,1,y+lng-1);
    g.drawLine(w-2,y,w-2,y+lng-1);
    g.setColor(c[1]);
    g.drawLine(w-1,y,w-1,y+lng-1);
    g.setColor(c[0]);
    g.fillRect(2,y,w-4,lng);
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(16,100);
  }

  public void paint(Graphics g)
  {
    update(g);
  }

  private int getMargin()
  {
    return _arrow+getSize().width;
  }

  private int getCursorLong()
  {
    int h=getSize().height;
    int margin=getMargin();
    if(_min==_max) return h-2*margin;
    double iSee=(h-2*margin)*_view;

    int cursorLong=(int)((iSee/(_max-_min+1))*(h-2*margin));
    if(cursorLong>(h-2*margin)/3) cursorLong=(h-2*margin)/3;
    return cursorLong;
  }

  private int getPos()
  {
    int h=getSize().height;
    int lng=h;

    int margin=getMargin();
    int cursorLong=getCursorLong();
    return (int)((_val*(lng-margin-cursorLong)+(_max-_val)*margin)/(_max)-margin);
  }

  public void update(Graphics g)
  {
    int w=getSize().width;
    int h=getSize().height;
    int margin=getMargin();
    int cursorLong=getCursorLong();

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

    gra.setColor(getColor(COLOR_BACK));
    gra.fillRect(0,0,w,h);

    //fleche du haut
    drawInside(gra,2,_arrow-2,_mouseDownUp);
    drawB(gra,margin-w,_mouseDownUp);

    Color c[]=getColors(_mouseDownUp);
    gra.setColor(c[3]);
    gra.drawLine(1,0,w-2,0);
    gra.drawLine(0,0,0,1);
    gra.setColor(c[4]);
    gra.drawLine(1,1,w-2,1);
    gra.setColor(c[1]);
    gra.drawLine(w-1,0,w-1,1);

    gra.setColor(c[4]);
    gra.drawLine(w/2,4,w/4+1,4+w/4-1);
    gra.drawLine(w/2,4,3*w/4-1,4+w/4-1);

    //fleche du bas
    drawInside(gra,h-_arrow,_arrow-2,_mouseDownDown);
    drawA(gra,h-margin+w,_mouseDownDown);

    c=getColors(_mouseDownDown);
    gra.setColor(c[3]);
    gra.drawLine(0,h-2,0,h-1);
    gra.setColor(c[1]);
    gra.drawLine(w-1,h-2,w-1,h-1);
    gra.drawLine(1,h-1,w-2,h-1);
    gra.setColor(c[4]);
    gra.drawLine(1,h-2,w-2,h-2);

    gra.setColor(c[4]);
    gra.drawLine(w/2,h-5,w/4+1,h-5-w/4+1);
    gra.drawLine(w/2,h-5,3*w/4-1,h-5-w/4+1);

    //curseur
    int pos=getPos()+margin;
    drawInside(gra,pos,cursorLong,_mouseDown);
    drawA(gra,pos,_mouseDown);
    drawB(gra,pos+cursorLong,_mouseDown);

    g.drawImage(buffer,0,0,this);
  }

  /**
   * Set minimum position.
   * @param v new minimum position.
   */
  public void setMinimum(int v)
  {
    _min=v;
    if(_min>_max) _min=_max;
    if(_val<_min) updateValue(_min);
    repaint();
  }

  /**
   * Set maximum position.
   * @param v new maximum position.
   */
  public void setMaximum(int v)
  {
    _max=v;
    if(_max<_min) _max=_min;
    if(_val>_max) updateValue(_max);
    repaint();
  }

  /**
   * Set value.
   * @param v new value.
   */
  public void setValue(int v)
  {
    _val=v;
    if(_val<_min) _val=_min;
    if(_val>_max) _val=_max;
    repaint();
  }

  /**
   * Get current value.
   * @return value.
   */
  public int getValue()
  {
    return (int)(_val+0.5);
  }

  private boolean inCursor(int x,int y)
  {
    int w=getSize().width;
    //int h=getSize().height;
    int l=getCursorLong();
    y-=getMargin();
    y-=getPos();

    return (x+y>=-1) && (y+x-l-w<=-1);
  }

  private boolean inSubArrow(int x,int y)
  {
    y-=getMargin();
    return (x+y<=-1);
  }

  private boolean inAddArrow(int x,int y)
  {
    int w=getSize().width;
    int h=getSize().height;
    return (y+x-h+getMargin()-w>=-1);
  }

  private double getValue(int x,int y)
  {
    //int w=getSize().width;
    int h=getSize().height;
    //int lrg=w;
    int lng=h;
    int margin=getMargin();

    lng-=margin*2+getCursorLong();

    int py=y-margin-_base;

    return (_max-_min)*py/lng+_min;
  }

  private void updateValue(double v)
  {
    int oldVal=getValue();
    _val=v;
    if(_val<_min) _val=_min;
    if(_val>_max) _val=_max;
    repaint();
    if(getValue()!=oldVal)
    {
      _listeners.sendEventAsync("valueChanged",this);
    }
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

  private void beginRepeat(MouseEvent e)
	{
		_repeatEvent=e;
		_repeatThread=new Thread(this,"Scrolling thread");
	  _repeatThread.start();
	}

  private void endRepeat()
	{
	  if(_repeatThread!=null)
		{
	    try
	    {
        _repeatThread.interrupt();
      }
      catch(Exception ex)
      {
      }
      try
      {
        _repeatThread.join(1000);
	    }
	    catch(Exception ex)
	    {
      }
      _repeatThread=null;
		}
	}

  public void mousePressed(MouseEvent e)
  {
    if(inCursor(e.getX(),e.getY()))
    {
      _base=e.getY()-getMargin()-getPos();
      _mouseDown=true;
      repaint();
			return;
    }
    else if(inSubArrow(e.getX(),e.getY()))
    {
      _mouseDownUp=true;
      updateValue(_val-1);
      repaint();
    }
    else if(inAddArrow(e.getX(),e.getY()))
    {
      _mouseDownDown=true;
      updateValue(_val+1);
      repaint();
    }
    else if(getValue(e.getX(),e.getY())<_val)
    {
      updateValue(_val-10);
      repaint();
    }
    else if(getValue(e.getX(),e.getY())>_val)
    {
      updateValue(_val+10);
      repaint();
    }
		if(_repeatThread==null) beginRepeat(e);
  }

  public void mouseReleased(MouseEvent e)
  {
		endRepeat();
    _mouseDown=false;
    _mouseDownUp=false;
    _mouseDownDown=false;
    repaint();
  }

  public void mouseDragged(MouseEvent e)
  {
    mouseMoved(e);
  }

  public void mouseMoved(MouseEvent e)
  {
	  _repeatEvent=e;
    if(_mouseDown)
    {
      updateValue(getValue(e.getX(),e.getY()));
    }
  }
}

