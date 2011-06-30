package com.frostwire.gui.bittorrent;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.UrlUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.HttpFetcher.HttpRequestInfo;
import com.frostwire.HttpFetcherListener;
import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;

public class ShareTorrentDialog extends JDialog implements ClipboardOwner {

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
	private JTextArea _textArea;
	private JLabel _tipsLabel;
	private Action[] _actions;

	private AbstractHttpFetcherListener _bitlyShortnerListener;
	private AbstractHttpFetcherListener _tinyurlShortnerListener;

	/** Add more URL Shortner listeners to this list on initURLShortnerListeners() for more fall back.
	 * They will be executed in the order they were added. */
	private List<AbstractHttpFetcherListener> _shortnerListeners;

	private String _link;
	private String _info_hash;
	private String _torrent_name;

	public ShareTorrentDialog(TOTorrent torrent) {
		_torrent = torrent;

		setupUI();
	}

	private void initURLShortnerListeners() {
		//R_749968a37da3260493d8aa19ee021d14
		_bitlyShortnerListener = new AbstractHttpFetcherListener("http://api.bit.ly/v3/shorten?format=txt&login=frostwire&apiKey=R_749968a37da3260493d8aa19ee021d14&longUrl="+getLongFormLink()) {
			
			@Override
			public void onSuccess(byte[] body) {
				System.out.println("SUCESS! - ["+ getShortenerURL() +"]");
				_link = new String(body);
				updateTextAreaWithShortenedLink();
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
				updateTextAreaWithShortenedLink();
			}

			@Override
			HttpRequestInfo getRequestInfo() {
				return new HttpRequestInfo(true,null,null);
			}

		};

		_shortnerListeners = new LinkedList<ShareTorrentDialog.AbstractHttpFetcherListener>(Arrays.asList(
				_bitlyShortnerListener,_tinyurlShortnerListener));


	}

	private void updateTextAreaWithShortenedLink() {
		GUIMediator.safeInvokeLater(new Runnable() {
			@Override
			public void run() {
				_textArea.setText("Download \"" + _torrent_name
						+ "\" at " + _link);
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
		c.insets = new Insets(10, 10, 10, 10);

		_textArea = new JTextArea("Download \"" + _torrent_name + "\" at "
				+ getLink(), 2, 80);
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
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(10, 10, 10, 10);
		ButtonRow buttonRow = new ButtonRow(_actions, ButtonRow.X_AXIS,
				ButtonRow.RIGHT_GLUE);
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

		pack();
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

	private void loadIconForAction(final Action action, String iconURL) {
		try {
			ImageCache.getInstance().getImage(new URL(iconURL),
					new OnLoadedListener() {

						@Override
						public void onLoaded(URL url, BufferedImage image,
								boolean fromCache, boolean fail) {
							if (!fail && action != null && image != null) {
								ImageIcon imageIcon = new ImageIcon(image);
								action.putValue(Action.LARGE_ICON_KEY,
										imageIcon);
								action.putValue(Action.SMALL_ICON, imageIcon);
							}
						}
					});
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void initActions() {
		_actions = new Action[3];

		_actions[0] = new TwitterAction();
		_actions[1] = new CopyToClipboardAction();
		_actions[2] = new CloseAction();

		// Load icons for actions
		loadIconForAction(_actions[0],
				"http://static.frostwire.com/images/20x20twitter.png");
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
	}

	private class TwitterAction extends AbstractAction {

		private static final long serialVersionUID = -2035234758115291468L;

		public TwitterAction() {
			putValue(Action.NAME, I18n.tr("Twitter"));
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
			putValue(Action.NAME, I18n.tr("Copy to Clipboard"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			systemClipboard.setContents(
					new StringSelection(_textArea.getText()),
					ShareTorrentDialog.this);
		}
	}

	private class CloseAction extends AbstractAction {

		private static final long serialVersionUID = 4608358456107049224L;

		public CloseAction() {
			putValue(Action.NAME, I18n.tr("Close"));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ShareTorrentDialog.this.dispose();
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// meh
	}
}