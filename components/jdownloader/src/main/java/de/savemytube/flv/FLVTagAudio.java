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



import de.log.Category;

public class FLVTagAudio extends FLVTag{
                
    public static final int SOUND_TYPE_MONO = 0;
    public static final int SOUND_TYPE_STEREO = 1;
    
    public static final int SOUND_SIZE_8BIT = 0;
    public static final int SOUND_SIZE_16BIT = 1;
    
    public static final int SOUND_RATE_5_5 = 0;
    public static final int SOUND_RATE_11 = 1;
    public static final int SOUND_RATE_22 = 2;
    public static final int SOUND_RATE_44 = 3;
    
    public static final int SOUND_FORMAT_UNCOMPRESSED=0;
    public static final int SOUND_FORMAT_ADPCM=1;
    public static final int SOUND_FORMAT_MP3=2;
    public static final int SOUND_FORMAT_NELLYMOSER_8_MONO=5;
    public static final int SOUND_FORMAT_NELLYMOSER=6;
    
    public static String[] S_TYPES = new String[] {"Mono","Stereo"};
    public static String[] S_SIZE = new String[] {"8 Bit","16 Bit"}; 
    public static String[] S_RATE = new String[] {"5.5 kHz","11 kHz","22 kHz","44 kHz"}; 
    public static String[] S_FORMAT = new String[] {"uncompressed","ADPCM","Mp3","","","Nellymoser 8 Bit Mono","Nellymoser"};
    
    private int soundType;
    private int soundSize;
    private int soundRate;
    private int soundFormat;
            
    static Category log = Category.getInstance(FLVTagAudio.class); 
        
    public FLVTagAudio(byte type,int bodyLength,int timestamp,byte timestampExtended,int streamId) {
        super(type,bodyLength,timestamp,timestampExtended,streamId); 
        
    }


    
    public void read(ReadableByteChannel channel) throws IOException{
        
        /**The first byte of an audio packet contains bitflags that describe the codec used, with the following layout:
         *  Name     Expression      Description
            soundType   (byte & 0×01) » 0   0: mono, 1: stereo
            soundSize   (byte & 0×02) » 1   0: 8-bit, 1: 16-bit
            soundRate   (byte & 0x0C) » 2   0: 5.5 kHz, 1: 11 kHz, 2: 22 kHz, 3: 44 kHz
            soundFormat     (byte & 0xf0) » 4   0: Uncompressed, 1: ADPCM, 2: MP3, 5: Nellymoser 8kHz mono, 6: Nellymoser
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
        
        soundFormat = (b & 0xF0) >> 4;
        soundType = (b & 0x01) >> 0;
        soundSize = (b & 0x02) >> 1;
        soundRate = (b & 0x0C) >> 2;
               
    }
    
    
    public String toString() {       
        String s = "---- AudioFormat ----\n";
        s = s +super.toString() +"\n";
        s = s+ S_FORMAT[soundFormat]+ " " +S_SIZE[soundSize] + " " +S_TYPES[soundType] + " " + S_RATE[soundRate];        
        return s;
    }


   
    
}
