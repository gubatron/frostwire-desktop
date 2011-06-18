package irc;

import java.awt.*;
import java.util.*;

import irc.security.*;

/**
 * NullItem.
 */
class NullItem
{
  //empty
}

/**
 * Global IRC configuration. Any call is synchronized and can be performed from
 * any thread.
 */
public class IRCConfiguration
{
  /**
   * Image is centered.
   */
  public static final int TILING_CENTER=0;
  /**
   * Image is stretched.
   */
  public static final int TILING_STRETCH=1;
  /**
   * Image is tiled.
   */
  public static final int TILING_TILE=2;
  /**
   * Image position is fixed.
   */
  public static final int TILING_FIXED=3;
  
  /**
   * Image position is fixed on the left.
   */
  public static final int TILING_HORIZONTAL_LEFT=0;
  /**
   * Image position is fixed on the right.
   */
  public static final int TILING_HORIZONTAL_RIGHT=256;
  /**
   * Image position if fixed upside.
   */
  public static final int TILING_VERTICAL_UP=0;
  /**
   * Image position is fixed downside.
   */
  public static final int TILING_VERTICAL_DOWN=512;

  private NullItem NULL_ITEM=new NullItem();

  private TextProvider _textProvider;
  private ImageLoader _loader;
  private SmileyLoader _smileyLoader;
  private URLHandler _handler;
  private FileHandler _file;
  private AudioConfiguration _audioConfig;

  private RuleList _backgroundImageRules;
  private RuleList _backgroundTilingRules;
  private RuleList _colorsRules;
  private RuleList _fontRules;

  private SmileyTable _table; //current smileys table. Could be erased if user choose to disable smilies
  private SmileyTable _backuptable; //never is erased, it replaces the _table
  private ListHandler _mayJoinList;
  private ListHandler _mayLeaveList;
  private ListHandler _mayCommandList;
  private Hashtable<String, Object> _htable;
  private SecurityProvider _provider;
  private ParameterProvider _paramProvider;
  private ParameterProvider _interfaceParamProvider;

  private String _guiInfoString;
  
  private String[] _initCommands;

  /**
   * Create a new IRCConfiguration.
   * @param text text provider to use.
   * @param handler URL handler to use.
   * @param loader image loader to use.
   * @param sound sound handler to use.
   * @param file file handler to use.
   * @param paramProvider parameter provider.
   * @param interfaceParamProvider interface parameter provider.
   */
  public IRCConfiguration(TextProvider text,URLHandler handler,ImageLoader loader,SoundHandler sound,FileHandler file,ParameterProvider paramProvider,ParameterProvider interfaceParamProvider)
  {
    _provider=new SecurityProvider();

    _paramProvider=paramProvider;
    _interfaceParamProvider=interfaceParamProvider;

    _htable=new Hashtable<String, Object>();

    _backgroundImageRules=new RuleList();
    _backgroundTilingRules=new RuleList();
    _backgroundTilingRules.setDefaultValue(new Integer(0));

    _fontRules=new RuleList();
    _fontRules.setDefaultValue(new Font("SanSerif",Font.PLAIN,13));

    _colorsRules=new RuleList();
    Color[] def=new Color[16];
    loadDefaultColors(def);
    _colorsRules.setDefaultValue(def);

		_audioConfig=new AudioConfiguration(sound);
    _table=new SmileyTable();
    _backuptable=new SmileyTable();
    _file=file;
    _loader=loader;
    _textProvider=text;
    _handler=handler;

    _guiInfoString="";
    
    _initCommands=new String[0];
  }

  /**
   * Create a new dummy IRCConfiguration. All parameters are set to default.
   * Note that the 'gui' parameter is not set.
   * @return a new IRCConfiguration instance.
   */
  public static IRCConfiguration createDummyIRCConfiguration()
  {
    try
    {
      FileHandler file=new LocalFileHandler();
      StreamParameterProvider provider=new StreamParameterProvider(null);
      ConfigurationLoader loader=new ConfigurationLoader(provider,
    		  new NullURLHandler(),
    		  new AWTImageLoader(),
    		  new NullSoundHandler(),
    		  file);
      return loader.loadIRCConfiguration();
    }
    catch(Exception ex)
    {
      /* should not reach here : bug */
      throw new Error("Error creating dummy IRCConfiguration : "+ex);
    }
  }

