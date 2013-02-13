/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.gui.components.slides;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.OSUtils;

import com.frostwire.JsonEngine;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.HttpClientType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * Contains all the SlideshowPanels.
 * @author gubatron
 *
 */
public class MultimediaSlideshowPanel extends JPanel implements SlideshowPanel {

    private static final Log LOG = LogFactory.getLog(MultimediaSlideshowPanel.class);

    private SlideshowListener listener;
    private CardLayout layout;
    private List<Slide> slides;

    private JPanel container;
    private boolean useControls;

    public MultimediaSlideshowPanel(List<Slide> slides) {
        setupUI();
        setup(slides);
    }

    public MultimediaSlideshowPanel(final String url) {
        setupUI();
        new Thread(new Runnable() {
            public void run() {
                load(url);
            }
        }).start();
    }

    @Override
    public void setListener(SlideshowListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCurrentSlideIndex() {
        Component[] components = getComponents();
        for (Component c : components) {
            if (c.isVisible() && c instanceof SlidePanel) {
                return ((SlidePanel) c).getIndex();
            }
        }
        return -1;
    }

    @Override
    public void switchToSlide(int slideIndex) {
        if (slideIndex >=0 && slideIndex < getNumSlides()) {
            layout.show(this,String.valueOf(slideIndex));    
        }
    }

    @Override
    public int getNumSlides() {
        if (slides == null) {
            return 0;
        } else {
            return slides.size();
        }
    }

    private void setupUI() {
        layout = new CardLayout();
        setLayout(layout);
    }

    private void setup(List<Slide> slides) {
        this.slides = filter(slides);

        Collections.reverse(slides);

        int i = 0;
        for (Slide s : slides) {
            add(new SlidePanel(s, i), String.valueOf(i));
            i++;
        }
        
        if (container != null && useControls) {
            container.add(new SlideshowPanelControls(this), BorderLayout.PAGE_END);
        }
    }

    private void load(final String url) {
        try {
            HttpClient client = HttpClientFactory.newInstance(HttpClientType.PureJava);
            String jsonString = client.get(url);

            if (jsonString != null) {
                final SlideList slideList = new JsonEngine().toObject(jsonString, SlideList.class);
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        try {
                            setup(slideList.slides);
                        } catch (Exception e) {
                            LOG.info("Failed load of Slide Show:" + url, e);
                            slides = Collections.emptyList();
                            // nothing happens
                        }
                    }
                });

            }
        } catch (Exception e) {
            LOG.info("Failed load of Slide Show:" + url, e);
            slides = Collections.emptyList();
            // nothing happens
        }
    }

    /*
     * Examples of when this returns true
     * given == lang in app
     * es_ve == es_ve
     * es == es_ve
     * * == es_ve
     */
    private boolean isMessageEligibleForMyLang(String lang) {

        if (lang == null || lang.equals("*"))
            return true;

        String langinapp = ApplicationSettings.getLanguage().toLowerCase();

        if (lang.length() == 2)
            return langinapp.toLowerCase().startsWith(lang.toLowerCase());

        return lang.equalsIgnoreCase(langinapp);
    }

    private boolean isMessageEligibleForMyOs(String os) {
        if (os == null)
            return true;

        boolean im_mac_msg_for_me = os.contains("mac") && OSUtils.isMacOSX();

        boolean im_windows_msg_for_me = os.contains("windows") && OSUtils.isWindows();

        boolean im_linux_msg_for_me = os.contains("linux") && OSUtils.isLinux();

        return im_mac_msg_for_me || im_windows_msg_for_me || im_linux_msg_for_me;
    }

    private boolean isMessageEligibleForMyVersion(String versions) {
        if (versions == null || versions.equals("*")) {
            return true;
        }

        String frostWireVersion = FrostWireUtils.getFrostWireVersion();
        for (String pattern : versions.split(",")) {
            if (Pattern.matches(pattern, frostWireVersion)) {
                return true; // for-loop-break?
            }
        }

        return false;
    }

    private List<Slide> filter(List<Slide> slides) {
        List<Slide> result = new ArrayList<Slide>(slides.size());

        for (Slide slide : slides) {
            if (isMessageEligibleForMyLang(slide.language) && 
                isMessageEligibleForMyOs(slide.os) && 
                isMessageEligibleForMyVersion(slide.includedVersions)) {
                result.add(slide);
            }
        }

        return result;
    }

    @Override
    public void setupContainerAndControls(JPanel container, boolean useControls) {
        this.container = container;
        this.useControls = useControls;
    }
}