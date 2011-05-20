package irc.gui.pixx;

import irc.ListenerGroup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

/**
 * The pixx nick list display.
 */
public class PixxNickList extends PixxPanel implements MouseListener,MouseMotionListener
{
  private Vector _nicks;
  private Image _buffer;
  private Font _font;
  private int _base;
  private int _selected;
	private int _overindex;
	private int _overX;
	private int _toScroll;
	private Object _scrollLock=new Object();
  private ListenerGroup _listeners;
  private char[] _prefixes;
  private boolean _leftAlign;

	/**
   * Create a new PixxNickList.
   * @param config global irc configuration.
   * @param prefixes known nick prefixes.
   */
  public PixxNickList(PixxConfiguration config,char[] prefixes)
  {
    super(config);
    _prefixes=prefixes;
    _toScroll=0;
    _selected=-1;
		_overindex=-1;
    _listeners=new ListenerGroup();
    addMouseListener(this);
    addMouseMotionListener(this);
    _base=0;
    _nicks=new Vector();
    _font=new Font("",0,12);
    _leftAlign=_pixxConfiguration.getB("leftnickalign");
  }

  public void release()
  {
    dispose();
    removeMouseListener(this);
    removeMouseMotionListener(this);
    super.release();
  }

  /**
   * Add listener.
   * @param lis listener to add.
   */
  public void addPixxNickListListener(PixxNickListListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove listener.
   * @param lis listener to remove.
   */
  public void removePixxNickListListener(PixxNickListListener lis)
  {
    _listeners.removeListener(lis);
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(_pixxConfiguration.getI("nicklistwidth"),16);
  }

  /**
   * Clear any off-screen ressources. The next display might be slower.
   */
  public void dispose()
  {
    reinit();
  }

  private void reinit()
  {
    synchronized(_scrollLock)
    {
      _toScroll=0;
      _buffer=null;
    }
  }

  private void scroll(int v)
  {
    synchronized(_scrollLock)
    {
      _toScroll+=v;
      _base+=v;
    }
  }

  private int getScrollValue()
  {
    synchronized(_scrollLock)
    {
      int ans=_toScroll;
      _toScroll=0;
      return ans;
    }
  }

  /**
   * Set first displayed nick.
   * @param b first displayed nick index.
   */
  public void setBase(int b)
  {
    scroll(b-_base);
    repaint();
  }

  /**
   * Get first displayed nick.
   * @return first dispalyed nick index.
   */
  public int getBase()
  {
    return _base;
  }

  /**
   * Get amount of nicks in the list.
   * @return nick list size.
   */
  public int getNickCount()
  {
    return _nicks.size();
  }

  /**
   * Add a nick in the list.
   * @param nick nickname to add.
   */
  public void add(String nick)
  {
    _nicks.insertElementAt(nick,_nicks.size());
    reinit();
    repaint();
  }

  /**
   * Remove the given nick name from the list.
   * @param nick nick name to remove.
   */
  public void remove(String nick)
  {
    for(int i=0;i<_nicks.size();i++)
    {
      String s=(String)_nicks.elementAt(i);
      if(s.equals(nick))
      {
        _nicks.removeElementAt(i);
        break;
      }
    }
    reinit();
    repaint();
  }

  /**
   * Change all the nicks in the list.
   * @param nicks new nick array.
   */
  public void set(String[] nicks)
  {
    _nicks=new Vector();
    for(int i=0;i<nicks.length;i++) _nicks.insertElementAt(nicks[i],_nicks.size());
    reinit();
    repaint();
  }

  /**
   * Remove all the nicks from the list.
   */
  public void removeAll()
  {
    _nicks=new Vector();
    //_buffer=null;
    reinit();
    repaint();
  }

  public void paint(Graphics g)
  {
    update(g);
  }

	private Color findColor(String info)
	{
    return _pixxConfiguration.getIRCConfiguration().getASLColor(info,getColor(COLOR_MALE),getColor(COLOR_FEMEALE),getColor(COLOR_UNDEF));
	}

  private void update(Graphics gra,int ytop,int height)
  {
    int w=getSize().width;
    //int h=getSize().height;
    int fh=_font.getSize();

    gra.setColor(getColor(COLOR_BACK));
    gra.fillRect(0,ytop,w,height);
    gra.setColor(getColor(COLOR_BLACK));
    gra.drawLine(w-1,ytop,w-1,ytop+height-1);

    gra.setFont(_font);

    int y=8;
    FontMetrics fm=gra.getFontMetrics();

    int i=_base;
    while(y+fh+5<ytop)
    {
      y+=fh+6;
      i++;
    }
    if(i>0)
    {
      y-=fh+6;
      i--;
    }
    while((i<_nicks.size()) && (y<=ytop+height))
    {
      String nick=(String)_nicks.elementAt(i);
      String info="";
      int pos=nick.indexOf(":");
      Color back=getColor(COLOR_FRONT);
      if(pos!=-1)
      {
        info=nick.substring(pos+1);
        nick=nick.substring(0,pos);
        back=findColor(info);
      }
      if(_selected==i) back=getColor(COLOR_SELECTED);

      char prefix=0;
      for(int ci=0;ci<_prefixes.length;ci++)
        if((nick.length()>0) && (nick.charAt(0)==_prefixes[ci])) prefix=_prefixes[ci];
      if(prefix>0) nick=nick.substring(1);

      int sw=fm.stringWidth(nick);

      gra.setColor(back);
      gra.fillRect(20,y-1,w-28,fh+2);
      gra.setColor(getColor(COLOR_WHITE));
      gra.drawRect(20,y-1,w-28,fh+2);

      gra.setColor(getColor(COLOR_WHITE));
      gra.setClip(20,y-1,w-28,fh+2);
      int px=w-sw-12;
      if(px<22) px=22;

      if(_leftAlign) px=22;

      gra.drawString(nick,px,y+fh-1);
      gra.setClip(0,0,getSize().width,getSize().height);

      if(prefix>0)
      {
        if(prefix=='@')
          gra.setColor(getColor(COLOR_OP));
        else if(prefix=='+')
          gra.setColor(getColor(COLOR_VOICE));
        else if(prefix=='%')
          gra.setColor(getColor(COLOR_SEMIOP));
        else
          gra.setColor(getColor(COLOR_FRONT));
        gra.fillRect(20-fh-6,y-1,fh+2,fh+2);
        gra.setColor(getColor(COLOR_WHITE));
        gra.drawRect(20-fh-6,y-1,fh+2,fh+2);

        gra.setColor(getColor(COLOR_WHITE));
        sw=fm.stringWidth(""+prefix);
        int tx=20-fh-6+(fh+2-sw)/2+1;
        int ty=y+fh-1;
        if(prefix=='@')
        {
          tx--;
          ty--;
        }
        gra.drawString(""+prefix,tx,ty);
      }

      y+=fh+6;
      i++;
    }
  }

  public void update(Graphics g)
  {
    int w=getSize().width;
    int h=getSize().height;
    int fh=_font.getSize();

    if(_buffer!=null)
    {
      if((_buffer.getWidth(this)!=w) || (_buffer.getHeight(this)!=h)) _buffer=null;
    }

    if(_buffer==null)
    {
      Graphics gra;
      try
      {
        _buffer=createImage(w,h);
        gra=_buffer.getGraphics();
        update(gra,0,h);
      }
      catch(Throwable e)
      {
        return;
      }
    }
    else
    {
      int scr=getScrollValue();
      if(scr!=0)
      {
        Graphics gra=_buffer.getGraphics();
        int dy=-(fh+6)*scr;
        if(dy<0)
        {
          gra.copyArea(0,-dy,w,h+dy,0,dy);
          update(gra,h+dy,-dy);
        }
        else
        {
          gra.copyArea(0,0,w,h-dy,0,dy);
          update(gra,0,dy);
        }
      }
    }

    if(_buffer!=null) g.drawImage(_buffer,0,0,this);
    if((_overindex!=-1) && _pixxConfiguration.getIRCConfiguration().getB("style:floatingasl"))
    {
      int x=_overX;
      int y=8+(_overindex-_base)*(fh+6)+2;
      if(y+fh+5>=h) y=h-fh-5;
      String info=getInfo(_overindex);
      String text=_pixxConfiguration.getIRCConfiguration().formatASL(info);
      if(text.length()>0)
      {
        int tw=g.getFontMetrics().stringWidth(text);
        if(x+tw+5>=w) x=w-tw-5;
        if(x<0) x=0;
        g.setColor(getAlphaColor(findColor(info),_pixxConfiguration.getIRCConfiguration().getI("style:floatingaslalpha")));
        g.fillRect(x,y,tw+4,fh+4);
        g.setColor(getColor(COLOR_WHITE));
        g.drawRect(x,y,tw+4,fh+4);
        g.drawString(text,x+2,y+fh);
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

  private int getIndex(int x,int y)
  {
    int fh=_font.getSize();
    //increase y value so that it won't reach negative value. neg y is a
    //problem since / rounds to 0, not to lower : -0.5 will lead to 0, not
    //to -1.
    y+=fh+6;
    y-=8;
    y/=fh+6;
    y--;
    y+=_base;
    if(y<0) y=-1;
    if(y>=_nicks.size()) y=-1;
    return y;
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
    if(_overindex!=-1)
    {
      _overindex=-1;
      repaint();
    }
  }

  public void mousePressed(MouseEvent e)
  {
    int index=getIndex(e.getX(),e.getY());
    _selected=index;
    reinit();
    repaint();
    if(_selected!=-1) _listeners.sendEventAsync("eventOccured",getNick(_selected),e);
  }

  public void mouseReleased(MouseEvent e)
  {
  }

  public void mouseDragged(MouseEvent e)
  {
    mouseMoved(e);
  }

  private String getUnprefixedNick(String nick)
  {
    if(nick.length()==0) return nick;
    for(int i=0;i<_prefixes.length;i++)
      if(nick.charAt(0)==_prefixes[i]) return nick.substring(1);
    return nick;
  }

  private String getNick(int index)
  {
    if(index==-1) return "";
    String nick=(String)_nicks.elementAt(index);
    nick=getUnprefixedNick(nick);
    int pos=nick.indexOf(":");
    if(pos!=-1)
    {
      nick=nick.substring(0,pos);
    }
    return nick;
  }

  private String getInfo(int index)
  {
    if(index==-1) return "";
    String nick=(String)_nicks.elementAt(index);
    nick=getUnprefixedNick(nick);
    int pos=nick.indexOf(":");
    String info="";
    if(pos!=-1)
    {
      info=nick.substring(pos+1);
    }
    return info;
  }

  public void mouseMoved(MouseEvent e)
  {
    if(!_pixxConfiguration.getIRCConfiguration().getASLMaster()) return;
    int index=getIndex(e.getX(),e.getY());
		if(index==_overindex) return;
		_overindex=index;
		_overX=e.getX();
		repaint();
    if(index!=-1) _listeners.sendEventAsync("ASLEventOccured",getNick(index),getInfo(index));
  }
}

