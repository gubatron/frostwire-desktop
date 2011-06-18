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

package irc;

import java.util.*;
import java.awt.*;

/**
 * SmileyItem.
 */
class SmileyItemOld
{
  /**
   * Matching string.
   */
  public String match;
  /**
   * Matching image.
   */
  public Image img;

  /**
   * Create a new SmileyItem
   * @param amatch matching string.
   * @param aimg matching image.
   */
  public SmileyItemOld(String amatch,Image aimg)
	{
	  this.match=amatch;
		this.img=aimg;
	}
}

/**
 * Smiley table.
 */
public class SmileyTable
{
  private Vector<SmileyItem> _table;

	/**
	 * Create a new, empty, smiley table.
	 */
	public SmileyTable()
	{
	  _table=new Vector<SmileyItem>();
	}

	/**
	 * Add a smiley in the table.
	 * @param match the macthing text.
	 * @param img image of the smiley.
	 */
	public void addSmiley(String match,Image img)
	{
	  if(img!=null) _table.insertElementAt(new SmileyItem(match,img),_table.size());
	}

	/**
	 * Get the smileys count.
	 * @return the amount of smileys in the table.
	 */
	public int getSize()
	{
	  return _table.size();
	}

	/**
	 * Get the i'th match in the smiley table.
	 * @param index table index.
	 * @return i'th smiley match.
	 */
	public String getMatch(int index)
	{
	  SmileyItem item=(SmileyItem)_table.elementAt(index);
		return item.match;
	}

	/**
	 * Get the i'th image in the smiley table.
	 * @param index table index.
	 * @return i'th image.
	 */
	public Image getImage(int index)
	{
	  if(index<0) return null;
		if(index>=getSize()) return null;
	  SmileyItem item=(SmileyItem)_table.elementAt(index);
		return item.img;
	}
}
