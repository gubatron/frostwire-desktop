/**
 * JToothpaste - Copyright (C) 2007 Matthias
 * Schuhmann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.http;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.html.parser.ParserDelegator;


public class HTML {
    HTMLHandler htmlHandler;

    /**
     * Creates a new HTML object.
     *
     * @param uri DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public HTML(String uri) throws IOException {
        ParserDelegator pd = new ParserDelegator();
        htmlHandler = new HTMLHandler();
        pd.parse(getReader(uri), htmlHandler, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: HTML uri (file or url)");

            return;
        }

        try {
            HTML html = new HTML(args[0]);

            List img_src_list = html.getImageSrcs(false);
            System.out.println("<im src");

            Iterator img_src_iterator = img_src_list.iterator();

            while (img_src_iterator.hasNext()) {
                System.out.println((String) img_src_iterator.next());
            }

            List a_href_list = html.getAnchorHRefs(false);
            System.out.println("<a href");

            Iterator a_href_iterator = a_href_list.iterator();

            while (a_href_iterator.hasNext()) {
                System.out.println((String) a_href_iterator.next());
            }

            List link_href_list = html.getLinkHRefs(false);
            System.out.println("<link href");

            Iterator link_href_iterator = link_href_list.iterator();

            while (link_href_iterator.hasNext()) {
                System.out.println((String) link_href_iterator.next());
            }
        } catch (Exception e) {
            System.err.println(".main(): " + e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param duplicate DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getAnchorHRefs(boolean duplicate) {
        if (duplicate) {
            return htmlHandler.getAllAHRefs();
        } else {
            return htmlHandler.getAHRefs();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param duplicate DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getLinkHRefs(boolean duplicate) {
        if (duplicate) {
            return htmlHandler.getAllLinkHRefs();
        } else {
            return htmlHandler.getLinkHRefs();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param duplicate DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getImageSrcs(boolean duplicate) {
        if (duplicate) {
            return htmlHandler.getAllImageSrcs();
        } else {
            return htmlHandler.getImageSrcs();
        }
    }

    private Reader getReader(String uri) throws IOException {
        if (uri.startsWith("http:")) {
            // uri is url
            URLConnection connection = new URL(uri).openConnection();

            return new InputStreamReader(connection.getInputStream());
        } else {
            // uri is file
            return new FileReader(uri);
        }
    }
}
