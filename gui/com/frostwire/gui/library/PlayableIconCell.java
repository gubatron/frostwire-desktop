package com.frostwire.gui.library;

import javax.swing.Icon;

public class PlayableIconCell {

    private Icon icon;
    private final boolean isPlaying;

    public PlayableIconCell(Icon icon, boolean isPlaying) {
        this.icon = icon;
        this.isPlaying = isPlaying;
    }

    public Icon getIcon() {
        return icon;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
