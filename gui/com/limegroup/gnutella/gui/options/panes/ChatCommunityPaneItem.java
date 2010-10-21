package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.settings.ChatSettings;


public final class ChatCommunityPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Community Chat");
    
    public final static String LABEL = I18n.tr("FrostWire's Community Chat Tab requires you to have a nickname to communicate with others in the chatrooms");
    
    private final String NAME_LABEL = 
            I18nMarker.marktr("Type your chat nickname here (any name):");

     /**
     * The chat Community nickname field.
     */
    private JTextField _ircNickField = new SizedTextField(15, SizePolicy.RESTRICT_HEIGHT); //Old style was this: new SizedTextField(new Dimension(100,SizedTextField.STANDARD_HEIGHT));;

    /**
     * The constructor constructs all of the elements of this 
     * <tt>AbstractPaneItem</tt>.
     *
     * @param key the key for this <tt>AbstractPaneItem</tt> that the
     *            superclass uses to generate locale-specific keys
     */
    public ChatCommunityPaneItem() {
        super(TITLE, LABEL);
        /*
        add(getVerticalSeparator());
        */
        LabeledComponent comp = new LabeledComponent(NAME_LABEL, _ircNickField);
        
        add(comp.getComponent());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Sets the options for the fields in this <tt>PaneItem</tt> when the 
     * window is shown.
     */
    public void initOptions() {
 	_ircNickField.setText(ChatSettings.CHAT_IRC_NICK.getValue());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Applies the options currently set in this window, displaying an
     * error message to the user if a setting could not be applied.
     *
     * @throws IOException if the options could not be applied for some reason
     */
    public boolean applyOptions() throws IOException {


	if (!checkChatNickIsNotFWGuest()) {
			return false;
		}
		
        ChatSettings.CHAT_IRC_NICK.setValue(_ircNickField.getText());
        
        GUIMediator.instance().tryToStartAndAddChat();
        
        //Send the nick to the IRC server.
        GUIMediator.instance().setIRCNick(ChatSettings.CHAT_IRC_NICK.getValue());
        
        return true;
    }
    
    public boolean isDirty() {
        return !ChatSettings.CHAT_IRC_NICK.getValue().equals(_ircNickField.getText());
    }

    public boolean checkChatNickIsNotFWGuest() {
		String nickInField = _ircNickField.getText().toLowerCase().trim();
		
		if (ChatSettings.CHAT_IRC_ENABLED.getValue() && (nickInField.equals("") || nickInField.startsWith("fw_guest"))) {
			JOptionPane.showMessageDialog(GUIMediator.getAppFrame(), 
			I18n.tr("The chosen nickname is not allowed."),
			I18n.tr("Warning: Forbidden name!"),
			JOptionPane.WARNING_MESSAGE);
			return false;
		} else return true;
	}	
}
