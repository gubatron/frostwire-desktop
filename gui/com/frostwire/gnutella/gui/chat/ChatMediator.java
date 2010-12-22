package com.frostwire.gnutella.gui.chat;

import irc.IRCApplication;
import irc.IRCConfiguration;
import irc.StartupConfiguration;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.frostwire.gnutella.gui.sponsors.BannerContainer;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.ChatSettings;

//import com.limegroup.gnutella.gui.download.DownloadModel;

/**
 * This class functions as an initializer for all of the elements
 * of the chat and as a mediator between chat objects.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class ChatMediator implements ThemeObserver {
//public final class ChatMediator extends AbstractTableMediator<ChatModel,ChatDataLine,
//        ChatItem> implements ThemeObserver{
//public final class ChatMediator extends AbstractTableMediator<ChatModel, DownloadDataLine, ChatItem>  implements ThemeObserver{


	/**
	 * The primary panel that contains all of the library elements.
	 */
	private static final JPanel MAIN_PANEL = new JPanel(new GridBagLayout());
    private static IRCApplication PJIRC;
    private static IRCConfiguration _config; // added to use config procedures
    private BannerContainer bannerContainer;
    private boolean _chatAlreadyStarted = false;

	/**
	 * Singleton instance of this class.
	 */
	private static ChatMediator INSTANCE; 
    
	/**
	 * @return the <tt>ChatMediator</tt> instance
	 */
	public static ChatMediator instance() { 
		if (INSTANCE == null) {
			INSTANCE = new ChatMediator();
		}
		return INSTANCE; 
	}

    /**
     * Override the default main panel setup
     */
