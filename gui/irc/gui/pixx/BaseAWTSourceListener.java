package irc.gui.pixx;

/**
 * The awt source listener.
 */
public interface BaseAWTSourceListener
{
  /**
   * This source title has changed.
   * @param source the source whose title has changed.
   */
  public void titleChanged(BaseAWTSource source);

  /**
   * An event has occured.
   * @param source the source on whose event has occured.
   */
  public void eventOccured(BaseAWTSource source);

}

