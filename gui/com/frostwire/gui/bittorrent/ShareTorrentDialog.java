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
import java.net.URL;

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

import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;

public class ShareTorrentDialog extends JDialog implements ClipboardOwner {

	private static final long serialVersionUID = -7466273012830791935L;
	private TOTorrent _torrent;
	private Container _container;
	private JLabel _introLabel;
	private JTextArea _textArea;
	private JLabel _tipsLabel;
	private Action[] _actions;

	public ShareTorrentDialog(TOTorrent torrent) {
		_torrent = torrent;

		setupUI();
	}

	private void setupUI() {
		setupWindow();

		String torrent_name = _torrent.getUTF8Name();
		String info_hash = null;
		try {
			info_hash = TorrentUtil.hashToString(_torrent.getHash());
		} catch (TOTorrentException e) {
		}

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
				torrent_name) : String.format(String.format(
				"Use the following text to share the \"%s\" file", torrent_name)));
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

		_textArea = new JTextArea("Download \"" + torrent_name
				+ "\" at http://maglnk.com/" + info_hash, 2, 80);
		Font f = new Font("Dialog", Font.BOLD, 13);
		_textArea.setFont(f);
		_textArea.setMargin(new Insets(10, 10, 10, 10));
		_textArea.selectAll();
		_textArea.setBorder(BorderFactory.createEtchedBorder());
		_container.add(_textArea, c);

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
		loadIconForAction(_actions[0],"http://static.frostwire.com/images/20x20twitter.png");
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