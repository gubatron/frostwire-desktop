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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The about dialog.
 */
public class AboutDialog extends WindowAdapter
{
  private JFrame _aboutFrame;

  /**
   * Open and display the about dialog, using the given IRCConfiguration.
   * @param config the IRCConfiguration.
   */
  public AboutDialog(IRCConfiguration config)
  {
    displayAboutPage(config);
  }

  private JLabel createLabel(String text)
  {
    JLabel b=new JLabel(text,JLabel.CENTER);
    b.setFont(new Font("",Font.PLAIN,12));
    return b;
  }

  private void displayAboutPage(IRCConfiguration config)
    {
    if(_aboutFrame!=null) return;
    _aboutFrame=new JFrame();
    _aboutFrame.setTitle(config.getText(IRCTextProvider.ABOUT_ABOUT));
    _aboutFrame.setLayout(new BorderLayout());
    _aboutFrame.setFont(new Font("",Font.PLAIN,12));

    JPanel text=new JPanel();

    text.setLayout(new GridLayout(20,1));
    text.add(createLabel("PJIRC v"+config.getVersion()));
    text.add(new JPanel());
    text.add(createLabel(config.getText(IRCTextProvider.ABOUT_GPL)));
    text.add(new JPanel());
    text.add(createLabel(config.getText(IRCTextProvider.ABOUT_PROGRAMMING)+" : Philippe Detournay alias Plouf (theplouf@yahoo.com)"));
    text.add(createLabel(config.getText(IRCTextProvider.ABOUT_DESIGN)+" : Raphael Seegmuller chez pixxservices.com (pixxservices@pixxservices.com)"));
    text.add(new JPanel());
    text.add(createLabel(config.getText(IRCTextProvider.ABOUT_THANKS)));
    text.add(new JPanel());
    text.add(createLabel("Mandragor : www.mandragor.org"));
    text.add(createLabel("Diboo : www.diboo.net"));
    text.add(createLabel("Kombat Falcon.be Jerarckill Red Spider"));
    text.add(createLabel("Ezequiel Jiquera"));
    text.add(new JPanel());
    text.add(createLabel(config.getText(IRCTextProvider.ABOUT_SUPPORT)));
    text.add(new JPanel());
    text.add(createLabel(config.getGUIInfoString()));
    text.add(new JPanel());
    text.add(createLabel("http://www.pjirc.com"));
    text.add(createLabel("http://www.pjirc.it"));
    _aboutFrame.addWindowListener(this);
    _aboutFrame.add(text,BorderLayout.CENTER);

    _aboutFrame.setSize(500,300);
    _aboutFrame.setResizable(false);
    //_aboutFrame.show();
    _aboutFrame.setVisible(true);
  }

  public void windowClosed(WindowEvent e)
  {
    _aboutFrame.removeWindowListener(this);
    _aboutFrame=null;
  }

  public void windowClosing(WindowEvent e)
  {
    //_aboutFrame.hide();
	 _aboutFrame.setVisible(false);
    _aboutFrame.dispose();
  }
}
