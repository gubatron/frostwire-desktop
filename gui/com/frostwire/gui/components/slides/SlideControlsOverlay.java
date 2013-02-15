package com.frostwire.gui.components.slides;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.LimeAction;

class SlideControlsOverlay extends JPanel {

    private static final Color BACKGROUND = new Color(45, 52, 58); // 2d343a
    private static final float BACKGROUND_ALPHA = 0.7f;
    private static final Color TEXT_FOREGROUND = new Color(255, 255, 255);
    private static final int TEXT_FONT_SIZE_DELTA = 3;
    private static final int SOCIAL_BAR_HEIGHT = 55;

    private final SlidePanelController controller;
    private final Composite overlayComposite;

    public SlideControlsOverlay(SlidePanelController controller) {
        this.controller = controller;
        this.overlayComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BACKGROUND_ALPHA);

        setupUI();
    }

    private void setupUI() {
        setOpaque(false);
        setLayout(new MigLayout("", "[grow][][][][grow]", //columns
                "[grow][center][grow][bottom]")); //rows
        setBackground(BACKGROUND);

        setupTitle();
        setupButtons();
        setupSocialBar();
    }

    private void setupTitle() {
        JLabel labelTitle = new JLabel(controller.getSlide().title);
        labelTitle.putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
        labelTitle.setForeground(TEXT_FOREGROUND);
        labelTitle.setFont(getFont().deriveFont(getFont().getSize2D() + TEXT_FONT_SIZE_DELTA));
        add(labelTitle, "cell 0 0, span 5, top");
    }

    private void setupButtons() {
        final Slide slide = controller.getSlide();

        if (slide.hasFlag(Slide.SHOW_PREVIEW_BUTTONS_ON_THE_LEFT)) {
            addPreviewButtons(slide, "cell 1 1", "cell 2 1");
            addDownloadInstallButton(slide, "cell 3 1");
        } else {
            addDownloadInstallButton(slide, "cell 1 1");
            addPreviewButtons(slide, "cell 2 1", "cell 3 1");
        }
    }

    private void setupSocialBar() {
        Slide slide = controller.getSlide();

        JLabel labelAuthor = new JLabel(slide.author + " " + I18n.tr("on"));
        labelAuthor.putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
        labelAuthor.setForeground(TEXT_FOREGROUND);
        labelAuthor.setFont(getFont().deriveFont(getFont().getSize2D() + TEXT_FONT_SIZE_DELTA));

        add(labelAuthor, "cell 1 3, span 3, aligny baseline");

        if (slide.facebook != null) {
            add(new OverlayIconButton(new SocialAction("Facebook", slide.facebook)), "cell 1 3");
        }

        if (slide.twitter != null) {
            add(new OverlayIconButton(new SocialAction("Twitter", slide.twitter)), "cell 1 3");
        }

        if (slide.gplus != null) {
            add(new OverlayIconButton(new SocialAction("Google Plus", slide.gplus, "gplus")), "cell 1 3");
        }

        if (slide.youtube != null) {
            add(new OverlayIconButton(new SocialAction("YouTube", slide.youtube)), "cell 1 3");
        }

        if (slide.instagram != null) {
            add(new OverlayIconButton(new SocialAction("Instagram", slide.instagram)), "cell 1 3");
        }

    
    }

    private void addPreviewButtons(final Slide slide, String constraintVideoPreview, String constraintAudioPreview) {
        if (slide.hasFlag(Slide.SHOW_VIDEO_PREVIEW_BUTTON)) {
            //add video preview button
            add(new OverlayIconButton(new PreviewVideoAction(controller)), constraintVideoPreview);
        }

        if (slide.hasFlag(Slide.SHOW_AUDIO_PREVIEW_BUTTON)) {
            //add audio preview button
            add(new OverlayIconButton(new PreviewAudioAction(controller)), constraintAudioPreview);
        }
    }

    private void addDownloadInstallButton(final Slide slide, String constraints) {
        if (slide.method == Slide.SLIDE_DOWNLOAD_METHOD_HTTP || slide.method == Slide.SLIDE_DOWNLOAD_METHOD_TORRENT) {

            if (slide.hasFlag(Slide.POST_DOWNLOAD_EXECUTE)) {
                //add install button
                add(new OverlayIconButton(new InstallAction(controller)), constraints);// "cell column row width height"
            } else {
                //add download button
                add(new OverlayIconButton(new DownloadAction(controller)), constraints);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Color background = getBackground();

        Composite c = g2d.getComposite();
        g2d.setComposite(overlayComposite);
        g2d.setColor(background);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(c);
        
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, getHeight()-SOCIAL_BAR_HEIGHT, getWidth(), SOCIAL_BAR_HEIGHT);
        
        g2d.setColor(background);
        
        super.paint(g);
    }

    private static final class InstallAction extends AbstractAction {

        private SlidePanelController controller;

        public InstallAction(SlidePanelController controller) {
            this.controller = controller;
            putValue(Action.NAME, I18n.tr("Install"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Install"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Install") + " " + controller.getSlide().title);
            putValue(LimeAction.ICON_NAME, "SLIDE_CONTROLS_OVERLAY_DOWNLOAD");
            putValue(LimeAction.ICON_NAME_ROLLOVER, "SLIDE_CONTROLS_OVERLAY_DOWNLOAD_ROLLOVER");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.installSlide();
        }
    }

    private static final class DownloadAction extends AbstractAction {

        private SlidePanelController controller;

        public DownloadAction(SlidePanelController controller) {
            this.controller = controller;
            putValue(Action.NAME, I18n.tr("Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Download"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Download") + " " + controller.getSlide().title);
            putValue(LimeAction.ICON_NAME, "SLIDE_CONTROLS_OVERLAY_DOWNLOAD");
            putValue(LimeAction.ICON_NAME_ROLLOVER, "SLIDE_CONTROLS_OVERLAY_DOWNLOAD_ROLLOVER");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.downloadSlide();
        }
    }

    private static final class PreviewVideoAction extends AbstractAction {

        private SlidePanelController controller;

        public PreviewVideoAction(SlidePanelController controller) {
            this.controller = controller;
            putValue(Action.NAME, I18n.tr("Video Preview"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Video Preview"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Play Video preview of") + " " + controller.getSlide().title);
            putValue(LimeAction.ICON_NAME, "SLIDE_CONTROLS_OVERLAY_PREVIEW");
            putValue(LimeAction.ICON_NAME_ROLLOVER, "SLIDE_CONTROLS_OVERLAY_PREVIEW_ROLLOVER");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.previewVideo();
        }
    }

    private static final class PreviewAudioAction extends AbstractAction {

        private SlidePanelController controller;

        public PreviewAudioAction(SlidePanelController controller) {
            this.controller = controller;
            putValue(Action.NAME, I18n.tr("Audio Preview"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Audio Preview"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Play Audio preview of") + " " + controller.getSlide().title);
            putValue(LimeAction.ICON_NAME, "SLIDE_CONTROLS_OVERLAY_PREVIEW");
            putValue(LimeAction.ICON_NAME_ROLLOVER, "SLIDE_CONTROLS_OVERLAY_PREVIEW_ROLLOVER");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.previewAudio();
        }
    }

    private static final class OverlayIconButton extends IconButton {

        public OverlayIconButton(Action action) {
            super(action);
            putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
            setForeground(TEXT_FOREGROUND);
            setFont(getFont().deriveFont(getFont().getSize2D() + TEXT_FONT_SIZE_DELTA));
        }
    }

    private static final class SocialAction extends AbstractAction {

        private final String url;

        public SocialAction(String networkName, String url) {
            this(networkName, url, networkName.toUpperCase());
        }
        
        public SocialAction(String networkName, String url, String imageName) {
            this.url = url;

            putValue(Action.SHORT_DESCRIPTION, networkName);

            putValue(LimeAction.ICON_NAME, "SLIDE_CONTROLS_OVERLAY_" + imageName);
            putValue(LimeAction.ICON_NAME_ROLLOVER, "SLIDE_CONTROLS_OVERLAY_" + imageName + "_ROLLOVER");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GUIMediator.openURL(url);
        }
    }
}