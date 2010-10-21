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

/**
 * A recognized item.
 */
public class DrawResultItem
{
  /**
   * The item rectangle on display, relative to its parent.
   */
  public StyledRectangle rectangle;
  /**
   * The item content. This content is the trimmed version of the stripped content.
   * Characters like parenthesis and braces are also trimmed. For instance, "(text)"
   * gives "text" in the itemized version. Item is used for word recognition.
   */
  public String item;
  /**
   * The original word content.
   */
  public String originalword;
  /**
   * The original word content, but stripped from all special codes.
   */
  public String originalstrippedword;
  /**
   * The parent DrawResult.
   */
  public DrawResult parent;

  public boolean equals(Object o)
  {
    if(!(o instanceof DrawResultItem)) return false;
    DrawResultItem i=(DrawResultItem)o;
    return i.rectangle.equals(rectangle) && i.originalword.equals(originalword) && i.parent.rectangle.equals(parent.rectangle);
  }

  public int hashCode()
  {
    return rectangle.hashCode()+originalword.hashCode();
  }

  public String toString()
  {
    return item+" at "+rectangle+" with parent at "+parent.rectangle;
  }
}

