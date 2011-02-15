package com.limegroup.gnutella.gui.options.panes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.gui.layout.SpringUtilities;
import com.limegroup.gnutella.gui.options.panes.StoreSaveTemplateProcessor.IllegalTemplateException;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 *  Creates a template pane for choosing a default subfolder generator for saving songs purchased from the
 *  LWS. 
 */
public final class StoreDirectoryTemplatePaneItem extends AbstractPaneItem {

    /**
     * Title of the item in the pane, this name will be displayed in a line border
     */
    public final static String TITLE = I18n.tr("LimeWire Store - Subfolder Template");
    
    /**
     * General description of what this item is, and what its used for 
     */
    public final static String LABEL = I18n.tr("You can customize where store files are saved. By creating a template Lime Wire can automatically save purchased files into folders using the artists name and album title. \n\ne.g:\nPurchased song: Artist Name - Track 1\n Template: ${artist} \n How its saved: LimeWireStore/Artist Name/Artist Name - Track 1");
    
    /**
     * Label for the finished template
     */
    private final String TEMPLATE_LABEL = I18n.tr("Template") + ":";
    
    /**
     * Label for a drop down with options that can be added to the template
     */
    private final String VARIABLES_LABEL = I18n.tr("Variables") + ":";
    
    /**
     * Label for predefined templates
     */
    private final String TEMPLATES_LABEL = I18n.tr("Templates") + ":";
       
    
    private final String artist     = I18n.tr("Artist");
    private final String album      = I18n.tr("Album");
    private final String artistVar  = "${" + artist.toLowerCase() + "}";
    private final String albumVar   = "${" + album.toLowerCase() + "}";
    
    /**
     * String for storing the initial template.
     */
    private String template;
    
    /**
     * Handle to the <tt>JTextField</tt> that displays the save template.
     */
    private JTextField templateField;
        
    /**
     * Drop down box containing various variables that can be added to the template
     */
    private JComboBox templateVariables;
    
    /**
     * Drop down box containing preconfigured templates
     */
    private JComboBox _presetTemplates;
    
    private JPanel templatePanel;
    
    /**
     * The constructor constructs all of the elements of this
     * <tt>AbstractPaneItem</tt>.
     */
    public StoreDirectoryTemplatePaneItem() {
        super(TITLE, LABEL);
        
        add(getTemplatePanel());
        add(getVerticalSeparator());
    }

    private JPanel getTemplatePanel(){
        if( templatePanel == null ) {
            templatePanel = new JPanel( new SpringLayout());
            
            templateField = new SizedTextField(25, SizePolicy.RESTRICT_HEIGHT);
            
            templatePanel.add( new JLabel(TEMPLATE_LABEL));
            templatePanel.add( templateField);
            templatePanel.add( new JLabel(VARIABLES_LABEL));
            templatePanel.add( getVariableOptions());
            templatePanel.add( new JLabel(TEMPLATES_LABEL));
            templatePanel.add(getPresetTemplates());

            SpringUtilities.makeCompactGrid(templatePanel,
                    3,2,
                    6,6,
                    6,6);
        }
        return templatePanel;
    }
    
