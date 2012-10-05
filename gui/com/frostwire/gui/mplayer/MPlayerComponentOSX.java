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

import java.awt.Component;
import java.awt.Dimension;

import org.limewire.util.CommonUtils;

import com.apple.eawt.CocoaComponent;

/**
 * @author aldenml
 *
 */
public class MPlayerComponentOSX extends CocoaComponent implements MPlayerComponent {

    private static final long serialVersionUID = -8610816510893757828L;

    private static final int JMPlayer_addNotify = 1;
    private static final int JMPlayer_dispose = 2;
    private static final int JMPlayer_toggleFS = 3;

    private long nsObject = 0;

    public MPlayerComponentOSX() {
    }

    @Override
    public int createNSView() {
        return (int) createNSViewLong();
    }

    @Override
    public long createNSViewLong() {
        com.apple.concurrent.Dispatch.getInstance().getBlockingMainQueueExecutor().execute(new Runnable() {
            @Override
            public void run() {
                nsObject = createNSView1(CommonUtils.getExecutableDirectory());
            }
        });

        return nsObject;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(25, 25);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 250);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        sendMsg(JMPlayer_addNotify);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    protected void dispose() {
        sendMsg(JMPlayer_dispose);
    }

    private void sendMsg(int messageID) {
        if (nsObject != 0) {
            sendMessage(messageID, null);
        }
    }

    private void sendMsg(int messageID, Object message) {
        if (nsObject != 0) {
            sendMessage(messageID, message);
        }
    }
    
    @Override
    public void toggleFullScreen() {
        sendMsg(JMPlayer_toggleFS);
    }
    
    private native long createNSView1(String appPath);
}
