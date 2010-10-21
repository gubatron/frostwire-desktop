package irc.gui.pixx;

import irc.*;

import java.util.*;
import java.awt.*;

/**
 * Toolkit for Configuration creation.
 */
public class PixxConfigurationLoader
{
  private IRCConfiguration _config;
  private ParameterProvider _provider;

  /**
   * Create a new PixxConfigurationLoader.
   * @param config the irc configuration.
   */
  public PixxConfigurationLoader(IRCConfiguration config)
  {
    _config=config;
    _provider=_config.getInterfaceParameterProvider();

  }

  /**
   * Create a new PixxConfiguration object, using the given IRCConfiguration.
   * @return a new PixxConfiguration instance.
   * @throws Exception if an error occurs.
   */
  public PixxConfiguration loadPixxConfiguration() throws Exception
  {
    return getPixxConfiguration();
  }

  private String getParameter(String key)
  {
    return _provider.getParameter(key);
  }

  private boolean getBoolean(String key,boolean def)
  {
    String v=getParameter(key);
    if(v==null) return def;
    v=v.toLowerCase(java.util.Locale.ENGLISH).trim();
    if(v.equals("true") || v.equals("on") || v.equals("1")) return true;
    return false;
  }

  private String getString(String key,String def)
  {
    String v=getParameter(key);
    if(v==null) return def;
    return v;
  }

  private int getInt(String key,int def)
  {
    String v=getParameter(key);
    if(v==null) return def;
    try
    {
      return Integer.parseInt(v);
    }
    catch(Exception e)
    {
      return def;
    }

  }

  private TextProvider getTextProvider()
  {
    String lang=getString("language","gui-english");
    String encoding=getString("languageencoding","");
    String extension=getString("lngextension","lng");
    String backlang=getString("backuplanguage","pixx-english");
    String backencoding=getString("backuplanguageencoding","");
    return new FileTextProvider(lang+"."+extension,encoding,backlang+"."+extension,backencoding,_config.getFileHandler());
  }

  private String[] getArray(String name)
  {
    Vector v=new Vector();
    String cmd;
    int i=1;
    do
    {
      cmd=getParameter(name+i);
      if(cmd!=null) v.insertElementAt(cmd,v.size());
      i++;
    }
    while(cmd!=null);
    String[] ans=new String[v.size()];
    for(i=0;i<v.size();i++) ans[i]=(String)v.elementAt(i);
    return ans;
  }

  private void readDocking(PixxConfiguration config)
  {
    StringParser parser=new StringParser();
    String[] arr=getArray("dockingconfig");
    for(int i=0;i<arr.length;i++)
    {
      String cmd=arr[i];
      String back[]=parser.parseString(cmd);
      if(back.length>=3)
      {
        String type=back[0].toLowerCase(java.util.Locale.ENGLISH);
        String name=back[1].toLowerCase(java.util.Locale.ENGLISH);
        String action=back[2].toLowerCase(java.util.Locale.ENGLISH);
        boolean undock=false;
        if(action.equals("undock")) undock=true;
        config.setDockingPolicy(type,name,undock);
      }
    }
  }

  private void readMouse(PixxConfiguration config,String name,int button,int click) throws Exception
  {
    String s=getString("mouse"+name.toLowerCase(java.util.Locale.ENGLISH),button+" "+click);
    int pos=s.indexOf(' ');
    if(pos<0) throw new Exception("mouse"+name.toLowerCase(java.util.Locale.ENGLISH)+" parameter syntax error");
    String before=s.substring(0,pos).trim();
    String after=s.substring(pos+1).trim();
    config.setMouseConfiguration(name,new Integer(before).intValue(),new Integer(after).intValue());
  }

  private String[] getHighLightWords()
  {
    String words=getParameter("highlightwords");
    if(words==null) return new String[0];
    return new StringParser().parseString(words);
  }

  private void add(Vector v,Object o)
  {
    v.insertElementAt(o,v.size());
  }

  /**
   * Load default popup menu configuration, based on the global IRCConfiguration.
   * @param config global IRCConfiguration.
   */
  public void loadDefaultPopup(PixxConfiguration config)
  {
    Vector v=config.getNickMenuVector();
//    String[] str;
    //add(v,new String[] {"View Profile","/url http://users.frostwire.com/profile.php?user=%1"});
    add(v,new String[] {"Private Message","/query %1"});
    add(v,new String[] {"--"});
    add(v,new String[] {"Ignore","/ignore %1"});
    add(v,new String[] {"Unignore","/unignore %1"});
    add(v,new String[] {"--"});
    add(v,new String[] {"Host","/mode %2 +o %1"});
    add(v,new String[] {"Participant","/mode %2 +v %1"});
    add(v,new String[] {"Spectator","/mode %2 -qaohv %1 %1 %1 %1 %1"});
    add(v,new String[] {"--"});
    add(v,new String[] {"Kick User","/kick %2 %1 Disruptive Behavior"});
    add(v,new String[] {"Ban User","/mode %2 +b %1!*@*","/kick %2 %1 Disruptive Behavior [Access Ban]"});
    add(v,new String[] {"--"});
    add(v,new String[] {"Local Time","/CTCP TIME %1"});
  }

