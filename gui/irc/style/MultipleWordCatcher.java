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

import java.util.*;

/**
 * Multiple word catcher.
 */
public class MultipleWordCatcher implements WordCatcher
{
  private Vector<WordRecognizer> _recognizers;

  /**
   * Create a new MultipleWordCatcher.
   */
  public MultipleWordCatcher()
  {
    _recognizers=new Vector<WordRecognizer>();
  }

  /**
   * Add a recognizer in the list.
   * @param wr recognizer to add.
   */
  public void addRecognizer(WordRecognizer wr)
  {
    _recognizers.insertElementAt(wr,_recognizers.size());
  }

  public String getType(String word)
  {
    Enumeration<WordRecognizer> e=_recognizers.elements();
    while(e.hasMoreElements())
    {
      WordRecognizer wr=(WordRecognizer)e.nextElement();
      if(wr.recognize(word)) return wr.getType();
    }
    return null;
  }
}

