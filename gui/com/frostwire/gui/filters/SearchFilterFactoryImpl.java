package com.frostwire.gui.filters;

import java.util.Vector;

import com.limegroup.gnutella.settings.FilterSettings;

public class SearchFilterFactoryImpl implements SearchFilterFactory {

    public SearchFilterFactoryImpl() {
    }

    public SearchFilter createFilter() {

        Vector<SearchFilter> buf = new Vector<SearchFilter>();

        String[] badWords = FilterSettings.BANNED_WORDS.getValue();

        boolean filterAdult = FilterSettings.FILTER_ADULT.getValue();

        if (badWords.length != 0 || filterAdult) {
            KeywordFilter kf = new KeywordFilter();
            for (int i = 0; i < badWords.length; i++)
                kf.disallow(badWords[i]);
            if (filterAdult)
                kf.disallowAdult();

            buf.add(kf);
        }

        return compose(buf);
    }

    /**
     * Returns a composite filter of the given filters.
     * @param filters a Vector of SpamFilter.
     */
    private static SearchFilter compose(Vector<? extends SearchFilter> filters) {
        //As a minor optimization, we avoid a few method calls in
        //special cases.
        if (filters.size() == 0) {
            return new AllowFilter();
        } else if (filters.size() == 1) {
            return filters.get(0);
        } else {
            SearchFilter[] delegates = new SearchFilter[filters.size()];
            filters.copyInto(delegates);
            return new CompositeFilter(delegates);
        }
    }
}