  /**
   * Get the initialisation commands.
   * @return the initialisation commands.
   */
  public String[] getInitialization()
  {
    return _initCommands;
  }
  
  /**
   * Set the initialisation commands.
   * @param init the initialisation commands.
   */
  public void setInitialisation(String[] init)
  {
    _initCommands=init;
  }
  
  /**
   * Get the GUI information string.
   * @return the GUI info string.
   */
  public String getGUIInfoString()
  {
    return _guiInfoString;
  }

  /**
   * Set the GUI information string.
   * @param string the GUI information string.
   */
  public void setGUIInfoString(String string)
  {
    _guiInfoString=string;
  }

  /**
   * Get the file handler.
   * @return the file handler.
   */
  public FileHandler getFileHandler()
  {
    return _file;
  }

  /**
   * Display the about page.
   */
  public void displayAboutPage()
  {
    new AboutDialog(this);
  }

  /**
   * Get the parameter provider used for creating this IRCConfiguration.
   * @return the parameter provider.
   */
  public ParameterProvider getParameterProvider()
  {
    return _paramProvider;
  }

  /**
   * Get the parameter provider whose should be used by an user interface.
   * @return the interface parameter provider.
   */
  public ParameterProvider getInterfaceParameterProvider()
  {
    return _interfaceParamProvider;
  }

  /**
   * Get the high version number.
   * @return the high version number.
   */
  public int getVersionHigh()
  {
    return 2;
  }

  /**
   * Get the middle version number.
   * @return the middle version number.
   */
  public int getVersionMed()
  {
    return 2;
  }

  /**
   * Get the low version number.
   * @return the low version number.
   */
  public int getVersionLow()
  {
    return 1;
  }

  /**
   * Get the version modifiers.
   * @return version modifiers.
   */
  public String getVersionModifiers()
  {
    return "";
  }

  /**
   * Get version number as a string.
   * @return high.med.lowmod version number.
   */
  public String getVersion()
  {
    return getVersionHigh()+"."+getVersionMed()+"."+getVersionLow()+getVersionModifiers();
  }

  /**
   * Get the security provider.
   * @return the security provider.
   */
  public SecurityProvider getSecurityProvider()
  {
    return _provider;
  }

  /**
   * Set the given property to the given value. This value may be null.
   * @param key property name.
   * @param obj property value.
   */
  public synchronized void set(String key,Object obj)
  {
    if(obj==null) obj=NULL_ITEM;
    _htable.put(key.toLowerCase(java.util.Locale.ENGLISH),obj);
  }

  /**
   * Set the given property to the given int value.
   * @param key property name.
   * @param val property value.
   */
  public synchronized void set(String key,int val)
  {
    set(key,new Integer(val));
  }

  /**
   * Set the given property to the given boolean value.
   * @param key property name.
   * @param val property value.
   */
  public synchronized void set(String key,boolean val)
  {
    set(key,new Boolean(val));
  }

  /**
   * Get the given property value.
   * @param key property name.
   * @return the property value.
   * @throws RuntimeException if the property is unknown.
   */
  public synchronized Object get(String key)
  {
    Object v=_htable.get(key.toLowerCase(java.util.Locale.ENGLISH));
    if(v==null) throw new RuntimeException("Unknown configuration property "+key);
    if(v==NULL_ITEM) v=null;
    return v;
  }

  /**
   * Get the given property value as an int value.
   * @param key property name.
   * @return the property value.
   * @throws RuntimeException if the property is unknown.
   */
  public synchronized int getI(String key)
  {
    Integer i=(Integer)get(key);
    return i.intValue();
  }

  /**
   * Get the given property value as a boolean value.
   * @param key property name.
   * @return the property value.
   * @throws RuntimeException if the property is unknown.
   */
  public synchronized boolean getB(String key)
  {
    Boolean b=(Boolean)get(key);
    return b.booleanValue();
  }

