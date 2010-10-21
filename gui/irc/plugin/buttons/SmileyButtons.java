package irc.plugin.buttons;

import irc.IRCApplication;
import irc.IRCConfiguration;
import irc.SmileyTable;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;


public class SmileyButtons extends WindowAdapter implements ActionListener,Runnable 
{
	private Thread _thread=null;
	private Frame _frame;
	private Panel _panel;
	private IRCApplication _appl;
	private IRCConfiguration _config;
	private SmileyTable _smileyTable;
	private static int _width=18;
	private static int _height=22;

	public static int getHeight() { return _height; }
	public static int getWidth() { return _width; }
	
	public SmileyButtons(IRCConfiguration config, IRCApplication appl)
	{
		_appl=appl;
		_config=config;
		_smileyTable=_config.getSmileyTable();
		_frame=new Frame();
		_frame.setVisible(false);
		_frame.setTitle("Smileys Available");
		
		_frame.setLayout(new BorderLayout());
		//_frame.setResizable(true);
		_frame.addWindowListener(this);

		if (_thread == null) 
		{
			_thread = new Thread(this, "Smiley_Picker");
			_thread.start();
		}
	}

	/**
	 * The Buttons to be shown at the SmileyPicker window.
	 * Should have as an ActionListener this SmileyButtons class.
	 * 
	 * @author gubatron
	 *
	 */
	class JSmileyButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1424L;
		private String _match; //the match (KEY) on the Smiley Table for this.

		public void setMatch(String m) { _match = m; }
		public String getMatch() { return _match; }
		
		/**
		 * 
		 * @param icon - Preferrably pass an ImageIcon to support animated gifs
		 * @param match - The text command used to pull out the icon represented by the button
		 */
		public JSmileyButton (java.awt.Image icon, String match) {
			super(new ImageIcon(icon));
			setMatch(match);
			setSize(SmileyButtons.getWidth(),SmileyButtons.getHeight());
		}
	}
	
	public void run() 
	{
		Thread myThread=Thread.currentThread();
		_panel=new Panel();
		//_panel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		int columns = 5;
		int rows = (_smileyTable.getSize()/columns) + 1;
		_panel.setLayout(new GridLayout(rows, columns));

		int s=_smileyTable.getSize();
		for(int i=0;i<s;i++)
		{
			String str=_smileyTable.getMatch(i);
			Image img=_smileyTable.getImage(i);
			JSmileyButton button = new JSmileyButton(img, str);
			button.setSize(_width, _height);
			button.addActionListener(this); //so this guy will do whatever needs to be done on click or enter
			_panel.add(button);
		}
		addPaneltoFrame(this._panel);
	}

	/**
	 * This happens whenever a Button on the Smiley Picker is clicked.
	 * We are the action Listener for each button.
	 * 
	 * Since all buttons are sending us an action performed events, we
	 * grab the source object on the ActionEvent, and from it, we ask
	 * what is the command (Smiley alias) to be sent to the Chat Text Field.
	 */
	public void actionPerformed(ActionEvent e) {
	 String cmd = ((JSmileyButton) e.getSource()).getMatch();
	 _appl.setFieldText(_appl.getFieldText() + cmd);
	}

	public void addPaneltoFrame(Panel panel)
	{
		int s=_smileyTable.getSize();
		_panel=panel;
		_frame.add(_panel);
		_frame.pack();
	}

	public void show()
	{
		_frame.setVisible(true);
		_frame.toFront();
	}
	
	public void windowDeactivated(WindowEvent e) 
	{
		_frame.setVisible(false);
	}
	
	public void windowClosing(WindowEvent evt)
	{
		_frame.setVisible(false);
	}
}