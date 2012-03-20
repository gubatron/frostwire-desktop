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

package de.savemytube.flv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class FLVTagVideo extends FLVTag{

    /**
     *  Name     Expression      Description
        codecID     (byte & 0x0f) » 0   2: Sorensen H.263, 3: Screen video, 4: On2 VP6, 5: On2 VP6 Alpha, 6: ScreenVideo 2
        frameType   (byte & 0xf0) » 4   1: keyframe, 2: inter frame, 3: disposable inter frame
     */
    public static final int VIDEO_CODECID_H263 = 2;
    public static final int VIDEO_CODECID_SCREENVIDEO = 3;
    public static final int VIDEO_CODECID_ON2_VP6 = 4;
    public static final int VIDEO_CODECID_ON2_VP6_ALPHA = 5;
    public static final int VIDEO_CODECID_SCREENVIDEO2 = 6;
    
    public static final int VIDEOFRAMETYPE_KEYFRAME = 1;
    public static final int VIDEOFRAMETYPE_INTERFRAME = 2;
    public static final int VIDEOFRAMETYPE_DISPOSABLEINTERFRAME = 3;
    
    public static final String[] S_CODEC = new String[] {"","","H.263","Screen video","On2 VP6","On2 VP6 Alpha","ScreenVideo 2"};
    public static final String[] S_FRAMETYPE = new String[] {"","keyframe","inter frame","disposable inter frame"};
    
    
    private int codecID;
    private int frameType;
    
        
    public FLVTagVideo(byte type,int bodyLength,int timestamp,byte timestampExtended,int streamId) {
        super(type,bodyLength,timestamp,timestampExtended,streamId);        
    }

    
    public void read(ReadableByteChannel channel)throws IOException {
        /**
         *  Name     Expression      Description
            codecID     (byte & 0x0f) » 0   2: Sorensen H.263, 3: Screen video, 4: On2 VP6, 5: On2 VP6 Alpha, 6: ScreenVideo 2
            frameType   (byte & 0xf0) » 4   1: keyframe, 2: inter frame, 3: disposable inter frame
         */
        ByteData bData = read(channel,1);
        ByteBuffer buf = bData.getByteBuffer();
        byte b = getBytes(buf)[0];
        setInfo(b);        
        setBodyLength(getBodyLength()-1);
        
        // read Data        
        bData = read(channel,getBodyLength());
        setBody(getBytes(bData.getByteBuffer()));
                                           
    }
    
    private void setInfo(byte b) {        
        codecID = (b & 0x0f) >> 0;  
        frameType = (b  & 0xf0) >> 4;                
    }
    
    
    public String toString() {
        String s = "---- VideoFormat ----\n";
        s = s + super.toString() +"\n";        
        s = s+ S_CODEC[codecID]+ " " +S_FRAMETYPE[frameType];        
        return s;
    }

    public int getCodecID() {
        return codecID;
    }

    public void setCodecID(int codecID) {
        this.codecID = codecID;
    }

    public int getFrameType() {
        return frameType;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }
}
