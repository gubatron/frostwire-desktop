package irc.gui.pixx;

import irc.EventDispatcher;
import irc.ListenerGroup;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * TaskBarItem.
 */
class TaskBarItem
{
  /**
   * Create a new TaskBarItem
   * @param src source.
   * @param arow row.
   * @param abring if should be brang.
   */
  public TaskBarItem(BaseAWTSource src,int arow,boolean abring)
  {
    source=src;
    eventWaiting=false;
    this.row=arow;
    this.bring=abring;
    visible=true;
  }

  /**
   * Source.
   */
  public BaseAWTSource source;
  /**
   * Row.
   */
  public int row;
  /**
   * True if some event is waiting.
   */
  public boolean eventWaiting;
  /**
   * True if source must be brang.
   */
  public boolean bring;
  /**
   * Source z-order.
   */
	public int zorder;
  /**
   * True if source is visible in the taskbar.
   */
  public boolean visible;
}

/**
 * The task bar.
 */
public class PixxTaskBar extends PixxPanel implements MouseListener,MouseMotionListener,BaseAWTSourceListener
{
  /**
     * 
     */
    private static final long serialVersionUID = -9081050062448414641L;

private ListenerGroup _listeners;

  private TaskBarItem _active;
  private TaskBarItem _pressed;
  private Vector<TaskBarItem> _items;

  private int[] _itemCount;
  private int[] _visibleItemCount;
  private Font _font;
  private Image _buffer;
  private int _iwidth;
  private int _ileft;
	private int _zorder;
	private boolean _handCursor;
	private int _overX;
	private int _overY;
	private int _maxWidth;

  /**
   * Create a new PixxTaskBar.
   * @param config global irc configuration.
   */
  public PixxTaskBar(PixxConfiguration config)
  {
    super(config);
    _font=new Font("",0,12);
    _listeners=new ListenerGroup();
    _active=null;
    _pressed=null;
    _items=new Vector<TaskBarItem>();
    _itemCount=new int[2];
    _visibleItemCount=new int[2];
    _itemCount[0]=0;
    _itemCount[1]=0;
    _visibleItemCount[0]=0;
    _visibleItemCount[1]=0;
    _ileft=60;
		_zorder=0;
		_handCursor=false;
		_overX=-1;
		_overY=-1;
    computeWidth();

    addMouseListener(this);
    addMouseMotionListener(this);
    _maxWidth=config.getI("taskbaritemwidth");
  }

