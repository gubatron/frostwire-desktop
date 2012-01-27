package com.frostwire.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.limewire.util.StringUtils;

import com.frostwire.HttpFetcher;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class GoogleIconSearchField extends IconSearchField {

    private static final long serialVersionUID = -7677894485818144062L;

    private static final String SUGGESTIONS_URL = buildSuggestionsUrl();
    private static final int HTTP_QUERY_TIMEOUT = 1000;

    private SuggestionsThread suggestionsThread;

    public GoogleIconSearchField(int columns, Icon icon) {
        super(columns, icon);
        this.dict = createDefaultDictionary();

        TextPrompt hint = new TextPrompt(I18n.tr("Hints by Google"), this);
        hint.setForeground(Color.GRAY);
        hint.changeAlpha(0.5f);
    }

    public void autoCompleteInput() {
        String input = getText();
        if (input != null && input.length() > 0) {

            if (suggestionsThread != null) {
                suggestionsThread.cancel();
            }

            suggestionsThread = new SuggestionsThread(input, this);
            suggestionsThread.start();

        } else {
            hidePopup();
        }
    }

    protected JComponent getPopupComponent() {
        if (entryPanel != null)
            return entryPanel;

        entryPanel = new JPanel(new GridBagLayout());
        entryPanel.setBorder(UIManager.getBorder("List.border"));
        entryPanel.setBackground(UIManager.getColor("List.background"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;

        entryList = new AutoCompleteList();
        JScrollPane entryScrollPane = new JScrollPane(entryList);
        entryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        entryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        entryPanel.add(entryScrollPane, c);

        return entryPanel;
    }

    private static String buildSuggestionsUrl() {
        String lang = Locale.getDefault().getLanguage();
        if (StringUtils.isNullOrEmpty(lang)) {
            lang = "en";
        }

        return "http://suggestqueries.google.com/complete/search?output=firefox&hl=" + Locale.getDefault() + "&q=%s";
    }

    private static final class SuggestionsThread extends Thread {

        private final String constraint;
        private final GoogleIconSearchField input;

        private boolean cancelled;

        public SuggestionsThread(String constraint, GoogleIconSearchField input) {
            this.constraint = constraint;
            this.input = input;
            this.setName("SuggestionsThread: " + constraint);
            this.setDaemon(true);
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
        }

        public void run() {
            try {
                String url = String.format(SUGGESTIONS_URL, URLEncoder.encode(constraint, "UTF-8"));

                HttpFetcher fetcher = new HttpFetcher(new URI(url), HTTP_QUERY_TIMEOUT);
                String json = new String(fetcher.fetch());

                if (!isCancelled()) {
                    final List<String> suggestions = readSuggestions((JSONArray) ((JSONArray) JSONValue.parse(json)).get(1));

                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            Iterator<String> it = suggestions.iterator();
                            if (it.hasNext())
                                input.showPopup(it);
                            else
                                input.hidePopup();
                        }
                    });
                }
            } catch (Throwable e) {
                // ignore
            }
        }

        private List<String> readSuggestions(JSONArray array) {
            List<String> suggestions = new ArrayList<String>(array.size());
            for (Object obj : array) {
                suggestions.add((String) obj);
            }
            return suggestions;
        }
    }

    /**
     *  The TextPrompt class will display a prompt over top of a text component when
     *  the Document of the text field is empty. The Show property is used to
     *  determine the visibility of the prompt.
     *
     *  The Font and foreground Color of the prompt will default to those properties
     *  of the parent text component. You are free to change the properties after
     *  class construction.
     */
    public static class TextPrompt extends JLabel implements FocusListener, DocumentListener {
        
        private static final long serialVersionUID = -8976017311870093822L;

        public enum Show {
            ALWAYS, FOCUS_GAINED, FOCUS_LOST;
        }

        private JTextComponent component;
        private Document document;

        private Show show;
        private boolean showPromptOnce;
        private int focusLost;

        public TextPrompt(String text, JTextComponent component) {
            this(text, component, Show.ALWAYS);
        }

        public TextPrompt(String text, JTextComponent component, Show show) {
            this.component = component;
            setShow(show);
            document = component.getDocument();

            setText(text);
            setFont(component.getFont());
            setForeground(component.getForeground());
            setBorder(new EmptyBorder(component.getInsets()));
            setHorizontalAlignment(JLabel.LEADING);

            component.addFocusListener(this);
            document.addDocumentListener(this);

            component.setLayout(new BorderLayout());
            component.add(this);
            checkForPrompt();
        }

        /**
         *  Convenience method to change the alpha value of the current foreground
         *  Color to the specifice value.
         *
         *  @param alpha value in the range of 0 - 1.0.
         */
        public void changeAlpha(float alpha) {
            changeAlpha((int) (alpha * 255));
        }

        /**
         *  Convenience method to change the alpha value of the current foreground
         *  Color to the specifice value.
         *
         *  @param alpha value in the range of 0 - 255.
         */
        public void changeAlpha(int alpha) {
            alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;

            Color foreground = getForeground();
            int red = foreground.getRed();
            int green = foreground.getGreen();
            int blue = foreground.getBlue();

            Color withAlpha = new Color(red, green, blue, alpha);
            super.setForeground(withAlpha);
        }

        /**
         *  Convenience method to change the style of the current Font. The style
         *  values are found in the Font class. Common values might be:
         *  Font.BOLD, Font.ITALIC and Font.BOLD + Font.ITALIC.
         *
         *  @param style value representing the the new style of the Font.
         */
        public void changeStyle(int style) {
            setFont(getFont().deriveFont(style));
        }

        /**
         *  Get the Show property
         *
         *  @return the Show property.
         */
        public Show getShow() {
            return show;
        }

        /**
         *  Set the prompt Show property to control when the promt is shown.
         *  Valid values are:
         *
         *  Show.AWLAYS (default) - always show the prompt
         *  Show.Focus_GAINED - show the prompt when the component gains focus
         *      (and hide the prompt when focus is lost)
         *  Show.Focus_LOST - show the prompt when the component loses focus
         *      (and hide the prompt when focus is gained)
         *
         *  @param show a valid Show enum
         */
        public void setShow(Show show) {
            this.show = show;
        }

        /**
         *  Get the showPromptOnce property
         *
         *  @return the showPromptOnce property.
         */
        public boolean getShowPromptOnce() {
            return showPromptOnce;
        }

        /**
         *  Show the prompt once. Once the component has gained/lost focus
         *  once, the prompt will not be shown again.
         *
         *  @param showPromptOnce  when true the prompt will only be shown once,
         *                         otherwise it will be shown repeatedly.
         */
        public void setShowPromptOnce(boolean showPromptOnce) {
            this.showPromptOnce = showPromptOnce;
        }

        /**
         *  Check whether the prompt should be visible or not. The visibility
         *  will change on updates to the Document and on focus changes.
         */
        private void checkForPrompt() {
            //  Text has been entered, remove the prompt

            if (document.getLength() > 0) {
                setVisible(false);
                return;
            }

            //  Prompt has already been shown once, remove it

            if (showPromptOnce && focusLost > 0) {
                setVisible(false);
                return;
            }

            //  Check the Show property and component focus to determine if the
            //  prompt should be displayed.

            if (component.hasFocus()) {
                if (show == Show.ALWAYS || show == Show.FOCUS_GAINED)
                    setVisible(true);
                else
                    setVisible(false);
            } else {
                if (show == Show.ALWAYS || show == Show.FOCUS_LOST)
                    setVisible(true);
                else
                    setVisible(false);
            }
        }

        //  Implement FocusListener

        public void focusGained(FocusEvent e) {
            checkForPrompt();
        }

        public void focusLost(FocusEvent e) {
            focusLost++;
            checkForPrompt();
        }

        //  Implement DocumentListener

        public void insertUpdate(DocumentEvent e) {
            checkForPrompt();
        }

        public void removeUpdate(DocumentEvent e) {
            checkForPrompt();
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }
}
