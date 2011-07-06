package com.frostwire.gui.bittorrent;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.UrlUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.HttpFetcher.HttpRequestInfo;
import com.frostwire.HttpFetcherListener;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;

public class ShareTorrentDialog extends JDialog {


	public class GoogleShortenerResponse {
		public String kind;
		public String id;
		public String longUrl;
	}
	
	private abstract class AbstractHttpFetcherListener implements
			HttpFetcherListener {

		private URI _shortenerUri;

		public AbstractHttpFetcherListener(String uri) {
			try {
				_shortenerUri = new URI(uri);
			} catch (URISyntaxException e) {
			}
		}

		@Override
		public void onError(Exception e) {
			if (_shortnerListeners.size() > 1) {
				_shortnerListeners.remove(0);
				performAsyncURLShortening(_shortnerListeners.get(0));
				
				System.out.println(">>> AbstractHttpFetcherListener ERROR >>>");
				e.printStackTrace();
				System.out.println();
				System.out.println("URL: [" + _shortenerUri.toString() + "]");
				System.out.println(">>> AbstractHttpFetcherListener ERROR >>>");
			}

		}

		abstract public void onSuccess(byte[] body);

		public URI getShortenerURL() {
			return _shortenerUri;
		}

		abstract HttpRequestInfo getRequestInfo();
			
	}

	private static final long serialVersionUID = -7466273012830791935L;
	private TOTorrent _torrent;
	private Container _container;
	private JLabel _introLabel;
	private JEditorPane _textArea;
	private JLabel _tipsLabel;
	private Action[] _actions;

	private AbstractHttpFetcherListener _bitlyShortnerListener;
	private AbstractHttpFetcherListener _tinyurlShortnerListener;

	/**
	 * Add more URL Shortner listeners to this list on
	 * initURLShortnerListeners() to have more URL Shortening services to fall
	 * back on. They will be executed in the order they were added.
	 */
	private List<AbstractHttpFetcherListener> _shortnerListeners;

	private String _link;
	private String _info_hash;
	private String _torrent_name;

	public ShareTorrentDialog(TOTorrent torrent) {
		_torrent = torrent;

		setupUI();
	}

	private void initURLShortnerListeners() {
		_bitlyShortnerListener = new AbstractHttpFetcherListener(
				"http://api.bit.ly/v3/shorten?format=txt&login=frostwire&apiKey=R_749968a37da3260493d8aa19ee021d14&longUrl="
						+ getLongFormLink()) {
			
			@Override
			public void onSuccess(byte[] body) {
				_link = new String(body);
				updateTextArea();
			}

			@Override
			HttpRequestInfo getRequestInfo() {
				return new HttpRequestInfo(true,null,null);
			}
		};
		
		_tinyurlShortnerListener = new AbstractHttpFetcherListener(
				"http://tinyurl.com/api-create.php?url=" + getLongFormLink()) {

			@Override
			public void onSuccess(byte[] body) {
				_link = new String(body);
				updateTextArea();
			}

			@Override
			HttpRequestInfo getRequestInfo() {
				return new HttpRequestInfo(true,null,null);
			}

		};

		_shortnerListeners = new LinkedList<ShareTorrentDialog.AbstractHttpFetcherListener>(Arrays.asList(
				_bitlyShortnerListener,_tinyurlShortnerListener));


	}

	private void updateTextArea() {
		GUIMediator.safeInvokeLater(new Runnable() {
			@Override
			public void run() {
				_textArea.setText(I18n.tr("Download") + " \"" + _torrent_name.replace("_", " ") + "\" " + I18n.tr("at") + " "
						+ getLink() + " "+ I18n.tr("via FrostWire"));
			}
		});
	}

	
	private void setupUI() {
		setupWindow();

		initTorrentname();

		initInfoHash();

		GridBagConstraints c = new GridBagConstraints();

		// INTRO LABEL
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(20, 10, 10, 10);
		TOTorrentFile[] files = _torrent.getFiles();
		boolean folderTorrent = files.length > 1;
		_introLabel = new JLabel(folderTorrent ? String.format(
				I18n.tr("Use the following text to share the \"%s\" folder"),
				_torrent_name) : String.format(String.format(
				"Use the following text to share the \"%s\" file",
				_torrent_name)));
		_introLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		_container.add(_introLabel, c);

		// TEXT AREA
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(1, 10, 1, 10);

		_textArea = new JEditorPane();
		_textArea.setEditable(false);
		updateTextArea();
		
		Font f = new Font("Dialog", Font.BOLD, 13);
		_textArea.setFont(f);
		_textArea.setMargin(new Insets(10, 10, 10, 10));
		_textArea.selectAll();
		_textArea.setBorder(BorderFactory.createEtchedBorder());
		_container.add(_textArea, c);

		initURLShortnerListeners();
		performAsyncURLShortening(_shortnerListeners.get(0));

		// BUTTON ROW
		initActions();

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		//c.weightx = 1.0;
		c.insets = new Insets(10, 10, 10, 10);
		ButtonRow buttonRow = new ButtonRow(_actions, ButtonRow.X_AXIS,
				ButtonRow.RIGHT_GLUE);
		
		fixButtonBorders(buttonRow);
		ToolTipManager.sharedInstance().setInitialDelay(200);
		
		_container.add(buttonRow, c);

		// TIPS LABEL
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.insets = new Insets(10, 10, 10, 10);
		_tipsLabel = new JLabel(
				"<html><p> > <strong>Keep FrostWire Open</strong> until the file has been downloaded by at least one other friend.</p><p>&nbsp;</p><p> > <strong>The more, the merrier.</strong> The more people sharing the faster it can be downloaded by others.</p></html>");
		_tipsLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		_tipsLabel.setBorder(BorderFactory.createTitledBorder(I18n.tr("Tips")));
		_container.add(_tipsLabel, c);

		GUIUtils.addHideAction((JComponent) getContentPane());
		pack();
	}

