package irc;

import java.util.*;

/**
 * Basic interpretor.
 */
public class BasicInterpretor extends RootInterpretor implements Interpretor
{
  /**
   * Create a new BasicInterpretor without default interpretor.
   * @param config the configuration.
   */
  public BasicInterpretor(IRCConfiguration config)
  {
    this(config,null);
  }

  /**
   * Create a new BasicInterpretor.
   * @param config the configuration.
   * @param next next interpretor to be used if the command is unknown. If null,
   * the command will be sent as it to the server.
   */
  public BasicInterpretor(IRCConfiguration config,Interpretor next)
  {
    super(config,next);
  }

  /**
   * Handle the received command.
   * @param source the source that emitted the command.
   * @param cmd the hole command line.
   * @param parts the parsed command line.
   * @param cumul the cumul parsed command line.
   */
  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    try
    {
      Server server=source.getServer();
      if(cmd.equals("echo"))
      {
        test(cmd,parts,1);
        source.report(cumul[1]);
      }
      else if(cmd.equals("sleep"))
      {
        test(cmd,parts,1);
        try
        {
          int ms=(new Integer(parts[1])).intValue();
          Thread.sleep(ms);
        }
        catch(Exception ex)
        {
          //Invalid integer or interrupted, ignore it...
        }
      }
      else if(cmd.equals("me"))
      {
        test(cmd,parts,1);
        sendString(source,"/ctcp action "+cumul[1]);
      }
      else if(cmd.equals("action"))
      {
        test(cmd,parts,1);
        sendString(source,"/ctcp action "+cumul[1]);
      }
      else if(cmd.equals("play"))
      {
			 test(cmd,parts,1);
       _ircConfiguration.getAudioConfiguration().play(parts[1]);
      }
      else if(cmd.equals("url"))
      {
        test(cmd,parts,1);
        if(parts.length>=3)
          _ircConfiguration.getURLHandler().openURL(parts[1],parts[2]);
        else
          _ircConfiguration.getURLHandler().openURL(parts[1]);
      }
      else if(cmd.equals("clear"))
      {
        source.clear();
      }
      else if(cmd.equals("leave"))
      {
        source.leave();
      }
      else if(cmd.equals("msg"))
      {
        test(cmd,parts,2);
        boolean said=false;
        Enumeration e=server.getSources();
        while(e.hasMoreElements())
        {
          Source s=(Source)e.nextElement();
          if(s.getName().equals(parts[1]))
          {
            say(s,cumul[2]);
            said=true;
          }
        }
        if(!said) server.say(parts[1],cumul[2]);
      }
      else
      {
        super.handleCommand(source,cmd,parts,cumul);
      }
    }
    catch(NotEnoughParametersException ex)
    {
      source.report(" *** Invalid command format: "+ex.getMessage());
    }
  }
}