  /**
   * Get the given property value as String value.
   * @param key property name.
   * @return the property value.
   * @throws RuntimeException if the property is unknown.
   */
  public synchronized String getS(String key)
  {
    return (String)get(key);
  }

  /**
   * Test wether the given channel may be left.
   * @param channel channel to be left.
   * @return true if channel may be left, false otherwise.
   */
  public synchronized boolean mayLeave(String channel)
  {
    return _mayLeaveList.inList(channel);
  }

  /**
   * Test wether the given channel may be joined.
   * @param channel channel to be joined.
   * @return true if channel may be joined, false otherwise.
   */
  public synchronized boolean mayJoin(String channel)
  {
    return _mayJoinList.inList(channel);
  }

  /**
   * Set the "may join" list.
   * @param list join list.
   */
  public synchronized void setJoinList(String list)
  {
    _mayJoinList=new ListHandler(list);
  }

  /**
   * Set the "may leave" list.
   * @param list leave list.
   */
  public synchronized void setLeaveList(String list)
  {
    _mayLeaveList=new ListHandler(list);
  }

  /**
   * Set the authorized command list.
   * @param list authorized command list.
   */
  public synchronized void setCommandList(String list)
  {
    _mayCommandList=new ListHandler(list);
  }

  /**
   * Check wether the given command may be executed by the user.
   * @param cmd command to test, not prefixed by /.
   * @return true if command may be executed, false otherwise.
   */
  public synchronized boolean mayCommand(String cmd)
  {
    if(cmd.startsWith("/")) cmd=cmd.substring(1);
    return _mayCommandList.inList(cmd);
  }

  /**
   * Get the background image associated with the given source type and name.
   * @param type source type.
   * @param name source name.
   * @return background image, or null if no image is to be displayed.
   */
  public synchronized Image getBackgroundImage(String type,String name)
  {
    if(!getB("style:backgroundImage")) return null;
    return (Image)_backgroundImageRules.findValue(new String[] {type,name});
  }

  /**
   * Get the background image tiling associated with the given source type and name.
   * @param type source type.
   * @param name source name.
   * @return tiling mode for background image.
   */
  public synchronized int getBackgroundTiling(String type,String name)
  {
    return ((Integer)_backgroundTilingRules.findValue(new String[] {type,name})).intValue();
  }

  /**
   * Set the background image to be used for the given source type and name.
   * @param type source type.
   * @param name source name.
   * @param image image name.
   */
  public synchronized void setBackgroundImage(String type,String name,String image)
  {
    _backgroundImageRules.addRule(new String[] {type,name},getImageLoader().getImage(image));
  }

  /**
   * Set the background image tiling to be used for the given source type and name.
   * @param type source type.
   * @param name source name.
   * @param tiling tiling mode to be used.
   */
  public synchronized void setBackgroundTiling(String type,String name,int tiling)
  {
    _backgroundTilingRules.addRule(new String[] {type,name},new Integer(tiling));
  }

  /**
   * Set the font to be used for the given source type and name.
   * @param type source type.
   * @param name source name.
   * @param f font to be used.
   */
  public synchronized void setFont(String type,String name,Font f)
  {
    _fontRules.addRule(new String[] {type,name},f);
  }


  /**
   * Format a string given asl info and current settings.
   * @param info user whois information.
   * @return formatted asl information. An empty string shouldn't be displayed.
   */
  public synchronized String formatASL(String info)
  {
    String noprefix=getS("noasldisplayprefix");
    if((noprefix.length()>0) && (info.startsWith(noprefix))) return "";
    
    String separator=getS("aslseparatorstring");
    if(separator.length()>0)
    {
      int pos=info.indexOf(separator);
      if(pos>=0) info=info.substring(0,pos);
    }
    
    String orig=info;
    int pos=info.indexOf(' ');
    if(pos<0) return orig;

    String age=info.substring(0,pos).trim();
    info=info.substring(pos+1).trim();
    pos=info.indexOf(' ');
    if(pos<0) return orig;
    String gender=info.substring(0,pos).trim().toLowerCase(java.util.Locale.ENGLISH);
    String location=info.substring(pos+1).trim();

    int text;
    if(gender.equals(getS("aslmale").toLowerCase(java.util.Locale.ENGLISH))) text=IRCTextProvider.ASL_MALE;
    else if(gender.equals(getS("aslfemale").toLowerCase(java.util.Locale.ENGLISH))) text=IRCTextProvider.ASL_FEMALE;
    else if(gender.equals(getS("aslunknown").toLowerCase(java.util.Locale.ENGLISH))) text=IRCTextProvider.ASL_UNKNOWN;
    else return orig;

    return getText(text,age,location);
  }