	private void fixButtonBorders(ButtonRow buttonRow) {
		if (_actions == null) {
			return;
		}

		for (int i = 0; i < _actions.length; i++) {
			JButton buttonAtIndex = buttonRow.getButtonAtIndex(i);
			buttonAtIndex.setBorderPainted(true);
		}
	}

	private void initTorrentname() {
		_torrent_name = _torrent.getUTF8Name();

		if (_torrent_name == null) {
			_torrent_name = new String(_torrent.getName());
		}
	}

	private void initInfoHash() {
		try {
			_info_hash = TorrentUtil.hashToString(_torrent.getHash());
		} catch (TOTorrentException e) {
		}
	}

	private String getLink() {
		if (_link == null) {
			_link = "http://maglnk.com/" + _info_hash;
		}
		return _link;
	}

	private void performAsyncURLShortening(AbstractHttpFetcherListener listener) {
		HttpFetcher asyncFetcher = new HttpFetcher(listener.getShortenerURL(), 2000);

		asyncFetcher.asyncRequest(listener.getRequestInfo(),listener);
	}

	private String getLongFormLink() {
		return "http://maglnk.com/" + _info_hash + "/"
				+ UrlUtils.encode(_torrent_name.replace(".torrent", ""));

	}

	private void initActions() {
		_actions = new Action[4];

		_actions[0] = new TwitterAction();
		_actions[1] = new CopyToClipboardAction();
		_actions[2] = new CopyLinkAction();
		_actions[3] = new CopyMagnetAction();

	}

	private void setupWindow() {
		setTitle(I18n.tr("All done! Now share the link"));

		Dimension prefDimension = new Dimension(540, 300);

		setSize(prefDimension);
		setMinimumSize(prefDimension);
		setPreferredSize(prefDimension);
		setResizable(false);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		GUIUtils.addHideAction((JComponent) getContentPane());

		_container = getContentPane();
		_container.setLayout(new GridBagLayout());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ToolTipManager.sharedInstance().setInitialDelay(500);
			}
		});
	}
	
	

	private class TwitterAction extends AbstractAction {

		private static final long serialVersionUID = -2035234758115291468L;

		public TwitterAction() {
			putValue(Action.NAME, I18n.tr("Twitter it"));
			putValue(Action.SHORT_DESCRIPTION,I18n.tr("Send the message above to Twitter"));
			putValue(LimeAction.ICON_NAME,"TWITTER");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			GUIMediator
					.openURL("http://twitter.com/intent/tweet?source=FrostWire&text="
							+ UrlUtils.encode(_textArea.getText()));
		}
	}

	private class CopyToClipboardAction extends AbstractAction {

		private static final long serialVersionUID = 2130811125951128397L;

		public CopyToClipboardAction() {
			putValue(Action.NAME, I18n.tr("Copy Text"));
			putValue(Action.SHORT_DESCRIPTION,I18n.tr("Copy entire message to Clipboard"));
			putValue(LimeAction.ICON_NAME,"COPY_PASTE");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			GUIMediator.setClipboardContent(_textArea.getText());					
		}
	}

	public class CopyLinkAction extends AbstractAction {

		private static final long serialVersionUID = 5396173442291772242L;

		public CopyLinkAction() {
			putValue(Action.NAME, I18n.tr("Copy Link"));
			putValue(Action.SHORT_DESCRIPTION,I18n.tr("Copy Link to Clipboard"));
			putValue(LimeAction.ICON_NAME,"LINK");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			GUIMediator.setClipboardContent(_link);
		}

	}
	
	private class CopyMagnetAction extends AbstractAction {

		private static final long serialVersionUID = 4972170728829407730L;

		public CopyMagnetAction() {
			putValue(Action.NAME, I18n.tr("Copy Magnet"));
			putValue(Action.SHORT_DESCRIPTION,I18n.tr("Copy Magnet URL to Clipboard"));
			putValue(LimeAction.ICON_NAME,"MAGNET");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			GUIMediator.setClipboardContent(TorrentUtil.getMagnet(_info_hash));
		}
	}
}