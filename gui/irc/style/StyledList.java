package irc.style;

import irc.IRCConfiguration;
import irc.ListenerGroup;
import irc.StyleContext;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * LimitedArray, used to remove too old lines from the text history.
 */
class LimitedArray
{
  private int _size;
  private int _maximum;
  private int _missing;
  private Object[] _array;

  /**
   * Create a new LimitedArray
   * @param max maximum number of items.
   */
  public LimitedArray(int max)
  {
    _size=0;
    _missing=0;
    _maximum=max;
    _array=new Object[4];
  }

  /**
   * Expand the actual number of stockable items.
   */
  public void expand()
  {
    if(_array.length>=_maximum) return;
    int ns=_array.length<<1;
    Object[] n=new Object[ns];
    System.arraycopy(_array,0,n,0,_array.length);
    _array=n;
  }

  /**
   * Add an object.
   * @param obj object to add.
   */
  public void add(Object obj)
  {
    if(size()>=_array.length) expand();
    if(size()>=_array.length) _missing++;
    _array[size()%_array.length]=obj;
    _size++;
  }

  /**
   * Get the object at given index.
   * @param index index.
   * @return the object at index, or null if object has been removed.
   */
  public Object get(int index)
  {
    if(index<_missing) return null;
    return _array[(index)%_array.length];
  }

  /**
   * Get the number of objects.
   * @return number of objects.
   */
  public int size()
  {
    return _size;
  }
}

/**
 * ResultPair.
 */
class ResultPair
{
  /**
   * Line count.
   */
  public int line;
  /**
   * Draw result.
   */
  public DrawResult result;
}

/**
 * A styled list is a panel designed for displaying lines of styled text, using
 * special color and style codes, and smileys.
 */
public class StyledList extends JPanel implements MouseListener,MouseMotionListener,FormattedStringDrawerListener
{
  private LimitedArray _list;
  private Hashtable _nickInfos;
  private boolean _wrap;
  private int _last;
  private int _first;
  private int _left;
  private int _width;
  private int _toScrollX;
  private int _toScrollY;
  private FormattedStringDrawer _drawer;
  private Image _buffer;
  private int _bufferWidth;
  private int _bufferHeight;
  private int _lastWidth;
  private int _lastHeight;
  private Hashtable _results;
  private MultipleWordCatcher _catcher;
  private WordListRecognizer _wordListRecognizer;
  private IRCConfiguration _ircConfiguration;

  private int _pressedX;
  private int _pressedY;
  private int _draggedX;
  private int _draggedY;
  private boolean _dragging;
  private DrawResultItem _currentItem;
  private DrawResultItem _currentFloatItem;
  private DrawResultItem _currentHighLightItem;
  private String _currentFloatText;
  private String _copiedString;
  private boolean _fullDraw;

  private ListenerGroup _listeners;

  private ResultPair[] _addedResults;
  private int _addedCount;

  private int _hdirection;
  private int _vdirection;
  private Color _colormale;
  private Color _colorfemeale;
  private Color _colorundef;

  private Vector _updateItems;
  private long _lastRefresh=System.currentTimeMillis();

  private Image _backImage;
  private int _backTiling;

  private int _maximumSize;
  private DecodedLine _emptyLine;

  private final static int BOTTOM=FormattedStringDrawer.BOTTOM;
  private final static int TOP=FormattedStringDrawer.TOP;
  /**Left to right direction.*/
  public final static int LEFT=FormattedStringDrawer.LEFT;
  /**Right to left direction.*/
  public final static int RIGHT=FormattedStringDrawer.RIGHT;

  private final static boolean _doubleBuffer=true; //NON-BUFFERED DISPLAY NOT YET FUNCTIONAL

  /**
   * Create a new StyledList with automatic text wraping.
   * @param config global irc configuration.
   * @param context style context.
   */
  public StyledList(IRCConfiguration config,StyleContext context)
  {
    this(config,true,context);
  }

  /**
   * Create a new StyledList.
   * @param config global irc configuration.
   * @param wrap true if wrapping must occur, false otherwise.
   * @param context style context.
   */
  public StyledList(IRCConfiguration config,boolean wrap,StyleContext context)
  {
    this(config,wrap,context,Color.blue,Color.pink,Color.gray);
  }

