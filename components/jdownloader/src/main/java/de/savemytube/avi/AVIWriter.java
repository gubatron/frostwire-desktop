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

package de.savemytube.avi;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import de.savemytube.flv.FLV;

public class AVIWriter extends FLV{

    private RandomAccessFile file;
    private ArrayList _index;
    private int _codecID;
    private int _width;
    private int _height;
    private int _framecount = 0;
    private int _moviDataSize;
    private int _frameCount;
    private int _indexChunkSize;
    
    public AVIWriter(String path, int codecID) throws Exception {
        if ((codecID != 2) && (codecID != 4) && (codecID != 5)) {
            throw new Exception("Unsupported video codec.");
        }
        
        _codecID = codecID;        
        file = new RandomAccessFile(path, "rw" );
                
        WriteFourCC("RIFF");
        writeInt(0); // chunk size
        WriteFourCC("AVI ");

        WriteFourCC("LIST");
        writeInt( 192);
        WriteFourCC("hdrl");

        WriteFourCC("avih");
        writeInt( 56);
        writeInt(0);
        writeInt(0);
        writeInt(0);
        writeInt( 0x10);
        writeInt(0); // frame count
        writeInt(0);
        writeInt( 1);
        writeInt(0);
        writeInt(0); // width
        writeInt(0); // height
        writeInt(0);
        writeInt(0);
        writeInt(0);
        writeInt(0);

        WriteFourCC("LIST");
        writeInt( 116);
        WriteFourCC("strl");

        WriteFourCC("strh");
        writeInt( 56);
        WriteFourCC("vids");
        WriteFourCC(getCodecFourCC(codecID));
        writeInt(0);
        writeInt(0);
        writeInt(0);
        writeInt(0); // frame rate denominator
        writeInt(0); // frame rate numerator
        writeInt(0);
        writeInt(0); // frame count
        writeInt(0);
        writeInt((int)-1);
        writeInt(0);
        writeShort((short)0);
        writeShort((short)0);
        writeShort((short)0); // width
        writeShort((short)0); // height

        WriteFourCC("strf");
        writeInt( 40);
        writeInt( 40);
        writeInt(0); // width
        writeInt(0); // height
        writeShort((short)1);
        writeShort((short)24);
        WriteFourCC(getCodecFourCC(codecID));
        writeInt(0); // biSizeImage
        writeInt(0);
        writeInt(0);
        writeInt(0);
        writeInt(0);

        WriteFourCC("LIST");
        writeInt(0); // chunk size
        WriteFourCC("movi");

        _index = new ArrayList();
        
        
        
        
    }
    
    
    private String getCodecFourCC(int _codecID) {       
        if (_codecID == 2) {
            return "FLV1";
        }
        if ((_codecID == 4) || (_codecID == 5)) {
            return "FLV4";
        }
        return "NULL";       
    }
    
