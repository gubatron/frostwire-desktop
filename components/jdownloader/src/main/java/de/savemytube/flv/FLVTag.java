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
import java.nio.channels.ReadableByteChannel;

public abstract class FLVTag extends FLV{

    /**
     *  Tag code     Name    Description
        0×08    AUDIO   Contains an audio packet similar to a SWF SoundStreamBlock plus codec information
        0×09    VIDEO   Contains a video packet similar to a SWF VideoFrame plus codec information
        0×12    META    Contains two AMF packets, the name of the event and the data to go with it
     */
    
    public final static byte TYPE_AUDIO = 8;
    public final static byte TYPE_VIDEO = 9;
    public final static byte TYPE_META = 12; 
    
    private byte type;
    private int bodyLength;
    private int timestamp;
    private byte timestampExtended;
    private int streamId;    
    private byte[] body;
    
    public FLVTag(byte type,int bodyLength,int timestamp,byte timestampExtended,int streamId) {
        this.type = type;
        this.bodyLength = bodyLength;       
        this.timestamp = timestamp;
        this.timestampExtended=timestampExtended;
        this.streamId = streamId;
        
    }
    
    public byte[] getBody() {
        return body;
    }
    public void setBody(byte[] body) {
        this.body = body;
    }
    public int getBodyLength() {
        return bodyLength;
    }
    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }
    public int getStreamId() {
        return streamId;
    }
    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }
    public int getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    public byte getTimestampExtended() {
        return timestampExtended;
    }
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setTimestampExtended(byte timestampExtended) {
        this.timestampExtended = timestampExtended;
    }
  
    public String toString() {
        String s = "";
        s = "TagInformation - bodyLength:" + bodyLength + " " + "timestamp:" + timestamp + " timestampExt:" + timestampExtended + " streamId:" + streamId;
        return s;
    }
    
    public abstract void read(ReadableByteChannel channel)throws IOException;
    
    
    
}