/*
    protected void setupMainPanel() {
        if (MAIN_PANEL == null)
            return;

        super.setupMainPanel();

	    JPanel status = new JPanel();
	    status.setLayout(new BorderLayout());
	    //status.add(SERVENT_STATUS, BorderLayout.WEST);
	    //status.add(NEIGHBORS, BorderLayout.EAST);

        MAIN_PANEL.add(status, 0);
    }
*/
	/**
	 * Returns the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 *
	 * @return the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 */
	public JComponent getComponent() {
		return MAIN_PANEL;
	}

	
	// inherit doc comment
	public void updateTheme() {
	}


	private ChatMediator() {
        	//HERE IS WHERE WE ADD THE PJIRC OBJECT.
  	        GUIMediator.setSplashScreenString(I18n.tr("Loading Community Chat..."));
        	ThemeMediator.addThemeObserver(this);
        	
        	//Tries to create an IRC chat, if the Nickname is still a default nick, it will fail
        	//and this function can be invoked again and again until a chat has been started and
        	//added to the Chat Tab. Once it succeeds invoking it will be a void action.
        	Runnable r = new Runnable() {
				public void run() {
				updateSplashScreen();				
				//System.out.println("Trying to start chat panel...");    
					tryToStartAndAddChat();
				//System.out.println("end of try...");    
				}
        	};
        	new Thread(r).start();
    }
	
	/**
	 * Checks if the user has setup already a nickname. If not, it'll show an input dialog
	 * until the user enters a valid nickname.
	 */
	public void ensureValidNickname() throws Exception {
        if (ChatSettings.CHAT_IRC_NICK.getValue().equals("") ||
            ChatSettings.CHAT_IRC_NICK.getValue().toLowerCase().startsWith("fw_guest")) {
            //we try and try until we get this dude to pick up a good nick name.
            String nickname = "";
            
            while (nickname.equals("")) {
                nickname = JOptionPane.showInputDialog(null, "Enter your desired chat Nickname", "Nickname required to connect", JOptionPane.INFORMATION_MESSAGE);

                //hit cancel button
                if (nickname == null)
                    throw new Exception("ChatMediator.ensureValidNickname() cancelled");
                
                //now make sure the nickname given will not break any rules
                if (isNickNameCensored(nickname)) {
                    JOptionPane.showMessageDialog(null, "The nickname '"+nickname+"' can't be accepted");
                    nickname = "";
                } else {
                    nickname = nickname.replaceAll(" ", "_");
                }
            } //while

            //System.out.println("Using: " + nickname);
            ChatSettings.CHAT_IRC_NICK.setValue(nickname);
            //System.out.println("After the while nick -> " + nickname);
            reloadConfiguration();

        } //if
	} //ensureValidNickname

	/**
	 * Given a string compares it to a lot of patterns. If it matches one of them
	 * the nick is invalid.
	 * @param nick
	 * @return
	 */
	private boolean isNickNameCensored(String nick) {
	    if (nick == null || 
	        nick.toLowerCase().startsWith("fw_guest") ||
	        nick.equals(""))
	        return true;
	    
	    String[] patterns = new String[] {
	     ".*pussy.*","^puss$", ".*pussi.*",".*fuck.*", ".*fu.*ck.*",  
         ".*fook.*", ".*feck.*", ".*fck.*", ".*fuq.*", ".*f.*u.*c.*k.*",  
         "^fuk$", "^fux$", ".*fuyck.*","^shit.*", "^whore.*", "^slut.*", ".*bitch.*",".*biach.*", 
         "^cunt.*", ".*f.*u.*c.*k.*", ".*fcuk.*",".*dick.*","^dik$","^twat$",
         ".*webcam.*",".*phuck.*",".*niga.*",".*nigga.*",".*nigger.*",".*nigglet.*",
         ".*wigger.*",".*porn.*",".*p0rn.*","^wank.*",".*kike.*",".*teen.*sex.*",
         ".*cyber.*sex.*","^http.*","^www\\..*",".*\\.com$",""};
	    
	    for (String regex : patterns) {
	        if (nick.matches(regex)) {
	            return true;
	        }
	    }
	
	    return false;
	} //isNickNameCensored
	
	/**
	 * It will reload the Chat configuration from whatever settings have been saved.
	 * Useful when we update ChatSettings and the Chat Tab has already been loaded.
	 */
	public void reloadConfiguration() {
		//System.out.println("initializing chat community...");	
    	IRCConfiguration ircConfiguration = IRCConfiguration.createDummyIRCConfiguration(); //default config	
    	ircConfiguration.set("gui", new String("pixx")); //this loads the GUI interface we all know from PJIRC   	
    	
		String currlang=ApplicationSettings.LANGUAGE.getValue();
		
		Hashtable defaultRooms = new Hashtable();
		//by popularity
		//(For further language code reference if this needs to be
		// mantained in the future, use:
		// http://unicode.org/onlinedat/languages.html
		// and
		// http://en.wikipedia.org/wiki/<LANGUAGE_NAME_IN_ENGLISH_HERE>_language
		
		defaultRooms.put("en", "Main"); //English
		defaultRooms.put("nl", "Hollands"); //Dutch
		defaultRooms.put("pt", "Portugues"); //Portuguese
		defaultRooms.put("fr", "Francais"); //French
		defaultRooms.put("de", "Deutsch"); //German
		defaultRooms.put("es", "Espanol"); //Spanish
		defaultRooms.put("tr", "Turkce"); //Turkish
		defaultRooms.put("no", "Norsk"); //Norwegian		
		defaultRooms.put("da", "Dansk"); //Danish 	
		defaultRooms.put("it", "Italiano"); //Italian
		defaultRooms.put("sv", "Svenska"); //Swedish	
		defaultRooms.put("pl", "Polski"); //Polish
		defaultRooms.put("cz", "Cesko"); //Czech
		defaultRooms.put("tl", "Tagalog"); //Filipino
		defaultRooms.put("ja", "Nihongo"); //Japanese
		defaultRooms.put("fi", "Suomi"); //Finnish
		defaultRooms.put("hu", "Magyar"); //Hungarian

		defaultRooms.put("el", "Greek"); //couldnt find a way to spell it on its own language
		
		String defaultRoom = (defaultRooms.containsKey(currlang)) ? 
				(String) defaultRooms.get((String) currlang) : "Main";
				
		StartupConfiguration startupConfiguration = new StartupConfiguration(ChatSettings.CHAT_IRC_NICK.getValue(), //nick
	    			ChatSettings.CHAT_IRC_NICK.getValue()+"????", //alt nick    			
	    			"[P:RX{0,G}]", //full name, dont change
	    			new String[] {""}, //passwords to use on the servers, dont change
	    			new String[] {ChatSettings.CHAT_SERVER.getValue()}, //servers to connect to
	    			new int[] {6667}, //ports of the server, dont change
	    			"", //alias, dont change
	    			//new String[] {new String("list"),new String("join #test")}, //commands to execute
	    			new String[] {new String("JOIN #" + defaultRoom)}, //commands to execute
	    			new String[] {"buttons.Smileys"}, //plugins to load, dont change
	    			ChatSettings.SMILEYS_ENABLED.getValue()); // Loads smileys
		
		//System.out.println("Creating new IRC App...");
		PJIRC = new IRCApplication(ircConfiguration,startupConfiguration,MAIN_PANEL);
		_config = ircConfiguration;
		//System.out.println("Initializing new IRC App...");
		PJIRC.init();
		//System.out.println("Initialized! new IRC App...");

	} //reloadConfiguration
	
	public void tryToStartAndAddChat() {
		
		if (_chatAlreadyStarted) {
			return;
		}

		//initializes PJRIC with saved settings
		reloadConfiguration();
		
		/*
    	DefaultSource _defaultSource=new DefaultSource(ircConfiguration);
    	DefaultInterpretor defaultInter=new DefaultInterpretor(ircConfiguration,startupConfiguration,PJIRC,PJIRC);
    	_defaultSource.setInterpretor(defaultInter);
    	*/

		try {
		    //System.out.println("getting nick: "+ ChatSettings.CHAT_IRC_NICK.getValue().toLowerCase() + "Chat enabled?: "+ (Boolean)ChatSettings.CHAT_IRC_ENABLED.getValue());
		    //ChatSettings.CHAT_IRC_NICK.setValue((String)"FTA");	
		    if (ChatSettings.CHAT_IRC_NICK.getValue().toLowerCase().trim().startsWith("fw_guest")
		            && ChatSettings.CHAT_IRC_ENABLED.getValue()) {    			
		        //System.out.println("***IRC CHAT IS CURRENTLY DISABLED***");		
		        //return;
		    }			
    	} catch (Exception e) {
    		//what are you gonna do...
    	}

    	//PJIRC.getIRCInterface().getComponent().setSize(MAIN_PANEL.getSize());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        MAIN_PANEL.setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.anchor = c.LINE_START;
        c.weightx = 1;
        c.weighty = 1;
    	
    	MAIN_PANEL.add(PJIRC.getIRCInterface().getComponent(),c);
    	//Fix for heavyweight component to not go on top of lightweight menu (Test in Windows)
    	//MAIN_PANEL.setComponentZOrder(PJIRC.getIRCInterface().getComponent(), 1);
    	//MAIN_PANEL.setComponentZOrder(MenuMediator.instance().getMenuBar(),0);

    	c.weightx = 0;
    	c.weighty = 0;
    	c.gridx = 1;
    	c.fill = c.NONE;
    	c.anchor = c.LINE_END;
	
	//FTA    	
    	bannerContainer = new BannerContainer();	
    	MAIN_PANEL.add(bannerContainer,c);
    	

    	_chatAlreadyStarted = true;
	/**
	 * new added for 4.17
	 */

	
	    ThemeMediator.addThemeObserver(this);
	    updateTheme();

	}
	

    public void disconnect() {
    	//PJIRC.sendString("/disconnect");

	    /**
    	if (bannerContainer != null) {
    		bannerContainer.setVisible(false);
			} */
    }
    
    public void connect() {
        if (PJIRC != null)
            PJIRC.sendString("/connect");
	    
	    /**
    	if (bannerContainer != null) {
    		bannerContainer.setVisible(true);
    	}
        */
    }

    public void nick(String newNick) {
    	PJIRC.sendString("/nick "+newNick);    	
    }
    
    public void smileyswindow() {
    	//System.out.println("Showing smileys...");
    	PJIRC.sendString("/smileys");
    }
    
    public void changesmileys(boolean newstatus) {
    	//System.out.println("Changing smileys status..."); 	
    	String color,txtnewstatus=null;
    	
    		if (newstatus == true) {
    			//color = "3";
    			//txtnewstatus="enabled";	
    			//System.out.println("enabling code here!...");    			
    			_config.restoreSmileyTable();
    			//System.out.println("Smileys are now enabled!");
    		} else {
    			//color = "4";
    			//txtnewstatus="disabled";	    			
    			//System.out.println("Status is now disabled!");
    			_config.resetSmileyTable();
    		}
    		
    		// The following code shows a notification in the chat window when user clicks in the "Show Smileys" option.
    		// Please keep it disabled because there's a unsolved bug with the previous reportReceived function under BaseAWTSource
    		// NOTE: It works but shows some warnings / errors
    		/*
    		if (PJIRC.isConnected() == true)
    			PJIRC.sendReport(""+ color +"      ��� Smileys are now "+ txtnewstatus +"!"); // just for debugging
    		*/	
    }
    protected JPopupMenu createPopupMenu() {
        return null;
    }
	/**
	 * Set up the necessary constants.
	 */
	protected void setupConstants() {
	    //  Create padded panel without bottom padding so that button
	    //  rows for all the tabs line up.
/*
		MAIN_PANEL = new PaddedPanel();
		DATA_MODEL = new ChatModel();
		TABLE = new LimeJTable(DATA_MODEL);
*/
		////BUTTON_ROW = (new ConnectionButtons(this)).getComponent();

		//SERVENT_STATUS = new JLabel("");
		//NEIGHBORS = new JLabel("");
    }

    /** Update the splash screen */
	protected void updateSplashScreen() {
		GUIMediator.setSplashScreenString(
                I18n.tr("Loading Community Chat Window..."));
    }
	public void handleNoSelection() {
    }
	public void handleNoSelection(int a) {
    }
  	public void handleSelection(int row) {
    }
  	public void handleActionKey() {
    }
}
