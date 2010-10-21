package irc.gui.pixx;

import java.awt.*;
import java.awt.event.*;
import irc.*;

/**
 * The font selector window.
 */
public class FontSelector implements WindowListener,ActionListener,Runnable
{
  private Choice _name;
  private TextField _size;
  private Button _ok;
  private Frame _f;
  private FontSelectorListener _lis;

  /**
   * Create a new font selector.
   * @param config the irc configuration.
   */
  public FontSelector(PixxConfiguration config)
  {
    _f=new Frame();
    _f.setTitle("Select font");
    _name=new Choice();
    _name.add("Monospaced");
    _name.add("Serif");
    _name.add("SansSerif");
    _name.add("Dialog");
    _name.add("DialogInput");
    _size=new TextField("12");
    _ok=new Button("OK");
    _ok.addActionListener(this);
    Panel p=new Panel();
    _f.add(p);
    p.setLayout(new FlowLayout(FlowLayout.CENTER));
    p.add(_name);
    p.add(_size);
    p.add(_ok);


    //_f.setResizable(false);
    _f.setSize(200,80);
    _f.addWindowListener(this);
  }

  public void run()
  {
    if(_f!=null) _f.dispose();
    _f=null;
  }

  /**
   * Release this object.
   */
  public void release()
  {
    _ok.removeActionListener(this);
    _f.removeWindowListener(this);
    _f.removeAll();
    Thread t=new Thread(this,"Frame disposal thread");
    t.start();
    //_f.dispose();
    //_f=null;
    _lis=null;
  }

  /**
   * Ask for a font to be selected, calling back the given listener once
   * choice is performed.
   * @param lis listener to be called once font is selected.
   */
  public void selectFont(FontSelectorListener lis)
  {
    _lis=lis;
    _f.setVisible(true);
  }

  public void windowActivated(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowClosing(WindowEvent e)
  {
    _f.setVisible(false);
  }

  public void windowDeactivated(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

  private Font getResult()
  {
    String name=_name.getSelectedItem() ;
    int size=12;
    try
    {
      size=(new Integer(_size.getText())).intValue();
    }
    catch(Exception ex)
    {
    }
    return new Font(name,Font.PLAIN,size);
  }

  public void actionPerformed(ActionEvent e)
  {
    _f.setVisible(false);
    Font f=getResult();
    if(_lis!=null) EventDispatcher.dispatchEventAsync(_lis,"fontSelected",new Object[] {f});
  }
}
