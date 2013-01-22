/*
 * Created by Alden Torres (aldenml)
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

package com.frostwire.gui.mplayer;

import java.awt.Canvas;
import java.awt.Component;

import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaPlayerListener;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.mplayer.MediaPlaybackState;

/**
 * @author aldenml
 *
 */
public class MPlayerComponentOSX2 extends Canvas implements MPlayerComponent, MediaPlayerListener {

    private static final long serialVersionUID = -4871743835162851226L;

    static {
        System.loadLibrary("JMPlayer");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addNSView();
    }

    public native void addNSView();

    @Override
    public void mediaOpened(MediaPlayer mediaPlayer, MediaSource mediaSource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void progressChange(MediaPlayer mediaPlayer, float currentTimeInSecs) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void volumeChange(MediaPlayer mediaPlayer, double currentVolume) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stateChange(MediaPlayer mediaPlayer, MediaPlaybackState state) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void icyInfo(MediaPlayer mediaPlayer, String data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean toggleFullScreen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getWindowID() {
        // TODO Auto-generated method stub
        return 0;
    }
}