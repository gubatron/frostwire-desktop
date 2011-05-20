package irc.style;

import irc.EventDispatcher;
import irc.IRCConfiguration;
import irc.StyleContext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.Vector;

/**
 * CharacterInfo.
 */
class CharacterInfo
{
  /**
   * Create a new CharacterInfo
   */
  public CharacterInfo()
  {
    frontColor=Color.black;
    backColor=Color.white;
    isBold=false;
    isUnderline=false;
    isReverse=false;
    isTransparent=true;
  }

  /**
   * Create a new CharacterInfo
   * @param base base info that will be copied in the newly created instance.
   */  
  public CharacterInfo(CharacterInfo base)
  {
    frontColor=base.frontColor;
    backColor=base.backColor;
    isBold=base.isBold;
    isUnderline=base.isUnderline;
    isReverse=base.isReverse;
    isTransparent=base.isTransparent;
  }

  public boolean equals(Object o)
  {
    if(!(o instanceof CharacterInfo)) return false;
    CharacterInfo c=(CharacterInfo)o;
    if(!frontColor.equals(c.frontColor)) return false;
    if(!backColor.equals(c.backColor)) return false;
    if(isBold!=c.isBold) return false;
    if(isUnderline!=c.isUnderline) return false;
    if(isTransparent!=c.isTransparent) return false;
    return true;
  }

  public int hashCode()
  {
    int c=0;
    if(isBold) c++;
    if(isUnderline) c++;
    if(isTransparent) c++;
    return c+frontColor.hashCode()+backColor.hashCode();
  }

  /**
   * Front color.
   */
  public Color frontColor;
  /**
   * Back color.
   */
  public Color backColor;
  /**
   * True if bold.
   */
  public boolean isBold;
  /**
   * True if underline.
   */
  public boolean isUnderline;
  /**
   * True if reverse.
   */
  public boolean isReverse;
  /**
   * True if transparent.
   */
  public boolean isTransparent;
}

/**
 * CharacterGroupItem.
 */
class CharacterGroupItem
{
  /**
   * String content.
   */
  public String s;
  /**
   * String style.
   */
  public CharacterInfo info;

  /**
   * Create a new CharacterGroupItem
   * @param nfo style.
   */
  public CharacterGroupItem(CharacterInfo nfo)
  {
    info=nfo;
    s="";
  }
}

/**
 * WordItem.
 */
class WordItem
{
  /**
   * Items.
   */
  public CharacterGroupItem[] items;
  /**
   * Original stripped word.
   */
  public String originalstrippedword;
  /**
   * Original word.
   */
  public String originalword;
  /**
   * Smiley-decoded word.
   */
  public String decodedword;
  /**
   * Last character style.
   */
  public CharacterInfo lastInfo;

  /**
   * Create a new WordItem
   * @param itm items.
   * @param lInfo last character style.
   */
  public WordItem(CharacterGroupItem[] itm,CharacterInfo lInfo)
  {
    lastInfo=lInfo;
    items=itm;
    decodedword="";
    for(int i=0;i<items.length;i++) decodedword+=items[i].s;
    originalword=decodedword;
    originalstrippedword=decodedword;
  }
}

/**
 * LineItem.
 */
class LineItem
{
  /**
   * First item.
   */
  public int first;
  /**
   * Number of items.
   */
  public int count;
}

/**
 * DecodedLineInternal.
 */
class DecodedLineInternal extends DecodedLine
{
  /**
   * All items.
   */
  public WordItem[] words;
}

/**
 * The formatted string drawer.
 */
public class FormattedStringDrawer implements ImageObserver
{
  private Font _font;
  private Font _fontPlain;
  private Font _fontBold;
  private Color[] _cols;
  private CharactersDrawer _drawer;
	private IRCConfiguration _config;
  private Dimension _tmp;
  private LineItem[] _lines;
  private int _vdirection;
  private int _hdirection;
  private FormattedStringDrawerListener _listener;

  /**
   * Bottom.
   */
  public static final int BOTTOM=0;
  /**
   * Top.
   */
  public static final int TOP=1;
  /**
   * Left.
   */
  public static final int LEFT=0;
  /**
   * Right.
   */
  public static final int RIGHT=1;

