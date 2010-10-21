package irc.gui.common;

import java.awt.*;
import java.awt.event.*;
import irc.*;

/**
 * A panel that can be docked.
 */
public class DockablePanel extends Panel implements WindowListener,Runnable
{
  private Component _comp;
  private boolean _docked;
  private Frame _frame;
  private int _behaviour;
  private ListenerGroup _listeners;

  /**
   * Closing behaviour: the panel will be docked upon undocked window closure.  
   */
  public static final int DOCK_ON_CLOSE=0;

  /**
   * Closing behaviour: nothing will be done upon undocked window closure.  
   */
  public static final int DO_NOTHING_ON_CLOSE=1;

  /**
   * Create a new Dockable Panel using the given component inside, and the given color.
   * @param c the unique component of this panel.
   * @param col the background color when the component is undocked.
   */
  public DockablePanel(Component c,Color col)
  {
    setBackground(col);
    setLayout(new BorderLayout());
    _comp=c;
    _docked=true;
    add(_comp,BorderLayout.CENTER);
    validate();

    _frame=new Frame();
    _frame.setLayout(new BorderLayout());
    _frame.addWindowListener(this);
    
    _behaviour=DOCK_ON_CLOSE;
    _listeners=new ListenerGroup();
  }

  /**
   * Add a new dockable panel listener.
   * @param lis the listener to add.
   */
  public void addDockablePanelListener(DockablePanelListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Removes a dockable panel listener.
   * @param lis the listener to remove.
   */  
  public void removeDockablePanelListener(DockablePanelListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Set the undocked window closing behaviour.
   * @param behaviour the new behaviour. By default, the behaviour is DOCK_ON_CLOSE.
   */
  public void setClosingBehaviour(int behaviour)
  {
    _behaviour=behaviour;
  }
  
  /**
   * Get the undocked window closing behaviour.
   * @return the window closing behaviour.
   */
  public int getClosingBehaviour()
  {
    return _behaviour;
  }

  /**
   * Get the component.
   * @return the component.
   */
  public Component getComponent()
  {
    return _comp;
  }

  /**
   * Undock the panel.
   * @param windowTitle the undocked window title.
   */
  public void undock(String windowTitle)
  {
    if(_comp==null) return;
    if(!_docked) return;
    _docked=false;
    _comp.setVisible(true);
    remove(_comp);
    validate();
    _frame.add(_comp,BorderLayout.CENTER);
    _frame.pack();
    _frame.setTitle(windowTitle);
    _frame.setVisible(true);
  }

  /**
   * Dock the panel.
   */
  public void dock()
  {
    if(_comp==null) return;
    if(_docked) return;
    _docked=true;
    _comp.setVisible(false);
    _frame.setVisible(false);
    _frame.remove(_comp);

    add(_comp,BorderLayout.CENTER);
    _comp.setVisible(isVisible());
    validate();
  }

  /**
   * Check the dock state.
   * @return if docked, false otherwise.
   */
  public boolean isDocked()
  {
    return _docked;
  }

  /**
   * If the source is undocked, this method will bring the
   * undocked window to the front of the screen.
   */
  public void bring()
  {
    _frame.toFront();
  }

  public void run()
  {
    if(_frame!=null) _frame.dispose();
    _frame=null;
  }

  /**
   * Release this object.
   */
  public void release()
  {
    if(_frame==null) return;
    dock();
    _frame.removeAll();
    removeAll();
    _frame.removeWindowListener(this);
    //EventDispatcher.dispatchEventAsync(this,"disposeFrame",new Object[0]);
    Thread t=new Thread(this,"Frame disposal thread");
    t.start();
    _comp=null;
  }

  public void setVisible(boolean b)
  {
    if(_comp==null) return;
    if(_docked) _comp.setVisible(b);
    super.setVisible(b);
  }

  public void windowActivated(WindowEvent e)
  {
    //nothing here...
  }

  public void windowClosed(WindowEvent e)
  {
    _listeners.sendEventAsync("DockablePanelWindowClosed",this);
  }

  public void windowClosing(WindowEvent e)
  {
    _listeners.sendEventAsync("DockablePanelWindowClosing",this);
    if(_behaviour==DOCK_ON_CLOSE)
      EventDispatcher.dispatchEventAsync(this,"dock",new Object[0]);
  }

  public void windowDeactivated(WindowEvent e)
  {
    //nothing here...
  }

  public void windowDeiconified(WindowEvent e)
  {
    //nothing here...
  }

  public void windowIconified(WindowEvent e)
  {
    //nothing here...
  }

  public void windowOpened(WindowEvent e)
  {
    //nothing here...
  }

}
