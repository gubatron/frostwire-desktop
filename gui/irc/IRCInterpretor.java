package irc;


/**
 * IRC interpretor.
 */
public class IRCInterpretor extends BasicInterpretor
{
  // private IRCConfiguration _config; it loads current configuration without plugins
  /**
   * Create a new IRCInterpretor.
   * @param config global configuration.
   */
  public IRCInterpretor(IRCConfiguration config)
  {
    super(config);
    //_config=config; // enables hability to load configuration without plugins (for future implementations)
  }

  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    try
    {

      IRCServer server=(IRCServer)source.getServer();
      /*if(cmd.equals("amsg"))
      {
        test(cmd,parts,1);
        Enumeration e=server.getChannels();
        while(e.hasMoreElements())
        {
          ((Channel)e.nextElement()).sendString(cumul[1]);
        }
      }
      else if(cmd.equals("ame"))
      {
        test(cmd,parts,1);
        Enumeration e=server.getChannels();
        while(e.hasMoreElements())
        {
          ((Channel)e.nextElement()).sendString("/me "+cumul[1]);
        }
      }*/
      /*
      // Do not delete please, it could be useful for future implementations
      if (cmd.equals("disablesmileys"))
      {
	  _config.resetSmileyTable();
	  System.out.println("Smileys are now disabled");
	  source.report("3      ��� Smileys are now disabled.");
      }
      else if (cmd.equals("enablesmileys"))
      {
	  _config.reloadSmileyTable();
	  System.out.println("Smileys are now enabled");
	  source.report("3      ��� Smileys are now enabled.");	
      }
      else if ....*/
      if(cmd.equals("list"))
      {
        if(parts.length<=1)
          server.execute("LIST");
        else
          server.execute("LIST "+parts[1]);
      }
      else if(cmd.equals("topic"))
      {
        test(cmd,parts,2);
        server.execute("TOPIC "+parts[1]+" :"+cumul[2]);
      }
      else if(cmd.equals("away"))
      {
        if(parts.length<=1)
          server.execute("AWAY");
        else
          server.execute("AWAY :"+cumul[1]);
      }
      else if(cmd.equals("quit"))
      {
        if(parts.length>1)
          server.execute("QUIT :"+cumul[1]);
        else
          server.execute("QUIT");
      }
      else if(cmd.equals("part"))
      {
        test(cmd,parts,1);
        if(parts.length==2)
        {
          server.execute("PART "+parts[1]);
        }
        else
        {
          server.execute("PART "+parts[1]+" :"+cumul[2]);
        }
      }
      else if(cmd.equals("kick"))
      {
        test(cmd,parts,2);
        if(parts.length==3)
        {
          server.execute("KICK "+parts[1]+" "+parts[2]);
        }
        else
        {
          server.execute("KICK "+parts[1]+" "+parts[2]+" :"+cumul[3]);
        }
      }
      else if(cmd.equals("notice"))
      {
        test(cmd,parts,2);
        server.execute("NOTICE "+parts[1]+" :"+cumul[2]);
        source.report("-> -"+parts[1]+"- "+cumul[2]);
      }
      /*else if(cmd.equals("onotice"))
      {
        test(cmd,parts,2);
        sendString(source,"/notice @"+parts[1]+" "+cumul[2]);
      }*/
      else if(cmd.equals("join"))
      {
        test(cmd,parts,1);
        String chan=parts[1];
        if(!chan.startsWith("#") && !chan.startsWith("!") && !chan.startsWith("&") && !chan.startsWith("+"))
          chan='#'+chan;
        if(parts.length<=2)
          server.execute("JOIN "+chan);
        else
          server.execute("JOIN "+chan+" "+parts[2]);
      }
      else if(cmd.equals("j"))
      {
        sendString(source,"/join "+cumul[1]);
      }
      else if(cmd.equals("query"))
      {
        test(cmd,parts,1);
        server.getQuery(parts[1],true);
      }
      else if(cmd.equals("whisper"))
      {
        test(cmd,parts,1);
        server.getQuery(parts[1],true);
      }
		else if(cmd.equals("ignore"))
		{
		  test(cmd,parts,1);
			if(!server.ignore(parts[1]))
			{
		    server.addIgnore(parts[1]);
				source.report("14      ��� You are now ignoring "+parts[1]+".");
			}
		}
		else if(cmd.equals("unignore"))
		{
		  test(cmd,parts,1);
			if(server.ignore(parts[1]))
			{
			  server.removeIgnore(parts[1]);
				source.report("14      ��� You are no longer ignoring "+parts[1]+".");
			}
		}
      else if(cmd.equals("server"))
      {
        test(cmd,parts,1);
        int port=6667;
        String pass="";
        if(parts.length>2) port=(new Integer(parts[2])).intValue();
        if(parts.length>3) pass=parts[3];
        String host=parts[1];
        if(server.isConnected()) server.disconnect();
        server.setServers(new String[] {host},new int[] {port},new String[] {pass});
        server.connect();
      }
      else if(cmd.equals("connect"))
      {
        server.connect();
      }
      else if(cmd.equals("disconnect"))
      {
        server.disconnect();
      }
      else
      {
        super.handleCommand(source,cmd,parts,cumul);
      }
    }
    catch(NotEnoughParametersException ex)
    {
      source.report("2      ��� Invalid command format: "+ex.getMessage());
    //source.report(getText(IRCTextProvider.INTERPRETOR_INSUFFICIENT_PARAMETERS,ex.getMessage()));
    }
  }
}