  /**
   * Create a new FormattedStringDrawer.
   * @param config the global configuration.
   * @param context the style context to use.
   * @param listener the listener to notify upon draw update.
   */
  public FormattedStringDrawer(IRCConfiguration config,StyleContext context,FormattedStringDrawerListener listener)
  {
    _listener=listener;
    _tmp=new Dimension();
    _lines=new LineItem[8];
    for(int i=0;i<_lines.length;i++) _lines[i]=new LineItem();
	  _config=config;
	  setFont(config.getStyleFont(context));
    _drawer=new CharactersDrawer(_config);
		setStyleContext(context);
		_vdirection=BOTTOM;
		_hdirection=LEFT;
    if(config.getB("style:righttoleft")) setHorizontalDirection(RIGHT);

  }

  /**
   * Create a new FormattedStringDrawer.
   * @param config the global configuration.
   * @param context the style context to use.
   */
  public FormattedStringDrawer(IRCConfiguration config,StyleContext context)
  {
    this(config,context,null);    
  }

  /**
   * Set the vertical alignment direction.
   * @param dir vertical direction.
   */
  public void setVerticalDirection(int dir)
  {
    _vdirection=dir;
  }

  /**
   * Get the vertical alignment direction.
   * @return the vertical direction.
   */
  public int getVerticalDirection()
  {
    return _vdirection;
  }

  /**
   * Set the horizontal alignment direction.
   * @param dir horizontal direction.
   */
  public void setHorizontalDirection(int dir)
  {
    _hdirection=dir;
  }

  /**
   * Get the horizontal alignment direction.
   * @return horizontal direction.
   */
  public int getHorizontalDirection()
  {
    return _hdirection;
  }

  /**
	 * Set the style context to use.
	 * @param context new color context.
	 */
	public void setStyleContext(StyleContext context)
	{
	  _cols=_config.getStyleColors(context);
	}

	/**
	 * Prepare a line for display.
	 * @param str line to prepare.
	 * @return prepared line, ready for display.
	 */
	public DecodedLine decodeLine(String str)
	{
    DecodedLineInternal ans=new DecodedLineInternal();
    ans.original=str;
    str+=(char)15;
	  String decoded=_drawer.decodeLine(str);
	  ans.decoded=decoded;
    ans.decoded_stripped=getStripped(decoded);
    Vector v=doWords(str,decoded);
    ans.words=new WordItem[v.size()];
    for(int i=0;i<ans.words.length;i++) ans.words[i]=(WordItem)v.elementAt(i);
	  return ans;
	}

  private Vector doWords(String ostr,String dstr)
  {
    Vector words=new Vector();
    CharacterInfo info=new CharacterInfo();
    info.frontColor=_cols[1];
    info.backColor=_cols[0];
    info.isTransparent=true;

    while(dstr.length()>0)
    {
      int opos=ostr.indexOf(' ');
      int dpos=dstr.indexOf(' ');
      WordItem word;
      if(dpos==-1)
      {
        word=decodeWord(info,dstr+" ",_cols);
        word.originalword=ostr+" ";
        word.originalstrippedword=getStripped(ostr+" ");
        dstr="";
      }
      else
      {
        String owrd=ostr.substring(0,opos);
        String dwrd=dstr.substring(0,dpos);
        word=decodeWord(info,dwrd+" ",_cols);
        word.originalword=owrd+" ";
        word.originalstrippedword=getStripped(owrd+" ");
        ostr=ostr.substring(opos+1);
        dstr=dstr.substring(dpos+1);
      }
      words.insertElementAt(word,words.size());
      info=word.lastInfo;
    }

    return words;
  }

  /**
   * Get the given string width, in pixel.
   * @param str the prepared line.
   * @param fm the FontMetrics that will be used on display.
   * @return the string width, in pixel.
   */
  public int getHeight(DecodedLine str,FontMetrics fm)
	{
	  return _drawer.getHeight(str.decoded_stripped,fm,this);
	}

  /**
   * Get the given string height, in pixel.
   * @param str the prepared line.
   * @param fm the FontMetrics that will be used on display.
   * @return the string height, in pixel.
   */
  public int getWidth(DecodedLine str,FontMetrics fm)
	{
    return _drawer.getWidth(str.decoded_stripped,fm,this);
	}

  private Font deriveFont(Font fnt,int style)
  {
    return new Font(fnt.getName(),style,fnt.getSize());
  }

  /**
   * Set the colors to use, overriding current colors from color context.
   * @param cols colors to use.
   */
  public void setColors(Color[] cols)
  {
    _cols=cols;
  }

