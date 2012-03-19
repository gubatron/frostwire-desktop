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

import java.util.ArrayList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import de.log.Category;

public class HTMLHandler extends ParserCallback {
    Category log = Category.getInstance(HTMLHandler.class);
    private ArrayList img_src;
    private ArrayList img_src_all;
    private ArrayList a_href;
    private ArrayList a_href_all;
    private ArrayList link_href;
    private ArrayList link_href_all;

    /**
     * Creates a new HTMLHandler object.
     */
    public HTMLHandler() {
        img_src_all = new ArrayList();
        img_src = new ArrayList();
        a_href_all = new ArrayList();
        a_href = new ArrayList();
        link_href_all = new ArrayList();
        link_href = new ArrayList();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     * @param pos DOCUMENT ME!
     */
    public void handleStartTag(Tag tag, MutableAttributeSet attributes, int pos) {
        if (tag.equals(HTML.Tag.A)) {
            String href = (String) attributes.getAttribute(HTML.Attribute.HREF);

            if (href != null) {
                a_href_all.add(href);

                if (!a_href.contains(href)) {
                    a_href.add(href);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tag DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     * @param pos DOCUMENT ME!
     */
    public void handleSimpleTag(Tag tag, MutableAttributeSet attributes, int pos) {
        if (tag.equals(HTML.Tag.IMG)) {
            String src = (String) attributes.getAttribute(HTML.Attribute.SRC);

            if (src != null) {
                img_src_all.add(src);

                if (!img_src.contains(src)) {
                    img_src.add(src);
                }
            }
        }

        if (tag.equals(HTML.Tag.LINK)) {
            String href = (String) attributes.getAttribute(HTML.Attribute.HREF);

            if (href != null) {
                link_href_all.add(href);

                if (!link_href.contains(href)) {
                    link_href.add(href);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getImageSrcs() {
        return img_src;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getAllImageSrcs() {
        return img_src_all;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getLinkHRefs() {
        return link_href;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getAllLinkHRefs() {
        return link_href_all;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getAHRefs() {
        return a_href;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getAllAHRefs() {
        return a_href_all;
    }
}