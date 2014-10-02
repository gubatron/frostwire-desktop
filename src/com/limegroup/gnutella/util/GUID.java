/*
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

package com.limegroup.gnutella.util;

import java.util.Locale;

import org.limewire.util.ByteOrder;


/**
 * A 16-byte globally unique ID.  Immutable.<p>
 *
 * Let the bytes of a GUID G be labelled G[0]..G[15].  All bytes are unsigned.
 * Let a "short" be a 2 byte little-endian** unsigned number.  Let AB be the
 * short formed by concatenating bytes A and B, with B being the most
 * significant byte.  LimeWire GUID's have the following properties:
 *
 * <ol>
 * <li>G[15]=0x00.  This is reserved for future use.
 * <li>G[9][10]= tag(G[4][5], G[6][7]).  This is LimeWire's "secret" 
 *  proprietary marking. 
 * </ol>
 *
 * Here tag(A, B)=OxFFFF & ((A+2)*(B+3) >> 8).  In other words, the result is
 * obtained by first taking pair of two byte values and adding "secret"
 * constants.  These two byte values are then multiplied together to form a 4
 * byte product.  The middle two bytes of this product are the tag.  <b>Sign IS
 * considered during this process, since Java does that by default.</b><p>
 *
 * As of 9/2004, LimeWire GUIDs used to be marked as such:
 * <li>G[8]==0xFF.  This serves to identify "new GUIDs", e.g. from BearShare.
 * This marking was deprecated.
 *
 * In addition, LimeWire GUIDs may be marked as follows:
 * <ol>
 * <li>G[13][14]=tag(G[0]G[1], G[9][10]).  This was used by LimeWire 2.2.0-2.2.3
 * to mark automatic requeries.  Unfortunately these versions inadvertently sent
 * requeries when cancelling uploads or when sometimes encountering a group of
 * busy hosts. VERSION 0
 * </ol>
 * <li>G[13][14]=tag(G[0][1], G[2][3]).  This marks requeries from versions of
 *  LimeWire that have fixed the requery bug, e.g., 2.2.4 and all 2.3s.  VERSION
 * 1
 * </ol>
 * <li>G[13][14]=tag(G[0][1], G[11][12]).  This marks requeries from versions of
 * LimeWire that have much reduced the amount of requeries that can be sent by
 * an individual client.  a client can only send 32 requeries amongst ALL
 * requeries a day.  VERSION 2
 * </ol>
 *
 * Note that this still leaves 10-12 bytes for randomness.  That's plenty of
 * distinct GUID's.  And there's only a 1 in 65000 chance of mistakenly
 * identifying a LimeWire.
 *
 * Furthermore, LimeWire GUIDs may be 'marked' by containing address info.  In
 * particular:
 * <ol>
 * <li>G[0][3] = 4-octet IP address.  G[13][14] = 2-byte port (little endian).
 * </ol>
 * Note that there is no way to tell from a guid if it has been marked in this
 * manner.  You need to have some indication external to the guid (i.e. for
 * queries the minSpeed field might have a bit set to indicate this).  Also,
 * this reduces the amount of guids per IP to 2^48 - plenty since IP and port
 * comboes are themselves unique.
 *  
 */
public class GUID implements Comparable<GUID> {
    /** The size of a GUID. */
    private static final int SZ=16;
    /** The contents of the GUID.  INVARIANT: bytes.length==SZ */
    private byte[] bytes;

    /**
     * Creates a new <tt>GUID</tt> instance with the specified array
     * of unique bytes.
     *
     * @param bytes the array of unique bytes
     */
    public GUID(byte[] bytes) {
        assert(bytes.length==SZ);
        this.bytes=bytes;
    }

    /** 
     * Compares this GUID to o, lexically.
     */
    public int compareTo(GUID o) {
        if (this == o)
			return 0;
        else
			return compare(this.bytes(), o.bytes());
    }
    
    /** Compares guid and guid2 lexically, which MUST be 16-byte guids. */
    private static final int compare(byte[] guid, byte[] guid2) {
        for (int i=0; i<SZ; i++) {
            int diff=guid[i]-guid2[i];
            if (diff!=0)
                return diff;
        }
        return 0;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof GUID) {
            byte[] bytes2=((GUID)o).bytes();
            assert bytes!=null : "Null bytes in GUID.equals";
            assert bytes2!=null : "Null bytes2 in GUID.equals";
            for (int i=0; i<SZ; i++)
                if (bytes[i]!=bytes2[i])
                    return false;
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        //Glum bytes 0..3, 4..7, etc. together into 32-bit numbers.
        byte[] ba=bytes;
        final int M1=0x000000FF;
        final int M2=0x0000FF00;
        final int M3=0x00FF0000;

        int a=(M1&ba[0])|(M2&ba[1]<<8)|(M3&ba[2]<<16)|(ba[3]<<24);
        int b=(M1&ba[4])|(M2&ba[5]<<8)|(M3&ba[6]<<16)|(ba[7]<<24);
        int c=(M1&ba[8])|(M2&ba[9]<<8)|(M3&ba[10]<<16)|(ba[11]<<24);
        int d=(M1&ba[12])|(M2&ba[13]<<8)|(M3&ba[14]<<16)|(ba[15]<<24);

        //XOR together to yield new 32-bit number.
        return a^b^c^d;
    }

    /** Warning: this exposes the rep!  Do not modify returned value. */
    public byte[] bytes() {
        return bytes;
    }

    public String toString() {
        return toHexString();
    }

    /**
     *  Create a hex version of a GUID for compact display and storage
     *  Note that the client guid should be read in with the
     *  Integer.parseByte(String s, int radix)  call like this in reverse
     */
    public String toHexString() {
        StringBuilder buf=new StringBuilder();
        String       str;
        int val;
        for (int i=0; i<SZ; i++) {
            //Treating each byte as an unsigned value ensures
            //that we don't str doesn't equal things like 0xFFFF...
            val = ByteOrder.ubyte2int(bytes[i]);
            str = Integer.toHexString(val);
            while ( str.length() < 2 )
            str = "0" + str;
            buf.append( str );
        }
        return buf.toString().toUpperCase(Locale.US);
    }


    //Unit test: in the tests project.
}