  /**
   * Get the current color at index i.
   * @param i color index.
   * @return the color at index i.
   */
	public Color getColor(int i)
	{
	  return _cols[i];
	}

  /**
   * Set the font to use.
   * @param fnt the font to be used.
   */
  public void setFont(Font fnt)
  {
    _font=fnt;
    _fontPlain=deriveFont(_font,Font.PLAIN);
    _fontBold=deriveFont(_font,Font.BOLD);
  }

  /**
   * Get the used font.
   * @return the used font.
   */
  public Font getFont()
  {
    return _font;
  }

  private WordItem decodeWord(CharacterInfo base,String str,Color[] cols)
  {
    Vector v=new Vector();
    CharacterInfo current=new CharacterInfo(base);
    CharacterGroupItem currentItem=new CharacterGroupItem(new CharacterInfo(current));
    int size=str.length();
    for(int pos=0;pos<size;pos++)
    {
      char c=str.charAt(pos);
      if(c<' ')
      {
        int code=c;
        if(code==15)
        {
          current.isBold=false;
          current.isUnderline=false;
          current.isReverse=false;
          current.frontColor=cols[1];
          current.backColor=cols[0];
          current.isTransparent=true;
        }
        else if(code==2)
        {
          current.isBold=!current.isBold;
        }
        else if(code==31)
        {
          current.isUnderline=!current.isUnderline;
        }
        else if(code==22)
        {
          current.isReverse=!current.isReverse;
          if(current.isReverse)
          {
            current.frontColor=cols[0];
            current.backColor=cols[1];
            current.isTransparent=false;
          }
          else
          {
            current.frontColor=cols[1];
            current.backColor=cols[0];
            current.isTransparent=true;
          }
        }
        else if(code==3)
        {
          boolean front=true;
          String frontC="";
          String backC="";
          pos++;
          while(pos<size)
          {
            char d=str.charAt(pos);
            if((d>='0') && (d<='9'))
            {
              if(front)
              {
                if(frontC.length()==2)
                {
                  pos--;
                  break;
                }
                frontC+=d;
              }
              else
              {
                if(backC.length()==2)
                {
                  pos--;
                  break;
                }
                backC+=d;
              }
              pos++;
            }
            else if(d==',')
            {
              if(front)
              {
                front=false;
                pos++;
              }
              else
              {
                pos--;
                break;
              }
            }
            else
            {
              pos--;
              break;
            }
          }
          if(frontC.length()==0) backC="";
          if(frontC.length()>0)
          {
            int col=Integer.parseInt(frontC);
            col%=_cols.length;
            current.frontColor=cols[col];
          }
          if(backC.length()>0)
          {
            int col=Integer.parseInt(backC);
            col%=_cols.length;
            current.backColor=cols[col];
            current.isTransparent=(col==0);
          }
          if((frontC.length()==0) && (backC.length()==0))
          {
            current.frontColor=cols[1];
            current.backColor=cols[0];
            current.isTransparent=true;
          }
        }
        if(!current.equals(currentItem.info))
        {
          v.insertElementAt(currentItem,v.size());
          currentItem=new CharacterGroupItem(new CharacterInfo(current));
        }
      }
      else
      {
        currentItem.s+=c;
      }
    }
    v.insertElementAt(currentItem,v.size());

    CharacterGroupItem[] ans=new CharacterGroupItem[v.size()];
    for(int i=0;i<v.size();i++)
    {
      ans[i]=(CharacterGroupItem)v.elementAt(i);
    }

    return new WordItem(ans,current);
  }


  private FontMetrics getFontMetrics(Graphics g,CharacterInfo nfo)
	{
	  Font old=g.getFont();
    if(nfo.isBold) g.setFont(_fontBold);
	  else g.setFont(_fontPlain);
		FontMetrics res=g.getFontMetrics();
		g.setFont(old);
		return res;
	}

