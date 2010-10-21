package irc.style;

/**
 * The word recognizer.
 */
public interface WordRecognizer
{
  /**
   * Try to recognize a word.
   * @param word word to analyse.
   * @return true if this word matches.
   */
  public boolean recognize(String word);

  /**
   * Get this recognizer type.
   * @return recognizer type name.
   */
  public String getType();
}

