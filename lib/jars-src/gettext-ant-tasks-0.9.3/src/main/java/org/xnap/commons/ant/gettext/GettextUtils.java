package org.xnap.commons.ant.gettext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class GettextUtils {

    public static String getJavaLocale(String locale) {
        if (locale == null) {
            throw new IllegalArgumentException();
        }
        
        List tokens = new ArrayList(3);
        StringTokenizer t = new StringTokenizer(locale, "_");
        while (t.hasMoreTokens()) {
            tokens.add(t.nextToken());
        }
        
        if (tokens.size() < 1 || tokens.size() > 3) {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }

        if (tokens.size() < 3) {
            // check for variant
            String lastToken = (String) tokens.get(tokens.size() - 1);
            int index = lastToken.indexOf("@");
            if (index != -1) {
                tokens.remove(tokens.size() - 1);
                tokens.add(lastToken.substring(0, index));
                if (tokens.size() == 1) {
                    // no country code was provided, but a variant
                    tokens.add("");
                }
                tokens.add(lastToken.substring(index + 1));
            }
        }

        StringBuffer sb = new StringBuffer();
        for (Iterator it = tokens.iterator(); it.hasNext();) {
            String token = (String) it.next();
            sb.append(token);
            if (it.hasNext()) {
                sb.append("_");
            }
        }
        
        return sb.toString();
    }

}