  private int drawPart(Graphics g,CharacterInfo nfo,String str,int x,int y,FontMetrics plainMetrics,int clipxl,int clipxr,ImageObserver obs,Vector handles)
  {
    FontMetrics fm=plainMetrics;
    //int up=plainMetrics.getAscent();
    int down=plainMetrics.getDescent();

    if(nfo.isBold) g.setFont(_fontBold);

    fm=g.getFontMetrics();

    int width=_drawer.getWidth(str,fm,this);
		if((x<=clipxr) && (x+width>clipxl))
		{
      int height=_drawer.getHeight(str,fm,this);
			Rectangle originalClip=g.getClipBounds();
			int cx=clipxl;
			int cy=y-height;
      int cw=clipxr-clipxl+1;
      int ch=height;

      g.clipRect(cx,cy,cw,ch);

      g.setColor(nfo.backColor);

      if(!nfo.isTransparent)
        g.fillRect(x,y-height,width,height);

      y-=down;

      g.setColor(nfo.frontColor);
      _drawer.draw(str,g,fm,x,y,obs,handles);

      if(nfo.isUnderline)
        g.drawLine(x,y+1,x+width-1,y+1);
			if(originalClip!=null)
			  g.setClip(originalClip.x,originalClip.y,originalClip.width,originalClip.height);
			else
        g.setClip(null);
    }

    if(nfo.isBold) g.setFont(_fontPlain);
    return width;
  }

  private void drawWord(Graphics g,WordItem word,int x,int y,boolean last,FontMetrics plainMetrics,int clipxl,int clipxr,ImageObserver obs,Vector handles)
  {
    for(int pos=0;pos<word.items.length;pos++)
    {
      CharacterGroupItem item=word.items[pos];
      x+=drawPart(g,item.info,item.s,x,y,plainMetrics,clipxl,clipxr,obs,handles);
    }
  }

  /**
   * Strip a line from all its color and special codes.
   * @param str string to strip.
   * @return stripped line.
   */
  public String getStripped(String str)
  {
    CharacterInfo info=new CharacterInfo();
    info.frontColor=_cols[1];
    info.backColor=_cols[0];
    info.isTransparent=true;
    String res="";
    while(str.length()>0)
    {
      int pos=str.indexOf(' ');
      WordItem word;
      if(pos==-1)
      {
        word=decodeWord(info,str,_cols);
        str="";
      }
      else
      {
        String wrd=str.substring(0,pos);
        word=decodeWord(info,wrd+" ",_cols);
        str=str.substring(pos+1);
      }
      if(res.length()>0)
        res+=" "+word.originalword;
      else
        res+=word.originalword;
    }
    return res;
  }

	private boolean isAlphaNum(char c)
	{
	  if((c=='(') || (c==')')) return false;
	  if((c=='<') || (c=='>')) return false;
	  if((c=='"') || (c=='"')) return false;
	  if((c=='{') || (c=='}')) return false;
	  if((c=='.') || (c==',')) return false;
    if(c==':') return false;
    //if(c=='-') return false;
    return true;
	}

	private String trimAlphaNum(String s)
	{
		int index=0;
		while((index<s.length()) && !isAlphaNum(s.charAt(index))) index++;
		if(index==s.length()) return "";
		s=s.substring(index);
		index=s.length()-1;
		while((index>=0) && !isAlphaNum(s.charAt(index))) index--;
		if(index==-1) return "";
		s=s.substring(0,index+1);
		return s;
	}

	private void getWordItemWidthHeight(Graphics g,WordItem item,Dimension res)
	{
	  int resx=0;
	  int resy=0;
		for(int i=0;i<item.items.length;i++)
		{
		  FontMetrics fm=getFontMetrics(g,item.items[i].info);
		  _drawer.getWidthHeight(item.items[i].s,fm,res,this);
		  resx+=res.width;
		  int h=res.height;
			if(h>resy) resy=h;
		}
		res.width=resx;
		res.height=resy;
	}

  private void expandLines()
  {
    LineItem[] n=new LineItem[_lines.length*2];
    System.arraycopy(_lines,0,n,0,_lines.length);
    for(int i=_lines.length;i<n.length;i++) n[i]=new LineItem();
    _lines=n;
  }

