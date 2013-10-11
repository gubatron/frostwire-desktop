/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.searchfield;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.limewire.util.LCS;
import org.limewire.util.OSUtils;
import org.limewire.util.StringUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.gui.theme.ThemeMediator;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class GoogleSearchField extends SearchField {

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
    
    @Override
    public void setText(String t) {
        
        try {
            if (t!=null) {
                t = t.replace("<html>", "").replace("</html>", "").replace("<b>", "").replace("</b>", "");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.setText(t);
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
        
        Font origFont = getFont();
        Font newFont = origFont;
        if (OSUtils.isWindows()) {
            newFont = ThemeMediator.DIALOG_FONT.deriveFont(origFont.getSize2D());
        }
        entryList.setFont(newFont);

        return entryPanel;
    }

    private static String buildSuggestionsUrl() {
        String lang = ApplicationSettings.LANGUAGE.getValue();
        if (StringUtils.isNullOrEmpty(lang)) {
            lang = "en";
        }

        return "http://suggestqueries.google.com/complete/search?output=firefox&hl=" + lang + "&q=%s";
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
                String json = StringUtils.getUTF8String(fetcher.fetch());

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
            String t = input.getText();
            List<String> suggestions = new ArrayList<String>(array.size());
            if (!StringUtils.isNullOrEmpty(t, true)) {
                for (Object obj : array) {
                    String s = LCS.lcsHtml(t, (String) obj);
                    suggestions.add(s);
                }
            }
            return suggestions;
        }
    }
}
