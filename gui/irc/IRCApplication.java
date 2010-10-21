package irc;

import irc.gui.*;
import irc.ident.*;
import java.util.*;
import java.io.*;
import irc.plugin.*;

import java.awt.*;
import java.awt.event.*;

/**
 * The IRC Application. This is the main class of PJIRC.
 */
public class IRCApplication extends IRCObject implements ServerListener,ServerManager,IdentListener,IRCInterfaceListener,WindowListener,ActionListener,PluginManager
{
  private DefaultSource _defaultSource;

  private Interpretor _inter;

  private IdentWrapper _ident;

  private StartupConfiguration _start;

  private IRCInterface _interface;
  private Vector _plugins;
  private Hashtable _pluginsTable;

  private Frame _frame;
  private Container _container;

  private Hashtable<Server,Server> _servers;

  private Object _nickLock=new Object();

  /**
   * Create a new IRCApplication.
   * @param config the IRC configuration.
   * @param startupConfig the startup configuration.
   * @param source a container in wich the application will display. Maybe null. If null,
   * a new Frame will be opened.
   */
  public IRCApplication(IRCConfiguration config,StartupConfiguration startupConfig,Container source)
  {
    super(config);
    _container=source;
    _start=startupConfig;
    _plugins=new Vector();
    _pluginsTable=new Hashtable();

    String gui=config.getS("gui");
    try
    {
      Class cl=Class.forName("irc.gui."+gui+".Interface");
      java.lang.reflect.Constructor ctr=cl.getDeclaredConstructor(new Class[] {config.getClass()});
      _interface=(IRCInterface)ctr.newInstance(new Object[] {config});
    }
    catch(java.lang.reflect.InvocationTargetException iex)
    {
      iex.getTargetException().printStackTrace();
      throw new Error("Unable to load interface "+gui+" : "+iex.getTargetException());
    }
    catch(Throwable ex)
    {
      ex.printStackTrace();
      throw new Error("Unable to load interface "+gui+" : "+ex);
    }

    _servers=new Hashtable();
    _defaultSource=new DefaultSource(_ircConfiguration);
    DefaultInterpretor defaultInter=new DefaultInterpretor(_ircConfiguration,_start,this,this);
    _defaultSource.setInterpretor(defaultInter);
  }

  /**
   * Init the application.
   */
  public synchronized void init()
  {
	loadPlugin(_interface);

    String[] plugins=_start.getPlugins();
    for(int i=0;i<plugins.length;i++) loadPlugin(plugins[i]); //uses string

    _interface.addIRCInterfaceListener(this);
    if(_container==null)
    {
      _frame=new Frame();
      _frame.addWindowListener(this);
      if(_interface.getComponent()!=null) _frame.add(_interface.getComponent());
      _frame.setFont(new Font("",Font.PLAIN,12));
      _frame.setSize(640,400);
      _frame.setVisible(true);
    }
    else
    {
      _frame=null;
      _container.removeAll();
      _container.setLayout(new GridLayout(1,1));
      if(_interface.getComponent()!=null) _container.add(_interface.getComponent());
    }

    _inter=new CTCPInterpretor(_ircConfiguration,_defaultSource.getInterpretor(),this);
    _inter.addLast(_interface.getInterpretor());

    if(_ircConfiguration.getB("useidentserver"))
    {
  		try
  		{
        _ident=new IdentWrapper(_ircConfiguration);
        Exception e=_ident.start(_start.getName(),this);
  		  if(e!=null)
  		  {
          _defaultSource.report("\3"+"6"+"*** "+getText(IRCTextProvider.IDENT_FAILED_LAUNCH,e.getMessage()));
  		  }
  		}
  		catch(Throwable ex)
  		{
        _ircConfiguration.internalError("ident error",ex,"bugs@frostwire.com");
  		}
  	}
  	else
  	{
  	  _ident=null;
    }

    String[] init=_ircConfiguration.getInitialization();
    for(int i=0;i<init.length;i++) _defaultSource.sendString(init[i]);

    IRCServer server=new IRCServer(_ircConfiguration,this,_start.getNick(),_start.getAltNick(),_start.getName(),_start.getAlias());
    server.setServers(_start.getHost(),_start.getPort(),_start.getPass());
    
    newServer(server,_ircConfiguration.getB("autoconnection"));
    
    if (_start.getSmileysSupport() == false) {
    	_ircConfiguration.resetSmileyTable();     	//_defaultSource.sendString("/dsmileys");    	
    	System.out.println("Smileys should be DISABLED!");
    } else {
    	_ircConfiguration.restoreSmileyTable();
    	System.out.println("Smileys should be ACTIVE!");  	
    }
    requestSourceFocus();
  }