  /**
   * Find the correct ASL color for the given info.
   * @param info user whois information.
   * @param male male color.
   * @param femeale femeale color.
   * @param undef undefined gender color.
   * @return asl color.
   */
  public synchronized Color getASLColor(String info,Color male,Color femeale,Color undef)
  {
    int pos=info.indexOf(' ');
    if(pos<0) return undef;
    info=info.substring(pos).trim();
    pos=info.indexOf(' ');
    if(pos<0) return undef;
    info=info.substring(0,pos).trim().toLowerCase(java.util.Locale.ENGLISH);
    if(info.equals(getS("aslmale").toLowerCase(java.util.Locale.ENGLISH))) return male;
    if(info.equals(getS("aslfemale").toLowerCase(java.util.Locale.ENGLISH))) return femeale;
    return undef;
  }

  /**
   * Get the audio configuration.
   * @return the audio configuration.
   */
  public AudioConfiguration getAudioConfiguration()
	{
	  return _audioConfig;
	}

  /**
   * Get the default style context.
   * @return the default style context.
   */
  public synchronized StyleContext getDefaultStyleContext()
	{
	  return getStyleContext("","");
	}

  /**
   * Get the style context associated with the given source type and name.
   * @param type source name.
   * @param name source name.
   * @return associated style context.
   */
  public synchronized StyleContext getStyleContext(String type,String name)
	{
    StyleContext ctx=new StyleContext();
		ctx.type=type;
		ctx.name=name;
	  return ctx;
	}

  /**
   * Load the given color array with the default source colors. The array
   * must be of length 16.
   * @param cols color array to be filled.
   */
  public synchronized void loadDefaultColors(Color[] cols)
	{
    cols[0]=new Color(0xFFFFFF);
    cols[1]=new Color(0x000000);
    cols[2]=new Color(0x00007F);
    cols[3]=new Color(0x009300);
    cols[4]=new Color(0xFF0000);
    cols[5]=new Color(0x7F0000);
    cols[6]=new Color(0x9C009C);
    cols[7]=new Color(0xFC7F00);
    cols[8]=new Color(0xFFFF00);
    cols[9]=new Color(0x00FC00);
    cols[10]=new Color(0x009393);
    cols[11]=new Color(0x00FFFF);
    cols[12]=new Color(0x0000FC);
    cols[13]=new Color(0xFF00FF);
    cols[14]=new Color(0x7F7F7F);
    cols[15]=new Color(0xD2D2D2);
	}

  /**
   * Set the source colors.
   * @param type source type.
   * @param name source name.
   * @param c color array.
   */
  public synchronized void setSourceColor(String type,String name,Color c[])
	{
    _colorsRules.addRule(new String[] {type,name},c);
	}

	/**
	 * Get the colors associated with the given style context.
	 * @param context the context to get colors from.
	 * @return color array for the given context.
	 */
  public synchronized Color[] getStyleColors(StyleContext context)
	{
		return (Color[])_colorsRules.findValue(new String[] {context.type,context.name});
	}

  /**
   * Get the font associated with the given style context.
   * @param context the context to get font from.
   * @return font for the given context.
   */
  public synchronized Font getStyleFont(StyleContext context)
  {
    return (Font)_fontRules.findValue(new String[] {context.type,context.name});
  }

  /**
   * Get the image background associated with the given style context.
   * @param context the context to get background from.
   * @return image for the given context.
   */
  public synchronized Image getStyleBackgroundImage(StyleContext context)
  {
    return getBackgroundImage(context.type,context.name);
  }

  /**
   * Get the tiling background associated with the given style context.
   * @param context the context to get tiling from.
   * @return tiling for the given context.
   */
  public synchronized int getStyleBackgroundTiling(StyleContext context)
  {
    return getBackgroundTiling(context.type,context.name);
  }