  public void release()
  {
    _buffer=null;
    removeMouseMotionListener(this);
    removeMouseListener(this);
    super.release();
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addPixxTaskBarListener(PixxTaskBarListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removePixxTaskBarListener(PixxTaskBarListener lis)
  {
    _listeners.removeListener(lis);
  }

  private TaskBarItem findItem(BaseAWTSource source)
  {
    if(source==null) return null;
    for(int i=0;i<_items.size();i++) if(((TaskBarItem)_items.elementAt(i)).source==source) return (TaskBarItem)_items.elementAt(i);
    return null;
  }

  private void removeFromVector(Vector<TaskBarItem> v,Object o)
  {
    for(int i=0;i<v.size();i++) if(v.elementAt(i)==o) v.removeElementAt(i);
  }

  private synchronized void enter(BaseAWTSource source,int row,boolean bring)
  {
    TaskBarItem item=new TaskBarItem(source,row,bring);
    source.addBaseAWTSourceListener(this);
    _items.insertElementAt(item,_items.size());
    _itemCount[row]++;
    _visibleItemCount[row]++;
    _listeners.sendEvent("AWTSourceAdded",this,source);
    if(bring) activate(source);
    _buffer=null;
    repaint();
  }

  private synchronized void leave(BaseAWTSource source,int row)
  {
    TaskBarItem item=findItem(source);
    source.removeBaseAWTSourceListener(this);
    _itemCount[row]--;
    _visibleItemCount[row]--;
    boolean change=getActive()==source;
    removeFromVector(_items,item);
    if(change) activate(null);
    _listeners.sendEvent("AWTSourceRemoved",this,source);
    _buffer=null;
    repaint();
  }

  /**
   * Get icons count in this task bar.
   * @return icons count.
   */
  public int getCount()
  {
    return _itemCount[0]+_itemCount[1];
  }

  /**
   * Add a new channel in the taskbar.
   * @param chan channel to add.
   * @param bring true if this new channel must be made the active awt source.
   */
  public void addChannel(AWTChannel chan,boolean bring)
  {
    enter(chan,0,bring);
  }

  /**
   * Remove the given channel from the taskbar.
   * @param chan channel to remove.
   */
  public void removeChannel(AWTChannel chan)
  {
    leave(chan,0);
  }

  /**
   * Add a new status in the taskbar.
   * @param status status to add.
   * @param bring true if this new status must be made the active awt source.
   */
  public void addStatus(AWTStatus status,boolean bring)
  {
    enter(status,1,bring);
  }

  /**
   * Remove the given status from the taskbar.
   * @param status status to remove.
   */
  public void removeStatus(AWTStatus status)
  {
    leave(status,1);
  }

  /**
   * Add a new default source in the taskbar.
   * @param source default source to add.
   * @param bring true if this new default source must be made the active awt source.
   */
  public void addDefaultSource(AWTDefaultSource source,boolean bring)
  {
    enter(source,1,bring);
  }

  /**
   * Remove the given default source from the taskbar.
   * @param source default sourceto remove.
   */
  public void removeDefaultSource(AWTDefaultSource source)
  {
    leave(source,1);
  }

  /**
   * Add a new query in the taskbar.
   * @param query query to add.
   * @param bring true if this new query must be made the active awt source.
   */
  public void addQuery(AWTQuery query,boolean bring)
  {
    enter(query,1,bring);
  }

  /**
   * Remove the given query from the taskbar.
   * @param query query to remove.
   */
  public void removeQuery(AWTQuery query)
  {
    leave(query,1);
  }

  /**
   * Add a new chanlist in the taskbar.
   * @param chanlist chanlist to add.
   * @param bring true if this new chanlist must be made the active awt source.
   */
  public void addChanList(AWTChanList chanlist,boolean bring)
  {
    enter(chanlist,1,bring);
  }

  /**
   * Remove the given chanlist from the taskbar.
   * @param chanlist chanlist to remove.
   */
  public void removeChanList(AWTChanList chanlist)
  {
    leave(chanlist,1);
  }

  /**
   * Add a new dcc chat in the taskbar.
   * @param chat dcc chat to add.
   * @param bring true if this new dcc chat must be made the active awt source.
   */
  public void addDCCChat(AWTDCCChat chat,boolean bring)
  {
    enter(chat,1,bring);
  }

  /**
   * Remove the given dcc chat from the taskbar.
   * @param chat dcc chat to remove.
   */
  public void removeDCCChat(AWTDCCChat chat)
  {
    leave(chat,1);
  }


  private BaseAWTSource findFirst()
  {
    TaskBarItem first=null;
    int maxz=-1;
    for(int i=0;i<_items.size();i++)
    {
      TaskBarItem item=(TaskBarItem)_items.elementAt(i);
      if(item.zorder>maxz)
      {
        maxz=item.zorder;
        first=item;
      }
    }
    if(first==null) return null;
    return first.source;
  }

  /**
   * Get all sources sorted by their z-order.
   * @return z-ordered sources.
   */
  public BaseAWTSource[] getZOrderedSources()
  {
    TaskBarItem[] items=new TaskBarItem[_items.size()];
    for(int i=0;i<items.length;i++) items[i]=(TaskBarItem)_items.elementAt(i);

    for(int i=0;i<items.length-1;i++)
    {
      TaskBarItem item=items[i];
      int max=item.zorder;
      int maxIndex=i;
      for(int j=i+1;j<items.length;j++)
      {
        item=items[j];
        if(item.zorder>max)
        {
          max=item.zorder;
          maxIndex=j;
        }
      }
      TaskBarItem tmp=items[i];
      items[i]=items[maxIndex];
      items[maxIndex]=tmp;
    }

    BaseAWTSource[] ans=new BaseAWTSource[items.length];
    for(int i=0;i<ans.length;i++) ans[i]=items[i].source;
    return ans;
  }
  
  /**
   * Make the given source visible in the taskbar.
   * @param source the source that should be visible.
   */
  public void show(BaseAWTSource source)
  {
    if(source==null) return;
    TaskBarItem item=findItem(source);
    if(item.visible) return;
    
    item.visible=true;
    _visibleItemCount[item.row]++;
    
    _buffer=null;
    repaint();
  }
  
  /**
   * Make the given source invisible in the taskbar.
   * @param source the source that should be invisible.
   */
  public void hide(BaseAWTSource source)
  {
    if(source==null) return;
    TaskBarItem item=findItem(source);
    if(!item.visible) return;

    item.visible=false;
    _visibleItemCount[item.row]--;

    _buffer=null;
    repaint();
  }

  /**
   * Activate the given source.
   * @param source the source to activate.
   */
  public void activate(BaseAWTSource source)
  {
    if(source==null) source=findFirst();
    if(source==null) return;
    TaskBarItem item=findItem(source);
    if(item==_active) return;
    if(!item.visible) return;
    if(_active!=null) _listeners.sendEvent("AWTSourceDesactivated",this,_active.source);

    _active=item;
    if(_active!=null)
    {
		  _active.zorder=_zorder++;
      _active.eventWaiting=false;
      _listeners.sendEvent("AWTSourceActivated",this,_active.source);
    }
    else
    {
      _listeners.sendEvent("AWTSourceActivated",this,null);
    }
    source.requestFocus();
    _buffer=null;
    repaint();
  }

  /**
   * Get the current active source.
   * @return active source, or null if there is no active source.
   */
  public BaseAWTSource getActive()
  {
    if(_active==null) return null;
    return _active.source;
  }

  public void paint(Graphics g)
  {
    update(g);
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(16,2*getItemHeight()+16);
  }

  private int getItemWidth()
  {
    return _iwidth;
  //  return 100;
  }

  private int getItemHeight()
  {
    return _font.getSize()+4;
  }

  private void computeWidth()
  {
    int w=getSize().width-63;
    int n=Math.max(_visibleItemCount[0],_visibleItemCount[1]);
    
    w-=9*n;
    if(n!=0)
      _iwidth=Math.min(_maxWidth,w/n);
    else
      _iwidth=_maxWidth;
  }

  private int getX(int col)
  {
    return col*(getItemWidth()+9)+_ileft;
  }

  private int getCol(int x)
  {
    return (x-_ileft)/(9+getItemWidth());
  }

  private int getY(int row)
  {
    return 4+(getItemHeight()+8)*row;
  }

  private int getRow(int y)
  {
    return (y-4)/(8+getItemHeight());
  }



  private void drawItem(Graphics g,int col,int row,Color c,String s)
  {
    int x=getX(col);
    int y=getY(row);
    int w=getItemWidth();
    int h=getItemHeight();
    g.setClip(x+1,y+1,w-1,h-1);
    g.setColor(c);
    g.fillRect(x,y,w,h);
    g.setColor(getColor(COLOR_BLACK));
    g.drawRect(x,y,w,h);
    g.setColor(getColor(COLOR_WHITE));
    g.drawRect(x+1,y+1,w-2,h-2);
    y+=h;
    int strw=g.getFontMetrics().stringWidth(s);
    y-=(h-_font.getSize())/2;
    g.drawString(s,x+(w-strw)/2,y-1);
    g.setClip(0,0,getSize().width,getSize().height);
  }

  private void drawItem(Graphics g,TaskBarItem item,int col)
  {
    int row=item.row;
    Color c=getColor(COLOR_FRONT);
    if((item==_active) || (item==_pressed)) c=getColor(COLOR_SELECTED);
    if((item!=_active) && (item.eventWaiting)) c=getColor(COLOR_EVENT);
    drawItem(g,col++,row,c,item.source.getShortTitle());
  }

  public void update(Graphics ug)
  {
    //int col=0;
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

      g.setFont(new Font("",Font.PLAIN,12));
      int sw=Math.max(g.getFontMetrics().stringWidth("Private Msg's   "),g.getFontMetrics().stringWidth("Chat Rooms"));
      _ileft=25+sw;
      computeWidth();

   //   g.setColor(new Color(0x084079));
      g.setColor(getColor(COLOR_BACK));
      g.fillRect(0,0,w,h);
      int col0=0;
      int col1=0;
      Enumeration<TaskBarItem> el=_items.elements();
      while(el.hasMoreElements())
      {
        TaskBarItem item=(TaskBarItem)el.nextElement();
        if(item.visible)
        {
          if(item.row==0)
            drawItem(g,item,col0++);
          else
            drawItem(g,item,col1++);
        }

      }

   //   g.setColor(new Color(0x336699));
      g.setColor(getColor(COLOR_FRONT));
      g.fillRect(4,5,sw+2,h-9);

      for(int y=3;y<h/4;y++)
      {
        g.drawLine(sw+3+y,y+3,sw+3+y,h/2+2-y);
        g.drawLine(sw+3+y,h-3-y,sw+3+y,h/2-1+y);
      }


   //   g.setColor(Color.black);
      g.setColor(getColor(COLOR_BLACK));
      g.drawLine(4,h/2-1,w-1,h/2-1);
      g.drawLine(4,h/2+1,w-1,h/2+1);
//      g.setColor(Color.white);
      g.setColor(getColor(COLOR_WHITE));
      g.drawLine(4,h/2,w-1,h/2);

      int y=getY(0)+getItemHeight();
      y-=(getItemHeight()-_font.getSize())/2;

      g.drawString("Chat Rooms",8,y+1);

      y=getY(1)+getItemHeight();
      y-=(getItemHeight()-_font.getSize())/2;

      g.drawString("Private Msg's   ",8,y-4);

   //   g.setColor(Color.black);
      g.setColor(getColor(COLOR_BLACK));
      g.drawLine(4,5,4,h-5);

      g.drawLine(4,5,sw+6,5);
      g.drawLine(4,h-5,sw+6,h-5);

      g.drawLine(sw+6,5,sw+3+h/4,h/4+2);
      g.drawLine(sw+3+h/4,h/4+2,sw+6,h/2-1);

      g.drawLine(sw+6,h-5,sw+3+h/4,h-1-h/4-1);
      g.drawLine(sw+3+h/4,h-h/4-2,sw+6,h-3-h/2+4);


   //   g.setColor(Color.white);
      g.setColor(getColor(COLOR_WHITE));
      g.drawLine(5,6,5,h-6);

      g.drawLine(5,6,sw+5,6);
      g.drawLine(5,h-6,sw+5,h-6);

      g.drawLine(sw+6,6,sw+2+h/4,h/4+2);
      g.drawLine(sw+2+h/4,h/4+2,sw+5,h/2-1);

      g.drawLine(sw+6,h-6,sw+2+h/4,h-1-h/4-1);
      g.drawLine(sw+2+h/4,h-2-h/4,sw+5,h-1-h/2+2);

    }

    if(_buffer!=null) ug.drawImage(_buffer,0,0,this);
    TaskBarItem item=getItemAt(_overX,_overY);
    if(item!=null)
    {
      String text=item.source.getShortTitle();
      int tw=ug.getFontMetrics().stringWidth(text);
      int x=getX(getCol(_overX))+(getItemWidth()-tw)/2-2;
      int y=getY(getRow(_overY))-(getItemHeight()-_font.getSize())/2-1;

      int fh=getItemHeight();
      if(text.length()>0)
      {
        if(tw>=getItemWidth())
        {

          if(x+tw+5>=w) x=w-tw-5;
          if(x<0) x=0;
          ug.setColor(getAlphaColor(getColor(COLOR_FRONT),200));
          ug.fillRect(x,y,tw+4,fh+4);
          ug.setColor(getColor(COLOR_WHITE));
          ug.drawRect(x,y,tw+4,fh+4);
          ug.drawString(text,x+2,y+fh);
        }
      }
    }
  }

  private Color getAlphaColor(Color c,int alpha)
  {
    try
    {
      return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
    }
    catch(Throwable ex)
    {
      return c;
    }
  }

  private TaskBarItem getItemAt(int x,int y)
  {
    int row=getRow(y);
    int col=getCol(x);

    x-=getX(col);
    y-=getY(row);
    if((x>=getItemWidth()) || (y>=getItemHeight())) return null;
    if((x<0) || (y<0)) return null;

    //prentre de la ligne row
    int currentcol=0;
    Enumeration<TaskBarItem> el=_items.elements();
    while(el.hasMoreElements())
    {
      TaskBarItem item=(TaskBarItem)el.nextElement();
      if(item.visible)
      {
        if(item.row==row)
        {
          if(currentcol==col) return item;
          currentcol++;
        }
      }
    }
    return null;
  }



  public void mouseMoved(MouseEvent e)
  {
    TaskBarItem item=getItemAt(e.getX(),e.getY());
    if(item!=null)
    {
      if(!_handCursor)
      {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        _handCursor=true;
      }
    }
    else
    {
      if(_handCursor)
      {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        _handCursor=false;
      }
    }
    TaskBarItem oldItem=getItemAt(_overX,_overY);
    if(oldItem!=item)
    {
      _overX=e.getX();
      _overY=e.getY();
      repaint();
    }
  }

  public void mouseDragged(MouseEvent e)
  {
    mouseMoved(e);
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
    mouseMoved(e);
  }

  public void mousePressed(MouseEvent e)
  {
    TaskBarItem np=getItemAt(e.getX(),e.getY());
    _pressed=np;
    _buffer=null;
    repaint();
    if(_pressed!=null) _listeners.sendEventAsync("eventOccured",this,_pressed.source,e);
  }

  public void mouseReleased(MouseEvent e)
  {
    _pressed=null;
    TaskBarItem src=getItemAt(e.getX(),e.getY());
    try
    {
      if((e.getModifiers() & InputEvent.BUTTON1_MASK)!=0) if(src!=null) EventDispatcher.dispatchEventAsyncAndWaitEx(this,"activate",new Object[] {src.source});
    }
    catch(InterruptedException ex)
    {
    }
    catch(Throwable ex)
    {
    }

    _buffer=null;
    repaint();
  }

  public void titleChanged(BaseAWTSource source)
  {
    _buffer=null;
    repaint();
  }

  public void eventOccured(BaseAWTSource source)
  {
    TaskBarItem item=findItem(source);
    if(item==null) return;
    if(item==_active) return;
    item.eventWaiting=true;
    _buffer=null;
    repaint();
  }

}

