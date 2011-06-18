package irc.gui.pixx;

import irc.EventDispatcher;
import irc.ListenerGroup;
import irc.StyleContext;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Extension for the style selector.
 */
public class AWTStyleSelectorEx extends JPanel implements ActionListener,FontSelectorListener
{
  /**
     * 
     */
    private static final long serialVersionUID = -1384171400904617575L;
private ListenerGroup _lis;
  private AWTStyleSelector _selector;
  private FontSelector _fs;
  private JButton _b;

  /**
   * Create a new AWTStyleSelectorEx.
   * @param config global irc configuration.
   */
  public AWTStyleSelectorEx(PixxConfiguration config)
  {
    _fs=new FontSelector(config);
    _selector=new AWTStyleSelector(config);
    setLayout(new BorderLayout());
    add(_selector,BorderLayout.CENTER);
    _b=new NonFocusableButton("Font");
    //_b=new NonFocusableButton(config.getText(PixxTextProvider.GUI_FONT));
    _b.setForeground(config.getColor(PixxColorModel.COLOR_WHITE));
    _b.setBackground(config.getColor(PixxColorModel.COLOR_FRONT));
    _b.addActionListener(this);
    if(config.getB("setfontonstyle")) add(_b,BorderLayout.EAST);
    _lis=new ListenerGroup();
  }

  /**
   * Release this object.
   */
  public void release()
  {
    removeAll();
    _b.removeActionListener(this);
    _selector.release();
    _fs.release();
    _selector=null;
    _fs=null;
  }

  /**
   * Set the style context.
   * @param ct style context.
   */
  public void setStyleContext(StyleContext ct)
  {
    _selector.setStyleContext(ct);
  }

  /**
   * Get prefix to use.
   * @return style prefix.
   */
  public String getPrefix()
  {
    return _selector.getPrefix();
  }

  /**
   * Get the style selector.
   * @return the style selector.
   */
  public AWTStyleSelector getStyleSelector()
  {
    return _selector;    
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addAWTStyleSelectorExListener(AWTStyleSelectorExListener lis)
  {
    _lis.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeAWTStyleSelectorExListener(AWTStyleSelectorExListener lis)
  {
    _lis.removeListener(lis);
  }

  public void actionPerformed(ActionEvent e)
  {
    EventDispatcher.dispatchEventAsync(_fs,"selectFont",new Object[] {this});
  }

  public void fontSelected(Font f)
  {
    if(f!=null) _lis.sendEvent("fontSelected",f);
  }
}
