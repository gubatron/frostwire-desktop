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

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * A scrollable nick list.
 */
public class ScrollablePixxNickList extends PixxPanel implements PixxScrollBarListener
{
  /**
     * 
     */
    private static final long serialVersionUID = 4546462702612938581L;
private PixxNickList _list;
  private PixxVerticalScrollBar _scroll;

  /**
   * Create a new ScrollablePixxNickList.
   * @param config the global irc configuration.
   * @param prefixes known nick prefixes.
   */
  public ScrollablePixxNickList(PixxConfiguration config,char[] prefixes)
  {
    super(config);
    setLayout(new BorderLayout());
    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());

    _list=new PixxNickList(config,prefixes);
    _scroll=new PixxVerticalScrollBar(config,0,0,0.1);
    _scroll.addPixxScrollBarListener(this);
    p.add(_list,BorderLayout.CENTER);
    p.add(_scroll,BorderLayout.EAST);

    add(p,BorderLayout.CENTER);
    add(new PixxSeparator(PixxSeparator.BORDER_LEFT),BorderLayout.WEST);
    add(new PixxSeparator(PixxSeparator.BORDER_RIGHT),BorderLayout.EAST);
    add(new PixxSeparator(PixxSeparator.BORDER_UP),BorderLayout.NORTH);
    add(new PixxSeparator(PixxSeparator.BORDER_DOWN),BorderLayout.SOUTH);
  }

  public void release()
  {
    _scroll.removePixxScrollBarListener(this);
    _list.release();
    _scroll.release();
    _list=null;
    _scroll=null;
    super.release();
  }

  /**
   * Add a listener.
   * @param lis the listener to add.
   */
  public void addPixxNickListListener(PixxNickListListener lis)
  {
    _list.addPixxNickListListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis the listener to remove.
   */
  public void removePixxNickListListener(PixxNickListListener lis)
  {
    _list.removePixxNickListListener(lis);
  }

  /**
   * Set all the nicks.
   * @param nicks new nick list.
   */
  public void set(String[] nicks)
  {
    _list.set(nicks);
    _scroll.setMaximum(_list.getNickCount()-1);
  }

  /**
   * Add a nick.
   * @param nick nick to add.
   */
  public void add(String nick)
  {
    _list.add(nick);
    _scroll.setMaximum(_list.getNickCount()-1);
  }

  /**
   * Remove all nicks.
   */
  public void removeAll()
  {
    _list.removeAll();
    _scroll.setMaximum(_list.getNickCount()-1);
  }

  public void valueChanged(PixxScrollBar pixScrollBar)
  {
    _list.setBase(pixScrollBar.getValue());
  }

  /**
   * Clear any off-screen ressources. The next display might be slower.
   */
  public void dispose()
  {
    _list.dispose();
  }

}

