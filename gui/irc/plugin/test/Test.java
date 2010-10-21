package irc.plugin.test;

import irc.*;
import irc.plugin.*;

/**
 * Simple test plugin.
 */
public class Test extends Plugin implements SourceListener
{
  /**
   * Create a new Test
   * @param config
   */
  public Test(IRCConfiguration config)
  {
    super(config);
  }

  public void sourceCreated(Source source,Boolean bring)
  {
    source.addSourceListener(this);
  }

  public void sourceRemoved(Source source)
  {
    source.removeSourceListener(this);
  }

  public void messageReceived(String nick,String msg,Source source)
  {
    if(msg.startsWith("!hello")) source.sendUserString("World!");
  }

  public void reportReceived(String message,Source source)
  {
    //default empty implementation...
  }

  public void noticeReceived(String nick,String message,Source source)
  {
    //default empty implementation...
  }

  public void action(String nick,String msg,Source source)
  {
    //default empty implementation...
  }

  public void clear(Source source)
  {
    //default empty implementation...
  }

}
