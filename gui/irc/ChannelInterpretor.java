package irc;

/**
 * Channel interpretor.
 */
public class ChannelInterpretor extends IRCInterpretor
{
  /**
   * Create a new ChannelInterpretor.
   * @param config global configuration.
   */
  public ChannelInterpretor(IRCConfiguration config)
  {
    super(config);
  }

  private boolean isChannel(String name,Source source)
  {
    if(name.length()==0) return false;
    Server s=source.getServer();
    if(s instanceof IRCServer)
    {
      char[] prefixes=((IRCServer)s).getChannelPrefixes();
      for(int i=0;i<prefixes.length;i++) if(name.charAt(0)==prefixes[i]) return true;
    }
    return false;
  }

  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    try
    {
      if(cmd.equals("part"))
      {
        if(parts.length==1)
        {
          sendString(source,"/part "+source.getName());
        }
        else
        {
          if(isChannel(parts[1],source))
            super.handleCommand(source,cmd,parts,cumul);
          else
            sendString(source,"/part "+source.getName()+" "+cumul[1]);
        }
      }
      else if(cmd.equals("hop"))
      {
        sendString(source,"/part");
        sendString(source,"/join "+source.getName());
      }
      else if(cmd.equals("onotice"))
      {
        test(cmd,parts,1);
        if(isChannel(parts[1],source))
          super.handleCommand(source,cmd,parts,cumul);
        else
          sendString(source,"/onotice "+source.getName()+" "+cumul[1]);
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

