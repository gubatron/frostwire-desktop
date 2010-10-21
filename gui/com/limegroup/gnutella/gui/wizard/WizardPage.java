package com.limegroup.gnutella.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.URLLabel;
import com.limegroup.gnutella.gui.wizard.Status.Severity;

/**
 * This abstract class creates a <tt>JPanel</tt> that uses <tt>BoxLayout</tt>
 * for setup windows. It defines many of the basic accessor and mutator methods
 * required by subclasses.
 */
// 2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public abstract class WizardPage extends JPanel {

	/**
	 * Variable for the name of this window for use with <tt>CardLayout</tt>.
	 */
	private String key;

	/**
	 * Variable for the key of the title to display.
	 */
	private String titleKey;

	/**
	 * Variable for the key of the label to display.
	 */
	private String descriptionKey;

	/** Variable for the URL where more info exists. Null if none. */
	private String url;

	/** Variable for the URL where more info exists. Null if none. */
	private String urlLabelKey;

	/**
	 * The dialog that displays the page.
	 */
	protected Wizard wizard;

	/** The label displaying the status icon and message. */
	protected JLabel statusLabel; 
	
	/**
	 * Creates a new wizard page with the specified label.
	 * 
	 * @param key a unique identifier for this page
	 * @param titleKey
	 *            the title of the window for use with <tt>CardLayout</tt> and
	 *            for use in obtaining the locale-specific caption for this
	 *            window
	 * @param descriptionKey
	 *            the key for locale-specific label to be displayed in the
	 *            window
	 */
	public WizardPage(String key, String titleKey, String descriptionKey) {
		this.key = key;
		this.titleKey = titleKey;
		this.descriptionKey = descriptionKey;
	}
	
	public WizardPage(String titleKey, String descriptionKey) {
		this(titleKey, titleKey, descriptionKey);
	}

	/**
	 * Creates the wizard page controls. 
	 *
	 * @see #createPageContent(JPanel)
	 */
	protected void createPage() {
		removeAll();
		setLayout(new BorderLayout());

		// JPanel jpTop = new DitherPanel(new Ditherer(15,
		// ThemeFileHandler.FILTER_TITLE_TOP_COLOR.getValue(),
		// ThemeFileHandler.FILTER_TITLE_COLOR.getValue()));
		// JPanel jpTop = new DitherPanel(new Ditherer(30, new Color(254, 254,
		// 2), new Color(73, 136, 19).brighter()));
		// JPanel jpTop = new DitherPanel(new Ditherer(new Color(73, 136,
		// 19).brighter(), Color.WHITE, Ditherer.X_AXIS, new
		// Ditherer.PolygonShader(1.1f)));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBackground(Color.white);
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		add(topPanel, BorderLayout.NORTH);

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setOpaque(false);
		topPanel.add(titlePanel, BorderLayout.CENTER);

		JLabel titleLabel = new JLabel(I18n.tr(titleKey));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		titleLabel.setForeground(Color.black);
		titleLabel.setOpaque(false);
		titlePanel.add(titleLabel, BorderLayout.NORTH);

		MultiLineLabel descriptionLabel = new MultiLineLabel(I18n
				.tr(descriptionKey));
		descriptionLabel.setOpaque(false);
		descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		descriptionLabel.setForeground(Color.black);
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN));
		titlePanel.add(descriptionLabel, BorderLayout.CENTER);

		if (url != null) {
			String label = (urlLabelKey != null) ? I18n.tr(urlLabelKey) : url;
			JLabel urlLabel = new URLLabel(url, label);
			urlLabel.setOpaque(false);
			urlLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			urlLabel.setForeground(Color.black);
			urlLabel.setOpaque(false);
			titlePanel.add(urlLabel, BorderLayout.SOUTH);
		}

//		JLabel iconLabel = new JLabel();
//		iconLabel.setOpaque(false);
//		iconLabel.setIcon(getIcon());
//		iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//		topPanel.add(iconLabel, BorderLayout.EAST);

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
		add(mainPanel, BorderLayout.CENTER);

		statusLabel = new JLabel(" ");
		statusLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		add(statusLabel, BorderLayout.SOUTH);
		
		createPageContent(mainPanel);
		revalidate();
	}

	public boolean canFlipToNextPage() {
		return isPageComplete() && getNext() != null;
	}
	
	/**
	 * Creates the main controls of the wizard page. 
	 * 
	 * @param panel the panel the controls need to be added to
	 */
	protected abstract void createPageContent(JPanel panel);

