	package irc;

/**
 * Root interpretor.
 */
public abstract class RootInterpretor extends IRCObject implements Interpretor
{
  /**
   * The used string parser.
   */
  protected StringParser _parser;

  /**
   * The next interpretor to be used if the command is unknown.
   */
  protected Interpretor _next;

  /**
   * Create a new BasicInterpretor without default interpretor.
   * @param config the configuration.
   */
  public RootInterpretor(IRCConfiguration config)
  {
    this(config,null);
  }

  /**
   * Create a new BasicInterpretor.
   * @param config the configuration.
   * @param next next interpretor to be used if the command is unknown. If null,
   * the command will be sent as it to the server.
   */
  public RootInterpretor(IRCConfiguration config,Interpretor next)
  {
    super(config);
    setNextInterpretor(next);
    _parser=new StringParser();
  }

  public void setNextInterpretor(Interpretor next)
  {
    _next=next;
  }

  public Interpretor getNextInterpretor()
  {
    return _next;
  }

  public boolean isInside(Interpretor in)
  {
    while(in!=null)
    {
      if(in==this) return true;
      in=in.getNextInterpretor();
    }
    return false;
  }

  public void addLast(Interpretor in)
  {
    if(isInside(in)) return;
    Interpretor c=this;
    while(c.getNextInterpretor()!=null) c=c.getNextInterpretor();
    c.setNextInterpretor(in);
  }

  /**
   * Test the given command against the expected number of parameters.
   * @param cmd the hole command line.
   * @param parts the parsed command line.
   * @param params expected amount of parameters.
   * @throws NotEnoughParametersException if there is not enough parameters. That's it :
   * if parts.length<=params.
   */
  protected void test(String cmd,String[] parts,int params) throws NotEnoughParametersException
  {
    if(parts.length<=params) throw new NotEnoughParametersException(cmd);
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
    if(_next==null)
    {
      Server server=source.getServer();
      if(server.isConnected())
        server.execute(cumul[0]);
      else
        source.report("2  You are currently not connected...");
    }
    else
      _next.sendString(source,"/"+cumul[0]);
  }

  public void sendString(Source source,String str)
  {
    if(str.length()==0) return;

    if(str.startsWith("/"))
    {
      str=str.substring(1);
      String[] parts=_parser.parseString(str);
      String[] cumul=new String[parts.length];
      for(int i=0;i<cumul.length;i++)
      {
        cumul[i]="";
        for(int j=i;j<parts.length;j++) cumul[i]+=parts[j]+" ";
        cumul[i]=StringParser.trim(cumul[i]);
      }

      for(int i=0;i<parts.length;i++)
      {
        if(parts[i].startsWith(""+'"') && parts[i].endsWith(""+'"')) parts[i]=parts[i].substring(1,parts[i].length()-1);
        else if(parts[i].startsWith("'") && parts[i].endsWith("'")) parts[i]=parts[i].substring(1,parts[i].length()-1);
      }

      String cmd=parts[0];
      handleCommand(source,cmd.toLowerCase(java.util.Locale.ENGLISH),parts,cumul);
    }
    else
    {
      say(source,str);
    }
  }

  /**
   * Say the given text.
   * @param source the source.
   * @param str what to say.
   */
  protected void say(Source source,String str)
  {
    Server server=source.getServer();
    if(source.talkable())
    {
      source.messageReceived(server.getNick(),str);
      server.say(source.getName(),str);
    }
    else
    {
      //source.report("2      >>> Not on channel.");
      //source.report(getText(IRCTextProvider.INTERPRETOR_NOT_ON_CHANNEL));
    }
  }


}

