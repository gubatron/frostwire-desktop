package irc;

/**
 * An IRC source.
 */
public abstract class IRCSource extends Source
{
  /**
   * Create a new IRCSource.
   * @param config the global configuration.
   * @param s the source IRCServer.
   */
  public IRCSource(IRCConfiguration config,IRCServer s)
  {
    super(config,s);
  }

  /**
   * Get the IRCServer.
   * @return the IRCServer this source is bound to.
   */
  public IRCServer getIRCServer()
  {
    return (IRCServer)_server;
  }

}

