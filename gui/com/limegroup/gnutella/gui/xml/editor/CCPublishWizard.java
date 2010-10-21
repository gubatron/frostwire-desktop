package com.limegroup.gnutella.gui.xml.editor;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.limewire.collection.NameValue;
import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.TitledPaddedPanel;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.gui.wizard.Status;
import com.limegroup.gnutella.gui.wizard.Wizard;
import com.limegroup.gnutella.gui.wizard.WizardPage;
import com.limegroup.gnutella.gui.wizard.WizardPageModificationHandler;
import com.limegroup.gnutella.gui.wizard.Status.Severity;
import com.limegroup.gnutella.licenses.CCConstants;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.licenses.PublishedCCLicense;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLNames;
import com.limegroup.gnutella.xml.LimeXMLSchema;

/**
 * This class provides a wizard for publishing a Creative Commons license.
 */
public class CCPublishWizard extends Wizard {

	private LimeXMLDocument doc;

	private FileDesc fd;

	private DetailsPage detailsPage;

	private UsagePage usagePage;

	private VerificationPage verificationPage;

	private WarningPage warningPage;

	private LimeXMLSchema schema;

	public CCPublishWizard(FileDesc fd, LimeXMLDocument doc, LimeXMLSchema schema) {
		this.fd = fd;
		this.doc = doc;
		this.schema = schema;
	}

	public void showDialog(Frame parent) {
		warningPage = new WarningPage();
		detailsPage = new DetailsPage();
		usagePage = new UsagePage();
		verificationPage = new VerificationPage();

		addPage(warningPage);
		addPage(usagePage);
		addPage(detailsPage);
		addPage(verificationPage);

		initInfo();
		
		JDialog dialog = createDialog(parent);
		dialog.setTitle(I18n.tr("Publish License"));
		dialog.setLocationRelativeTo(MessageService.getParentComponent());
		dialog.setVisible(true);
	}

	@Override
	public void performFinish() {
		if (warningPage.MODIFY_LICENSE.isSelected()) {
			// save settings
			MetaDataSaver saver = new MetaDataSaver(new FileDesc[] { fd }, schema,
                    GuiCoreMediator.getLimeXMLDocumentFactory().createLimeXMLDocument(
                            getInputValues(), schema.getSchemaURI()).getXMLString());
            saver.saveMetaData(getFileEventListener());
            dialog.dispose();
		} else {
		    DialogOption answer = GUIMediator.showYesNoMessage(I18n.tr("Are you sure you want to permanently remove the license from your local copy of this file?"), DialogOption.YES);
			if(answer == DialogOption.YES) {
				List<NameValue<String>> valList = CCPublishWizard.getPreviousValList(doc, false);
				valList.add(new NameValue<String>(LimeXMLNames.AUDIO_LICENSE, ""));
				valList.add(new NameValue<String>(LimeXMLNames.AUDIO_LICENSETYPE, ""));
				
				MetaDataSaver saver = new MetaDataSaver(new FileDesc[] { fd }, schema, 
						GuiCoreMediator.getLimeXMLDocumentFactory().createLimeXMLDocument(
                                valList, schema.getSchemaURI()).getXMLString());
				saver.saveMetaData();
				dialog.dispose();
			}
		}
	}

	/**
	 * Initializes the fieds with the file's Meta Data only if a license does
	 * not exist.If a license exists, it populates the verification URL field
	 * and the license distribution details.
	 */
	private void initInfo() {
		License license = fd.getLicense();
		if (license != null) {
			warningPage.setLicenseAvailable(true);
			if (license.getLicenseURI() != null) {
				verificationPage.VERIFICATION_URL_FIELD.setText(license.getLicenseURI()
						.toString());
				verificationPage.SELF_VERIFICATION.setSelected(true);
				verificationPage.updateVerification();
			}
			String licenseDeed = license.getLicenseDeed(fd.getSHA1Urn()).toString();
			if (licenseDeed != null) {
				if (licenseDeed.equals(CCConstants.ATTRIBUTION_NON_COMMERCIAL_NO_DERIVS_URI)) {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(false);
					usagePage.ALLOW_MODIFICATIONS_NO.setSelected(true);
				} else if (licenseDeed.equals(CCConstants.ATTRIBUTION_NO_DERIVS_URI)) {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(true);
					usagePage.ALLOW_MODIFICATIONS_NO.setSelected(true);
				} else if (licenseDeed.equals(CCConstants.ATTRIBUTION_NON_COMMERCIAL_URI)) {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(false);
					usagePage.ALLOW_MODIFICATIONS_YES.setSelected(true);
				} else if (licenseDeed.equals(CCConstants.ATTRIBUTION_SHARE_NON_COMMERCIAL_URI)) {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(false);
					usagePage.ALLOW_MODIFICATIONS_SHAREALIKE.setSelected(true);
				} else if (licenseDeed.equals(CCConstants.ATTRIBUTION_SHARE_URI)) {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(true);
					usagePage.ALLOW_MODIFICATIONS_SHAREALIKE.setSelected(true);
				} else {
					usagePage.ALLOW_COMMERCIAL_YES.setSelected(true);
					usagePage.ALLOW_MODIFICATIONS_YES.setSelected(true);
				}
			}
		} else {
			warningPage.setLicenseAvailable(false);
		}
		
		if (doc != null) {
			// license does not exist and file has XML doc
			detailsPage.COPYRIGHT_HOLDER.setText(doc.getValue(LimeXMLNames.AUDIO_ARTIST));
			detailsPage.COPYRIGHT_YEAR.setText(doc.getValue(LimeXMLNames.AUDIO_YEAR));
			detailsPage.WORK_TITLE.setText(doc.getValue(LimeXMLNames.AUDIO_TITLE));
		}
	}