  /**
   * Create a new StyledList.
   * @param config global irc configuration.
   * @param wrap true if wrapping must occur, false otherwise.
   * @param context style context.
   * @param male male color for asl.
   * @param femeale femeale color for asl.
   * @param undef undefined gender color for asl.
   */
  public StyledList(IRCConfiguration config,boolean wrap,StyleContext context,Color male,Color femeale,Color undef)
  {
    super();
    _backImage=null;
    _backTiling=IRCConfiguration.TILING_CENTER;
    _colormale=male;
    _colorfemeale=femeale;
    _colorundef=undef;
    _nickInfos=new Hashtable();
    _fullDraw=false;
    _addedResults=new ResultPair[64];
    for(int i=0;i<_addedResults.length;i++) _addedResults[i]=new ResultPair();
    _addedCount=0;
    _hdirection=LEFT;
    _vdirection=BOTTOM;
    _ircConfiguration=config;
    _copiedString="";
    _dragging=false;
    _currentFloatItem=null;
    _currentFloatText=null;
    _currentItem=null;
    _toScrollX=0;
    _toScrollY=0;
    _left=0;
    _wrap=wrap;
    _buffer=null;
    _drawer=new FormattedStringDrawer(_ircConfiguration,context,this);
    _drawer.setHorizontalDirection(_hdirection);
    _drawer.setVerticalDirection(_vdirection);
    _catcher=new MultipleWordCatcher();
    _wordListRecognizer=new WordListRecognizer();
    _catcher.addRecognizer(new ChannelRecognizer());
    _catcher.addRecognizer(new URLRecognizer());
    _catcher.addRecognizer(_wordListRecognizer);
    _results=new Hashtable();
    _listeners=new ListenerGroup();
    addMouseListener(this);
    addMouseMotionListener(this);
    _maximumSize=_ircConfiguration.getI("style:maximumlinecount");
    _emptyLine=_drawer.decodeLine("");
    clear();
    setBackgroundImage(_ircConfiguration.getStyleBackgroundImage(context));
    setBackgroundTiling(_ircConfiguration.getStyleBackgroundTiling(context));
    if(_ircConfiguration.getB("style:righttoleft")) setHorizontalDirection(RIGHT);
  }

  /**
   * Release this object. No further call may be performed on this object.
   */
  public void release()
  {
    clear();
    dispose();
    removeMouseListener(this);
    removeMouseMotionListener(this);
  }

  private void drawBackImage(Graphics g,int w,int h)
  {
    int iw=_backImage.getWidth(this);
    int ih=_backImage.getHeight(this);
    switch(_backTiling&0xff)
    {
      case IRCConfiguration.TILING_FIXED:
      {
        int x=0;
        int y=0;
        if((_backTiling&IRCConfiguration.TILING_HORIZONTAL_RIGHT)!=0) x=w-iw-1;
        if((_backTiling&IRCConfiguration.TILING_VERTICAL_DOWN)!=0) y=h-ih-1;
        g.setColor(_drawer.getColor(0));
        g.fillRect(0,0,w,h);
        g.drawImage(_backImage,x,y,_drawer.getColor(0),this);
        break;
      }
      case IRCConfiguration.TILING_CENTER:
      {
        int x=(w-iw)/2;
        int y=(h-ih)/2;
        g.setColor(_drawer.getColor(0));
        g.fillRect(0,0,w,h);
        g.drawImage(_backImage,x,y,_drawer.getColor(0),this);
        break;
      }
      case IRCConfiguration.TILING_STRETCH:
      {
        g.drawImage(_backImage,0,0,w,h,_drawer.getColor(0),this);
        break;
      }
      case IRCConfiguration.TILING_TILE:
      {
        int x=0;
        while(x<w)
        {
          int y=0;
          while(y<h)
          {
            g.drawImage(_backImage,x,y,_drawer.getColor(0),this);
            y+=ih;
          }
          x+=iw;
        }
        break;
      }
    }
  }

  private void expandResult()
  {
    ResultPair[] n=new ResultPair[_addedResults.length*2];
    System.arraycopy(_addedResults,0,n,0,_addedResults.length);
    for(int i=_addedResults.length;i<n.length;i++) n[i]=new ResultPair();
    _addedResults=n;
  }

