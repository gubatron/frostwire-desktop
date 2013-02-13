package com.frostwire.gui.components.slides;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.LimeAction;

public class SlideControlsOverlay extends JPanel {
    private SlidePanelController controller;

    public SlideControlsOverlay(SlidePanelController controller) {
        this.controller = controller;
        setupUI();
    }

    private void setupUI() {
        setOpaque(false);
        setLayout(new FlowLayout());
        setBackground(new Color(0, 0, 0));
        setupButtons();
    }

    private void setupButtons() {
        Slide slide = controller.getSlide();
        if (slide.hasFlag(Slide.SLIDE_DOWNLOAD_METHOD_HTTP) || slide.hasFlag(Slide.SLIDE_DOWNLOAD_METHOD_TORRENT)) {

            if (slide.hasFlag(Slide.POST_DOWNLOAD_EXECUTE)) {
                //add install button
                add(new IconButton(new InstallAction(controller)));
            } else {
                //add download button
                add(new IconButton(new DownloadAction(controller)));
            }
        }

        if (slide.hasFlag(Slide.SHOW_VIDEO_PREVIEW_BUTTON)) {
            //add video preview button
            add(new IconButton(new PreviewVideoAction(controller)));
        }

        if (slide.hasFlag(Slide.SHOW_AUDIO_PREVIEW_BUTTON)) {
            //add audio preview button
            add(new IconButton(new PreviewAudioAction(controller)));
        }
    }

    @Override
    public void paint(Graphics g) {
        Color background = getBackground();
        Graphics2D g2d = (Graphics2D) g;

        Composite c = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(background);
        g.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(c);

        super.paint(g);
    }

    private static final class InstallAction extends AbstractAction {

        private SlidePanelController controller;

        public InstallAction(SlidePanelController controller) {
            this.controller = controller;
            putValue(Action.NAME, I18n.tr("Install"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Install"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Install") + " " + controller.getSlide().title);
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
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
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");

        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
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
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");

        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
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
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");

        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            controller.previewAudio();
        }
    }
}