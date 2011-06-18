package irc.gui.pixx;

import irc.dcc.*;

/**
 * The AWTDCCChat.
 */
public class AWTDCCChat extends BaseAWTSource
{
  /**
     * 
     */
    private static final long serialVersionUID = 911219386513601491L;

/**
   * Create a new AWTDCCChat.
   * @param config global irc configuration.
   * @param s source DCCChat.
   */
  public AWTDCCChat(PixxConfiguration config,DCCChat s)
  {
    super(config,s);
  }

}

