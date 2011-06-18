package irc;

import java.util.*;
import java.awt.*;

/**
 * A single server.
 */
class ServerItem
{
  /**
   * Hostname.
   */
  public String host;
  /**
   * Server post.
   */
  public int port;
  /**
   * Optionnal server password.
   */
  public String pass;
}

/**
 * Toolkit for Configuration creation.
 */
public class ConfigurationLoader
{
  private ParameterProvider _provider;
  private URLHandler _handler;
  private ImageLoader _loader;
  private SoundHandler _sound;
  private FileHandler _file;

  /**
   * Create a new IRCConfigurationLoader.
   * @param provider parameter provider to load the configuration from.
   * @param handler URL handler.
   * @param loader Image loader.
   * @param sound Sound handler.
   * @param file File handler.
   */
  public ConfigurationLoader(ParameterProvider provider,URLHandler handler,ImageLoader loader,SoundHandler sound,FileHandler file)
  {
    _provider=provider;
    _handler=handler;
    _loader=loader;
    _sound=sound;
    _file=file;
  }

  /**
   * Create a new IRCConfiguration object, using the given ParameterProvider.
   * @return a new IRCConfiguration instance.
   * @throws Exception if an error occurs.
   */
  public IRCConfiguration loadIRCConfiguration() throws Exception
  {
    return getIRCConfiguration();
  }