  private ImageLoader getSmileyLoader() {
	  if (_smileyLoader == null) {
		  _smileyLoader = new SmileyLoader();
	  }
	  return _smileyLoader;
  }

  
	/**
	 * Add a smiley in the smiley table.
	 * @param match the matching text to replace.
	 * @param file image file name.
	 */
  public synchronized void addSmiley(String match,String file)
	{
	  _table.addSmiley(match,getSmileyLoader().getImage(file));
	  _backuptable.addSmiley(match,getSmileyLoader().getImage(file));
	}

  /**
   * Get the image loader.
   * @return the image loader.
   */
  public ImageLoader getImageLoader()
	{
	  return _loader;
	}

  /**
   * Get the smileys table.
   * @return the smiley table.
   */
  public SmileyTable getSmileyTable()
	{
	  return _table;
	}

  /**
   * Reset the smileys table.
   * @return an empty smiley table.
   */
  public SmileyTable resetSmileyTable()
	{	  	
          return _table=new SmileyTable();
	}
  /**
   * Restore the smileys table.
   * @return the original smiley table.
   */
  public SmileyTable restoreSmileyTable()
	{		  	  
          return _table=_backuptable; // restores the smiley table   
	}
  /**
   * Reload the smileys table.
   * @return the final status.
   */
  public String reloadSmileyTable()
	{
	  int tsize=_table.getSize(); // table size	  
	  System.out.print("The Smiley Table has been changed, previous value: ");
	  System.out.println(tsize);
	  if (tsize == 0) {
		  System.out.println("Smileys are now enabled");
		  restoreSmileyTable();
		  return "Smileys are now enabled";
	  } else {
		  System.out.println("Smileys are now disabled");
		  resetSmileyTable();
		  return "Smileys are now disabled";
	  }
         
	}  
  /**
   * Return wether asl should be processed, because asl field is enabled or floating
   * asl is enabled.
   * @return master asl status.
   */
  public synchronized boolean getASLMaster()
  {
    return getB("asl") | getB("style:floatingasl");
  }

  /**
   * Get URLHandler.
   * @return URLHandler.
   */
  public URLHandler getURLHandler()
  {
    return _handler;
  }

  /**
   * Get text provider.
   * @return text provider.
   */
  public TextProvider getTextProvider()
  {
    return _textProvider;
  }

  /**
   * Report an internal error.
   * @param message error message.
   * @param ex optional (can be null) cause exception.
   * @param mail mail address where the bug report should be sent.
   */
  public void internalError(String message,Throwable ex,String mail)
  {
    System.err.println("************ Internal error ************");
    System.err.println("Please submit a bug report to "+mail+" including the following information:");
    System.err.println("Message:");
    System.err.println(message);
    
    if(ex!=null)
    {
      System.err.println("Root cause:");
      ex.printStackTrace();
    }
    System.err.println("Stack trace:");
    Thread.dumpStack();
  }
  
  /**
   * Get formatted text associated with the given text code, with no parameter.
   * @param code text code.
   * @return formatted text.
   */
  public synchronized String getText(int code)
  {
    return _textProvider.getString(code);
  }

  /**
   * Get formatted text associated with the given text code, with one parameter.
   * @param code text code.
   * @param p1 first parameter.
   * @return formatted text.
   */
  public synchronized String getText(int code,String p1)
  {
    return _textProvider.getString(code,p1);
  }

  /**
   * Get formatted text associated with the given text code, with two parameters.
   * @param code text code.
   * @param p1 first parameter.
   * @param p2 second parameter.
   * @return formatted text.
   */
  public synchronized String getText(int code,String p1,String p2)
  {
    return _textProvider.getString(code,p1,p2);
  }

  /**
   * Get formatted text associated with the given text code, with three parameters.
   * @param code text code.
   * @param p1 first parameter.
   * @param p2 second parameter.
   * @param p3 third parameter.
   * @return formatted text.
   */
  public synchronized String getText(int code,String p1,String p2,String p3)
  {
    return _textProvider.getString(code,p1,p2,p3);
  }

}