//	/**
//	 * Accessor for the name of the panel
//	 * 
//	 * @return the unique identifying name for this panel
//	 */
//	public String getName() {
//		// GTK L&F calls this method before the constructor has finished,
//		// so we can't do a lookup with a null key.
//		if (key == null)
//			return null;
//		else
//			return GUIMediator.getStringResource(key);
//	}

	/**
	 * Accessor for the unique identifying key of the window in the
	 * <tt>CardLayout</tt>.
	 * 
	 * @return the unique identifying key for the window.
	 */
	public String getKey() {
		return key;
	}

//	/**
//	 * Mutator for the labelKey.
//	 */
//	protected void setLabelKey(String newKey) {
//		labelKey = newKey;
//	}

	/**
	 * Accessor for the next page in the sequence.
	 * 
	 * @return the next window in the sequence
	 */
	public WizardPage getNext() {
		return (wizard != null) ?  wizard.getNextPage(this) : null;
	}

	/**
	 * Accessor for the previous page in the sequence.
	 * 
	 * @return the previous window in the sequence
	 */
	public WizardPage getPrevious() {
		return (wizard != null) ?  wizard.getPreviousPage(this) : null;
	}

	/**
	 * Returns the wizard instance holding this page.
	 * 
	 * @return null, if the page has not been added to a wizard
	 */
	public Wizard getWizard() {
		return wizard;
	}
	
	/**
	 * Returns true, if input is valid and the user may proceed to the following
	 * wizard page or close the wizard if this is the last page.
	 */
	public abstract boolean isPageComplete();

	/**
	 * Invoked when the page becomes the active page. Validates the input and
	 * updates the buttons of the wizard.
	 */
	public void pageShown() {
		validateInput();
		wizard.updateButtons();
	}

	/**
	 * Displays message with severity {@link Severity#ERROR} in the status area
	 * of the wizard page.
	 * 
	 * @param message the message to display; if null, no message is displayed
	 * @see #setStatusMessage(Severity, String)
	 */
	public void setSatusMessage(String message) {
		setStatusMessage(Severity.ERROR, message);
	}

	/**
	 * Displays message in the status area of the wizard page. 
	 * 
	 * @param severity
	 * @param message
	 */
	public void setStatusMessage(Severity severity, String message) {
		if (message == null) {
			statusLabel.setText(" ");
			statusLabel.setIcon(null);
		} else {
			statusLabel.setText(message);
			if (severity == Severity.ERROR) {
				statusLabel.setIcon(GUIMediator.getThemeImage("stop_small"));
			} else if (severity == Severity.INFO) {
				statusLabel.setIcon(GUIMediator.getThemeImage("annotate_small"));
			} else {
				statusLabel.setIcon(null);
			}
		}
	}
	
	/**
	 * Subclasses should reimplement this method to validate input.
	 *
	 * @see #isPageComplete()
	 */
	public void validateInput() {
	}
	
	/**
	 * Updates the message 
	 * 
	 * @param status if null or status.length == 0, no status 
	 */
	public void updateStatus(Status... status) {
		if (status == null || status.length == 0) {
			setSatusMessage(null);
		} else {
			setStatusMessage(status[0].getSeverity(), status[0].getMessage());
		}
	}
	
	public void setWizard(Wizard wizard) {
		this.wizard = wizard;
	}
	
	/**
	 * Sets a url that is displayed in the title area of the wizard page.
	 * 
	 * @param url
	 *            the url
	 * @param urlLabelKey
	 *            the resource key of the label used to display url; if null,
	 *            url will be used as label
	 */
	public void setURL(String url, String urlLabelKey) {
		this.url = url;
		this.urlLabelKey = urlLabelKey;
	}
	
	/**
	 * Label that wraps text. Used to display the description.
	 */
	private static class MultiLineLabel extends JTextArea {

		/**
		 * Creates a label that can have multiple lines and that has the default
		 * width.
		 * 
		 * @param s
		 *            the <tt>String</tt> to display in the label
		 */
		public MultiLineLabel(String s) {
			setEditable(false);
			setLineWrap(true);
			setWrapStyleWord(true);
			setHighlighter(null);
			LookAndFeel.installBorder(this, "Label.border");
			LookAndFeel.installColorsAndFont(this, "Label.background",
					"Label.foreground", "Label.font");
			setSelectedTextColor(UIManager.getColor("Label.foreground"));
			setText(s);
		}

		public MultiLineLabel() {
			this(" ");
		}

	}
	
}
