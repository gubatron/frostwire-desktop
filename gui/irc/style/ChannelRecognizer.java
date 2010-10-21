package irc.style;

/**
 * The channel recognizer.
 */
public class ChannelRecognizer implements WordRecognizer
{
  public boolean recognize(String word)
  {
    return word.startsWith("#") && (word.length()>0);
  }

  public String getType()
  {
    return "channel";
  }

}