  /**
   * Set the horizontal display direction.
   * @param direction horizontal display direction.
   */
  public void setHorizontalDirection(int direction)
  {
    _hdirection=direction;
    _drawer.setHorizontalDirection(_hdirection);
  }

  /**
   * Get the horizontal display direction.
   * @return horizontal display direction.
   */
  public int getHorizontalDirection()
  {
    return _hdirection;
  }

  /**
   * Set the background image for display.
   * @param img background image, or null if no background image is to be displayed.
   */
  public void setBackgroundImage(Image img)
  {
    _backImage=img;
    repaint();
  }

  /**
   * Set the background image tiling.
   * @param t background image tiling mode. See IRCConfiguration for tiling modes.
   */
  public void setBackgroundTiling(int t)
  {
    _backTiling=t;
    repaint();
  }

  /**
   * Set the font to be used for display.
   * @param fnt font to be used.
   */
    public void setFont(Font fnt) {
        if (_drawer != null) {
            _drawer.setFont(fnt);
            reinit();
            repaint();
        }
    }

  /**
   * Set the wrap mode.
   * @param wrap mode, true if end-of-line wrapping must be performed, false otherwise.
   */
  public void setWrap(boolean wrap)
  {
    _wrap=wrap;
    reinit();
    repaint();
  }

	/**
	 * Set the nick list for recognition.
	 * @param list the nick list.
	 */
  public synchronized void setNickList(String[] list)
  {
    String[] actualList=new String[list.length];
    _nickInfos.clear();
    for(int i=0;i<list.length;i++)
    {
      String nick=list[i];
      String info="";
      int pos=nick.indexOf(":");
      if(pos!=-1)
      {
        info=nick.substring(pos+1);
        nick=nick.substring(0,pos);
      }
      actualList[i]=nick;
      _nickInfos.put(nick.toLowerCase(java.util.Locale.ENGLISH),info);
    }
    _wordListRecognizer.setList(actualList);
  }

