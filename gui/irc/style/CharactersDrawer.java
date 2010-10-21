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

package irc.style;

import irc.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Atomic characters drawer. The CharactersDrawer handles graphical smileys.
 */
public class CharactersDrawer
{
  private IRCConfiguration _ircConfiguration;
	private char[] _current;
	private int _lineSpacing;

	/**
   * Create a new CharactersDrawer.
   * @param config the irc configuration.
   */
  public CharactersDrawer(IRCConfiguration config)
  {
	  _ircConfiguration=config;
	  _current=new char[256];
	  _lineSpacing=config.getI("style:linespacing");
  }

	private int getBitmapSmileyWidth(int c,ImageObserver obs)
	{
	  Image img=_ircConfiguration.getSmileyTable().getImage(c);
		if(img==null) return 0;
		return img.getWidth(obs);
	}

	private int getBitmapSmileyHeight(int c,ImageObserver obs)
	{
	  Image img=_ircConfiguration.getSmileyTable().getImage(c);
		if(img==null) return 0;
		return img.getHeight(obs);
	}

	private Object drawBitmapSmiley(Graphics g,FontMetrics fm,int smiley,int x,int y,ImageObserver obs)
	{
	  Image img=_ircConfiguration.getSmileyTable().getImage(smiley);
		if(img==null) return null;
		int h=getBitmapSmileyHeight(smiley,obs);
		y-=h;
    y+=fm.getDescent();

	  g.drawImage(img,x,y,obs);
    return img;
	}

  private String handleSmiley(String line,String ascii,char code)
  {
    int pos=line.indexOf(ascii);
    if(pos==-1) return line;

    String previous=line.substring(0,pos);
    String after=line.substring(pos+ascii.length());
    char toAdd=(char)(code+0xE000);
    line=previous+toAdd+after;
    return handleSmiley(line,ascii,code);
  }

  /**
   * Prepare and decode the given line for smileys.
   * @param line source line, before smileys replacement.
   * @return prepared line with special characters.
   */
  public String decodeLine(String line)
  {
	  SmileyTable table=_ircConfiguration.getSmileyTable();
		int s=table.getSize();
		for(int i=0;i<s;i++)
		{
		  String m=table.getMatch(i);
			line=handleSmiley(line,m,(char)(i));
		}
		return line;
  }

	/**
	 * Get the given string width, in pixel.
	 * @param str the prepared line.
	 * @param fm the FontMetrics that will be used on display.
   * @param obs the image observer in case of bitmap handling.
	 * @return the string width, in pixel.
	 */
  public int getWidth(String str,FontMetrics fm,ImageObserver obs)
  {
    if(_current.length<str.length()) _current=new char[str.length()*2];
    int size=0;
    int w=0;
    for(int i=0;i<str.length();i++)
    {
      char c=str.charAt(i);
      if((c>=0xE000) && (c<=0xF8FF))
      {
        c-=0xE000;
        w+=fm.charsWidth(_current,0,size);
        size=0;
        w+=getBitmapSmileyWidth(c,obs);
      }
      else
      {
        _current[size++]=c;
      }
    }
    w+=fm.charsWidth(_current,0,size);
    return w;
  }

  /**
   * Get the given string height, in pixel.
   * @param str the prepared line.
   * @param fm the FontMetrics that will be used on display.
   * @param obs the image observer in case of bitmap handling.
   * @return the string height, in pixel.
   */
  public int getHeight(String str,FontMetrics fm,ImageObserver obs)
	{
    int h=0;
		int mh=0;
    for(int i=0;i<str.length();i++)
    {
      char c=str.charAt(i);
      if((c>=0xE000) && (c<=0xF8FF))
      {
        c-=0xE000;
        h=getBitmapSmileyHeight(c,obs);
				if(h>mh) mh=h;
      }
    }
    h=fm.getFont().getSize()+1;
		if(h>mh) mh=h;
    return mh+_lineSpacing;
	}

  /**
   * Get the given string width and height, in pixel.
   * @param str the prepared line.
   * @param fm the FontMetrics that will be used on display.
   * @param res Dimension to be used for result.
   * @param obs the image observer in case of bitmap handling.
   */
  public void getWidthHeight(String str,FontMetrics fm,Dimension res,ImageObserver obs)
  {
    if(_current.length<str.length()) _current=new char[str.length()*2];
    int size=0;
    int h=0;
    int w=0;
    int mh=0;
    for(int i=0;i<str.length();i++)
    {
      char c=str.charAt(i);
      if((c>=0xE000) && (c<=0xF8FF))
      {
        c-=0xE000;
        w+=fm.charsWidth(_current,0,size);
        h=getBitmapSmileyHeight(c,obs);
        if(h>mh) mh=h;
        w+=getBitmapSmileyWidth(c,obs);
        size=0;
      }
      else
      {
        _current[size++]=c;
      }
    }
    w+=fm.charsWidth(_current,0,size);
    h=fm.getFont().getSize()+1;
    if(h>mh) mh=h;
    res.width=w;
    res.height=mh+_lineSpacing;
  }

	/**
	 * Draw the given prepared line.
	 * @param str prepared line to draw.
	 * @param g graphics to draw onto.
	 * @param fm fontmetrics to use.
	 * @param x x position.
	 * @param y y position.
   * @param obs the image observer in case of bitmap display.
   * @param handles target vector, where all update handles will be put.
	 */
  public void draw(String str,Graphics g,FontMetrics fm,int x,int y,ImageObserver obs,Vector handles)
  {
    if(_current.length<str.length()) _current=new char[str.length()*2];
    int size=0;
    for(int i=0;i<str.length();i++)
    {
      char c=str.charAt(i);
      if((c>=0xE000) && (c<=0xF8FF))
      {
        c-=0xE000;
        g.drawChars(_current,0,size,x,y);
        x+=fm.charsWidth(_current,0,size);
        size=0;
        Object handle=drawBitmapSmiley(g,fm,c,x,y,obs);
        if(handles==null) handles=new Vector();
        handles.insertElementAt(handle,handles.size());
        x+=getBitmapSmileyWidth(c,obs);
      }
      else
      {
        _current[size++]=c;
      }
    }
    g.drawChars(_current,0,size,x,y);
  }
}

