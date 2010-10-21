
package irc;

import java.util.Date;

/**
 * The CTCP interpretor.
 */
public class CTCPInterpretor extends BasicInterpretor
{
  /**
   * The CTCPFilter.
   */
  //protected CTCPFilter _filter;
  protected ServerManager _mgr;

  /**
   * Create a new CTCPInterpretor.
   * @param config global configuration.
   * @param next next interpretor to use if command is unknown.
   * @param mgr server manager
   */
  public CTCPInterpretor(IRCConfiguration config,Interpretor next/*,CTCPFilter filter*/,ServerManager mgr)
  {
    super(config,next);
    _mgr=mgr;
    //_filter=filter;
  }

  private void send(Server s,String destination,String msg)
  {
    s.say(destination,"\1"+msg+"\1");
  }

  protected void handleCommand(Source source,String cmd,String[] parts,String[] cumul)
  {
    try
    {
      if(cmd.equals("ctcp"))
      {
        test(cmd,parts,1);
        if(parts[1].toLowerCase(java.util.Locale.ENGLISH).equals("ping"))
        {
          test(cmd,parts,2);
          send(source.getServer(),parts[2],"PING "+(new Date()).getTime());
          //_filter.ping(source.getServer(),parts[2]);
        }
        else if(parts[1].toLowerCase(java.util.Locale.ENGLISH).equals("action"))
        {
          test(cmd,parts,2);
          if(source.talkable())
          {
            send(source.getServer(),source.getName(),"ACTION "+cumul[2]);
            //_filter.action(source.getServer(),source.getName(),cumul[2]);
            source.action(source.getServer().getNick(),cumul[2]);
          }
          else
          {
            source.report(" *** Your message cannot be sent.");
          }
        }
        else if(parts[1].toLowerCase(java.util.Locale.ENGLISH).equals("raw"))
        {
          test(cmd,parts,3);
          send(source.getServer(),parts[2],cumul[3]);
//          _filter.genericSend(source.getServer(),parts[2],cumul[3]);
        }
        else
        {
          test(cmd,parts,2);
          send(source.getServer(),parts[2],parts[1]);
          //_filter.genericSend(source.getServer(),parts[2],parts[1]);
        }
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