	private MetaDataEventListener getFileEventListener() {
		return new CCRDFOuptut(fd, detailsPage.COPYRIGHT_HOLDER.getText(), 
					detailsPage.WORK_TITLE.getText(), 
					detailsPage.COPYRIGHT_YEAR.getText(),
					detailsPage.DESCRIPTION.getText(), 
					verificationPage.VERIFICATION_URL_FIELD.getText(),
					getLicenseType());
	}

	private int getLicenseType() {
		int type = CCConstants.ATTRIBUTION;
		if (!usagePage.ALLOW_COMMERCIAL_YES.isSelected()) {
			type |= CCConstants.ATTRIBUTION_NON_COMMERCIAL;
		}
		if (usagePage.ALLOW_MODIFICATIONS_SHAREALIKE.isSelected()) {
			type |= CCConstants.ATTRIBUTION_SHARE;
		} else if (usagePage.ALLOW_MODIFICATIONS_NO.isSelected()) {
			type |= CCConstants.ATTRIBUTION_NO_DERIVS;
		}
		return type;
	}

	/**
	 * Returns an ArrayList with the <name,value> and MetaData of the license.
	 * 
	 * @return an ArrayList with the <name,value> tuples for the license and
	 *         licensetype.
	 */
	private List<NameValue<String>> getInputValues() {
		List<NameValue<String>> valList = new ArrayList<NameValue<String>>();
		String holder = detailsPage.COPYRIGHT_HOLDER.getText();
		String year = detailsPage.COPYRIGHT_YEAR.getText();
		String title = detailsPage.WORK_TITLE.getText();
		String description = detailsPage.DESCRIPTION.getText();
		int type = getLicenseType();
		String url = verificationPage.VERIFICATION_URL_FIELD.getText();
		boolean saveDetails = detailsPage.SAVE_DETAILS_CHECKBOX.isSelected();
		valList.addAll(getPreviousValList(doc, saveDetails));
		String embeddedLicense = PublishedCCLicense.getEmbeddableString(
				holder, title, year, url, description, type);
		if (embeddedLicense != null) {
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_LICENSE, embeddedLicense));
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_LICENSETYPE, CCConstants.CC_URI_PREFIX));
		}
		
		if (saveDetails) {
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_TITLE, title));
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_YEAR, year));			
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_ARTIST, holder));
			valList.add(new NameValue<String>(LimeXMLNames.AUDIO_COMMENTS, description));
		}
		
		return valList;
	}

	public static List<NameValue<String>> getPreviousValList(LimeXMLDocument doc, boolean excludeDetails) {
		List<NameValue<String>> valList = new ArrayList<NameValue<String>>();
		if (doc != null) {
			for (Map.Entry<String, String> entry : doc.getNameValueSet()) {
				String key = entry.getKey();
				if (!isLicenseKey(key) && !(excludeDetails && isDetailsKey(key))) {
					valList.add(new NameValue<String>(entry.getKey(), entry
							.getValue()));
				}
				
			}
		}
		return valList;
	}
	
	private static boolean isLicenseKey(String key) {
		return key.equals(LimeXMLNames.AUDIO_LICENSE)
			|| key.equals(LimeXMLNames.AUDIO_LICENSETYPE);
	}
	
	private static boolean isDetailsKey(String key) {
		return key.equals(LimeXMLNames.AUDIO_TITLE)
			|| key.equals(LimeXMLNames.AUDIO_YEAR)
			|| key.equals(LimeXMLNames.AUDIO_ARTIST)
			|| key.equals(LimeXMLNames.AUDIO_COMMENTS);
	}

	private class WarningPage extends WizardPage {

		private final String WARNING_MESSAGE_CREATE = I18n
				.tr("I understand that to publish a file, I must either own its copyrights or be authorized to publish them under a Creative Commons license.");

		private final String WARNING_MESSAGE_MODIFY = I18n
				.tr("This file already has a license. If you want to modify it, click the checkbox to attest that you either own its copyrights or are authorized to publish them under a Creative Commons license.");

		private final JCheckBox WARNING_CHECKBOX = new JCheckBox();

		private final TitledPaddedPanel MODE_SELECTION_PANEL = new TitledPaddedPanel();
		
		final JRadioButton MODIFY_LICENSE = new JRadioButton(I18n
				.tr("Modify the license of this file"));

		final JRadioButton REMOVE_LICENSE = new JRadioButton(I18n
				.tr("Permanently remove the license from this file"));

		public WarningPage() {
			super("warningPage", I18nMarker.marktr("Publish License"), I18nMarker
                    .marktr("This tool helps you publish audio under a Creative Commons license."));

			setURL(SharingSettings.CREATIVE_COMMONS_INTRO_URL.getValue(),
					I18nMarker.marktr("How does it work?"));
		}

		@Override
		protected void createPageContent(JPanel parent) {
			parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

			WARNING_CHECKBOX.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateActions();
				}
			});
			// XXX make sure the dialog does not get too wide
			WARNING_CHECKBOX.setPreferredSize(new Dimension(540, -1));
			parent.add(WARNING_CHECKBOX);

			// make sure the panel expands horizontally
			MODE_SELECTION_PANEL.add(Box.createHorizontalGlue());
			parent.add(MODE_SELECTION_PANEL);

			MODIFY_LICENSE.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateActions();
				}
			});
			REMOVE_LICENSE.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateActions();
				}
			});
			ButtonGroup bg = new ButtonGroup();
			bg.add(MODIFY_LICENSE);
			bg.add(REMOVE_LICENSE);

			BoxPanel buttonPanel = new BoxPanel();
			buttonPanel.add(MODIFY_LICENSE);
			buttonPanel.add(REMOVE_LICENSE);
			MODE_SELECTION_PANEL.add(buttonPanel);
			
			// set defaults
			MODIFY_LICENSE.setSelected(true);
			updateActions();
			setLicenseAvailable(true);
		}

		public void setLicenseAvailable(boolean available) {
			if (available) {
				WARNING_CHECKBOX.setText("<html>" + WARNING_MESSAGE_MODIFY
						+ "</html>");
				MODE_SELECTION_PANEL.setVisible(true);
			} else {
				WARNING_CHECKBOX.setText("<html>" + WARNING_MESSAGE_CREATE
						+ "</html>");
				MODIFY_LICENSE.setSelected(true);
				MODE_SELECTION_PANEL.setVisible(false);
			}
		}
		
		@Override
		public boolean canFlipToNextPage() {
			return MODIFY_LICENSE.isSelected();
		}
		
		@Override
		public boolean isPageComplete() {
			return WARNING_CHECKBOX.isSelected();
		}

		private void updateActions() {
			MODIFY_LICENSE.setEnabled(WARNING_CHECKBOX.isSelected());
			REMOVE_LICENSE.setEnabled(WARNING_CHECKBOX.isSelected());
			getWizard().updateButtons();
		}

	}
	
	private class UsagePage extends WizardPage {

		final JRadioButton ALLOW_COMMERCIAL_YES = new JRadioButton(I18n
				.tr("Yes"));

		final JRadioButton ALLOW_COMMERCIAL_NO = new JRadioButton(I18n
				.tr("No"));

		final JRadioButton ALLOW_MODIFICATIONS_SHAREALIKE = new JRadioButton(
				I18n
						.tr("ShareAlike"));

		final JRadioButton ALLOW_MODIFICATIONS_YES = new JRadioButton(
				I18n.tr("Yes"));

		final JRadioButton ALLOW_MODIFICATIONS_NO = new JRadioButton(
				I18n.tr("No"));

		public UsagePage() {
			super("usagePage", I18nMarker.marktr("Publish License"), I18nMarker
                    .marktr("This tool helps you publish audio under a Creative Commons license."));
		}

		@Override
		protected void createPageContent(JPanel panel) {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			TitledPaddedPanel commercialUsePanel = new TitledPaddedPanel(I18n.tr("Allow commercial use of your work?"));
			// make sure the panel expands horizontally
			commercialUsePanel.add(Box.createHorizontalGlue());
//			commercialUsePanel.add(new JLabel("<html>" + GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_ALLOWCOM_LABEL")									+ "</html>"));
//			commercialUsePanel.add(Box.createRigidArea(BoxPanel.VERTICAL_COMPONENT_GAP));

			ButtonGroup bg = new ButtonGroup();
			bg.add(ALLOW_COMMERCIAL_YES);
			bg.add(ALLOW_COMMERCIAL_NO);

			BoxPanel buttonPanel = new BoxPanel();
			commercialUsePanel.add(buttonPanel);
			buttonPanel.add(ALLOW_COMMERCIAL_YES);
			buttonPanel.add(ALLOW_COMMERCIAL_NO);
			panel.add(commercialUsePanel);

			panel.add(Box.createRigidArea(BoxPanel.LINE_GAP));

			TitledPaddedPanel modificationsPanel = new TitledPaddedPanel(I18n.tr("Allow modification of your work?"));
			// make sure the panel expands horizontally
			modificationsPanel.add(Box.createHorizontalGlue());
//			modificationsPanel.add(new JLabel("<html>" + GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_ALLOWMOD_LABEL")	+ "</html>"));
//			modificationsPanel.add(Box.createRigidArea(BoxPanel.VERTICAL_COMPONENT_GAP));
			
			bg = new ButtonGroup();
			bg.add(ALLOW_MODIFICATIONS_SHAREALIKE);
			bg.add(ALLOW_MODIFICATIONS_YES);
			bg.add(ALLOW_MODIFICATIONS_NO);

			buttonPanel = new BoxPanel();
			modificationsPanel.add(buttonPanel);
			buttonPanel.add(ALLOW_MODIFICATIONS_SHAREALIKE);
			buttonPanel.add(ALLOW_MODIFICATIONS_YES);
			buttonPanel.add(ALLOW_MODIFICATIONS_NO);
			panel.add(modificationsPanel);
			
			// set defaults
			ALLOW_COMMERCIAL_NO.setSelected(true);
			ALLOW_MODIFICATIONS_NO.setSelected(true);
		}

		@Override
		public boolean isPageComplete() {
			return true;
		}

	}

	private class DetailsPage extends WizardPage {

		private final JTextField COPYRIGHT_HOLDER = new SizedTextField(24, SizePolicy.RESTRICT_HEIGHT);

		private final JTextField WORK_TITLE = new SizedTextField(24, SizePolicy.RESTRICT_HEIGHT);

		private final JTextField COPYRIGHT_YEAR = new SizedTextField(6, SizePolicy.RESTRICT_HEIGHT);

		private final JTextArea DESCRIPTION = new JTextArea(4, 24);

		final JCheckBox SAVE_DETAILS_CHECKBOX = new JCheckBox(I18n.tr("Save details to file"));

		private boolean complete = false;
		
		public DetailsPage() {
			super("detailsPage", I18nMarker.marktr("Publish License"), I18nMarker
                    .marktr("This tool helps you publish audio under a Creative Commons license."));
		}

		@Override
		protected void createPageContent(JPanel panel) {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			BoxPanel holderPanel = new BoxPanel(BoxPanel.X_AXIS);
			panel.add(holderPanel);
			LabeledComponent c = new LabeledComponent(
					I18nMarker.marktr("Copyright Holder:"), COPYRIGHT_HOLDER,
					LabeledComponent.NO_GLUE, LabeledComponent.TOP_LEFT);
			COPYRIGHT_HOLDER.getDocument().addDocumentListener(new WizardPageModificationHandler(this)) ;
			holderPanel.add(c.getComponent());
			holderPanel.add(Box
					.createRigidArea(BoxPanel.HORIZONTAL_COMPONENT_GAP));
			c = new LabeledComponent(I18nMarker.marktr("Copyright Year:"),
					COPYRIGHT_YEAR, LabeledComponent.NO_GLUE,
					LabeledComponent.TOP_LEFT);
			COPYRIGHT_YEAR.getDocument().addDocumentListener(new WizardPageModificationHandler(this)) ;
			holderPanel.add(c.getComponent());
			panel.add(Box.createRigidArea(BoxPanel.LINE_GAP));

			c = new LabeledComponent(I18nMarker.marktr("Title of Work:"),
					WORK_TITLE, LabeledComponent.NO_GLUE,
					LabeledComponent.TOP_LEFT);
			WORK_TITLE.getDocument().addDocumentListener(new WizardPageModificationHandler(this)) ;
			panel.add(c.getComponent());
			panel.add(Box.createRigidArea(BoxPanel.LINE_GAP));

			c = new LabeledComponent(I18nMarker.marktr("Comment:"),
					new JScrollPane(DESCRIPTION), LabeledComponent.NO_GLUE,
					LabeledComponent.TOP_LEFT);
			panel.add(c.getComponent());
			
			panel.add(c.getComponent());
			panel.add(Box.createRigidArea(BoxPanel.LINE_GAP));
//			panel.add(SAVE_DETAILS_CHECKBOX);
		}

		@Override
		public boolean isPageComplete() {
			return complete;
		}

		@Override
		public void validateInput() {
			complete = true;
			if ("".equals(COPYRIGHT_HOLDER.getText()) 
					|| "".equals(COPYRIGHT_YEAR.getText())
					|| "".equals(WORK_TITLE.getText())) {
				updateStatus(new Status(I18n.tr("Please enter the copyright holder, copright year and title."), Severity.INFO));
				complete = false;
			} else {
				try {
					Integer.parseInt(COPYRIGHT_YEAR.getText());
				} catch(NumberFormatException e) {
					updateStatus(new Status(I18n.tr("Please enter a valid year for the file you want to publish."), Severity.ERROR));
					complete = false;
				}
			}
			
			if (complete) {
				updateStatus();
			}
			
			updateButtons();
		}
		
	}

	private class VerificationPage extends WizardPage {

		/**
		 * The Verification URL field
		 */
		final JTextField VERIFICATION_URL_FIELD = new SizedTextField(20, SizePolicy.RESTRICT_HEIGHT);

		final String VERIFICATION_ARCHIVE = I18n
				.tr("I want to use the Internet Archive to host the file.");

		final String VERIFICATION_SELF = I18n
				.tr("I want to host the verification file myself at the following URL:");

		final JRadioButton SELF_VERIFICATION = new JRadioButton(
				VERIFICATION_SELF);

		private boolean complete;

		public VerificationPage() {
			super("verificationPage", I18nMarker.marktr("Publish License"),
					I18nMarker
                            .marktr("Where do you want to store the verification URL?"));

			setURL(SharingSettings.CREATIVE_COMMONS_VERIFICATION_URL.getValue(),
					I18nMarker.marktr("What is this?"));
		}

		@Override
		protected void createPageContent(JPanel panel) {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			ButtonGroup bg = new ButtonGroup();
			bg.add(SELF_VERIFICATION);

			SELF_VERIFICATION.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					updateVerification();
				}
			});
			BoxPanel p = new BoxPanel(BoxPanel.X_AXIS);
			//p.add(SELF_VERIFICATION);
			p.add(new JLabel(I18n
					.tr("The verification file is hosted at the following URL:")));
			p.add(Box.createHorizontalGlue());
			panel.add(p);
			panel.add(Box.createRigidArea(BoxPanel.VERTICAL_COMPONENT_GAP));
			
			VERIFICATION_URL_FIELD.setText("http://");
			p = new BoxPanel(BoxPanel.X_AXIS);
			p.addVerticalComponentGap();
			//p.add(Box.createHorizontalStrut(30));
			p.add(VERIFICATION_URL_FIELD);
			VERIFICATION_URL_FIELD.getDocument().addDocumentListener(new WizardPageModificationHandler(this));
			//p.add(Box.createHorizontalStrut(30));
			p.setMaximumSize(new Dimension(Integer.MAX_VALUE, VERIFICATION_URL_FIELD
					.getPreferredSize().height));
			panel.add(p);

			panel.add(Box.createVerticalGlue());

			// set defaults
			//			 disabled: GUI-87
//			ARCHIVE_VERIFICATION.setSelected(true);
			SELF_VERIFICATION.setSelected(true);
			updateVerification();
		}

		@Override
		public boolean isPageComplete() {
			return complete;
		}

		private void updateVerification() {
			VERIFICATION_URL_FIELD.setEnabled(true);			
			validateInput();
		}

		@Override
		public void validateInput() {
			complete = true;
			String url = VERIFICATION_URL_FIELD.getText();
            if (url.equals("") || !url.startsWith("http://") || url.length() < 8) {
                updateStatus(new Status(I18n.tr("Please enter a verification URL for the license."), Severity.INFO));
                complete = false;
            }
            try {
                new URL(url);
            } catch(MalformedURLException invalidURL) {
                updateStatus(new Status(I18n.tr("Please enter a verification URL for the license."), Severity.ERROR));
                complete = false;
            }

			if (complete) {
				updateStatus();
			}
			updateButtons();				
		}
	}

}