    /**
     * Creates a combobox with values that can be added to the template
     * @return
     */
    private JComboBox getVariableOptions() {
        if( templateVariables == null ) {
 
            // The combo box for inserting variables
             ListNode[] templateOptionStrings = new ListNode[]{
                     new ListNode("--" + I18n.tr("Insert a Variable") + "--", null),
                     new ListNode(artist, artistVar),
                     new ListNode(album, albumVar),
                     
            };

            templateVariables = new JComboBox(templateOptionStrings);
            templateVariables.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // 
                    // Ignore the first row and add a variable to the template field
                    //
                    if (templateVariables.getSelectedIndex() == 0) 
                        return;
                    templateField.setText(templateField.getText() + ((ListNode)templateVariables.getSelectedItem()).displayTemplateText);
                }
            });
            templateVariables.setMaximumSize(new Dimension(200,25));
        }
        return templateVariables;
    }
    
    /**
     * Creates a combobox with saved templates. When one of these templates is chosen, it
     * overwrites any template values that have already been defined
     */
    private JComboBox getPresetTemplates(){
        if( _presetTemplates == null ) {

            // The combo box for precanned templates  
            ListNode[] templateOptionStrings = new ListNode[] {
                    new ListNode("--" + I18n.tr("Insert a template") + "--", null),
                    new ListNode(album, albumVar),
                    new ListNode(artist, artistVar),
                    new ListNode(artist + "-" +album, artistVar + "-" + albumVar),
                    new ListNode(I18n.tr("iTunes like"), artistVar + File.separatorChar + albumVar)
            };
            
            _presetTemplates = new JComboBox(templateOptionStrings);
            _presetTemplates.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // 
                    // Ignore the first row and set the new text for the template field
                    //
                    if (_presetTemplates.getSelectedIndex() == 0) 
                        return;

                    templateField.setText(((ListNode)_presetTemplates.getSelectedItem()).displayTemplateText);
                }
            });
            _presetTemplates.setMaximumSize(new Dimension(200,25));
        }
        return _presetTemplates;
    }
    
    /**
     * Try saving the template. If the template is invalid, throw an error message to the
     * user till they change it to an acceptable template format
     */
    @Override
    public boolean applyOptions() throws IOException {
        // First make sure the template is valid
        Map<String,String> substitutions = new HashMap<String,String>();
        substitutions.put(artist.toLowerCase(), StoreSaveTemplateProcessor.ARTIST_LABEL );
        substitutions.put(album.toLowerCase(), StoreSaveTemplateProcessor.ALBUM_LABEL );
        
        // convert the textfield into english, all templates are saved in english
        final String templateText = convertTemplateLanguage(templateField.getText(), substitutions);

        try { 
            new StoreSaveTemplateProcessor().isValid(templateText);
        } catch (IllegalTemplateException e) {
            GUIMediator.showError(I18n.tr("Invalid template {0}", e.getMessage()));
            throw new IOException();
        }
        if (!templateText.equals(template)) {
            SharingSettings.setSaveLWSTemplate(templateText);
        }
        return false;
    }

    /**
     * Load the template with the currently saved format
     */
    @Override
    public void initOptions() {

        // create a map to perform conversion between english and the target language
        Map<String,String> substitutions = new HashMap<String,String>();
        substitutions.put(StoreSaveTemplateProcessor.ARTIST_LABEL, artist.toLowerCase());
        substitutions.put(StoreSaveTemplateProcessor.ALBUM_LABEL, album.toLowerCase());
        
        // convert the saved template to the local language that is selected
        templateField.setText( convertTemplateLanguage(SharingSettings.getSaveLWSTemplate(), substitutions));
        template = SharingSettings.getSaveLWSTemplate();
    }
    
    /**
     * Takes a template and converts between english and another language. The text is always displayed in
     * the local language chosen but templates are always saved in english to remove ambiguity when converting
     * between different languages over time. 
     * 
     * @param savedTemplate - template to convert
     * @param substitutions - contains english/target language or target language/english mappings to perform
     *          conversion between
     * @return - template in target language
     */
    private String convertTemplateLanguage(String savedTemplate, final Map<String,String> substitutions) {
        String  template = "";
        if( savedTemplate == null || savedTemplate.length() == 0) return template;
        
        template = savedTemplate;

        // this is a slow replacement method but this method will not run often and when it
        // does, the strings will likely be < 30 in length making the extra overhead neglible
        for( String s: substitutions.keySet()) {
            template = template.replaceAll(s, substitutions.get(s));
        }
        return template;
    }

    /**
     * Returns true if the template has changed since the last save
     */
    public boolean isDirty() {
        return !SharingSettings.getSaveLWSTemplate().equals(template);
    }

    /**
     *  Holder for items in a comboBox. The displayed value of the 
     *  combo box and the template value are different from each other
     */
    private class ListNode {
        
        /**
         * Value to display in the combo box
         */
        private final String displayText;
        
        /**
         * Value to display in the template
         */
        private final String displayTemplateText;
        
        public ListNode(String displayText, String displayTemplateText){
            this.displayText = displayText;
            this.displayTemplateText = displayTemplateText;
        }
        
        public String toString(){
            return displayText;
        }
    }
}
