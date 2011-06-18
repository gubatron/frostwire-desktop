package irc.gui.pixx;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * A progress bar panel.
 */
public class AWTProgressBar extends JPanel
{

  /**
     * 
     */
    private static final long serialVersionUID = -2123371744045378595L;
private double _v;
  private Color _c;

  /**
   * Set the value, between 0 and 1 inclusive.
   * @param v the value, between 0 and 1 inclusive.
   */
  public void setValue(double v)
  {
    _v=v;
  }

  /**
   * Set the display color.
   * @param c the display color.
   */
  public void setColor(Color c)
  {
    _c=c;
  }

  public void paint(Graphics g)
  {
    super.paint(g);
    int w=getSize().width;
    int h=getSize().height;

    int pos=(int)(_v*w);
    g.setColor(_c);
    g.fillRect(0,0,pos,h);
    g.setColor(Color.white);
    g.fillRect(pos,0,w-pos,h);
  }

}

