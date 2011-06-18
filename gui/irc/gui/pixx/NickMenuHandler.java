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

import irc.EventDispatcher;
import irc.Source;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JComponent;

/**
 * Popupmenu handling for nicknames.
 */
public class NickMenuHandler implements ActionListener
{
  private String _selectedNick;
  private String _whoisNick;
  private PopupMenu _menu;
  private Source _source;
  private JComponent _parent;
  private PixxConfiguration _pixxConfiguration;

  /**
   * Create a new NickMenuHandler.
   * @param config the pixx configuration.
   * @param parent a parent component for the popup menu.
   * @param source the source associated with this popup menu.
   */
  public NickMenuHandler(PixxConfiguration config,JComponent parent,Source source)
  {
    _pixxConfiguration=config;
    _parent=parent;
    _source=source;
    _menu=new PopupMenu();
    _menu.addActionListener(this);
    _parent.add(_menu);
  }

  /**
   * Release this instance.
   */
  public void release()
  {
    _menu.removeActionListener(this);
    _parent.remove(_menu);
    _menu=null;
    _parent=null;
    _source=null;
    _pixxConfiguration=null;
  }

  private String parameters(String on,String[] params)
  {
    for(int i=0;i<on.length()-1;i++)
    {
      if(on.charAt(i)=='%')
      {
        char next=on.charAt(i+1);
        if(next=='%')
        {
          String before=on.substring(0,i);
          String after=on.substring(i+2);
          on=before+"%"+after;
        }
        else if((next>='1') && (next<='9'))
        {
          int c=next-'1';
          if(c<params.length)
          {
            String before=on.substring(0,i);
            String after=on.substring(i+2);
            on=before+params[c]+after;
          }
        }
      }
    }
    return on;
  }

  public void actionPerformed(ActionEvent e)
  {
    EventDispatcher.dispatchEventAsync(this,"actionPerformedEff",new Object[] {e});
  }

  /**
   * Internally used.
   * @param e
   */
  public void actionPerformedEff(ActionEvent e)
  {
    String cmd=e.getActionCommand();
    String[] params=new String[] {_selectedNick,_source.getName(),_pixxConfiguration.getIRCConfiguration().formatASL(_whoisNick),_whoisNick};
    for(int j=0;j<_pixxConfiguration.getNickMenuVector().size();j++)
    {
      String[] cmds=(String[])_pixxConfiguration.getNickMenuVector().elementAt(j);
      if(cmds[0].equals(cmd))
      {
        for(int i=1;i<cmds.length;i++)
          _source.sendString(parameters(cmds[i],params));
      }
    }
  }

  /**
   * Open and handle a popup menu for a nickname.
   * @param nick the nickname.
   * @param whois the whois or asl nickname information.
   * @param c the component where the menu is to be displayed. This component
   * must be a child of the parent component for this menu.
   * @param x x-position relative to c.
   * @param y y-position relative to c.
   */
  public void popup(String nick,String whois,JComponent c,int x,int y)
  {
    if(_pixxConfiguration.getNickMenuVector().size()==0) return;
    _selectedNick=nick;
    _whoisNick=whois;

    _menu.removeAll();

    Enumeration<?> keys=_pixxConfiguration.getNickMenuVector().elements();
    while(keys.hasMoreElements())
    {
      String[] v=(String[])keys.nextElement();
      if(v[0].equals("--"))
        _menu.addSeparator();
      else
        _menu.add(new MenuItem(v[0]));
    }

    _menu.show(c,x,y);
  }

}
