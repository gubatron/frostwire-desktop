package irc.style;

/**
 * The URLRecognizer.
 */
public class URLRecognizer implements WordRecognizer
{
  private boolean isAlpha(String s)
  {
    s=s.toLowerCase(java.util.Locale.ENGLISH);
    for(int i=0;i<s.length();i++) if((s.charAt(i)<'a') || (s.charAt(i)>'z')) return false;
    return true;
  }

  public boolean recognize(String word)
  {
    if(word.startsWith("http://")) return true;
    //if(word.startsWith("ftp://")) return true;
    if(word.startsWith("magnet://")) return true;
    //if(word.startsWith("www.")) return true;
    //if(word.startsWith("ftp.")) return true;
    int a=word.indexOf('.');
    if(a==-1) return false;
    int b=word.lastIndexOf('.');
    if(a==b) return false;
    String ext=word.substring(b+1);
    if(!isAlpha(ext)) return false;
    if((ext.length()==2) || (ext.length()==3)) return true;
    return false;
  }

  public String getType()
  {
    return "url";
  }


}

