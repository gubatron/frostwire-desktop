package irc.style;

/**
 * Recognizer for list of words.
 */
public class WordListRecognizer implements WordRecognizer
{
  private String[] _list;

  /**
   * Create a new WordListRecognizer.
   */
  public WordListRecognizer()
  {
    setList(new String[0]);
  }

  /**
   * Set the list of words to recognize.
   * @param list the list of words.
   */
  public void setList(String[] list)
  {
    _list=new String[list.length];
    for(int i=0;i<list.length;i++) _list[i]=list[i].toLowerCase(java.util.Locale.ENGLISH);
  }

  public boolean recognize(String word)
  {
    String lcase=word.toLowerCase(java.util.Locale.ENGLISH);
    for(int i=0;i<_list.length;i++) if(lcase.equals(_list[i])) return true;
    return false;
  }

  public String getType()
  {
    return "wordlist";
  }

}

