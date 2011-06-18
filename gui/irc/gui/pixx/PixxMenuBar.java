package irc.gui.pixx;

import irc.IRCConfiguration;
import irc.ListenerGroup;
import irc.StyleContext;
import irc.style.DecodedLine;
import irc.style.DrawResult;
import irc.style.FormattedStringDrawer;
import irc.style.FormattedStringDrawerListener;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The menu bar.
 */
public class PixxMenuBar extends PixxPanel implements MouseListener,MouseMotionListener,Runnable,FormattedStringDrawerListener
{

  /**
     * 
     */
    private static final long serialVersionUID = 5289774818741275230L;
private int _pressedIndex;
  private boolean _closePressed;
  private boolean _dockPressed;
  private ListenerGroup _listeners;
  private boolean _connected;
  private boolean _title;
  private Image _buffer;
  private FormattedStringDrawer _drawer;
  private String _titleString;
  private DecodedLine _decodedTitle;
  private int _connectIndex;
	private int _chanlistIndex;
	private int _aboutIndex;
	private int _helpIndex;
	private int _titleLeft;
	private int _mouseDownX;
	private boolean _mouseScroll;
	private DrawResult _drawResult;
	private boolean _terminated;
	private boolean _redrawTitle;
	private Thread _scrollThread;
	private Object _scrollLock;
	private boolean _freeze;
  private int _scrollDelay;

	/**
	 * Create a new PixxMenuBar without title displayed.
	 * @param config the global irc configuration.
	 */
  public PixxMenuBar(PixxConfiguration config)
  {
    this(config,false);
  }

  /**
   * Create a new PixxMenuBar.
   * @param config the global irc configuration.
   * @param title true if this menu bar should display its own title, false otherwise.
   */
  public PixxMenuBar(PixxConfiguration config,boolean title)
  {
    super(config);
		_titleLeft=0;
    _title=title;
		_mouseScroll=false;
    _titleString="";
    IRCConfiguration _ircConfiguration=config.getIRCConfiguration();
    _drawer=new FormattedStringDrawer(_ircConfiguration,_ircConfiguration.getDefaultStyleContext(),this);
    _decodedTitle=_drawer.decodeLine(_titleString);
    _connected=false;
    _pressedIndex=-1;
    _closePressed=false;
    _dockPressed=false;
    _listeners=new ListenerGroup();
		int currentIndex=0;
    if(config.getB("showconnect")) _connectIndex=currentIndex++;
    if(config.getB("showchanlist")) _chanlistIndex=currentIndex++;
    if(config.getB("showabout")) _aboutIndex=currentIndex++;
    if(config.getB("showhelp")) _helpIndex=currentIndex++;
    _scrollDelay=config.getI("scrollspeed");
    if(_scrollDelay!=0) _scrollDelay=1000/_scrollDelay;
		_drawResult=new DrawResult();
    addMouseListener(this);
		addMouseMotionListener(this);
		_terminated=false;
		_redrawTitle=false;
		_scrollLock=new Object();
		if(_scrollDelay>0)
    {
		  _scrollThread=new Thread(this);
		  _scrollThread.start();
    }
  }

  public void release()
  {
    removeMouseListener(this);
    removeMouseMotionListener(this);
    super.release();
  }

  /**
   * Set title.
   * @param title title string.
   * @param context title style context.
   */
  public void setTitle(String title,StyleContext context)
  {
    _drawer.setStyleContext(context);
    _titleString=title;
    _decodedTitle=_drawer.decodeLine(_titleString);
    _buffer=null;
    repaint();
  }