  private void readPopup(PixxConfiguration config)
  {
    if(!getBoolean("configurepopup",false))
    {
      loadDefaultPopup(config);
      return;
    }
    Vector v=config.getNickMenuVector();
    String[] str=getArray("popupmenustring");
    for(int i=0;i<str.length;i++)
    {

      String cmd[]=getArray("popupmenucommand"+(i+1)+"_");
      String[] fin=new String[1+cmd.length];
      fin[0]=str[i];
      for(int j=0;j<cmd.length;j++) fin[j+1]=cmd[j];
      add(v,fin);
    }
  }

  private PixxColorModel getColorModel()
  {
    PixxColorModel model=new PixxColorModel();

    for(int i=0;i<model.getColorCount();i++)
    {
      String color=getParameter("color"+i);
      try
      {
        if(color!=null)
        {
          model.setColor(i,new Color(Integer.parseInt(color,16)));
        }
      }
      catch(Exception e)
      {
      }
    }
    return model;
  }


  private PixxConfiguration getPixxConfiguration() throws Exception
  {
    PixxConfiguration config=new PixxConfiguration(_config);
    config.setTextProvider(getTextProvider());

    Vector v=new Vector();
    String[] words=getHighLightWords();
    for(int i=0;i<words.length;i++) v.insertElementAt(words[i],v.size());
    config.setHighLightConfig(getInt("highlightcolor",5),getBoolean("highlightnick",false),v);
    config.set("highlight",getBoolean("highlight",false));

    config.set("timestamp",getBoolean("timestamp",false));
    config.set("showclose",getBoolean("showclose",true));
    config.set("showconnect",getBoolean("showconnect",true));
    config.set("showchanlist",getBoolean("showchanlist",true));
    config.set("showabout",getBoolean("showabout",false));
    config.set("showhelp",getBoolean("showhelp",false));
    config.set("nicklistwidth",getInt("nicklistwidth",150));
    config.set("nickfield",getBoolean("nickfield",false));
    config.set("showstatus",getBoolean("showstatus",true));
    config.set("styleselector",getBoolean("styleselector",true));
    config.set("setfontonstyle",getBoolean("setfontonstyle",false));
    config.set("helppage",getString("helppage","http://www.frostwire.com/chat/help"));
    config.set("showchannelnickchanged",getBoolean("showchannelnickchanged",false));
    config.set("showchannelnickmodeapply",getBoolean("showchannelnickmodeapply",false));
    config.set("showchannelmodeapply",getBoolean("showchannelmodeapply",false));
    config.set("showchanneltopicchanged",getBoolean("showchanneltopicchanged",true));
    config.set("showchannelnickquit",getBoolean("showchannelnickquit",true));
    config.set("showchannelnickkick",getBoolean("showchannelnickkick",true));
    config.set("showchannelnickpart",getBoolean("showchannelnickpart",true));
    config.set("showchannelnickjoin",getBoolean("showchannelnickjoin",true));
    config.set("showdock",getBoolean("showdock",false));
    config.set("prefixops",getBoolean("prefixops",true));
    config.set("automaticqueries",getBoolean("automaticqueries",true));
    config.set("leftnickalign",getBoolean("leftnickalign",false));
    config.set("taskbaritemwidth",getInt("taskbaritemwidth",100));
    config.set("scrollspeed",getInt("scrollspeed",0));
    config.set("leaveonundockedwindowclose",getBoolean("leaveonundockedwindowclose",true));
    /*config.set("nickprefix",getString("nickprefix"," <"));
    config.set("nickpostfix",getString("nickpostfix","> "));*/
    config.set("showchannelyoujoin",getBoolean("showchannelyoujoin",false));
    
    config.set("displayentertexthere",getBoolean("displayentertexthere",false));
    config.set("ignoreallmouseevents",getBoolean("ignoreallmouseevents",false));
    config.set("hideundockedsources",getBoolean("hideundockedsources",true));

    config.set("displaychannelname",getBoolean("displaychannelname",false));
    config.set("displaychannelmode",getBoolean("displaychannelmode",false));
    config.set("displaychannelcount",getBoolean("displaychannelcount",true));
    config.set("displaychanneltopic",getBoolean("displaychanneltopic",false));
    
    config.setColorModel(getColorModel());

    readMouse(config,"nickquery",1,2);
    readMouse(config,"urlopen",1,2);
    readMouse(config,"channeljoin",1,2);
    readMouse(config,"nickpopup",3,1);
    readMouse(config,"taskbarpopup",3,1);

    readPopup(config);

    readDocking(config);

    return config;
  }


}
