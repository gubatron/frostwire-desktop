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

import java.nio.ByteBuffer;

public class ByteData {

    private ByteBuffer byteBuffer;
    private boolean eof;
    
    public ByteData(ByteBuffer byteBuffer,boolean eof) {
        this.byteBuffer = byteBuffer;
        this.eof = eof;
    }
    
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    public boolean isEof() {
        return eof;
    }
    public void setEof(boolean eof) {
        this.eof = eof;
    }
    
}
