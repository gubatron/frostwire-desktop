package irc;

/**
 * A source interpretor.
 */
public interface Interpretor
{
  /**
   * Send the given string to the server.
   * @param s the source.
   * @param str the string to send.
   */
  public void sendString(Source s,String str);

  /**
   * Set the next interpretor.
   * @param next interpretor to use. May be null.
   */
  public void setNextInterpretor(Interpretor next);

  /**
   * Get the next interpretor.
   * @return the next interpretor, or null if this interpretor is the last.
   */
  public Interpretor getNextInterpretor();

  /**
   * Check whether the given interpretor is already in the interpretor chain.
   * @param in the interpretor to check.
   * @return true if in is in the chain, false otherwise.
   */
  public boolean isInside(Interpretor in);

  /**
   * Add the given interpretor at the end of this interpretor chain. If in is
   * already in the chain, nothing is done.
   * @param in interpretor to add.
   */
  public void addLast(Interpretor in);
}