  /**
   * Add listener.
   * @param lis listener to add.
   */
  public void addPixxMenuBarListener(PixxMenuBarListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove listener.
   * @param lis listener to remove.
   */
  public void removePixxMenuBarListener(PixxMenuBarListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Set the connected flag.
   * @param b true if application is connected to the server, false otherwise.
   */
  public void setConnected(boolean b)
  {
    _connected=b;
    _buffer=null;
    repaint();
  }

  public Dimension getPreferredSize()
  {
    if(_title)
      return new Dimension(16,getItemHeight()+getTitleHeight()+4);
    return new Dimension(16,getItemHeight()+4);
  }

  private int getClosePositionX()
  {
    int w=getSize().width;
    return w-18;
  }

  private int getClosePositionY()
  {
    return getY(0)+1;
  }

  private int getDockPositionX()
  {
    int w=getSize().width;
    if(!_pixxConfiguration.getB("showclose")) return w-18;
    return w-18-18;
  }

  private int getDockPositionY()
  {
    return getY(0)+1;
  }

  private boolean isClosePressed(int x,int y)
  {
    if(!_pixxConfiguration.getB("showclose")) return false;
    x-=getClosePositionX();
    if(x<0) return false;
    if(x>=16) return false;
    y-=getClosePositionY();
    if(y<0) return false;
    if(y>=16) return false;
    return true;
  }

  private boolean isDockPressed(int x,int y)
  {
    if(!_pixxConfiguration.getB("showdock")) return false;
    x-=getDockPositionX();
    if(x<0) return false;
    if(x>=16) return false;
    y-=getDockPositionY();
    if(y<0) return false;
    if(y>=16) return false;
    return true;
  }

  private int getItemWidth()
  {
    return 100;
  }

  private int getItemHeight()
  {
    return 17;
  }

  private int getIconWidth()
  {
    return 16;
  }

  private int getIconHeight()
  {
    return getItemHeight()-4;
  }

  private int getX(int pos)
  {
    return pos*(getItemWidth()+8)+2;
  }

  private int getPos(int x)
  {
    return (x-2)/(getItemWidth()+8);
  }

  private int getY(int pos)
  {
    if(!_title)
      return 2;
    return 2+getTitleHeight()*0;
  }

  private int getTitleY()
  {
   // return 0;
    return getItemHeight()+4;
  }

  private int getTitleHeight()
  {
    return 18;
  }


  private int getIndex(int x)
  {
    int pos=getPos(x);
    if(pos<0) return -1;
    if(pos>4) return -1;
    x-=getX(pos);
    if(x>=getItemWidth()) return -1;
    return pos;
  }

  private int getIndex(int x,int y)
  {
    if(y<getY(0)) return -1;
    y-=getY(0);
    if(y>=getItemHeight()) return -1;
    return getIndex(x);
  }

  private void drawTitle(Graphics g,int y)
  {
    int w=getSize().width;

    g.setColor(_drawer.getColor(0));
    g.fillRect(0,y,w,getTitleHeight());
    g.setClip(0,y,w,getTitleHeight());
    _drawer.draw(_decodedTitle,g,5+_titleLeft,w-5+_titleLeft,y+getTitleHeight()-2,0,w-1,false,false,_drawResult);
    g.setClip(0,0,getSize().width,getSize().height);

    drawSeparator(g,0,y,w,getTitleHeight());
  }

  private void drawDisconnectIcon(Graphics g,int x,int y)
  {
    int w=getIconWidth();
    int h=getIconHeight();
    g.setColor(new Color(0xEFEFEF));
    g.fillRect(x,y,w,h);

    g.setColor(new Color(0xAFAFAF));
    g.drawLine(x,y+h/2-1,x+5,y+h/2-1);
    g.drawLine(x+w-1,y+h/2-1,x+w-5,y+h/2-1);
    g.setColor(Color.black);
    g.drawLine(x,y+h/2,x+4,y+h/2);
    g.drawLine(x+w-1,y+h/2,x+w-6,y+h/2);

    g.drawLine(x+4,y+h/2+1,x+7,y+h/2-2);
    g.drawLine(x+8,y+h/2+1,x+11,y+h/2-2);
  }

  private void drawConnectIcon(Graphics g,int x,int y)
  {
    int w=getIconWidth();
    int h=getIconHeight();
    g.setColor(new Color(0xEFEFEF));
    g.fillRect(x,y,w,h);

    g.setColor(new Color(0xA2A2A2));
    g.drawLine(x,y+h/2-1,x+w-1,y+h/2-1);
    g.setColor(Color.black);
    g.drawLine(x,y+h/2,x+w-1,y+h/2);


    g.setColor(new Color(0x960000));
    g.drawLine(x+2,y+2,x+14,y+2);
    g.drawLine(x+12,y,x+14,y+2);
    g.drawLine(x+12,y+4,x+14,y+2);

    g.setColor(new Color(0x2A5B90));
    g.drawLine(x+2,y+9,x+14,y+9);
    g.drawLine(x+2,y+9,x+4,y+7);
    g.drawLine(x+2,y+9,x+4,y+11);
  }

  private void drawChanListIcon(Graphics g,int x,int y)
  {
    int w=getIconWidth();
    int h=getIconHeight();
    g.setColor(new Color(0xEFEFEF));
    g.fillRect(x,y,w,h);
    g.setColor(Color.black);
    x++;
    g.drawLine(x,y+1,x+9,y+1);
    g.drawLine(x,y+3,x+5,y+3);
    g.drawLine(x,y+5,x+12,y+5);
    g.drawLine(x,y+7,x+11,y+7);
    g.drawLine(x,y+9,x+9,y+9);
    g.drawLine(x,y+11,x+13,y+11);
  }

  private void drawHelpIcon(Graphics g,int x,int y)
  {
    int w=getIconWidth();
    int h=getIconHeight();
    g.setColor(new Color(0xEFEFEF));
    g.fillRect(x,y,w,h);
    g.setColor(Color.black);
    x+=4;
    y++;
    g.fillRect(x+0,y+0,2,3);
    g.fillRect(x+2,y+0,4,2);
    g.fillRect(x+6,y+0,2,6);
    g.fillRect(x+3,y+4,3,2);
    g.fillRect(x+3,y+6,2,2);
    g.fillRect(x+3,y+9,2,2);
  }

  private void drawAboutIcon(Graphics g,int x,int y)
  {
    int w=getIconWidth();
    int h=getIconHeight();
    g.setColor(new Color(0xEFEFEF));
    g.fillRect(x,y,w,h);
    g.setColor(Color.black);
    g.drawLine(x+5,y+4,x+8,y+4);
    g.drawLine(x+5,y+11,x+10,y+11);
    g.fillRect(x+7,y+4,2,7);
    g.fillRect(x+7,y+1,2,2);
  }


  private void drawCloseButtonCross(Graphics g,int x,int y)
  {
    int w=13;
    int h=11;
    g.setColor(getColor(COLOR_CLOSE));
    g.fillRect(x,y,w,h);
    g.setColor(getColor(COLOR_BLACK));
    for(int i=0;i<4;i++)
    {
      g.drawLine(x+3+i,y+2+i,x+4+i,y+2+i);
      g.drawLine(x+9-i,y+2+i,x+10-i,y+2+i);

      g.drawLine(x+3+i,y+8-i,x+4+i,y+8-i);
      g.drawLine(x+9-i,y+8-i,x+10-i,y+8-i);
    }
  }

  private void drawDockButtonInternal(Graphics g,int x,int y)
  {
    int w=13;
    int h=11;
    g.setColor(getColor(COLOR_CLOSE));
    g.fillRect(x,y,w,h);
    g.setColor(getColor(COLOR_BLACK));
    int ox=4;
    int oy=1;
    g.drawRect(x+ox,y+oy,6,5);
    g.drawLine(x+ox+1,y+oy+1,x+ox+6,y+oy+1);
    ox=2;
    oy=4;
    g.setColor(getColor(COLOR_BLACK));
    g.drawRect(x+ox,y+oy,6,5);
    g.drawLine(x+ox+1,y+oy+1,x+ox+6,y+oy+1);
    g.setColor(getColor(COLOR_CLOSE));
    g.fillRect(x+ox+1,y+oy+2,5,3);
  }

  private void drawItem(Graphics g,int x,int y,boolean selected,String s)
  {
    int w=getItemWidth();
    int h=getItemHeight();
    int iw=getIconWidth();
    g.setColor(getColor(COLOR_FRONT));
    if(selected) g.setColor(getColor(COLOR_SELECTED));
    g.fillRect(x,y,w,h);
    g.setColor(getColor(COLOR_BLACK));
    g.drawRect(x,y,w-1,h-1);
    g.setColor(getColor(COLOR_WHITE));
    g.drawRect(x+1,y+1,w-3,h-3);
    g.drawLine(x+iw+2,y+1,x+iw+2,y+h-2);
    int sw=g.getFontMetrics().stringWidth(s);
    w-=(5+iw);
    g.drawString(s,x+iw+3+(w-sw)/2,y+h-4);
  }

  private void drawDisconnectItem(Graphics g,int x,int y,boolean pressed)
  {
    drawItem(g,x,y,pressed,"Disconnect");
    //drawItem(g,x,y,pressed,getText(PixxTextProvider.GUI_DISCONNECT));
    drawDisconnectIcon(g,x+2,y+2);
  }

  private void drawConnectItem(Graphics g,int x,int y,boolean pressed)
  {
    drawItem(g,x,y,pressed,"Connect");
    //drawItem(g,x,y,pressed,getText(PixxTextProvider.GUI_CONNECT));
    drawConnectIcon(g,x+2,y+2);
  }

  private void drawChanListItem(Graphics g,int x,int y,boolean pressed)
  {
    drawItem(g,x,y,pressed,"Chat Rooms");
    //drawItem(g,x,y,pressed,getText(PixxTextProvider.GUI_CHANNELS));
    drawChanListIcon(g,x+2,y+2);
  }

  private void drawAboutItem(Graphics g,int x,int y,boolean pressed)
  {
    drawItem(g,x,y,pressed,"About");
    //drawItem(g,x,y,pressed,getText(PixxTextProvider.GUI_ABOUT));
    drawAboutIcon(g,x+2,y+2);
  }

  private void drawHelpItem(Graphics g,int x,int y,boolean pressed)
  {
    drawItem(g,x,y,pressed,"Help");
    //drawItem(g,x,y,pressed,getText(PixxTextProvider.GUI_HELP));
    drawHelpIcon(g,x+2,y+2);
  }

  private void drawSmallButton(Graphics g,int x,int y,boolean pressed)
  {
    int w=16;
    int h=16;
    if(!pressed)
    {
      g.setColor(getColor(COLOR_WHITE));
      g.drawLine(x+0,y+1,x+w-2,y+1);
      g.drawLine(x+0,y+1,x+0,y+h-2);
      g.setColor(getColor(COLOR_BLACK));
      g.drawLine(x+w-1,y+h-2,x+w-1,y+1);
      g.drawLine(x+w-1,y+h-2,x+0,y+h-2);
      g.setColor(getColor(COLOR_DARK_GRAY));
      g.drawLine(x+w-2,y+h-3,x+w-2,y+2);
      g.drawLine(x+w-2,y+h-3,x+1,y+h-3);
    }
    else
    {
      g.setColor(getColor(COLOR_BLACK));
      g.drawLine(x+0,y+1,x+w-2,y+1);
      g.drawLine(x+0,y+1,x+0,y+h-2);
      g.setColor(getColor(COLOR_WHITE));
      g.drawLine(x+w-1,y+h-2,x+w-1,y+1);
      g.drawLine(x+w-1,y+h-2,x+0,y+h-2);
      g.setColor(getColor(COLOR_DARK_GRAY));
      g.drawLine(x+1,y+2,x+1,y+h-3);
      g.drawLine(x+1,y+2,x+w-2,y+2);
    }
  }

  private void drawCloseButtonItem(Graphics g,int x,int y,boolean pressed)
  {
    drawSmallButton(g,x,y,pressed);
    if(!pressed)
      drawCloseButtonCross(g,x+1,y+2);
    else
      drawCloseButtonCross(g,x+2,y+3);
  }

  private void drawDockButtonItem(Graphics g,int x,int y,boolean pressed)
  {
    drawSmallButton(g,x,y,pressed);
    if(!pressed)
      drawDockButtonInternal(g,x+1,y+2);
    else
      drawDockButtonInternal(g,x+2,y+3);
  }
  public void paint(Graphics g)
  {
    update(g);
  }

  public void update(Graphics ug)
  {
    int w=getSize().width;
    int h=getSize().height;

    if(_buffer!=null)
    {
      if((_buffer.getWidth(this)!=w) || (_buffer.getHeight(this)!=h)) _buffer=null;
    }

    if(_buffer==null)
    {
      Graphics g;
      try
      {
        _buffer=createImage(w,h);
        g=_buffer.getGraphics();
      }
      catch(Throwable e)
      {
        return;
      }


      g.setFont(new Font("Dialog",Font.PLAIN,12));

   //   g.setColor(new Color(0x084079));
      g.setColor(getColor(COLOR_BACK));
      g.fillRect(0,0,w,h);

      //drawSeparator(g,0,0,w,getItemHeight()+4);

      if(_pixxConfiguration.getB("showconnect"))
			{
        if(!_connected)
          drawConnectItem(g,getX(_connectIndex),getY(0),_pressedIndex==_connectIndex);
        else
          drawDisconnectItem(g,getX(_connectIndex),getY(0),_pressedIndex==_connectIndex);
			}
      if(_pixxConfiguration.getB("showchanlist")) drawChanListItem(g,getX(_chanlistIndex),getY(0),_pressedIndex==_chanlistIndex);
      if(_pixxConfiguration.getB("showabout")) drawAboutItem(g,getX(_aboutIndex),getY(0),_pressedIndex==_aboutIndex);
      if(_pixxConfiguration.getB("showhelp")) drawHelpItem(g,getX(_helpIndex),getY(0),_pressedIndex==_helpIndex);

      if(_pixxConfiguration.getB("showclose")) drawCloseButtonItem(g,getClosePositionX(),getClosePositionY(),_closePressed);
      if(_pixxConfiguration.getB("showdock")) drawDockButtonItem(g,getDockPositionX(),getDockPositionY(),_dockPressed);

      if(_title)
        drawTitle(g,getTitleY());

    }
    else
    {
      Graphics g=_buffer.getGraphics();
      if(_redrawTitle) drawTitle(g,getTitleY());
    }

    _redrawTitle=false;


    if(_buffer!=null) ug.drawImage(_buffer,0,0,this);

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
    _pressedIndex=getIndex(e.getX(),e.getY());
    _closePressed=isClosePressed(e.getX(),e.getY());
    _dockPressed=isDockPressed(e.getX(),e.getY());
    _buffer=null;
	  if(_title && (e.getY()>=getTitleY()))
		{
		  _mouseDownX=e.getX();
			_mouseScroll=true;
		}
    repaint();
  }

  public void mouseReleased(MouseEvent e)
  {
	  _mouseScroll=false;
    int index=getIndex(e.getX(),e.getY());
    boolean close=isClosePressed(e.getX(),e.getY());
    boolean dock=isDockPressed(e.getX(),e.getY());
    if(index==_connectIndex) 
        if(_pixxConfiguration.getB("showconnect")) 
            _listeners.sendEventAsync("connectionClicked",this);
    
    if(index==_chanlistIndex) if(_pixxConfiguration.getB("showchanlist")) _listeners.sendEventAsync("chanListClicked",this);
    if(index==_aboutIndex) if(_pixxConfiguration.getB("showabout")) _listeners.sendEventAsync("aboutClicked",this);
    if(index==_helpIndex) if(_pixxConfiguration.getB("showhelp")) _listeners.sendEventAsync("helpClicked",this);
    if(close) _listeners.sendEventAsync("closeClicked",this);
    if(dock) _listeners.sendEventAsync("dockClicked",this);
    _closePressed=false;
    _dockPressed=false;
    _pressedIndex=-1;
    _buffer=null;
    repaint();
  }

	public void mouseMoved(MouseEvent e)
	{
	  if(_title && (e.getY()>=getTitleY()))
		{
		  /*
		   * E_RESIZE_CURSOR
		   */
      if(!getCursor().equals(new Cursor(Cursor.DEFAULT_CURSOR)))
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
		else
		{
      if(!getCursor().equals(new Cursor(Cursor.DEFAULT_CURSOR)))
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

  /**
   * Release any ressources used by this component.
   */
  public void dispose()
  {
    _terminated=true;
  }

  private void scrollTitle(int deltaX)
  {
    synchronized(_scrollLock)
    {

      if(_drawResult.rectangle==null) return;
      _titleLeft-=deltaX;
      /**--- WRAP BEHAVIOUR ---**/
      int min=-_drawResult.rectangle.width;
      int max=getSize().width;
      if(_titleLeft>max) _titleLeft=min;
      if(_titleLeft<min) _titleLeft=max;

      /**--- CLAMP BEHAVIOUR ---**/
      /*int min=-_drawResult.rectangle.width;
      int max=0;
      if(_titleLeft>max) _titleLeft=max;
      if(_titleLeft<min) _titleLeft=min;*/



      _redrawTitle=true;
      repaint();
    }
  }

	public void mouseDragged(MouseEvent e)
	{
	  if(!_mouseScroll) return;
    if(_drawResult.rectangle==null) return;
    int deltaX=_mouseDownX-e.getX();
    scrollTitle(deltaX);
    _freeze=true;
		_mouseDownX=e.getX();
	}

  public void run()
  {
    while(!_terminated)
    {
      if(!_freeze) scrollTitle(4);
      try
      {
        if(_freeze)
        {
          Thread.sleep(2000);
          _freeze=false;
        }
        else
          Thread.sleep(_scrollDelay);
      }
      catch(InterruptedException ex)
      {
      }
    }
  }

  public Boolean displayUpdated(Object handle,Integer what)
  {
    if(_drawResult==null) return Boolean.FALSE;
    for(int i=0;i<_drawResult.updateHandles.size();i++)
    {
      if(_drawResult.updateHandles.elementAt(i)==handle)
      {
        _redrawTitle=true;
        repaint();
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }
}