  /**
   * Get the IRC interface.
   * @return the IRC interface.
   */
  public IRCInterface getIRCInterface()
  {
    return _interface;
  }

  public void newServer(Server server,boolean connect)
  {
    server.addServerListener(this);
    _servers.put(server,server);

    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      plugin.serverCreated(server);
    }

    server.enumerateSourcesAsCreated(this);
    if(connect) server.connect();
  }

  /**
   * Uninit the application.
   */
  public synchronized void uninit()
  {
    Enumeration en=_servers.keys();
    while(en.hasMoreElements())
    {
      Server s=(Server)en.nextElement();
      s.leave();
    }

    if(_ident!=null) _ident.stop();
    _interface.removeIRCInterfaceListener(this);

    if(_frame!=null) _frame.removeWindowListener(this);
    _frame=null;

    while(_plugins.size()>0)
    {
      unloadPlugin((Plugin)_plugins.elementAt(_plugins.size()-1));
    }
    _pluginsTable=new Hashtable();

    if(_container!=null) _container.removeAll();
    
    EventDispatcher.clearCache();
  }

  public boolean loadPlugin(String str)
  {
    if(_pluginsTable.get(str)!=null) return false;
    Plugin plugin;
    try
    {
      Class cl=Class.forName("irc.plugin."+str);
      java.lang.reflect.Constructor ctr=cl.getDeclaredConstructor(new Class[] {_ircConfiguration.getClass()});
      plugin=(Plugin)ctr.newInstance(new Object[] {_ircConfiguration});
      loadPlugin(plugin);
    }
    catch(Throwable ex)
    {
      System.out.println("IRCApplication.loadPlugin() here is where the exception got thrown");
      //ex.printStackTrace();
      return false;
    }
    _pluginsTable.put(str,plugin);
    return true;
  }

  public boolean unloadPlugin(String str)
  {
    Plugin plugin=(Plugin)_pluginsTable.get(str);
    if(plugin==null) return false;
    _pluginsTable.remove(str);
    unloadPlugin(plugin);
    return true;
  }

  private void loadPlugin(Plugin plugin)
  {
    class addHandler implements ServerListener
    {
      private Plugin _plugin;
      /**
       * Create a new addHandler
       * @param plug plugin.
       */
      public addHandler(Plugin plug)
      {
        _plugin=plug;
      }

      public void serverConnected(Server s) {/*...*/}
      public void serverDisconnected(Server s) {/*...*/}
      public void serverLeft(Server s) {/*...*/}
      public String[] cannotUseRequestedNicknames(Server s) {return null;}
      public void sourceCreated(Source source,Server server,Boolean bring)
      {
        _plugin.sourceCreated(source,bring);
      }

      public void sourceRemoved(Source source,Server server) {/*...*/}
      public Object specialServerRequest(String request,Server server,Object[] params) {return null;}
    }

    plugin.setIRCApplication(this);
    plugin.load();
    _plugins.insertElementAt(plugin,_plugins.size());
    plugin.sourceCreated(_defaultSource,Boolean.TRUE);

    Enumeration en=_servers.keys();
    while(en.hasMoreElements())
    {
      Server s=(Server)en.nextElement();
      plugin.serverCreated(s);

      s.enumerateSourcesAsCreated(new addHandler(plugin));
    }

  }

  private void unloadPlugin(Plugin plugin)
  {
    class removeHandler implements ServerListener
    {
      private Plugin _plugin;
      
      /**
       * Create a new removeHandler
       * @param plug plugin.
       */
      public removeHandler(Plugin plug)
      {
        _plugin=plug;
      }

      public void serverConnected(Server s) {/*...*/}
      public void serverDisconnected(Server s) {/*...*/}
      public void serverLeft(Server s) {/*...*/}
      public String[] cannotUseRequestedNicknames(Server s) {return null;}
      public void sourceCreated(Source source,Server server,Boolean bring) {/*...*/}
      public void sourceRemoved(Source source,Server server)
      {
        _plugin.sourceRemoved(source);
      }
      public Object specialServerRequest(String request,Server server,Object[] params) {return null;}
    }

    for(int i=0;i<_plugins.size();i++)
    {
      if(_plugins.elementAt(i)==plugin)
      {
        _plugins.removeElementAt(i);
        Enumeration en=_servers.keys();
        while(en.hasMoreElements())
        {
          Server s=(Server)en.nextElement();
          s.enumerateSourcesAsRemoved(new removeHandler(plugin));
          plugin.serverRemoved(s);
        }
        plugin.sourceRemoved(_defaultSource);
        plugin.unload();
        return;
      }
    }
  }

  public void sourceCreated(Source source,Server server,Boolean bring)
  {
    source.getInterpretor().addLast(_inter);
    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      //System.out.println("IRCAPPLICATION FTA DEBUG: " + source._server.getServerName() );
      plugin.sourceCreated(source,bring);
    }
  }

  public void sourceRemoved(Source source,Server server)
  {
    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      plugin.sourceRemoved(source);
    }
  }

  public void serverLeft(Server s)
  {
    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      plugin.serverRemoved(s);
    }
    _servers.remove(s);
    s.removeServerListener(this);
  }

  private Frame getParentFrame()
  {
    if(_frame!=null) return _frame;
    Container ans=_container;
    while(ans!=null)
    {
      if(ans instanceof Frame) return (Frame)ans;
      ans=ans.getParent();
    }
    return null;
  }
  
  /**
   * Get the application's parent container. The GUI instance will be added into this container or one of its children. 
   * @return application's parent container.
   */
  public Container getContainer()
  {
    if(_frame!=null) return _frame;
    return _container;
  }
  
  public Object specialServerRequest(String request,Server s,Object[] params)
  {
    if(request.equals("DCCFileRequest"))
    {
      File f=_ircConfiguration.getSecurityProvider().getSaveFile(params[1].toString(),getText(IRCTextProvider.FILE_SAVEAS)+" ["+params[1]+"]");
      return f;
    }
    else if(request.equals("DCCChatRequest"))
    {
      boolean accept=_ircConfiguration.getSecurityProvider().confirm(getParentFrame(),getText(IRCTextProvider.GUI_DCC_CHAT_WARNING_TITLE),getText(IRCTextProvider.GUI_DCC_CHAT_WARNING_TEXT,(String)params[0]));
      return new Boolean(accept);
    }
    else
    {
      _ircConfiguration.internalError("Unknown request : "+request,null,"bugs@frostwire.com");
      return null;
    }
  }

  public void serverConnected(Server server)
  {
    for(int i=0;i<_start.getCommands().length;i++)
    {
      String cmd=_start.getCommands()[i];
      if((cmd.startsWith("/")) && (server instanceof IRCServer))
        ((IRCServer)server).getStatus().sendString(_start.getCommands()[i]);
      else
        server.execute(_start.getCommands()[i]);
    }

    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      plugin.serverConnected(server);
    }

  }

  public void serverDisconnected(Server s)
  {
    Enumeration enum1=_plugins.elements();
    while(enum1.hasMoreElements())
    {
      Plugin plugin=(Plugin)enum1.nextElement();
      plugin.serverDisconnected(s);
    }
  }

  public void identRequested(String source,Integer result,String reply)
  {
    _defaultSource.report("\3"+"6"+"*** "+getText(IRCTextProvider.IDENT_REQUEST,source));
    String s="";
    switch(result.intValue())
    {
      case IdentListener.IDENT_ERROR:s=getText(IRCTextProvider.IDENT_ERROR);break;
      case IdentListener.IDENT_OK:s=getText(IRCTextProvider.IDENT_REPLIED,reply);break;
      case IdentListener.IDENT_DEFAULT:s=getText(IRCTextProvider.IDENT_REPLIED,getText(IRCTextProvider.IDENT_DEFAULT_USER)+" : "+reply);break;
      case IdentListener.IDENT_NOT_FOUND:s=getText(IRCTextProvider.IDENT_NO_USER);break;
      default: s=getText(IRCTextProvider.IDENT_UNDEFINED);break;
    }
    _defaultSource.report("\3"+"6"+"*** "+s);

  }

  public void identRunning(Integer port)
  {
    _defaultSource.report("\3"+"6"+"*** "+getText(IRCTextProvider.IDENT_RUNNING_ON_PORT,port+""));
  }

  public void identLeaving(String message)
  {
    _defaultSource.report("\3"+"6"+"*** "+getText(IRCTextProvider.IDENT_LEAVING,message));
  }

  public void activeChanged(GUISource source,IRCInterface inter)
  {
    if(source!=null)
    {
      if(source.getSource().mayDefault())
        source.getSource().getServer().setDefaultSource(source.getSource());
      if(_frame!=null) _frame.setTitle(source.getTitle());
    }
  }

  public void windowActivated(WindowEvent e)
  {
    //nothing here
  }

  public void windowClosed(WindowEvent e)
  {
    if(e.getSource()==_frame)
    {
      EventDispatcher.dispatchEventAsync(this,"uninit",new Object[0]);
    }
  }

  public void windowClosing(WindowEvent e)
  {
    //((Frame)e.getSource()).hide();
    ((Frame)e.getSource()).setVisible(false);
    ((Frame)e.getSource()).dispose();
  }

  public void windowDeactivated(WindowEvent e)
  {
    //nothing here
  }

  public void windowDeiconified(WindowEvent e)
  {
    //nothing here
  }

  public void windowIconified(WindowEvent e)
  {
    //nothing here
  }

  public void windowOpened(WindowEvent e)
  {
    //nothing here
  }

  private GUISource getActiveSource()
  {
    return _interface.getActive();
  }

  private Source getSource(String serverName,String type,String name)
  {
    Enumeration e=_servers.keys();
    while(e.hasMoreElements())
    {
      Server s=(Server)e.nextElement();
      String sname=s.getServerName();
      if(sname.equals(serverName) || serverName.length()==0)
      {
        Enumeration f=s.getSources();
        while(f.hasMoreElements())
        {
          Source src=(Source)f.nextElement();
          if(src.getType().equals(type) && src.getName().equals(name)) return src;
        }
      }
    }
    return null;
  }

  /**
   * Request the active source to gain focus.
   */
  public void requestSourceFocus()
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    current.requestFocus();
  }
  
  /**
   * Request the given source to gain focus.
   * @param serverName the source's server name, or an empty string if no server filtering needs to be done.
   * @param type the source type.
   * @param name the source name.
   */
  public void requestSourceFocus(String serverName,String type,String name)
  {
    Source source=getSource(serverName,type,name);
    if(source!=null)
    {
      GUISource gui=_interface.getGUISource(source);
      if(gui!=null) _interface.setActive(gui);
    }
  }
  
  /**
   * Send the given command to the given source interpretor.
   * @param serverName the source's server name, or an empty string if no server filtering needs to be done.
   * @param type the source type.
   * @param name the source name.
   * @param cmd the command to send.
   */
  public void sendString(String serverName,String type,String name,String cmd)
  {
    Source source=getSource(serverName,type,name);
    if(source!=null) source.sendString(cmd);
  }
  
  /**
   * Send the given command as user string. sendstrings causes thread errors when useer is already connected
   * @param str string to send.
   */
  public void sendUserString(String str)
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    current.getSource().sendUserString(str);
  }
  
  /**
   * Send the given string to the current source interpretor.
   * @param str string to send to the interpretor.
   */
  public void sendString(String str)
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    current.getSource().sendString(str);
  }  
  
  /**
   * Get the current connection status.
   * Called from changesmileys
   * @return boolean.
   */
  public boolean isConnected()
  {
    GUISource current=getActiveSource();
    if(current==null) return false;    
    return current.getSource().isConnected();
  }
  
  /**
   * Sends reports to IRC chat window.
   * Called from changesmileys
   * @return boolean.
   */
  public void sendReport(String description)
  {
    GUISource current=getActiveSource();
    if(current==null) return;    
    current.getSource().report(description);
  }
  
  /**
   * Set the current textfield text.
   * @param txt new textfield text.
   */
  public void setFieldText(String txt)
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    current.setFieldText(txt);
  }

  /**
   * Get the current textfield text.
   * @return the current textfield text.
   */
  public String getFieldText()
  {
    GUISource current=getActiveSource();
    if(current==null) return "";
    return current.getFieldText();
  }

  /**
   * Validate the current textfield text, as if user pressed return key.
   */
  public void validateText()
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    current.validateText();
  }

  /**
   * Send the given event value to the given plugin.
   * @param pluginName the plugin name.
   * @param event the event value to be sent.
   */
  public void sendPluginEvent(String pluginName,Object event)
  {
    Plugin plugin=(Plugin)_pluginsTable.get(pluginName);
    if(plugin==null) return;
    plugin.externalEvent(event);
  }

  /**
   * Get the plugin value from the given plugin name.
   * @param pluginName the plugin name.
   * @param valueName the value name.
   * @return the returned plugin value, or null if the plugin is not found.
   */
  public Object getPluginValue(String pluginName,Object valueName)
  {
    Plugin plugin=(Plugin)_pluginsTable.get(pluginName);
    if(plugin==null) return null;
    return plugin.getValue(valueName);
  }

  public String[] cannotUseRequestedNicknames(Server s)
  {
    synchronized(_nickLock)
    {
      if(_interface.getComponent()!=null) _interface.getComponent().setEnabled(false);
      if(_frame!=null) _frame.setEnabled(false);
      Frame f=new Frame();
      f.setLayout(new FlowLayout());
      f.setSize(200,65);
      f.setTitle("Change nickname to");
      //f.setTitle(getText(IRCTextProvider.GUI_CHANGE_NICK));
      TextField field=new TextField(_start.getNick());
      Button b=new Button("Ok");
      b.addActionListener(this);
      f.add(field);
      f.add(b);
      f.setVisible(true);
      try
      {
        _nickLock.wait();
      }
      catch(InterruptedException ex)
      {
        //ignore...
      }
      f.setVisible(false);
      f.remove(b);
      f.remove(field);
      f.dispose();
      String[] ans=new String[1];
      ans[0]=field.getText();
      if(_frame!=null) _frame.setEnabled(true);
      if(_interface.getComponent()!=null) _interface.getComponent().setEnabled(true);
      return ans;
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    synchronized(_nickLock)
    {
      _nickLock.notifyAll();
    }
  }

  private static void usage()
  {
    System.out.println("Usage :");
    System.out.println("   java irc.IRCApplication -f configfile");
    System.out.println("or java irc.IRCApplication -p nick fullname host gui");
    System.out.println("");
    System.out.println("Without any parameter, '-f pjirc.cfg' parameters are assumed.");
  }

  /**
   * Actual entry point.
   * @param args application parameters.
   */
  public static void go(String[] args)
  {
    FileHandler file=new LocalFileHandler();
    IRCConfiguration ircConfiguration;
    StartupConfiguration startupConfiguration;

    try
    {

      if((args.length==0) || ((args.length>=2) && (args[0].equals("-f"))))
      {
        String f="pjirc.cfg";
        if(args.length>=2) f=args[1];
        StreamParameterProvider provider=new StreamParameterProvider(file.getInputStream(f));
        ConfigurationLoader loader=new ConfigurationLoader(provider,new NullURLHandler(),new AWTImageLoader(),new NullSoundHandler(),file);
        ircConfiguration=loader.loadIRCConfiguration();
        startupConfiguration=loader.loadStartupConfiguration();
      }
      else if((args.length>=5) && (args[0].equals("-p")))
      {
        StreamParameterProvider provider=new StreamParameterProvider(null);
        ConfigurationLoader loader=new ConfigurationLoader(provider,new NullURLHandler(),new AWTImageLoader(),new NullSoundHandler(),file);
        ircConfiguration=loader.loadIRCConfiguration();
        ircConfiguration.set("gui",args[4]);
        startupConfiguration=new StartupConfiguration(args[1],"",args[2],new String[] {""},new String[] {args[3]},new int[] {6667},"",new String[] {},new String[] {}, true); // last value "true" set smileys to true by default
      }
      else
      {
        usage();
        return;
      }

      IRCApplication application=new IRCApplication(ircConfiguration,startupConfiguration,null);
      EventDispatcher.dispatchEventAsyncAndWaitEx(application,"init",new Object[0]);
    }
    catch(Throwable ex)
    {
      System.out.println("Error : "+ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Main entry point.
   * @param args application parameters.
   */
  public static void main(String[] args)
  {
    go(args);
  }

}
