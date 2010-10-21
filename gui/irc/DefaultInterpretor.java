package irc;

/**
 * A default interpretor.
 */
public class DefaultInterpretor extends BasicInterpretor
{
  private StartupConfiguration _start;
  private ServerManager _mgr;
  private PluginManager _plugin;

  /**
   * Create a new DefaultInterpretor.
   * @param config global irc configuration.
   * @param start statup configuration.
   * @param mgr the server manager to be called upon server creation.
   * @param plugin the plugin manager.
   */
  public DefaultInterpretor(IRCConfiguration config,StartupConfiguration start,ServerManager mgr,PluginManager plugin)
  {
    super(config);
    _start=start;
    _mgr=mgr;
    _plugin=plugin;
  }

  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    try
    {
      if(cmd.equals("newserver"))
      {
        if(_ircConfiguration.getB("multiserver"))
        {
          test(cmd,parts,2);
          int port=6667;
          String pass="";
          String alias=parts[1];
          if(parts.length>3) port=new Integer(parts[3]).intValue();
          if(parts.length>4) pass=parts[4];
          String host=parts[2];

          IRCServer server=new IRCServer(_ircConfiguration,_mgr,_start.getNick(),_start.getAltNick(),_start.getName(),alias);
          server.setServers(new String[] {host},new int[] {port},new String[] {pass});
          _mgr.newServer(server,true);
        }
        else
        {
          //source.report(getText(IRCTextProvider.INTERPRETOR_MULTISERVER_DISABLED));
        }
      }
      else if(cmd.equals("load"))
      {
        test(cmd,parts,1);
        _plugin.loadPlugin(parts[1]);
      }
      else if(cmd.equals("unload"))
      {
        test(cmd,parts,1);
        _plugin.unloadPlugin(parts[1]);
      }
      else
      {
        super.handleCommand(source,cmd,parts,cumul);
      }
    }
    catch(NotEnoughParametersException ex)
    {
    	source.report("2      *** Invalid command format: "+ex.getMessage());
      //source.report(getText(IRCTextProvider.INTERPRETOR_INSUFFICIENT_PARAMETERS,ex.getMessage()));
    }
  }
}

