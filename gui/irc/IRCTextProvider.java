package irc;

/**
 * Text constants for the IRC engine.
 */
public interface IRCTextProvider
{
  /**
   * Not on a channel
   */
  public static final int INTERPRETOR_NOT_ON_CHANNEL=0x001;
  /**
   * %1 : unknown dcc subcommand
   */
  public static final int INTERPRETOR_UNKNOWN_DCC=0x002;
  /**
   * %1 : insufficient parameters
   */
  public static final int INTERPRETOR_INSUFFICIENT_PARAMETERS=0x003;
  /**
   * %1 : unable to perform in current context
   */
  public static final int INTERPRETOR_BAD_CONTEXT=0x004;
  /**
   * Cannot send CTCP codes via DCC Chat
   */
  public static final int INTERPRETOR_CANNOT_CTCP_IN_DCCCHAT=0x005;
  /**
   * %1 : unknown config subcommand
   */
  public static final int INTERPRETOR_UNKNOWN_CONFIG=0x006;
  /**
   * Timestamp enabled
   */
  public static final int INTERPRETOR_TIMESTAMP_ON=0x007;
  /**
   * Timestamp disabled
   */
  public static final int INTERPRETOR_TIMESTAMP_OFF=0x008;
  /**
   * Graphical smileys enabled
   */
  public static final int INTERPRETOR_SMILEYS_ON=0x009;
  /**
   * Graphical smileys disabled
   */
  public static final int INTERPRETOR_SMILEYS_OFF=0x00A;
  /**
   * Now ignoring %1
   */
  public static final int INTERPRETOR_IGNORE_ON=0x00B;
  /**
   * Not ignoring %1 anymore
   */
  public static final int INTERPRETOR_IGNORE_OFF=0x00C;
  /**
   * Multiserver support is disabled
   */
  public static final int INTERPRETOR_MULTISERVER_DISABLED=0x00D;

  /**
   * Waiting for incoming connection...
   */
  public static final int DCC_WAITING_INCOMING=0x101;
  /**
   * Unable to open connection : %1
   */
  public static final int DCC_UNABLE_TO_OPEN_CONNECTION=0x102;
  /**
   * DCC Connection established
   */
  public static final int DCC_CONNECTION_ESTABLISHED=0x103;
  /**
   * Connection closed
   */
  public static final int DCC_CONNECTION_CLOSED=0x104;
  /**
   * Error : %1
   */
  public static final int DCC_ERROR=0x105;
  /**
   * %1 : unable to send to %2
   */
  public static final int DCC_UNABLE_TO_SEND_TO=0x106;
  /**
   * Unable to execute command from current context
   */
  public static final int DCC_BAD_CONTEXT=0x107;
  /**
   * Not connected
   */
  public static final int DCC_NOT_CONNECTED=0x108;
  /**
   * Unable to initialize passive mode
   */
  public static final int DCC_UNABLE_PASSIVE_MODE=0x109;
  /**
   * [%1 PING reply] : %2 seconds
   */
  public static final int CTCP_PING_REPLY=0x10A;
  /**
   * Stream closed
   */
  public static final int DCC_STREAM_CLOSED=0x10B;

  /**
   * Failed to launch Ident server : %1
   */
  public static final int IDENT_FAILED_LAUNCH=0x201;
  /**
   * Ident request from %1
   */
  public static final int IDENT_REQUEST=0x202;
  /**
   * Error occurred
   */
  public static final int IDENT_ERROR=0x203;
  /**
   * 
   */
  public static final int IDENT_REPLIED=0x204;
  /**
   * default user
   */
  public static final int IDENT_DEFAULT_USER=0x205;
  /**
   * No user for request
   */
  public static final int IDENT_NO_USER=0x206;
  /**
   * Ident server running on port %1
   */
  public static final int IDENT_RUNNING_ON_PORT=0x207;
  /**
   * Ident server leaving : %1
   */
  public static final int IDENT_LEAVING=0x208;
  /**
   * none
   */
  public static final int IDENT_NONE=0x209;
  /**
   * unknown
   */
  public static final int IDENT_UNKNOWN=0x20A;
  /**
   * Undefined result
   */
  public static final int IDENT_UNDEFINED=0x20B;

  /**
   * Save file as
   */
  public static final int FILE_SAVEAS=0x301;

  /**
   * About
   */
  public static final int ABOUT_ABOUT=0x401;
  /**
   * Programming
   */
  public static final int ABOUT_PROGRAMMING=0x402;
  /**
   * Design
   */
  public static final int ABOUT_DESIGN=0x403;
  /**
   * Thanks to
   */
  public static final int ABOUT_THANKS=0x404;
  /**
   * for support, ideas and testing
   */
  public static final int ABOUT_SUPPORT=0x405;
  /**
   * This software is licensed under the GPL license
   */
  public static final int ABOUT_GPL=0x406;

  /**
   * Unable to connect : %1
   */
  public static final int SERVER_UNABLE_TO_CONNECT=0x501;
  /**
   * Unable to connect to %1 : currently trying to connect to %2
   */
  public static final int SERVER_UNABLE_TO_CONNECT_STILL=0x502;
  /**
   * Disconnecting from %1
   */
  public static final int SERVER_DISCONNECTING=0x503;
  /**
   * Connecting...
   */
  public static final int SERVER_CONNECTING=0x504;
  /**
   * Not connected
   */
  public static final int SERVER_NOT_CONNECTED=0x505;
  /**
   * Logging in...
   */
  public static final int SERVER_LOGIN=0x506;
  /**
   * Disconnected from %1
   */
  public static final int SERVER_DISCONNECTED=0x507;
  /**
   * Error : %1
   */
  public static final int SERVER_ERROR=0x508;
  /**
   * Attempting to rejoin channel %1...
   */
  public static final int SERVER_AUTOREJOIN_ATTEMPT=0x0509;
  /**
   * Unable to rejoin channel %1
   */
  public static final int SERVER_AUTOREJOIN_FAILED=0x050a; 

  /**
   * Change nick to
   */
  public static final int GUI_CHANGE_NICK=0x71A;
  /**
   * Copy text
   */
  public static final int GUI_COPY_WINDOW=0x71B;
  /**
   * Warning
   */
  public static final int GUI_DCC_CHAT_WARNING_TITLE=0x71C;
  /**
   * Do you want to accept DCC chat from %1?
   */
  public static final int GUI_DCC_CHAT_WARNING_TEXT=0x71D;
  /**
   * Boy, %1 years old, %2
   */
  public static final int ASL_MALE=0x801;
  /**
   * Girl, %1 years old, %2
   */
  public static final int ASL_FEMALE=0x802;
  /**
   * %1 years old from %2
   */
  public static final int ASL_UNKNOWN=0x803;
  
  /**
   * %1 has been idle for %2
   */
  public static final int REPLY_IDLE=0x901;
  /**
   * %1 connected on %2
   */
  public static final int REPLY_SIGNON=0x902;
}
