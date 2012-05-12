package com.frostwire.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.limewire.util.StringUtils;

import com.frostwire.HttpFetcher;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class GoogleSearchField extends SearchField {

    private static final long serialVersionUID = -7677894485818144062L;

    private static final String SUGGESTIONS_URL = buildSuggestionsUrl();
    private static final int HTTP_QUERY_TIMEOUT = 1000;

    private SuggestionsThread suggestionsThread;

    public GoogleSearchField() {
        this.dict = createDefaultDictionary();

        setPrompt(I18n.tr("Hints by Google"));
        setSearchMode(SearchMode.REGULAR);
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
        private final GoogleSearchField input;

        private boolean cancelled;

        public SuggestionsThread(String constraint, GoogleSearchField input) {
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
                                if (!StringUtils.isNullOrEmpty(input.getText(), true)) {
                                    input.showPopup(it);
                                }
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
}