  /**
   * Get the height of the given decoded line.
   * @param str decoded line.
   * @param g graphics where the string will be displayed.
   * @param x display position.
   * @param wmax maximum width.
   * @param wrap true if wrapping must occur.
   * @return actual height.
   */
  public int getHeight(DecodedLine str,Graphics g,int x,int wmax,boolean wrap)
  {
    WordItem[] words=((DecodedLineInternal)str).words;

    Font currFont=_fontPlain;
    g.setFont(currFont);
    FontMetrics plainFm=g.getFontMetrics();

    int currentLineLength=0;
    int w=0;
    int h=0;

    int mh=0;
    for(int i=0;i<words.length;i++)
    {
      WordItem word=words[i];
      getWordItemWidthHeight(g,word,_tmp);
      int wordWidth=_tmp.width;
      if((w+wordWidth>wmax) && (currentLineLength>0) && wrap)
      {
        w=_drawer.getWidth("  ",plainFm,this);
        currentLineLength=0;
        h+=mh;
        mh=0;
      }
      if(_tmp.height>mh) mh=_tmp.height;
      currentLineLength++;
      w+=wordWidth;
    }
    if(currentLineLength>0) h+=mh;
    return h;
  }

  /**
   * Draw the given prepared line.
   * @param str the prepared line to draw.
   * @param g the graphics where to draw.
   * @param left left margin.
   * @param right right margin.
   * @param y y position.
   * @param clipxl left clip position : drawing doesn't have to be complete left to this position.
   * @param clipxr right clip position : drawing doens't have to be complete right to this position.
   * @param analyse true if word per word analyse must be performed, false otherwise. If not
   * analyse is requested, the DrawResultItem array in res will be zero-sized.
   * @param wrap true is wrapping must be done, false otherwise.
   * @param res analyse report destination for the drawed line.
   */
  public void draw(DecodedLine str,Graphics g,int left,int right,int y,int clipxl,int clipxr,boolean analyse,boolean wrap,DrawResult res)
  {
    WordItem[] words=((DecodedLineInternal)str).words;
    if(res.updateHandles==null)
    {
      res.updateHandles=new Vector();
    }
    else
    {
      if(res.updateHandles.size()>0) res.updateHandles.removeAllElements(); 
    }

    //split lines
    Font currFont=_fontPlain;
    g.setFont(currFont);
    FontMetrics plainFm=g.getFontMetrics();

    int lineCount=0;
    int firstWordInLine=0;
    int currentLineLength=0;
    int wmax=right-left+1;
    if(wrap)
    {
      int w=0;
      for(int i=0;i<words.length;i++)
      {
        WordItem word=words[i];
  		  getWordItemWidthHeight(g,word,_tmp);
        int wordWidth=_tmp.width;
        if((w+wordWidth>wmax) && (currentLineLength>0))
        {
          w=_drawer.getWidth("  ",plainFm,this);
          LineItem newLine=_lines[lineCount++];
          if(lineCount==_lines.length) expandLines();
          newLine.first=firstWordInLine;
          newLine.count=currentLineLength;
          firstWordInLine=i;
          currentLineLength=0;
        }

        currentLineLength++;
        w+=wordWidth;
      }
      if(currentLineLength!=0)
      {
        LineItem newLine=_lines[lineCount++];
        if(lineCount==_lines.length) expandLines();
        newLine.first=firstWordInLine;
        newLine.count=currentLineLength;
      }
    }
    else
    {
      LineItem newLine=_lines[lineCount++];
      newLine.first=0;
      newLine.count=words.length;
    }

    //display
    int s=0;
    if(analyse) s=words.length;
    if((res.items==null) || (res.items.length!=s)) res.items=new DrawResultItem[s];

    DrawResultItem[] dres=res.items;
    int minX=right;
    int maxX=left;
    int h=0;
    int py=0;
    int marginWidth=_drawer.getWidth("  ",plainFm,this);

    int hdir=1;
    if(_hdirection==RIGHT) hdir=-1;

    if(_vdirection==BOTTOM)
    {
      for(int i=lineCount-1;i>=0;i--)
      {
        int px=left;
        if(hdir==-1) px=right;
			  int maxHeight=0;
        if(i!=0) px+=hdir*marginWidth;
        LineItem line=_lines[i];
        for(int j=line.first;j<line.first+line.count;j++)
        {

          getWordItemWidthHeight(g,words[j],_tmp);

          int wordWidth=_tmp.width;
          int wordHeight=_tmp.height;

          if(hdir==-1) px-=wordWidth;

          int trimmedWidth=wordWidth;
  				if(maxHeight<wordHeight) maxHeight=wordHeight;
          if((px+wordWidth>clipxl) && (px<=clipxr))
            drawWord(g,words[j],px,y,j==line.first+line.count-1,plainFm,clipxl,clipxr,this,res.updateHandles);

          if(analyse)
          {
            //String wrd=words[j].decodedword;
            String owrd=words[j].originalword;
            String swrd=words[j].originalstrippedword;
            String twrd=trimAlphaNum(swrd.trim());
            if(dres[j]==null) dres[j]=new DrawResultItem();
            DrawResultItem ritem=dres[j];
            ritem.parent=res;
            ritem.item=twrd;
            ritem.originalword=owrd;
            ritem.originalstrippedword=swrd;
            ritem.rectangle=new StyledRectangle(px,py-wordHeight,trimmedWidth,wordHeight);
          }
          if(hdir==1) px+=wordWidth;
          if(px>maxX) maxX=px;
          if(px<minX) minX=px;
        }
        y-=maxHeight;
        py-=maxHeight;
        h+=maxHeight;
      }

      if(analyse) for(int i=0;i<dres.length;i++) dres[i].rectangle.y+=h;
    }
    else if(_vdirection==TOP)
    {
      for(int i=0;i<lineCount;i++)
      {
        int px=left;
        if(hdir==-1) px=right;
        int maxHeight=0;
        if(i!=0) px+=hdir*marginWidth;
        LineItem line=_lines[i];
        for(int j=line.first;j<line.first+line.count;j++)
        {
          getWordItemWidthHeight(g,words[j],_tmp);

          int wordWidth=_tmp.width;
          int wordHeight=_tmp.height;
          if(hdir==-1) px-=wordWidth;

          int trimmedWidth=wordWidth;
          if(maxHeight<wordHeight) maxHeight=wordHeight;

          if((px+wordWidth>clipxl) && (px<=clipxr))
            drawWord(g,words[j],px,y+wordHeight,j==line.first+line.count-1,plainFm,clipxl,clipxr,this,res.updateHandles);

          if(analyse)
          {
            String wrd=words[j].decodedword;
            String owrd=words[j].originalword;
            String swrd=words[j].originalstrippedword;
            String twrd=trimAlphaNum(wrd.trim());
            if(dres[j]==null) dres[j]=new DrawResultItem();
            DrawResultItem ritem=dres[j];
            ritem.parent=res;
            ritem.item=twrd;
            ritem.originalword=owrd;
            ritem.originalstrippedword=swrd;
            ritem.rectangle=new StyledRectangle(px,py,trimmedWidth,wordHeight);
          }
          if(hdir==1) px+=wordWidth;
          if(px>maxX) maxX=px;
          if(px<minX) minX=px;
        }
        y+=maxHeight;
        py+=maxHeight;
        h+=maxHeight;
      }
      y-=h;
    }


    int x1=left;
    int x2=maxX;
    if(hdir==-1)
    {
      x1=minX;
      x2=right;
    }

    if(analyse) for(int i=0;i<res.items.length;i++) res.items[i].rectangle.x-=x1;

    if(res.rectangle==null)
    {
      res.rectangle=new StyledRectangle(x1,y,x2-x1+1,h);
    }
    else
    {
      res.rectangle.x=x1;
      res.rectangle.y=y;
      res.rectangle.width=x2-x1+1;
      res.rectangle.height=h;
    }
  }

