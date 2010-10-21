package irc;

/**
 * Root IRC Object.
 */
public class IRCObject
{
  /**
   * The global IRC configuration.
   */
  protected IRCConfiguration _ircConfiguration;

  /**
   * Create a new IRCObject.
   * @param ircConfiguration the global irc configuration.
   */
  public IRCObject(IRCConfiguration ircConfiguration)
  {
    _ircConfiguration=ircConfiguration;
  }

  /**
   * get the irc configuration.
   * @return the global irc configuration.
   */
  public IRCConfiguration getIRCConfiguration()
  {
    return _ircConfiguration;
  }

  /**
   * Release this object. No further method call can be performed on this object.
   */
  public void release()
  {
    //empty default implementation
  }

  /**
   * Get formatted text associated with the given text code, with no parameter.
   * @param code text code.
   * @return formatted text.
   */
  public String getText(int code)
  {
    return _ircConfiguration.getText(code);
  }

  /**
   * Get formatted text associated with the given text code, with one parameter.
   * @param code text code.
   * @param p1 first parameter.
   * @return formatted text.
   */
  public String getText(int code,String p1)
  {
    return _ircConfiguration.getText(code,p1);
  }

  /**
   * Get formatted text associated with the given text code, with two parameters.
   * @param code text code.
   * @param p1 first parameter.
   * @param p2 second parameter.
   * @return formatted text.
   */
  public String getText(int code,String p1,String p2)
  {
    return _ircConfiguration.getText(code,p1,p2);
  }

  /**
   * Get formatted text associated with the given text code, with three parameters.
   * @param code text code.
   * @param p1 first parameter.
   * @param p2 second parameter.
   * @param p3 third parameter.
   * @return formatted text.
   */
  public String getText(int code,String p1,String p2,String p3)
  {
    return _ircConfiguration.getText(code,p1,p2,p3);
  }
}