  /**
   * Add a listener.
   * @param lis the new listener.
   */
  public synchronized void addStyledListListener(StyledListListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis the listener to remove.
   */
  public synchronized void removeStyledListListener(StyledListListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Set the left offset for this list rendering.
   * @param left the left offset, in pixel.
   */
  public synchronized void setLeft(int left)
  {
	  //int w=getSize().width;
    int oldLeft=_left;
    _left=left;
    if(_left<0) _left=0;
    if(_left>=getLogicalWidth()) _left=getLogicalWidth()-1;

    if(_hdirection==RIGHT) _left=-_left;

    if(_left!=oldLeft)
    {
      addToScroll(_left-oldLeft,0);
      repaint();
    }
  }

  /**
   * Get the left offset.
   * @return the left offset, in pixel.
   */
  public int getLeft()
  {
    if(_hdirection==RIGHT) return -_left;
    return _left;
  }

  /**
   * Set the first line to be displayed.
   * @param first the first line to be displayed.
   */
	public synchronized void setFirst(int first)
	{
	 if(_vdirection!=TOP) _fullDraw=true;
    _vdirection=TOP;
    _drawer.setVerticalDirection(TOP);
    int oldFirst=_first;
    _first=first;
    if(_first<0) _last=0;
    if(_first>=_list.size()) _last=_list.size()-1;
    if(_first!=oldFirst)
    {
      addToScroll(0,_first-oldFirst);
      repaint();
    }
	}

  /**
   * Set the last line to be displayed.
   * @param last last line to be displayed.
   */
  public synchronized void setLast(int last)
  {
    if(_vdirection!=BOTTOM) _fullDraw=true;
    _vdirection=BOTTOM;
    _drawer.setVerticalDirection(BOTTOM);
    int oldLast=_last;
    _last=last;
    if(_last<0) _last=0;
    if(_last>=_list.size()) _last=_list.size()-1;
    if(_last!=oldLast)
    {
      addToScroll(0,_last-oldLast);
      repaint();
    }
  }

  /**
   * Get the logical width of this list.
   * @return the logical width, in pixel.
   */
	public int getLogicalWidth()
	{
	  return _width;
	}

  /**
   * Get the last displayed line.
   * @return the last displayed line.
   */
  public int getLast()
  {
    return _last;
  }

  /**
   * Get the number of line in this list.
   * @return the line count.
   */
  public synchronized int getLineCount()
  {
    return _list.size();
  }

  /**
   * Add a line at the end of this list.
   * @param line new line to add.
   */
  public synchronized void addLine(String line)
  {
    DecodedLine dline=_drawer.decodeLine(line);
    _list.add(dline);

    if(_vdirection==BOTTOM)
    {
      if(_last==_list.size()-2) setLast(_last+1);
    }
    else if(_vdirection==TOP)
    {
      _fullDraw=true;
      repaint();
    }
  }

  /**
   * Add the given lines at the end of this list.
   * @param lines lines to add.
   */
  public synchronized void addLines(String[] lines)
  {
    boolean willScroll=(_list.size()-1==_last);

    for(int i=0;i<lines.length;i++) _list.add(_drawer.decodeLine(lines[i]));

    if(_vdirection==BOTTOM)
    {
      if(willScroll) setLast(_list.size()-1);
    }
    else if(_vdirection==TOP)
    {
      _fullDraw=true;
      repaint();
    }
  }

  private void reinit()
  {
    if(_buffer!=null) _buffer.flush();
    _buffer=null;
    _results=new Hashtable();
  }

  /**
   * Dispose any off-screen ressources used by the list. This method won't put the
   * list in a non-drawable state, but next screen refresh might me slower.
   */
  public synchronized void dispose()
  {
    reinit();
  }

  /**
   * Clear all the lines in this list.
   */
  public synchronized void clear()
  {
    _list=new LimitedArray(_maximumSize);
    _last=_list.size()-1;
    _first=0;
    setLeft(0);
		_width=getSize().width;
		_fullDraw=true;
    repaint();
  }

  /**
   * Clear all the lines in this list, reconfiguring the maximum line count to max.
   * @param max the new maximum line count.
   */
  public synchronized void clear(int max)
  {
    _maximumSize=max;
    clear();
  }
  
  private void drawPart(Graphics g,int x,int y,int w,int h,boolean analyse,int gw,int gh)
  {
    //System.out.println("draw part "+x+","+y+","+w+","+h);
    if(y<0)
    {
      h+=y;
      y=0;
    }

    if(_backImage!=null)
    {
      drawBackImage(g,gw,gh);
    }
    else
    {
      g.setColor(_drawer.getColor(0));
      g.fillRect(x,y,w,h);
    }

    if(_vdirection==BOTTOM)
    {
      int first=_last;
      int posY=getSize().height;
      while((posY>y+h) && (first>=0)) posY-=getHeight(first--,g);
      if(first!=_last) posY+=getHeight(++first,g);
      draw(g,0,first,posY,y,x,x+w-1,analyse);
    }
    else if(_vdirection==TOP)
    {
      int first=_first;
      int posY=0;
      while((posY<y) && (first<_list.size())) posY+=getHeight(first++,g);
      if(first!=_first) posY-=getHeight(--first,g);
      draw(g,first,_list.size()-1,posY,y+h,x,x+w-1,analyse);
    }
  }

  public synchronized void paint(Graphics g)
  {
    if(_doubleBuffer || (_toScrollX!=0) || (_toScrollY!=0))
    {
      if((_toScrollX!=0) || (_toScrollY!=0)) _fullDraw=true;
      update(g);
      return;
    }
    int x=0;
    int y=0;
    int w=getSize().width;
    int h=getSize().height;
    Rectangle cl=g.getClipBounds();
    if(cl!=null)
    {
      x=cl.x;
      y=cl.y;
      w=cl.width;
      h=cl.height;
    }
    drawPart(g,x,y,w,h,false,w,h);
  }

  private int getHeight(Graphics g,int a,int b)
  {
    if(b<a)
    {
      int tmp=a;
      a=b;
      b=tmp;
    }
    int res=0;
    for(int i=a;i<=b;i++) res+=getHeight(i,g);
    return res;
  }

  private void draw(Graphics g,int from,int to,int y,int crossy,int debx,int finx,boolean analyse)
  {
    int w=getSize().width;
    //int h=getSize().height;
    //int wrapPos=w;
    _addedCount=0;

    DrawResult res=new DrawResult();

    if(_vdirection==BOTTOM)
    {
      int index=to;
      while((index>=from) && (y>crossy))
      {
        DecodedLine str=(DecodedLine)_list.get(index);
        if(str==null) str=_emptyLine;
        _drawer.draw(str,g,-_left,w-1-_left,y,debx,finx,analyse,_wrap,res);
        StyledRectangle rect=res.rectangle;
        if(rect.width>_width)
        {
          _width=rect.width;
          _listeners.sendEventAsync("virtualSizeChanged",this);
        }
        if(analyse)
        {
          ResultPair p=_addedResults[_addedCount++];
          if(_addedCount==_addedResults.length) expandResult();
          p.line=index;
          p.result=res;
          res=new DrawResult();
        }
        y-=rect.height;
        index--;
      }
    }
    else
    {
      int index=from;
      while((index<=to) && (y<crossy))
      {
        DecodedLine str=(DecodedLine)_list.get(index);
        if(str==null) str=_emptyLine;
        _drawer.draw(str,g,-_left,w-1-_left,y,debx,finx,analyse,_wrap,res);
        StyledRectangle rect=res.rectangle;
        if(rect.width>_width)
        {
          _width=rect.width;
          _listeners.sendEventAsync("virtualSizeChanged",this);
        }
        if(analyse)
        {
          ResultPair p=_addedResults[_addedCount++];
          if(_addedCount==_addedResults.length) expandResult();
          p.line=index;
          p.result=res;
          res=new DrawResult();
        }
        y+=rect.height;
        index++;
      }

    }
  }

  private void addToScroll(int vx,int vy)
  {
    _toScrollX+=vx;
    _toScrollY+=vy;
  }

  private int getScrollX()
  {
    if(_dragging) return 0;
    int res=_toScrollX;
    _toScrollX=0;
    return res;
  }

  private int getScrollY()
  {
    if(_dragging) return 0;
    int res=_toScrollY;
    _toScrollY=0;
    return res;
  }

  private void scrollDrawItems(int dx,int dy)
  {
    int h=getSize().height;
    Enumeration e=_results.keys();
    while(e.hasMoreElements())
    {
      Integer key=(Integer)e.nextElement();
      DrawResult res=(DrawResult)_results.get(key);
      res.rectangle.x+=dx;
      res.rectangle.y+=dy;
      if((res.rectangle.y+res.rectangle.height<0) || (res.rectangle.y>=h))
      {
        _results.remove(key);
      }
    }
  }

  private void combineItems()
  {
    for(int i=0;i<_addedCount;i++)
    {
      ResultPair k=_addedResults[i];
      _results.put(new Integer(k.line),k.result);
    }
    _addedCount=0;
  }

  private DrawResultItem findItem(int x,int y)
  {
    Enumeration e=_results.elements();
    while(e.hasMoreElements())
    {
      DrawResult result=(DrawResult)e.nextElement();
      if(result.rectangle.contains(x,y))
      {
        int rx=x-result.rectangle.x;
        int ry=y-result.rectangle.y;
        for(int i=0;i<result.items.length;i++)
        {
          DrawResultItem item=result.items[i];
          if(item.rectangle.contains(rx,ry)) return item;
        }
      }
    }
    return null;
  }

  private int findLine(int y)
  {
    Enumeration e=_results.keys();
    while(e.hasMoreElements())
    {
      Integer i=(Integer)e.nextElement();

      DrawResult result=(DrawResult)_results.get(i);
      if((result.rectangle.y<=y) && (result.rectangle.y+result.rectangle.height>y))
      {
        return i.intValue();
      }
    }
    return -1;
  }

  private int getHeight(int lineIndex,Graphics g)
  {
    DrawResult r=(DrawResult)_results.get(new Integer(lineIndex));
    if(r!=null) return r.rectangle.height;

    int wrapPos=getSize().width;
    DecodedLine str=(DecodedLine)_list.get(lineIndex);
    if(str==null) str=_emptyLine;
    return _drawer.getHeight(str,g,-_left,wrapPos,_wrap);
  }

  private Color findColor(String info)
  {
    return _ircConfiguration.getASLColor(info,_colormale,_colorfemeale,_colorundef);
  }

  private synchronized Vector getUpdateItems()
  {
    Vector items=_updateItems;
    _updateItems=null;
    return items;
  }
  
  private synchronized boolean addToUpdateItems(Integer line)
  {
    if(_updateItems==null) _updateItems=new Vector();
    for(int i=0;i<_updateItems.size();i++)
    {
      Integer r=(Integer)_updateItems.elementAt(i);
      if(r.equals(line)) return false;
    }
    _updateItems.insertElementAt(line,_updateItems.size());
    return true;
  }

  public synchronized void update(Graphics g)
  {
    int w=getSize().width;
    int h=getSize().height;
    if(h<=0 || w<=0) return;
    
    Graphics gra=g;
    if(_doubleBuffer)
    {
      if(_buffer!=null)
      {
        if((_bufferWidth<w) || (_bufferHeight<h))
        {
          reinit();
        }
        //Optimize memory usage
        if((_bufferHeight>w*1.5) || (_bufferHeight>h*1.5))
        {
          reinit();
        }
        if((_lastWidth!=w) || (_lastHeight!=h))
        {
          _fullDraw=true;
        }
      }

      _lastWidth=w;
      _lastHeight=h;

      if(_buffer==null)
      {
        _buffer=createImage(w,h);
        if(_buffer==null)
        {
          repaint();
          return;
        }
        _bufferWidth=w;
        _bufferHeight=h;
        _fullDraw=true;
      }

      gra=_buffer.getGraphics();
    }

    if(_ircConfiguration.getB("style:backgroundimage")) _fullDraw=true;

    int scrx=getScrollX();
    int scry=getScrollY();
    Vector items=getUpdateItems();

    if(!_fullDraw)
    {
     
			if(scrx<0)
			{
			  gra.copyArea(0,0,w+scrx,h,-scrx,0);
        scrollDrawItems(-scrx,0);
        drawPart(gra,0,0,-scrx,h,false,w,h);
			}
 			else if(scrx>0)
 			{
        gra.copyArea(scrx,0,w-scrx,h,-scrx,0);
        scrollDrawItems(-scrx,0);
        drawPart(gra,w-scrx,0,scrx,h,false,w,h);
      }

      if(scry>0)
      {
        int baseY;
        if(_vdirection==BOTTOM)
          baseY=getHeight(gra,_last-scry+1,_last);
        else
          baseY=getHeight(gra,_first-scry,_first-1);


        gra.copyArea(0,baseY,w,h-baseY,0,-baseY);
        scrollDrawItems(0,-baseY);
        drawPart(gra,0,h-baseY,w,baseY,true,w,h);
        combineItems();
      }
      else if(scry<0)
      {
        int baseY;
        if(_vdirection==BOTTOM)
          baseY=getHeight(gra,_last+1,_last-scry);
        else
          baseY=getHeight(gra,_first,_first-scry-1);

        gra.copyArea(0,0,w,h-baseY,0,baseY);
        scrollDrawItems(0,baseY);
        drawPart(gra,0,0,w,baseY,true,w,h);
        combineItems();
      }
      
      if(items!=null)
      {
        for(int i=0;i<items.size();i++)
        {
          Integer line=(Integer)items.elementAt(i);
          DrawResult res=(DrawResult)_results.get(line);
          if(res!=null)
          {
            StyledRectangle r=res.rectangle;
            drawPart(gra,r.x,r.y,r.width,r.height,false,w,h);
          }
        }
      }
      
    }
    else
    {
      _results=new Hashtable();
      drawPart(gra,0,0,w,h,true,w,h);
      combineItems();
      _fullDraw=false;
    }

    if(_dragging) makeXor(gra);
    if(_doubleBuffer) g.drawImage(_buffer,0,0,this);
    if(_dragging) makeXor(gra);

    if(!_dragging && (_currentFloatItem!=null) && _ircConfiguration.getB("style:floatingasl"))
    {
      int x=_currentFloatItem.rectangle.x+_currentFloatItem.parent.rectangle.x+4;
      int y=_currentFloatItem.rectangle.y+_currentFloatItem.parent.rectangle.y;
      if(_vdirection==TOP) y+=8;
      else y-=8;

      if(y<0) y=0;

      String info=_currentFloatText;
      String text=_ircConfiguration.formatASL(info);
      if(text.length()>0)
      {
        int tw=g.getFontMetrics().stringWidth(text);
        int fh=g.getFont().getSize();

        if(y+fh+5>h) y=h-fh-5;
        if(x+tw+5>w) x=w-tw-5;
        g.setColor(getAlphaColor(findColor(info),_ircConfiguration.getI("style:floatingaslalpha")));
        g.fillRect(x,y,tw+4,fh+4);
        g.setColor(Color.white);
        g.drawString(text,x+2,y+fh);
      }
    }
    
    if(_ircConfiguration.getB("style:highlightlinks"))
    {
      if(!_dragging && (_currentHighLightItem!=null))
      {
        int x=_currentHighLightItem.rectangle.x+_currentHighLightItem.parent.rectangle.x;
        int y=_currentHighLightItem.rectangle.y+_currentHighLightItem.parent.rectangle.y;
        g.setXORMode(Color.white);
        g.setColor(Color.black);
        g.fillRect(x,y,_currentHighLightItem.rectangle.width,_currentHighLightItem.rectangle.height);
        g.setPaintMode();
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

  private void makeXor(Graphics g)
  {
    String res="";
    int dw=_draggedX-_pressedX;
    int dh=_draggedY-_pressedY;

    int pressedX=_pressedX;
    int pressedY=_pressedY;

    g.setXORMode(Color.white);
    g.setColor(Color.black);
    int i=findLine(pressedY);
    int basei=i;
    DrawResult result=(DrawResult)_results.get(new Integer(i));

    if(result==null)
    {
      _copiedString="";
      return;
    }

    int px,py;

    px=pressedX-result.rectangle.x;
    py=pressedY-result.rectangle.y;

    DrawResultItem item=null;
    int a,b=0;
    for(a=0;a<result.items.length;a++)
    {
      if(result.items[a].rectangle.contains(px,py))
      {
        item=result.items[a];
        b=a;
      }
    }

    if((item==null) || ((px+dw<item.rectangle.x) && (py+dh<item.rectangle.y)) || (py+dh<item.rectangle.y))
    {
      _copiedString="";
      return;
    }
    boolean terminated=false;
    while(!terminated)
    {
      res+=item.originalstrippedword;
      StyledRectangle r=item.rectangle;
      g.fillRect(r.x+result.rectangle.x,r.y+result.rectangle.y,r.width,r.height);

      if(!((i==basei)&&(a==b)) && (item.rectangle.contains(px+dw,py+dh))) break;
      b++;
      if(b>=result.items.length)
      {
        b=0;
        i++;
        result=(DrawResult)_results.get(new Integer(i));
        if(result==null) break;
        px=pressedX-result.rectangle.x;
        py=pressedY-result.rectangle.y;
        res+="\n";
      }
      item=result.items[b];
      if(item.rectangle.y>py+dh) terminated=true;
      if(_hdirection==LEFT) if((item.rectangle.x>px+dw) && (item.rectangle.y+item.rectangle.height>py+dh)) terminated=true;
      if(_hdirection==RIGHT) if((item.rectangle.x+item.rectangle.width<px+dw) && (item.rectangle.y+item.rectangle.height>py+dh)) terminated=true;
    }

    _copiedString=res;
    g.setPaintMode();
  }

  public synchronized void mouseClicked(MouseEvent e)
  {
    if((e.getModifiers()&InputEvent.SHIFT_MASK)!=0)
    {
      String res="";
      for(int i=0;i<_list.size();i++)
      {
        DecodedLine str=(DecodedLine)_list.get(i);
        if(str==null) str=_emptyLine;
        res+=str.original+"\n";
      }
      _listeners.sendEventAsync("copyEvent",this,res,e);
    }
  }

  public void mouseEntered(MouseEvent e)
  {
    _currentFloatItem=null;
    _currentItem=null;
    _currentHighLightItem=null;
    defCursor();
    mouseMoved(e);
  }

  public void mouseExited(MouseEvent e)
  {
    _currentFloatItem=null;
    _currentItem=null;
    _currentHighLightItem=null;
    repaint();
  }

  public synchronized void mousePressed(MouseEvent e)
  {
    _pressedX=e.getX();
    _pressedY=e.getY();
    _draggedX=_pressedX;
    _draggedY=_pressedY;
    _copiedString="";
    _dragging=false;
    _currentItem=null;
    DrawResultItem item=findItem(e.getX(),e.getY());
    if(item!=null)
    {
      String type=_catcher.getType(item.item);
      if(type==null)
      {
        //ignore...
      }
      else if(type.equals("channel"))
      {
        _listeners.sendEventAsync("channelEvent",this,item.item,e);
      }
      else if(type.equals("url"))
      {
        _listeners.sendEventAsync("URLEvent",this,item.item,e);
      }
      else if(type.equals("wordlist"))
      {
        _listeners.sendEventAsync("nickEvent",this,item.item,e);
      }
    }
  }

  public synchronized void mouseReleased(MouseEvent e)
  {
    if(_dragging)
    {
      _dragging=false;
      repaint();
      if(_copiedString.length()>0) _listeners.sendEventAsync("copyEvent",this,_copiedString,e);
    }
  }

  public synchronized void mouseDragged(MouseEvent e)
  {
    _draggedX=e.getX();
    _draggedY=e.getY();
    _dragging=true;
    DrawResultItem item=findItem(e.getX(),e.getY());
    if(item!=_currentItem)
    {
      _currentItem=item;
      repaint();
    }
  }

  private void handCursor()
  {
    if(!getCursor().equals(new Cursor(Cursor.HAND_CURSOR))) setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private void defCursor()
  {
    if(!getCursor().equals(new Cursor(Cursor.DEFAULT_CURSOR))) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private boolean sameItem(DrawResultItem a,DrawResultItem b)
  {
    if((a==null) && (b==null)) return true;
    if(a==null) return false;
    if(b==null) return false;
    return a.equals(b);
  }

  public synchronized void mouseMoved(MouseEvent e)
  {
    DrawResultItem item=findItem(e.getX(),e.getY());
    DrawResultItem oldFloat=_currentFloatItem;
    DrawResultItem oldHigh=_currentHighLightItem;
    if(!sameItem(item,_currentItem))
    {
      _currentItem=item;
      _currentFloatItem=null;
      _currentHighLightItem=null;
      if(item!=null)
      {
        String type=_catcher.getType(item.item);
        if(type!=null)
        {
          handCursor();
          if(type.equals("wordlist"))
          {
            String info=(String)_nickInfos.get(item.item.toLowerCase(java.util.Locale.ENGLISH));
            if(info==null) info="";
            if(info.length()==0)
            {
              _currentFloatItem=null;
            }
            else
            {
              _currentFloatItem=item;
              _currentFloatText=info;
            }
          }
          
          if(_currentFloatItem==null) _currentHighLightItem=item;
        }
        else defCursor();
      }
      else defCursor();
    }
    
    boolean repaint=false;
    if(!sameItem(oldFloat,_currentFloatItem)) if(_ircConfiguration.getB("style:floatingasl")) repaint=true;
    if(!sameItem(oldHigh,_currentHighLightItem)) repaint=true;
    if(repaint) repaint();
  }

  public boolean imageUpdate(Image img,int infoflags,int x,int y,int width,int height)
  {
    //the background image has been updated
    _fullDraw=true;
    repaint();
    return true;
  }

  public synchronized Boolean displayUpdated(Object handle,Integer what)
  {
    boolean foundSome=false;
    
    //now we should go through all our draw results, and find which of them belong to this handle.
    Enumeration e=_results.keys();
    while(e.hasMoreElements())
    {
      Integer line=(Integer)e.nextElement();
      DrawResult result=(DrawResult)_results.get(line);
      if(result.updateHandles!=null)
      {
        for(int i=0;i<result.updateHandles.size();i++)
        {
          if(result.updateHandles.elementAt(i).equals(handle))
          {
            if((what.intValue()&FormattedStringDrawerListener.SIZE)!=0)
            {
              _fullDraw=true;
              repaint();
              return Boolean.TRUE;
            }
            
            //ok, so the line number 'line' must be redrawn
            foundSome=true;
  
            //invalidate line line
            addToUpdateItems(line);
            if((System.currentTimeMillis()-_lastRefresh>10) || ((what.intValue()&FormattedStringDrawerListener.DATA)!=0))
            {
              repaint();
              _lastRefresh=System.currentTimeMillis();
            } 
          }
        }
      }
    }

    if(foundSome) return Boolean.TRUE;
    return Boolean.FALSE;    
  }
}