  public boolean imageUpdate(Image img,int infoflags,int x,int y,int width,int height)
  {
    if((infoflags&ImageObserver.ERROR)!=0) return false;
    if((infoflags&ImageObserver.ABORT)!=0) return false;
    
    //We don't care about properties, just go ahead...
    if((infoflags&ImageObserver.PROPERTIES)!=0) return true;

    if(_listener!=null)
    {
      Boolean b;
      int what=0;
      if((infoflags&ImageObserver.WIDTH)!=0) what|=FormattedStringDrawerListener.SIZE;
      if((infoflags&ImageObserver.HEIGHT)!=0) what|=FormattedStringDrawerListener.SIZE;
      if((infoflags&ImageObserver.ALLBITS)!=0) what|=FormattedStringDrawerListener.DATA;
      if((infoflags&ImageObserver.FRAMEBITS)!=0) what|=FormattedStringDrawerListener.FRAME;
      if((infoflags&ImageObserver.SOMEBITS)!=0) what|=FormattedStringDrawerListener.DATA;
            
      try
      {
        b=(Boolean)EventDispatcher.dispatchEventAsyncAndWaitEx(_listener,"displayUpdated",new Object[] {img,new Integer(what)});
        return b.booleanValue();      
      }
      catch (Throwable e)
      {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }
}
