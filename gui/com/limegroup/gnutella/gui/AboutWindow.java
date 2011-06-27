package com.limegroup.gnutella.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.limewire.util.OSUtils;
import org.limewire.util.VersionUtils;

import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * Contains the <tt>JDialog</tt> instance that shows "about" information for the
 * application.
 */
// 2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class AboutWindow {
	/**
	 * Constant handle to the <tt>JDialog</tt> that contains about information.
	 */
	private final JDialog DIALOG;

	/**
	 * Constant for the scolling pane of credits.
	 */
	private final ScrollingTextPane SCROLLING_PANE;

	/**
	 * Check box to specify whether to scroll or not.
	 */
	private final JCheckBox SCROLL_CHECK_BOX = new JCheckBox(I18n
			.tr("Automatically Scroll"));

	/**
	 * Constructs the elements of the about window.
	 */
	AboutWindow() {
		DIALOG = new JDialog(GUIMediator.getAppFrame());

		if (!OSUtils.isMacOSX())
			DIALOG.setModal(true);

		DIALOG.setSize(new Dimension(450, 400));
		DIALOG.setResizable(false);
		DIALOG.setTitle(I18n.tr("About FrostWire"));
		DIALOG.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		DIALOG.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				SCROLLING_PANE.stopScroll();
			}

			public void windowClosing(WindowEvent we) {
				SCROLLING_PANE.stopScroll();
			}
		});

		// set up scrolling pane
		SCROLLING_PANE = createScrollingPane();
		SCROLLING_PANE.addHyperlinkListener(GUIUtils.getHyperlinkListener());

		// set up limewire version label
		JLabel client = new JLabel(I18n.tr("FrostWire") + " "
				+ FrostWireUtils.getFrostWireVersion());
		client.setHorizontalAlignment(SwingConstants.CENTER);

		// set up java version label
		JLabel java = new JLabel("Java " + VersionUtils.getJavaVersion());
		java.setHorizontalAlignment(SwingConstants.CENTER);

		// set up frostwire.com label
		JLabel url = new URLLabel("http://www.frostwire.com");
		url.setHorizontalAlignment(SwingConstants.CENTER);

		// set up scroll check box
		SCROLL_CHECK_BOX.setSelected(true);
		SCROLL_CHECK_BOX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (SCROLL_CHECK_BOX.isSelected())
					SCROLLING_PANE.startScroll();
				else
					SCROLLING_PANE.stopScroll();
			}
		});

		// set up close button
		JButton button = new JButton(I18n.tr("Close"));
		DIALOG.getRootPane().setDefaultButton(button);
		button.setToolTipText(I18n.tr("Close This Window"));
		button.addActionListener(GUIUtils.getDisposeAction());

		// layout window
		JComponent pane = (JComponent) DIALOG.getContentPane();
		GUIUtils.addHideAction(pane);

		pane.setLayout(new GridBagLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(GUIConstants.SEPARATOR,
				GUIConstants.SEPARATOR, GUIConstants.SEPARATOR,
				GUIConstants.SEPARATOR));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridwidth = 2;
		gbc.gridy = 0;

		LogoPanel logo = new LogoPanel();
		logo.setSearching(true);
		pane.add(logo, gbc);

		gbc.gridy = 1;
		pane.add(Box.createVerticalStrut(GUIConstants.SEPARATOR), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		pane.add(client, gbc);

		gbc.gridy = 3;
		pane.add(java, gbc);

		gbc.gridy = 4;
		pane.add(url, gbc);

		gbc.gridy = 5;
		pane.add(Box.createVerticalStrut(GUIConstants.SEPARATOR), gbc);

		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 6;
		pane.add(SCROLLING_PANE, gbc);

		gbc.gridy = 7;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		pane.add(Box.createVerticalStrut(GUIConstants.SEPARATOR), gbc);

		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.gridy = 8;
		pane.add(SCROLL_CHECK_BOX, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.EAST;
		pane.add(button, gbc);

	}
	
	private void appendListOfNames(String commaSepNames, StringBuilder sb) {
		for (String name : commaSepNames.split(","))
			sb.append("<li>"+name+"</li>");
	}

	private ScrollingTextPane createScrollingPane() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");

        Color color = new JLabel().getForeground();
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        String hex = toHex(r) + toHex(g) + toHex(b);
        sb.append("<body text='#" + hex + "'>");

        //  introduction
        sb.append(I18n.tr("<h1>FrostWire Logo Designer</h1>"));
        sb.append("<ul><li>Luis Ramirez (Venezuela - <a href='http://www.elblogo.com'>ElBlogo.com</a>)</li></ul>");

        sb.append(I18n.tr("<h1>FrostWire Graphics Designers/Photographers</h1>"));
        sb.append("<ul>");
        sb.append("<li>Kirill Grouchnikov - Breadcrumbs component and Substance library <a href='http://www.pushing-pixels.org/'>Pushing-Pixels.org</a></li>");
        sb.append("<li>Arianys Wilson - Splash 4.18 (New York - <a href='http://nanynany.com/blog/?from=frostwire'>NanyNany.com</a>)</li>");
        sb.append("<li>Scott Kellum - Splash 4.17 (New York - <a href='http://www.scottkellum.net'>ScottKellum.net</a>)</li>");
        sb.append("<li>Shelby Allen - Splash 4.13 (New Zealand - <a href='http://www.stitzu.com'>Stitzu.com</a>)</li>");
        sb.append("<li>Cecko Hanssen - <a href='http://www.flickr.com/photos/cecko/95013472/'>Frozen Brothers</a> CC Photograph for 4.17 Splash (Tilburg, Netherlands)</li>");
        sb.append("</ul>");

        sb.append(I18n.tr("<h1>Thanks to Former FrostWire Developers</h1>"));
        sb.append("<li>Gregorio Roper (Germany)</li>");
        sb.append("<li>Fernando Toussaint '<strong>FTA</strong>' - <a href='http://www.cybercultura.net'>Web</a></li>");
        sb.append("<br><br>");

        sb.append(I18n.tr("<h1>Thanks to the FrostWire Tester Community!</h1>"));
        sb.append(I18n.tr("Special thanks to <strong>Hobo</strong> for being the most passionate and amazing software tester ever. <a href=\"https://groups.google.com/group/frostwire-5-testers/subscribe?note=1&hl=en&noredirect=true&pli=1\">Become a tester</a>"));
        
        sb.append(I18n.tr("<h1>Thanks to the FrostWire Chat Community!</h1>"));
        sb.append(I18n.tr("Thanks to everybody that has helped us everyday in the forums and chatrooms, " +
        		"you not only help new users but you also warn the FrostWire team of any problem that " +
                "occur on our networks. Thank you all, without you this wouldn't be possible!"));
        sb.append(I18n.tr("<br><br>In Special we give thanks to the Chatroom Operators and Forum Moderators"));
        sb.append("<ul>");

        sb.append(I18n.tr("<h1>FrostWire Chat Operators</h1>"));
        String chat_operators = "Aubrey,Casper,COOTMASTER,Emily,Flying_Dutch_ManxD,Gummo,Hobo,Humanoid,iDan,lexie,Lynx,Marshall,OfficerSparker,PwincessJess,Tea,THX1138,The_Fox,WolfWalker,Wyrdjax";
        appendListOfNames(chat_operators, sb);
        sb.append("</ul>");

        sb.append(I18n.tr("<h1>FrostWire Forum Moderators</h1>"));
        String forum_moderators="Aaron.Walkhouse,Calliope,cootmaster,Efrain,et voil&agrave;,nonprofessional,Only A Hobo,spuggy,stief,The_Fox";
        sb.append("<ul>");
        appendListOfNames(forum_moderators,sb);
        sb.append("</ul>");
        
        sb.append("<h1>Many Former Chat Operators</h1>");
        String former_operators="AlleyCat,Coelacanth,Gollum,Jewels,Jordan,Kaapeli,Malachi,Maya,Sabladowah,Sweet_Songbird,UB4T,jwb,luna_moon,nonproffessional,sug,the-jack,yummy-brummy";
        sb.append("<ul>");
        appendListOfNames(former_operators, sb);
        sb.append("</ul>");

        sb.append(I18n.tr("And also to the Support Volunteer Helpers:"));
        sb.append("<ul>");
        appendListOfNames("dutchboy,Lelu,udsteve",sb);
        sb.append("</ul>");
        
        // azureus/vuze devs.
        sb.append("<h1>Thanks to the Azureus Core Developers</h1>");
        String az_devs = "Olivier Chalouhi (gudy),Alon Rohter (nolar),Paul Gardner (parg),ArronM (TuxPaper),Paul Duran (fatal_2),Jonathan Ledlie(ledlie),Allan Crooks (amc1),Xyrio (muxumx),Michael Parker (shadowmatter),Aaron Grunthal (the8472)";
        sb.append("<ul>");
        appendListOfNames(az_devs, sb);
        sb.append("</ul>");

        //  developers                                                                                                                                                               
        sb.append(I18n.tr("<h1>Thanks to the LimeWire Developer Team</h1>"));
        sb.append("<ul>\n" +
        		"  <li>Greg Bildson</li>\n" + 
        		"  <li>Sam Berlin</li>\n" + 
        		"  <li>Zlatin Balevsky</li>\n" + 
        		"  <li>Felix Berger</li>\n" +
        		"  <li>Mike Everett</li>\n" +
        		"  <li>Kevin Faaborg</li>\n" +
        		"  <li>Jay Jeyaratnam</li>\n" +               
        		"  <li>Curtis Jones</li>\n" +
        		"  <li>Tim Julien</li>\n" +
        		"  <li>Akshay Kumar</li>\n" +
        		"  <li>Jeff Palm</li>\n" + 
        		"  <li>Mike Sorvillo</li>\n" +
        		"  <li>Dan Sullivan</li>\n" +
        "</ul>");
        
        //  community VIPs
        sb.append(I18n.tr("Several colleagues in the Gnutella community merit special thanks. These include:"));
        sb.append("<ul>\n" + 
        		"  <li>Vincent Falco -- Free Peers, Inc.</li>\n" + 
        		"  <li>Gordon Mohr -- Bitzi, Inc.</li>\n" + 
        		"  <li>John Marshall -- Gnucleus</li>\n" +
        		"  <li>Jason Thomas -- Swapper</li>\n" +
        		"  <li>Brander Lien -- ToadNode</li>\n" +
        		"  <li>Angelo Sotira -- www.gnutella.com</li>\n" +
        		"  <li>Marc Molinaro -- www.gnutelliums.com</li>\n" +
        		"  <li>Simon Bellwood -- www.gnutella.co.uk</li>\n" +
        		"  <li>Serguei Osokine</li>\n" +
        		"  <li>Justin Chapweske</li>\n" +
        		"  <li>Mike Green</li>\n" +
        		"  <li>Raphael Manfredi</li>\n" +
        		"  <li>Tor Klingberg</li>\n" +
        		"  <li>Mickael Prinkey</li>\n" +
        		"  <li>Sean Ediger</li>\n" +
        		"  <li>Kath Whittle</li>\n" +
        "</ul>");
        
        sb.append(I18n.tr("<h1>Thanks to the PJIRC Staff</h1>"));
        sb.append("<ul>");
        sb.append("<li>Plouf</li>");
        sb.append("<li>Jiquera</li>");
        sb.append("<li>Ezequiel</li>");
        sb.append("<li>Superchatbar.nl</li>");
        sb.append("<li>Thema</li>");
        sb.append("</ul>");

        sb.append(I18n.tr("<h1>Thanks to the Automatix Team</h1>"));
        sb.append("<p>For helping distribute Frostwire to opensource communities in a very simple manner.");
        sb.append("<ul>");
        sb.append("<li>Arnieboy</li>");
        sb.append("<li>JimmyJazz</li>");
        sb.append("<li>Mstlyevil</li>");
        sb.append("<li>WildTangent</li>");
        sb.append("</ul>");
        
        sb.append(I18n.tr("<h1>Thanks to Ubuntu/Kubuntu Teams</h1>"));
        sb.append(I18n.tr("<p>For making the world a better place with such an excellent distro, you'll be the ones to make a difference on the desktop.</p>"));

        sb.append(I18n.tr("<h1>Thanks to the NSIS Project</h1>"));
        sb.append(I18n.tr("<p>Thanks for such an awesome installer builder system and documentation.</p>"));

        sb.append(I18n.tr("<h1>Thanks to our families</h1>"));
        sb.append(I18n.tr("For being patient during our many sleepless nights"));        
        
        // bt notice
        sb.append("<small>");
        sb.append("<br><br>");
        sb.append(I18n.tr("BitTorrent, the BitTorrent Logo, and Torrent are trademarks of BitTorrent, Inc."));
        sb.append("</small>");
        
        sb.append("</body></html>");
        
        return new ScrollingTextPane(sb.toString());
    }

	/**
	 * Returns the int as a hex string.
	 */
	private String toHex(int i) {
		String hex = Integer.toHexString(i).toUpperCase();
		if (hex.length() == 1)
			return "0" + hex;
		else
			return hex;
	}

	/**
	 * Displays the "About" dialog window to the user.
	 */
	void showDialog() {
		GUIUtils.centerOnScreen(DIALOG);

		if (SCROLL_CHECK_BOX.isSelected()) {
			ActionListener startTimerListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					// need to check isSelected() again,
					// it might have changed in the past 10 seconds.
					if (SCROLL_CHECK_BOX.isSelected()) {
						// activate scroll timer
						SCROLLING_PANE.startScroll();
					}
				}
			};

			Timer startTimer = new Timer(10000, startTimerListener);
			startTimer.setRepeats(false);
			startTimer.start();
		}
		DIALOG.setVisible(true);
	}
}
