package irc.gui.pixx;
import irc.*;

/**
 * The AWTDefaultSource.
 */
public class AWTDefaultSource extends BaseAWTSource
{
  /**
   * Create a new AWTDefaultSource.
   * @param config the global irc configuration.
   * @param source the default source.
   */
  public AWTDefaultSource(PixxConfiguration config,DefaultSource source)
  {
    super(config,source);
    setTitle("FrostWire Chat");
  }
}

