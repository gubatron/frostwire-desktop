package irc.style;

/**
 * A word catcher.
 */
public interface WordCatcher
{
  /**
   * Get the type of this word, or null if not found.
   * @param word word to analyse.
   * @return word type, or null if not found.
   */
  public String getType(String word);
}

