package irc.gui.pixx;

import java.awt.*;
import java.awt.event.InputEvent;

import irc.*;
import java.util.*;

/**
 * NullItem.
 */
class NullItem
{
}

/**
 * MouseEventConfiguration.
 */
class MouseEventConfiguration
{
  /**
   * Create a new MouseEventConfiguration
   * @param m mouse button mack.
   * @param c click count.
   */
  public MouseEventConfiguration(int m,int c)
  {
    Mask=m;
    ClickCount=c;
  }

  /**
   * Mouse button mask.
   */
  public int Mask;
  /**
   * Click count.
   */
  public int ClickCount;
}

/**
 * PixxConfiguration.
 */
public class PixxConfiguration
{
  private IRCConfiguration _config;
  private PixxColorModel _colorModel=new PixxColorModel();
  private TextProvider _textProvider;

  private RuleList _dockingRules;
  private Hashtable _htable;
  private Hashtable _mouseConfig;
  private Vector _highLightWords;
  private Vector _nickMenuVector;


  private NullItem NULL_ITEM=new NullItem();

  /**
   * Create a new PixxConfiguration
   * @param config original global configuration.
   */
  public PixxConfiguration(IRCConfiguration config)
  {
    _config=config;
    _htable=new Hashtable();
    _mouseConfig=new Hashtable();
    _dockingRules=new RuleList();
    _dockingRules.setDefaultValue(new Boolean(false));
    _highLightWords=new Vector();
    _nickMenuVector=new Vector();
  }

  /**
   * Get the nick menu configuration vector.
   * @return the nick menu vector.
   */
  public synchronized Vector getNickMenuVector()
  {
    return _nickMenuVector;
  }

  /**
   * Set the text provider to be used.
   * @param txt text provider.
   */
  public void setTextProvider(TextProvider txt)
  {
    _textProvider=txt;
  }

  /**
   * Configure the given mouse event name.
   * @param eventName event name.
   * @param button mouse button index.
   * @param count mouse click count.
   */
  public synchronized void setMouseConfiguration(String eventName,int button,int count)
  {
    int mask;
    switch(button)
    {
      case 1:mask=InputEvent.BUTTON1_MASK;break;
      case 2:mask=InputEvent.BUTTON2_MASK;break;
      case 3:mask=InputEvent.BUTTON3_MASK;break;
      default:mask=InputEvent.BUTTON1_MASK;break;
    }
    _mouseConfig.put(eventName.toLowerCase(java.util.Locale.ENGLISH),new MouseEventConfiguration(mask,count));
  }

  /**
   * Check whether the given event name and the given mouse event match.
   * @param eventName mouse event name.
   * @param event mouse event.
   * @return true if events match, false otherwise.
   */
  public synchronized boolean matchMouseConfiguration(String eventName,java.awt.event.MouseEvent event)
  {
    if(getB("ignoreallmouseevents")) return false;
    MouseEventConfiguration config=(MouseEventConfiguration)_mouseConfig.get(eventName.toLowerCase(java.util.Locale.ENGLISH));
    if(config==null) throw new Error(eventName+" : unknown mouse event name");
    if(config.ClickCount!=event.getClickCount()) return false;
    return (event.getModifiers() & config.Mask)!=0;
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
   * Set highlight configuration.
   * @param color hightlight color.
   * @param nick true if nick highlight enabled.
   * @param words hightlight words set.
   */
  public synchronized void setHighLightConfig(int color,boolean nick,Vector words)
  {
    set("highlightcolor",color);
    set("highlightnick",nick);
    _highLightWords=new Vector();
    for(int i=0;i<words.size();i++) _highLightWords.insertElementAt(words.elementAt(i),_highLightWords.size());
  }

  /**
   * Get nick highlight flag.
   * @return nick highlight flag.
   */
  public synchronized boolean highLightNick()
  {
    return getB("highlightnick") && getB("highlight");
  }

  /**
   * Get highlight words.
   * @return enumeration of String.
   */
  public synchronized Enumeration getHighLightWords()
  {
    if(!getB("highlight")) return new Vector().elements();
    return _highLightWords.elements();
  }

  /**
   * Add a word into the highlight word list.
   * @param word new word to add.
   */
  public synchronized void addHighLightWord(String word)
  {
    _highLightWords.insertElementAt(word,_highLightWords.size());
  }
  
  /**
   * Remove a word from the highlight word list.
   * @param word the word to remove.
   */
  public synchronized void removeHighLightWord(String word)
  {
    for(int i=0;i<_highLightWords.size();i++)
    {
      if(((String)_highLightWords.elementAt(i)).equals(word))
      {
        _highLightWords.removeElementAt(i);
        i--;
      }
    }
  }
  
  /**
   * Set the color model to be used.
   * @param model the color model.
   */
  public void setColorModel(PixxColorModel model)
  {
    _colorModel=model;
  }

  /**
   * Get the original IRC configuration.
   * @return original IRCConfiguration.
   */
  public IRCConfiguration getIRCConfiguration()
  {
    return _config;
  }

  /**
   * Get color model in use.
   * @return color model.
   */
  public PixxColorModel getColorModel()
  {
    return _colorModel;
  }

  /**
   * Get the color at given index.
   * @param i index.
   * @return the i'th color.
   */
  public Color getColor(int i)
  {
    return _colorModel.getColor(i);
  }

  /**
   * Set the docking policy associated with the given source type and name.
   * @param type source type.
   * @param name source name.
   * @param action true if source should be undocked, false otherwise.
   */
  public synchronized void setDockingPolicy(String type,String name,boolean action)
  {
    _dockingRules.addRule(new String[] {type,name},new Boolean(action));
  }

  /**
   * Get the docking policy for the given source type and name.
   * @param type source type.
   * @param name source name.
   * @return true if the source should be undocked, false otherwise.
   */
  public synchronized boolean getDockingPolicy(String type,String name)
  {
    return ((Boolean)(_dockingRules.findValue(new String[] {type,name}))).booleanValue();
  }

  /**
   * Get formatted text associated with the given text code, with no parameter.
   * @param code text code.
   * @return formatted text.
   */
  public synchronized String getText(int code)
  {
    if(code<TextProvider.USER_BASE) return _config.getText(code);
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
    if(code<TextProvider.USER_BASE) return _config.getText(code,p1);
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
    if(code<TextProvider.USER_BASE) return _config.getText(code,p1,p2);
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
    if(code<TextProvider.USER_BASE) return _config.getText(code,p1,p2,p3);
    return _textProvider.getString(code,p1,p2,p3);
  }

}