    private void WriteFourCC(String string) throws Exception{       
        byte[] bytes = string.getBytes();
        if (bytes.length != 4) {
            throw new Exception("Invalid FourCC length.");
        }
        file.write(bytes);        
    }
        
    
    public void writeChunk(byte[] chunk, int timeStamp, int frameType)throws Exception {
        int offset, len;

        offset = 0;
        if (_codecID == 4) offset = 1;
        if (_codecID == 5) offset = 4;
        len = Math.max(chunk.length - offset, 0);

        _index.add((frameType == 1) ? new Integer(0x10) : new Integer(0));
        _index.add(new Integer(_moviDataSize + 4));
        _index.add(new Integer(len));

        if ((_width == 0) && (_height == 0)) {
            getFrameSize(chunk);
        }

        WriteFourCC("00dc");
        writeInt(len);        
        file.write(chunk, offset, len);

        if ((len % 2) != 0) {
            file.write((byte)0);
            len++;
        }
        _moviDataSize += len + 8;
        _frameCount++;
    }
    
    
    private void getFrameSize(byte[] chunk) {
        if (_codecID == 2) {
            // Reference: flv_h263_decode_picture_header from libavcodec's h263.c

            if ((chunk[0] != 0) || (chunk[1] != 0)) {
                return;
            }
            
            byte[] d = new byte[] {chunk[2],chunk[3],chunk[4],chunk[5]};                        
            //reverse(d);
            long x = arr2long(d,0);
            int format;

            /*if (readBits(x, 1) != 1) {
                return;
            }*/
            readBits(x, 5);
            readBits(x, 8);

            format =readBits(x, 3);
            switch (format) {
                case 0:
                    _width =readBits(x, 8);
                    _height = readBits(x, 8);
                    break;
                case 1:
                    _width = readBits(x, 16);
                    _height =readBits(x, 16);
                    break;
                case 2:
                    _width = 352;
                    _height = 288;
                    break;
                case 3:
                    _width = 176;
                    _height = 144;
                    break;
                case 4:
                    _width = 128;
                    _height = 96;
                    break;
                case 5:
                    _width = 320;
                    _height = 240;
                    break;
                case 6:
                    _width = 160;
                    _height = 120;
                    break;
                default:
                    return;
            }
        }
        else if ((_codecID == 4) || (_codecID == 5)) {
            // Reference: CFLVSplitterFilter::CreateOutputs from Media Player Classic's FLVSplitter.cpp
            
            byte[] d = null;
            if (_codecID != 4) {
                d = new byte[] {chunk[0],chunk[1],chunk[2],chunk[3]};
            }
            else {
                d = new byte[] {chunk[0]};
            }
            
            long x =getInt(d);

            if ((readBits(x, 16) & 0x80fe) != 0x0046) {
                return;
            }

            _height = readBits(x, 8) * 16;
            _width = readBits(x, 8) * 16;
        }
    }
    
    private int readBits(long x, int length) {
        int r = (int)(x >> (32 - length));
        x <<= length;
        return r;
    }
    
    private void WriteIndexChunk() throws Exception {
        int indexDataSize = (int)_frameCount * 16;

        WriteFourCC("idx1");
        writeInt(indexDataSize);

        for (int i = 0; i < _frameCount; i++) {
            WriteFourCC("00dc");
            Integer x1 = (Integer)_index.get((i * 3) + 0);
            Integer x2 = (Integer)_index.get((i * 3) + 1);
            Integer x3 = (Integer)_index.get((i * 3) + 2);
            writeInt(x1.intValue());
            writeInt(x2.intValue());
            writeInt(x3.intValue());
        }

        _indexChunkSize = indexDataSize + 8;
    }

    
    private void writeInt(int x) throws IOException{ 
        x = Integer.reverseBytes(x);        
        file.writeInt(x);
    }
    
    private void writeShort(short x) throws IOException{          
        x = Short.reverseBytes(x);               
        file.writeShort(x);
    }
    
    
    
    public void finish(Framerate averageFrameRate) throws Exception{
        WriteIndexChunk();
        
        int p = 4; 
        file.seek(p);
        writeInt((int)(224 + _moviDataSize + _indexChunkSize - 8));

        p = 24+8;
        file.seek(p);
        writeInt((int)0);
        p = p + 12 + 4;
        file.seek(p);
        writeInt((int)_frameCount);
        p = p + 12 + 4;
        file.seek(p);
        writeInt((int)_width);
        writeInt((int)_height);

        p = 100 + 28;
        file.seek(p);
        writeInt((int)averageFrameRate.D);        
        writeInt((int)averageFrameRate.N);
        
        p = p + 4 + 4 + 4;
        file.seek(p);
        writeInt((int)_frameCount);
        
        p = p + 16 + 4;
        file.seek(p);       
        writeShort((short)_width);
        writeShort((short)_height);

        p = 164 + 12;
        file.seek(p);
        writeInt((int)_width);
        writeInt((int)_height);
        
        p = p +8 + 4 + 4;
        file.seek(p);
        writeInt((int)(_width * _height * 6));

        p = 212 + 4;
        file.seek(p);
        writeInt((int)(_moviDataSize + 4));

        file.close();
    }
    
    
}
