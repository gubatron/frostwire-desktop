package irc.gui;

import irc.*;

import java.awt.*;

/**
 * The common root class for all PJIRC user interfaces.
 */
public abstract class IRCInterface extends irc.plugin.Plugin
{
  /**
   * The IRCInterfaceListener group.
   */
  protected ListenerGroup _listenerGroup;

  private Interpretor _nullInterpretor;

  /**
   * Create a new IRCInterface with the given IRCConfiguration instance.
   * @param ircConfiguration the global IRCConfiguration instance.
   */
  public IRCInterface(IRCConfiguration ircConfiguration)
  {
    super(ircConfiguration);
    _listenerGroup=new ListenerGroup();
    _nullInterpretor=new NullInterpretor(_ircConfiguration);
  }

  /**
   * Trigger the "activeChanged" event for all IRCInterfaceListeners.
   * If this method is called in the event thread, event dispatching will
   * be synchroneous. Otherwise, asynchroneous event will be dispatched.
   * @param source the newly activated source.
   */
  protected void triggerActiveChanged(GUISource source)
  {
    if(EventDispatcher.isEventThread())
      _listenerGroup.sendEvent("activeChanged",source,this);
    else
      _listenerGroup.sendEventAsync("activeChanged",source,this);
  }

  /**
   * Add an IRCInterfaceListener on this interface.
   * @param lis the listener to add.
   */
  public void addIRCInterfaceListener(IRCInterfaceListener lis)
  {
    _listenerGroup.addListener(lis);
  }

  /**
   * Remove an existing IRCInterfaceListener from this interface.
   * @param lis the listener to remove.
   */
  public void removeIRCInterfaceListener(IRCInterfaceListener lis)
  {
    _listenerGroup.removeListener(lis);
  }

  /**
   * Get the active source. For instance, return the keyboard focused source. May
   * be null if no particular source should be considered as being active.
   * @return the active gui source.
   */
  public GUISource getActive()
  {
    return null;
  }
  
  /**
   * Set the active source. For instance, the one that should have the keybord
   * focus.
   * @param source the new source to be active.
   */
  public void setActive(GUISource source)
  {
    //nothing here
  }
  
  /**
   * Return the GUISource that belongs to the given source, or null if there is no
   * such mapping available.
   * @param source source to get the GUI source from.
   * @return the GUISource, or null if there is no such mapping.
   */
  public GUISource getGUISource(Source source)
  {
    return null;
  }

  /**
   * Get the default interpretor to be used when an unknown command is entered
   * by the user.
   * @return the default interpretor.
   */
  public Interpretor getInterpretor()
  {
    return _nullInterpretor;
  }

  /**
   * Get the component associated with this interface.
   * @return the interface component, or null if no component is defined.
   */
  public Component getComponent()
  {
    return null;
  }

} //IRCInterface