  /**
   * Create a new StartupConfiguration object, using the given ParameterProvider.
   * @return a new StartupConfiguration instance.
   * @throws Exception if a mandatory parameter is not supplied or if an error occurs.
   */
  public StartupConfiguration loadStartupConfiguration() throws Exception
  {
    return getStartupConfiguration();
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

  private void readBackgroundConfig(IRCConfiguration config)
  {
    StringParser parser=new StringParser();
    String[] arr=getArray("style:backgroundimage");
    for(int i=0;i<arr.length;i++)
    {
      String cmd=arr[i];
      String back[]=parser.parseString(cmd);
      if(back.length>=4)
      {
        String type=back[0];
        String name=back[1];
        int tiling=new Integer(back[2]).intValue();
        String image=back[3];
        config.setBackgroundImage(type,name,image);
        config.setBackgroundTiling(type,name,tiling);
      }
    }
  }

  private TextProvider getTextProvider()
  {
    String lang=getString("language","english");
    String encoding=getString("languageencoding","Unicode");
    String extension=getString("lngextension","lng");
    String backlang=getString("backuplanguage","english");
    String backencoding=getString("backuplanguageencoding","");
    return new FileTextProvider(lang+"."+extension,encoding,backlang+"."+extension,backencoding,_file);
  }

  private String[] getArray(String name)
  {
    Vector<String> v=new Vector<String>();
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

  private void readSmileys(IRCConfiguration config)
  {
    config.addSmiley("+us","smileys/us.gif");
    config.addSmiley("%us","smileys/usflag.gif");
    config.addSmiley("+ar","smileys/ar.gif");
    config.addSmiley("+au","smileys/au.gif");
    config.addSmiley("+be","smileys/be.gif");
    config.addSmiley("+br","smileys/br.gif");
    config.addSmiley("+ca","smileys/ca.gif");
    config.addSmiley("%ca","smileys/Canada.gif");
    config.addSmiley("+de","smileys/de.gif");
    config.addSmiley("+eu","smileys/eu.gif");
    config.addSmiley("%eu","smileys/e-u.gif");
    config.addSmiley("+fr","smileys/France.gif");
    config.addSmiley("+it","smileys/it.gif");
    config.addSmiley("+jp","smileys/jp.gif");
    config.addSmiley("+mx","smileys/mexico.gif");    
    config.addSmiley("+nl","smileys/nl.gif");
    config.addSmiley("+no","smileys/no.gif");
    config.addSmiley("+nr","smileys/nr.gif");
    config.addSmiley("+pl","smileys/pl.gif");
    config.addSmiley("%pl","smileys/poland.gif");
    config.addSmiley("+uk","smileys/UK.gif");
    config.addSmiley("+ve","smileys/ve.gif");
    config.addSmiley("%ve","smileys/venezuela.gif");
    config.addSmiley("(smile)","smileys/smiley.gif");
    config.addSmiley(":D","smileys/icon_biggrin.gif");
    //config.addSmiley(":-D","smileys/icon_biggrin.gif");
    config.addSmiley(":)","smileys/icon_smile.gif");
    //config.addSmiley(":-)","smileys/icon_smile.gif");
    config.addSmiley(":(","smileys/icon_sad.gif");
    //config.addSmiley(":-(","smileys/icon_sad.gif");
    config.addSmiley(":o","smileys/icon_surprised.gif");
    //config.addSmiley(":-o","smileys/icon_surprised.gif");
    config.addSmiley(":O","smileys/icon_eek.gif");
    //config.addSmiley(":-O","smileys/icon_eek.gif");
    config.addSmiley("O.O","smileys/icon_eek.gif");
    config.addSmiley(":?","smileys/icon_confused.gif");
    //config.addSmiley(":-?","smileys/icon_confused.gif");
    config.addSmiley("8)","smileys/icon_cool.gif");
    //config.addSmiley("8-)","smileys/icon_cool.gif");
    config.addSmiley("(lol)","smileys/icon_lol.gif");
    //config.addSmiley(":P","smileys/icon_razz.gif");
    config.addSmiley(":p","smileys/icon_razz.gif");
    config.addSmiley(":s","smileys/icon_confused.gif");
    //config.addSmiley(":S","smileys/icon_confused.gif");
    config.addSmiley(":$","smileys/icon_redface.gif");
    //config.addSmiley(":\\","smileys/icon_redface.gif");
    //config.addSmiley(":-/","smileys/icon_redface.gif");
    //config.addSmiley(":-\\","smileys/icon_redface.gif");
    config.addSmiley(":'(","smileys/icon_cry.gif");    
    config.addSmiley(":@","smileys/icon_mad.gif");
    //config.addSmiley(":-@","smileys/icon_mad.gif");
    config.addSmiley("(evil1)","smileys/icon_twisted.gif");
    config.addSmiley("(evil)","smileys/icon_evil.gif");
    config.addSmiley("(roll)","smileys/icon_rolleyes.gif");
    config.addSmiley(";)","smileys/icon_wink.gif");
    //config.addSmiley(";-)","smileys/icon_wink.gif");
    config.addSmiley("(!)","smileys/icon_exclaim.gif");
    config.addSmiley("(?)","smileys/icon_question.gif");
    config.addSmiley("(i)","smileys/icon_idea.gif");
    config.addSmiley("(I)","smileys/icon_idea.gif");
    config.addSmiley("(>)","smileys/icon_arrow.gif");
    config.addSmiley(":|","smileys/icon_neutral.gif");
    config.addSmiley("(greensmile)","smileys/icon_mrgreen.gif");
    config.addSmiley("xD","smileys/aiwebs_011.gif");
    //config.addSmiley("XD","smileys/aiwebs_011.gif");
  }

  private void configureFonts(IRCConfiguration config)
  {
    //type name fname fsize
    StringParser parser=new StringParser();
    String[] arr=getArray("style:sourcefontrule");
    for(int i=0;i<arr.length;i++)
    {
      String cmd=arr[i];
      String back[]=parser.parseString(cmd);
      if(back.length>=4)
      {
        String type=back[0].toLowerCase(java.util.Locale.ENGLISH);
        String name=back[1].toLowerCase(java.util.Locale.ENGLISH);
        String fname=back[2].toLowerCase(java.util.Locale.ENGLISH);
        
        if(fname.startsWith("'") && fname.endsWith("'"))
          fname=fname.substring(1,fname.length()-1);
        
        int fsize=new Integer(back[3].toLowerCase(java.util.Locale.ENGLISH)).intValue();
        config.setFont(type,name,new Font(fname,Font.PLAIN,fsize));
      }
    }
  }

  private void configureTextColors(IRCConfiguration config)
  {
    //type name {index=value}*
    String[] arr=getArray("style:sourcecolorrule");
    for(int i=0;i<arr.length;i++)
    {
      StringTokenizer tok=new StringTokenizer(arr[i]);
      if(!tok.hasMoreElements()) continue;
      String type=(String)tok.nextElement();
      if(!tok.hasMoreElements()) continue;
      String name=(String)tok.nextElement();
      Color[] c=new Color[16];
      config.loadDefaultColors(c);
      while(tok.hasMoreElements())
      {
        String s=(String)tok.nextElement();
        int pos=s.indexOf('=');
        if(pos<0) continue;
        String before=s.substring(0,pos).trim();
        String after=s.substring(pos+1).trim();
        int index=Integer.parseInt(before);
        Color col=new Color(Integer.parseInt(after,16));
        if((index>=0) && (index<=15)) c[index]=col;
      }
      config.setSourceColor(type,name,c);
    }
  }

  private void readSound(IRCConfiguration config)
  {
    AudioConfiguration ac=config.getAudioConfiguration();
    if(getParameter("soundbeep")!=null) ac.setBeep(getParameter("soundbeep"));
    if(getParameter("soundquery")!=null) ac.setQuery(getParameter("soundquery"));
    String[] arr=getArray("soundword");
    for(int i=0;i<arr.length;i++)
    {
      String cmd=arr[i];
      cmd=cmd.trim();
      int pos=cmd.indexOf(' ');
      if(pos!=-1)
      {
        String word=cmd.substring(0,pos).trim();
        String sound=cmd.substring(pos+1).trim();
        ac.setWord(word,sound);
      }
    }
  }
  
  private IRCConfiguration getIRCConfiguration() throws Exception
  {
    String gui=getString("gui","pixx");
    IRCConfiguration config=new IRCConfiguration(getTextProvider(),_handler,_loader,_sound,_file,_provider,new PrefixedParameterProvider(_provider,gui+":"));

    config.setJoinList(getString("authorizedjoinlist","all"));
    config.setLeaveList(getString("authorizedleavelist","all"));
    //config.setCommandList(getString("authorizedcommandlist","all"));
    //config.setCommandList(getString("authorizedcommandlist","none+me+away+ignore+unignore+kick+topic+ctcp+url+mode+join"));
    config.setCommandList(getString("authorizedcommandlist","all-dcc-load-newserver-ping-raw-server-sleep-names"));
    config.set("style:floatingasl",getBoolean("style:floatingasl",false));
    config.set("style:floatingaslalpha",getInt("style:floatingaslalpha",200));
    config.set("style:backgroundimage",getBoolean("style:backgroundimage",false));
    config.set("style:bitmapsmileys",getBoolean("style:bitmapsmileys",false));
    config.set("style:linespacing",getInt("style:linespacing",2));
    config.set("style:maximumlinecount",getInt("style:maximumlinecount",4096));

    config.set("style:highlightlinks",getBoolean("style:highlightlinks",false));
    
    config.set("aslseparatorstring",getString("aslseparatorstring",""));
    config.set("noasldisplayprefix",getString("noasldisplayprefix",""));
    config.set("quitmessage",getString("quitmessage","User Quit"));
    config.set("asl",getBoolean("asl",false));	
    config.set("aslmale",getString("aslmale","m"));
    config.set("aslfemale",getString("aslfemale","f"));
    config.set("useinfo",getBoolean("useinfo",true));
    config.set("coding",getInt("coding",3));
    config.set("userid",getString("userid","GatewayX"));
    config.set("style:righttoleft",getBoolean("style:righttoleft",false));
    config.set("autoconnection",getBoolean("autoconnection",false));
    config.set("useidentserver",getBoolean("useidentserver",false));
    config.set("multiserver",getBoolean("multiserver",false));
    config.set("aslunknown",getString("aslunknown","x"));
    config.set("gui",getString("gui","pixx"));
    config.set("fingerreply",getString("fingerreply","i5Hx3Qh3Ji1Vx7Md9Kf2Pp9Xy5Y"));
    config.set("userinforeply",getString("userinforeply","n0Cm2Vk9Rx7Dt9Fb2Cv7Ey8Fa9Cd3Yj6Iv4Zi3Kh5Jo4Iu3Gh9Bk8Od6Z"));
    config.set("allowdccchat",getBoolean("allowdccchat",false));
    config.set("allowdccfile",getBoolean("allowdccfile",false));
    config.set("disablequeries",getBoolean("disablequeries",false));
    config.set("autorejoin",getBoolean("autorejoin",false));
    
    config.setInitialisation(getArray("init"));
    
    readBackgroundConfig(config);
    readSmileys(config);
    configureFonts(config);
    configureTextColors(config);
    readSound(config);

    return config;
  }

  private ServerItem[] readServers(String dhost,int dport,String dpass)
  {
    Vector<ServerItem> res=new Vector<ServerItem>();
    ServerItem item=new ServerItem();
    item.host=dhost;
    item.port=new Integer(dport).intValue();
    item.pass=dpass;
    res.insertElementAt(item,res.size());

    String[] arr=getArray("alternateserver");
    for(int i=0;i<arr.length;i++)
    {
      String cmd=arr[i];
      int pos=cmd.indexOf(" ");
      if(pos>=0)
      {
        String host=cmd.substring(0,pos).trim();
        String port=cmd.substring(pos+1).trim();
        String pass="";
        pos=port.indexOf(" ");
        if(pos>=0)
        {
          pass=port.substring(pos+1).trim();
          port=port.substring(0,pos).trim();
        }
        item=new ServerItem();
        item.host=host;
        item.port=new Integer(port).intValue();
        item.pass=pass;
        res.insertElementAt(item,res.size());
      }
    }
    ServerItem[] ans=new ServerItem[res.size()];
    for(int i=0;i<ans.length;i++) ans[i]=(ServerItem)res.elementAt(i);
    return ans;
  }

  private StartupConfiguration getStartupConfiguration() throws Exception
  {
    String nick=getParameter("nick");
    if(nick==null) throw new Exception("Your nickname cannot be blank, please choose another.");
    String name=getParameter("name");
    if(name==null) name=getParameter("fullname");
    if(name==null) name="[P:RX{0,G}]"; //Future profile usages. Profile:No Profile{0,Guest}
    // if(name==null) throw new Exception("Register failed. You may have been permanently banned from this chat room.");
    String host=getParameter("host");
    if(host==null) host="chat.peercommons.net";
    String pass=getParameter("password");
    if(pass==null) pass="";
    String sport=getParameter("port");
    if(sport==null) sport="6667";
    int port=new Integer(sport).intValue();
    String altNick=getParameter("alternatenick");
    if(altNick==null) altNick=nick+"????";
    String alias=getParameter("serveralias");
    if(alias==null) alias="";

    ServerItem[] items=readServers(host,port,pass);
    String[] hosts=new String[items.length];
    int[] ports=new int[items.length];
    String[] passs=new String[items.length];
    for(int i=0;i<items.length;i++)
    {
      hosts[i]=items[i].host;
      ports[i]=items[i].port;
      passs[i]=items[i].pass;
    }
    return new StartupConfiguration(nick,altNick,name,passs,hosts,ports,alias,getArray("command"),getArray("plugin"),true); //smileys set to true by default
  }

}
