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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;

import de.log.Category;
import de.savemytube.avi.AVIWriter;
import de.savemytube.avi.Framerate;
import de.savemytube.avi.FramerateCalculator;

public class FLV {

    static Category log = Category.getInstance(FLV.class); 
    private String filename;
    
    private boolean video=false;
    private boolean audio=false;
    private byte version;
    private String signature;
    private int offset;
    
    private ArrayList tagsAudio = new ArrayList();
    private ArrayList videoTimeStamps = new ArrayList();
    
    boolean debug = false;
    private AVIWriter aviWriter;
    
    private boolean extract_audio;
    private boolean extract_video;
    public FLV() {
       
    }
    
    public FLV(String filename,boolean extract_audio,boolean extract_video) {
        this.filename = filename;
        this.extract_audio = extract_audio;
        this.extract_video = extract_video;
        read();
    }

    private void read() {

        try {
            
            File file = new File(filename);
            long size = file.length();
            // Obtain a channel
            ReadableByteChannel channel = new FileInputStream(filename).getChannel();     
            
            // Header:
            /*
             *  Signature    byte[3]     �FLV�   Always �FLV�
                Version     uint8   �\x01� (1)  Currently 1 for known FLV files
                Flags   uint8 bitmask   �\x05� (5, audio+video)     Bitmask: 4 is audio, 1 is video
                Offset  uint32_be   �\x00\x00\x00\x09� (9)  Total size of header (always 9 for known FLV files)
             */
            
            // Create a direct ByteBuffer; see also e158 Creating a ByteBuffer
            
            ByteData byteData = read(channel,9);            
            ByteBuffer buf = byteData.getByteBuffer();                              
            signature = new String(getBytes(buf,0,3)); // alway FLV        
            version = getBytes(buf,4,1)[0]; // 1.0
            byte audio_video = getBytes(buf,5,1)[0]; // audio and/or video
            if ((audio_video & 4) == 4) audio = true; // 4 is audio      
            if ((audio_video & 1) == 1) video = true; // 1 is video
            byte[] bOffset = getBytes(buf,6,4);
            offset = getInt(bOffset); // always 9
            log.debug(toString());
                        
            // Tags
            read(channel,3);
            readTags(channel);
                                   
            channel.close();
          
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
    
    
    private void readTags(ReadableByteChannel channel) throws Exception{
        
        // First Byte of Tags
        /*
         * Field    Data Type   Example     Description
           PreviousTagSize     uint32_be   �\x00\x00\x00\x00� (0)  Always 0
        */
        ByteData byteData = read(channel,1);     
        ByteBuffer buf = byteData.getByteBuffer();       
        byte bFLVStream = getBytes(buf,0,1)[0]; // always 0
        
        /*
         *  Type     uint8   �\x12� (0�12, META)     Determines the layout of Body, see below for tag types
            BodyLength  uint24_be   �\x00\x00\xe0� (224)    Size of Body (total tag size - 11)
            Timestamp   uint24_be   �\x00\x00\x00� (0)  Timestamp of tag (in milliseconds)
            TimestampExtended   uint8   �\x00� (0)  Timestamp extension to form a uint32_be. This field has the upper 8 bits.
            StreamId    uint24_be   �\x00\x00\x00�  (0)  Always 0
            Body    byte[BodyLength]    ...     Dependent on the value of Type
         */
        
        // Tags
        
        int counterAudio = 0;
        int counterVideo = 0;
        while (byteData != null && !byteData.isEof()) {
            byteData = read(channel,11);  
            buf = byteData.getByteBuffer();   
            
            byte type = getBytes(buf,0,1)[0];
            int bodyLength = getInt(getBytes(buf,1,3));
            int timestamp = getInt(getBytes(buf,4,3));
            byte timestampExt = getBytes(buf, 7,1)[0];            
            int streamId = getInt(getBytes(buf,8,3));  
            
            timestamp |= timestampExt << 24; 
            
            FLVTag tag = null;
            switch(type) {
                case FLVTag.TYPE_AUDIO:{
                    //if (extract_audio) {
                        tag = new FLVTagAudio(type,bodyLength,timestamp,timestampExt,streamId);
                        tag.read(channel);                    
                        //if (counterAudio == 0) {
                        log.debug(tag.toString());                        
                        //}    
                        counterAudio++;
                        tagsAudio.add(tag);
                    //}
                    break;
                }
                case FLVTag.TYPE_VIDEO:{
                    //if (extract_video) {
                        FLVTagVideo tagVideo = new FLVTagVideo(type,bodyLength,timestamp,timestampExt,streamId);
                        tagVideo.read(channel);                    
                        //if (counterVideo == 0) {
                        log.debug(tagVideo.toString());                                              
                        //}    
                    if (extract_video) {    
                        AVIWriter aw = getAVIWriter(filename.substring(0,filename.length()-4) +".avi", tagVideo.getCodecID());
                        aw.writeChunk(tagVideo.getBody(), timestamp, tagVideo.getFrameType());
                    }    
                        videoTimeStamps.add(new Long(timestamp));
                        counterVideo++;
                    //}
                    break;
                }
                default:{
                    byteData = read(channel,bodyLength);
                }                    
            }
                
           
            
            // Read 4 bytes
            read(channel,4);
            
        }
        
        
        if (extract_audio) {
            if (audio) {
                writeAudioFile(filename.substring(0,filename.length()-4) + ".mp3",this.tagsAudio);
            }   
        }
        
        if (extract_video) {
            if (video) {
                Framerate framerate = FramerateCalculator.calculateTrueFrameRate(videoTimeStamps);
                getAVIWriter("",0).finish(framerate);
            }   
        }
        
        
    }
    
    
    public ByteData read(ReadableByteChannel channel,int bytes) throws IOException{
        if (bytes == 0) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocateDirect(bytes);        
        buf.rewind();                               
        int eof = channel.read(buf);
        if (buf != null) {
            buf.rewind();
        }
        
        
        if (debug) {
            byte[] b = getBytes(buf);
            for (int i = 0; i < b.length;i++) {
                log.debug((i+1) + ": " + b[i]);
            }  
            buf.rewind();
        }
        
        ByteData bd = new ByteData(buf,(eof == -1));
        return bd;                
    }
    
    public byte[] getBytes(ByteBuffer buf) {
        return getBytes(buf,0,buf.capacity());
    }
    
    public byte[] getBytes(ByteBuffer buf,int from,int len) {       
        byte[] bytes = new byte[len];
        buf.get(bytes, 0, bytes.length);
        return bytes;
    }
    
    public static int getInt(byte[] data) {
        
        int number = 0;            
        for (int i = 0; i < data.length; ++i) {
          byte b = data[(data.length-1)-i];
          int bitsToShift = i << 3;
          int add = ( b & 0xff) << bitsToShift;
          number = number | add;
        }
        return number;
    }
    
    public static long arr2long (byte[] arr, int start) {
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return accum;
    }
    
    public byte[] getBytes(int number) {
      byte[] data = new byte[4]; 
      for (int i = 0; i < 4; ++i) {
        int shift = i << 3; // i * 8
        data[3-i] = (byte)((number & (0xff << shift)) >>> shift);
      }
      return data;
    } 
    
   

    
   
    
    
    public void writeAudioFile(String filename,ArrayList lTags) throws FileNotFoundException,IOException{        
        RandomAccessFile file = new RandomAccessFile(filename, "rw" );
        for (int i = 0; i < lTags.size();i++) {
            FLVTag tag = (FLVTag)lTags.get(i);
            file.write(tag.getBody());
        }
        file.close();
    }
    
    public void reverse(byte[] b) {
        int left  = 0;          // index of leftmost element
        int right = b.length-1; // index of rightmost element
       
        while (left < right) {
           // exchange the left and right elements
           byte temp = b[left]; 
           b[left]  = b[right]; 
           b[right] = temp;
          
           // move the bounds toward the center
           left++;
           right--;
        }
     }//endmethod reverse
                  
    
    
    public String toString() {
        String s = "FLVInformation - Signature:"+ signature + " Version:"+version + " hasAudio:" + audio + " hasVideo:" + video + " Offset:" + offset;        
        return s;
    }
    
    private AVIWriter getAVIWriter(String filename,int codec) throws Exception{
        if (aviWriter == null) {
            aviWriter = new AVIWriter(filename,codec);
        }
        return aviWriter;
    }

}
