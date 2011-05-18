package com.frostwire.gnutella.gui.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.limegroup.gnutella.gui.search.SearchResult;

/** 
 * A spam filter that removes certain "bad" keywords. 
 * If <i>any</i> words in a query are in the banned set, the
 * query is disallowed.
 */
public class KeywordFilter implements SearchFilter {

    /** INVARIANT: strings in ban contain only lowercase */
    private List<String> ban = new ArrayList<String>();

    /** 
     * @modifies this
     * @effects bans the given phrase.  Capitalization does not matter.
     */
    public void disallow(String phrase) {
        String canonical = phrase.toLowerCase(Locale.US);
        if (!ban.contains(canonical)) {
            ban.add(canonical);
        }
    }

    /** 
     * @modifies this
     * @effects bans several well-known "adult" words.
     */
    public void disallowAdult() {
        disallow("adult");
        disallow("anal");
        disallow("anul");
        disallow("ass");
        disallow("bisex");
        disallow("boob");
        disallow("bukake");
        disallow("bukkake");
        disallow("blow");
        disallow("blowjob");
        disallow("bondage");
        disallow("centerfold");
        disallow("cock");
        disallow("cum");
        disallow("cunt");
        disallow("crack");
        disallow("cracked");
        disallow("dick");
        disallow("facial");
        disallow("fetish");
        disallow("fisting");
        disallow("fuck");
        disallow("gangbang");
        disallow("gay");
        disallow("hentai");
        disallow("horny");
        disallow("incest");
        disallow("jenna");
        disallow("masturbat");
        disallow("menage");
        disallow("milf");
        disallow("keygen");
        disallow("nipple");
        disallow("orgy");
        disallow("penis");
        disallow("playboy");
        disallow("porn");
        disallow("pedo");
        disallow("pussy");
        disallow("penetration");
        disallow("rape");
        disallow("sex");
        disallow("shaved");
        disallow("slut");
        disallow("slutty");
        disallow("squirt");
        disallow("stripper");
        disallow("suck");
        disallow("tittie");
        disallow("titty");
        disallow("trois");
        disallow("twat");
        disallow("vagina");
        disallow("whore");
        disallow("xxx");
        disallow("shaking orgasm");
        disallow("orgasm");
        disallow("teenfuns");
    }

    public boolean allow(SearchResult m) {
        return !matches(m.getFilenameNoExtension());
    }

    /** 
     * Returns true if phrase matches any of the entries in ban.
     */
    protected boolean matches(String phrase) {
        String canonical = phrase.toLowerCase(Locale.US);
        for (int i = 0; i < ban.size(); i++) {
            String badWord = ban.get(i);
            if (canonical.indexOf(badWord) != -1)
                return true;
        }
        return false;
    }
}
