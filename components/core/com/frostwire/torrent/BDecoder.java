/*
 * BeDecoder.java
 *
 * Created on May 30, 2003, 2:44 PM
 * Copyright (C) 2003, 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */

package com.frostwire.torrent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A set of utility methods to decode a bencoded array of byte into a Map.
 * integer are represented as Long, String as byte[], dictionnaries as Map, and list as List.
 * 
 * @author TdC_VgA
 *
 */
final class BDecoder {

    private static final int MAX_BYTE_ARRAY_SIZE = 16 * 1024 * 1024;
    private static final int MAX_MAP_KEY_SIZE = 64 * 1024;

    private boolean recovery_mode;
    private boolean verify_map_order;

    public static Map<String, Object> decode(byte[] data)

    throws IOException {
        return (new BDecoder().decodeByteArray(data));
    }

    public static Map<String, Object> decode(byte[] data, int offset, int length)

    throws IOException {
        return (new BDecoder().decodeByteArray(data, offset, length));
    }

    public static Map<String, Object> decode(BufferedInputStream is)

    throws IOException {
        return (new BDecoder().decodeStream(is));
    }

    public BDecoder() {
    }

    public Map<String, Object> decodeByteArray(byte[] data) throws IOException {
        return decode(new BDecoderInputStreamArray(data));
    }

    public Map<String, Object> decodeByteArray(byte[] data, int offset, int length) throws IOException {
        return decode(new BDecoderInputStreamArray(data, offset, length));
    }

    // used externally 
    public Map<String, Object> decodeByteBuffer(ByteBuffer buffer) throws IOException {
        InputStream is = new BDecoderInputStreamArray(buffer);
        Map<String, Object> result = decode(is);
        buffer.position(buffer.limit() - is.available());
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeStream(BufferedInputStream data) throws IOException {
        Object res = decodeInputStream(data, "", 0);

        if (res == null) {

            throw (new BEncodingException("BDecoder: zero length file"));

        } else if (!(res instanceof Map)) {

            throw (new BEncodingException("BDecoder: top level isn't a Map"));
        }

        return (Map<String, Object>) res;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decode(InputStream data) throws IOException {
        Object res = decodeInputStream(data, "", 0);

        if (res == null) {

            throw (new BEncodingException("BDecoder: zero length file"));

        } else if (!(res instanceof Map)) {

            throw (new BEncodingException("BDecoder: top level isn't a Map"));
        }

        return ((Map<String, Object>) res);
    }

    // reuseable objects for key decoding
    private ByteBuffer keyBytesBuffer = ByteBuffer.allocate(32);
    private CharBuffer keyCharsBuffer = CharBuffer.allocate(32);
    private CharsetDecoder keyDecoder = Constants.BYTE_CHARSET.newDecoder();

    private Object decodeInputStream(InputStream dbis, String context, int nesting) throws IOException {
        if (nesting == 0 && !dbis.markSupported()) {
            throw new IOException("InputStream must support the mark() method");
        }

        //set a mark

        dbis.mark(Integer.MAX_VALUE);

        //read a byte

        int tempByte = dbis.read();

        //decide what to do

        switch (tempByte) {
        case 'd':
            //create a new dictionary object

            HashMap<String, Object> tempMap = new HashMap<String, Object>();

            try {
                byte[] prev_key = null;

                //get the key   

                while (true) {

                    dbis.mark(Integer.MAX_VALUE);

                    tempByte = dbis.read();
                    if (tempByte == 'e' || tempByte == -1)
                        break; // end of map

                    dbis.reset();

                    // decode key strings manually so we can reuse the bytebuffer

                    int keyLength = (int) getPositiveNumberFromStream(dbis, ':');

                    if (keyLength > MAX_MAP_KEY_SIZE) {
                        byte[] remaining = new byte[128];
                        getByteArrayFromStream(dbis, 128, remaining);
                        String msg = "dictionary key is too large, max=" + MAX_MAP_KEY_SIZE + ": value=" + new String(remaining);
                        System.err.println(msg);
                        throw (new IOException(msg));
                    }

                    if (keyLength < keyBytesBuffer.capacity()) {
                        keyBytesBuffer.position(0).limit(keyLength);
                        keyCharsBuffer.position(0).limit(keyLength);
                    } else {
                        keyBytesBuffer = ByteBuffer.allocate(keyLength);
                        keyCharsBuffer = CharBuffer.allocate(keyLength);
                    }

                    getByteArrayFromStream(dbis, keyLength, keyBytesBuffer.array());

                    if (verify_map_order) {

                        byte[] current_key = new byte[keyLength];

                        System.arraycopy(keyBytesBuffer.array(), 0, current_key, 0, keyLength);

                        if (prev_key != null) {

                            int len = Math.min(prev_key.length, keyLength);

                            int state = 0;

                            for (int i = 0; i < len; i++) {

                                int cb = current_key[i] & 0x00ff;
                                int pb = prev_key[i] & 0x00ff;

                                if (cb > pb) {
                                    state = 1;
                                    break;
                                } else if (cb < pb) {
                                    state = 2;
                                    break;
                                }
                            }

                            if (state == 0) {
                                if (prev_key.length > keyLength) {

                                    state = 2;
                                }
                            }

                            if (state == 2) {

                                // Debug.out( "Dictionary order incorrect: prev=" + new String( prev_key ) + ", current=" + new String( current_key ));

                                if (!(tempMap instanceof HashMapEx)) {

                                    HashMapEx x = new HashMapEx(tempMap);

                                    x.setFlag(HashMapEx.FL_MAP_ORDER_INCORRECT, true);

                                    tempMap = x;
                                }
                            }
                        }

                        prev_key = current_key;
                    }

                    keyDecoder.reset();
                    keyDecoder.decode(keyBytesBuffer, keyCharsBuffer, true);
                    keyDecoder.flush(keyCharsBuffer);
                    String key = new String(keyCharsBuffer.array(), 0, keyCharsBuffer.limit());

                    // keys often repeat a lot - intern to save space
                    //					if (internKeys)
                    //						key = StringInterner.intern( key );

                    //decode value

                    Object value = decodeInputStream(dbis, key, nesting + 1);

                    // value interning is too CPU-intensive, let's skip that for now
                    /*if(value instanceof byte[] && ((byte[])value).length < 17)
                    value = StringInterner.internBytes((byte[])value);*/

                    // recover from some borked encodings that I have seen whereby the value has
                    // not been encoded. This results in, for example, 
                    // 18:azureus_propertiesd0:e
                    // we only get null back here if decoding has hit an 'e' or end-of-file
                    // that is, there is no valid way for us to get a null 'value' here

                    if (value == null) {

                        System.err.println("Invalid encoding - value not serialsied for '" + key + "' - ignoring");

                        break;
                    }

                    if (tempMap.put(key, value) != null) {

                        Debug.out("BDecoder: key '" + key + "' already exists!");
                    }
                }

                /*	
                if ( tempMap.size() < 8 ){

                tempMap = new CompactMap( tempMap );
                }*/

                dbis.mark(Integer.MAX_VALUE);
                tempByte = dbis.read();
                dbis.reset();
                if (nesting > 0 && tempByte == -1) {

                    throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of dictionary"));
                }
            } catch (Throwable e) {

                if (!recovery_mode) {

                    if (e instanceof IOException) {

                        throw ((IOException) e);
                    }

                    throw (new IOException(Debug.getNestedExceptionMessage(e)));
                }
            }

            //tempMap.compactify(-0.9f);

            //return the map

            return tempMap;

        case 'l':
            //create the list

            ArrayList<Object> tempList = new ArrayList<Object>();

            try {
                //create the key

                String context2 = context;

                Object tempElement = null;
                while ((tempElement = decodeInputStream(dbis, context2, nesting + 1)) != null) {
                    //add the element
                    tempList.add(tempElement);
                }

                tempList.trimToSize();
                dbis.mark(Integer.MAX_VALUE);
                tempByte = dbis.read();
                dbis.reset();
                if (nesting > 0 && tempByte == -1) {

                    throw (new BEncodingException("BDecoder: invalid input data, 'e' missing from end of list"));
                }
            } catch (Throwable e) {

                if (!recovery_mode) {

                    if (e instanceof IOException) {

                        throw ((IOException) e);
                    }

                    throw (new IOException(Debug.getNestedExceptionMessage(e)));
                }
            }
            //return the list
            return tempList;

        case 'e':
        case -1:
            return null;

        case 'i':
            return Long.valueOf(getNumberFromStream(dbis, 'e'));

        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            //move back one
            dbis.reset();
            //get the string
            return getByteArrayFromStream(dbis, context);

        default: {

            int rem_len = dbis.available();

            if (rem_len > 256) {

                rem_len = 256;
            }

            byte[] rem_data = new byte[rem_len];

            dbis.read(rem_data);

            throw (new BEncodingException("BDecoder: unknown command '" + tempByte + ", remainder = " + new String(rem_data)));
        }
        }
    }

    /** only create the array once per decoder instance (no issues with recursion as it's only used in a leaf method)
     */
    private final char[] numberChars = new char[32];

    /**
     * @note will break (likely return a negative) if number >
     * {@link Integer#MAX_VALUE}.  This check is intentionally skipped to
     * increase performance
     */
    private int getPositiveNumberFromStream(InputStream dbis, char parseChar) throws IOException {
        int tempByte = dbis.read();
        if (tempByte < 0) {
            return -1;
        }
        if (tempByte != parseChar) {

            int value = tempByte - '0';

            tempByte = dbis.read();
            // optimized for single digit cases
            if (tempByte == parseChar) {
                return value;
            }
            if (tempByte < 0) {
                return -1;
            }

            while (true) {
                // Base10 shift left --> v*8 + v*2 = v*10
                value = (value << 3) + (value << 1) + (tempByte - '0');
                // For bounds check:
                // if (value < 0) return something;
                tempByte = dbis.read();
                if (tempByte == parseChar) {
                    return value;
                }
                if (tempByte < 0) {
                    return -1;
                }
            }
        } else {
            return 0;
        }
    }

    private long getNumberFromStream(InputStream dbis, char parseChar) throws IOException {

        int tempByte = dbis.read();

        int pos = 0;

        while ((tempByte != parseChar) && (tempByte >= 0)) {
            numberChars[pos++] = (char) tempByte;
            if (pos == numberChars.length) {
                throw (new NumberFormatException("Number too large: " + new String(numberChars, 0, pos) + "..."));
            }
            tempByte = dbis.read();
        }

        //are we at the end of the stream?

        if (tempByte < 0) {

            return -1;

        } else if (pos == 0) {
            // support some borked impls that sometimes don't bother encoding anything

            return (0);
        }

        try {
            return (parseLong(numberChars, 0, pos));

        } catch (NumberFormatException e) {

            String temp = new String(numberChars, 0, pos);

            try {
                double d = Double.parseDouble(temp);

                long l = (long) d;

                Debug.out("Invalid number '" + temp + "' - decoding as " + l + " and attempting recovery");

                return (l);

            } catch (Throwable f) {
            }

            throw (e);
        }
    }

    // This is similar to Long.parseLong(String) source
    // It is also used in projects external to azureus2/azureus3 hence it is public
    public static long parseLong(char[] chars, int start, int length) {
        if (length > 0) {
            // Short Circuit: We don't support octal parsing, so if it 
            // starts with 0, it's 0
            if (chars[start] == '0') {

                return 0;
            }

            long result = 0;

            boolean negative = false;

            int i = start;

            long limit;

            if (chars[i] == '-') {

                negative = true;

                limit = Long.MIN_VALUE;

                i++;

            } else {
                // Short Circuit: If we are only processing one char,
                // and it wasn't a '-', just return that digit instead
                // of doing the negative junk
                if (length == 1) {
                    int digit = chars[i] - '0';

                    if (digit < 0 || digit > 9) {

                        throw new NumberFormatException(new String(chars, start, length));

                    } else {

                        return digit;
                    }
                }

                limit = -Long.MAX_VALUE;
            }

            int max = start + length;

            if (i < max) {

                int digit = chars[i++] - '0';

                if (digit < 0 || digit > 9) {

                    throw new NumberFormatException(new String(chars, start, length));

                } else {

                    result = -digit;
                }
            }

            long multmin = limit / 10;

            while (i < max) {

                // Accumulating negatively avoids surprises near MAX_VALUE

                int digit = chars[i++] - '0';

                if (digit < 0 || digit > 9) {

                    throw new NumberFormatException(new String(chars, start, length));
                }

                if (result < multmin) {

                    throw new NumberFormatException(new String(chars, start, length));
                }

                result *= 10;

                if (result < limit + digit) {

                    throw new NumberFormatException(new String(chars, start, length));
                }

                result -= digit;
            }

            if (negative) {

                if (i > start + 1) {

                    return result;

                } else { /* Only got "-" */

                    throw new NumberFormatException(new String(chars, start, length));
                }
            } else {

                return -result;
            }
        } else {

            throw new NumberFormatException(new String(chars, start, length));
        }

    }

    private byte[] getByteArrayFromStream(InputStream dbis, String context) throws IOException {
        int length = (int) getPositiveNumberFromStream(dbis, ':');

        if (length < 0) {
            return null;
        }

        // note that torrent hashes can be big (consider a 55GB file with 2MB pieces
        // this generates a pieces hash of 1/2 meg

        // aldenml: Note for android, we need to refactor this to allow fine control of memory allocation
        if (length > MAX_BYTE_ARRAY_SIZE) {
            throw (new IOException("Byte array length too large (" + length + ")"));
        }

        byte[] tempArray = new byte[length];

        getByteArrayFromStream(dbis, length, tempArray);

        return tempArray;
    }

    private void getByteArrayFromStream(InputStream dbis, int length, byte[] targetArray) throws IOException {

        int count = 0;
        int len = 0;
        //get the string
        while (count != length && (len = dbis.read(targetArray, count, length - count)) > 0)
            count += len;

        if (count != length)
            throw (new IOException("BDecoder::getByteArrayFromStream: truncated"));
    }

    public void setVerifyMapOrder(boolean b) {
        verify_map_order = b;
    }

    public void setRecoveryMode(boolean r) {
        recovery_mode = r;
    }

    public static void print(Object obj) {
        StringWriter sw = new StringWriter();

        PrintWriter pw = new PrintWriter(sw);

        print(pw, obj);

        pw.flush();

        System.out.println(sw.toString());
    }

    public static void print(PrintWriter writer, Object obj) {
        print(writer, obj, "", false);
    }

    private static void print(PrintWriter writer, Object obj, String indent, boolean skip_indent) {
        String use_indent = skip_indent ? "" : indent;

        if (obj instanceof Long) {

            writer.println(use_indent + obj);

        } else if (obj instanceof byte[]) {

            byte[] b = (byte[]) obj;

            if (b.length == 20) {
                writer.println(use_indent + " { " + ByteFormatter.nicePrint(b) + " }");
            } else if (b.length < 64) {
                writer.println(new String(b) + " [" + ByteFormatter.encodeString(b) + "]");
            } else {
                writer.println("[byte array length " + b.length);
            }

        } else if (obj instanceof String) {

            writer.println(use_indent + obj);

        } else if (obj instanceof List) {

            @SuppressWarnings("unchecked")
            List<Object> l = (List<Object>) obj;

            writer.println(use_indent + "[");

            for (int i = 0; i < l.size(); i++) {

                writer.print(indent + "  (" + i + ") ");

                print(writer, l.get(i), indent + "    ", true);
            }

            writer.println(indent + "]");

        } else {

            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) obj;

            Iterator<String> it = m.keySet().iterator();

            while (it.hasNext()) {

                String key = (String) it.next();

                if (key.length() > 256) {
                    writer.print(indent + key.substring(0, 256) + "... = ");
                } else {
                    writer.print(indent + key + " = ");
                }

                print(writer, m.get(key), indent + "  ", true);
            }
        }
    }

    /**
     * Converts any byte[] entries into UTF-8 strings.
     * REPLACES EXISTING MAP VALUES
     * 
     * @param map
     * @return
     */

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeStrings(Map<String, Object> map) {
        if (map == null) {

            return (null);
        }

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();

            Object value = entry.getValue();

            if (value instanceof byte[]) {

                try {
                    entry.setValue(new String((byte[]) value, "UTF-8"));

                } catch (Throwable e) {

                    System.err.println(e);
                }
            } else if (value instanceof Map) {

                decodeStrings((Map<String, Object>) value);
            } else if (value instanceof List) {

                decodeStrings((List<Object>) value);
            }
        }

        return (map);
    }

    /**
     * Decodes byte arrays into strings.  
     * REPLACES EXISTING LIST VALUES
     * 
     * @param list
     * @return the same list passed in
     */
    @SuppressWarnings("unchecked")
    public static List<Object> decodeStrings(List<Object> list) {
        if (list == null) {

            return (null);
        }

        for (int i = 0; i < list.size(); i++) {

            Object value = list.get(i);

            if (value instanceof byte[]) {

                try {
                    String str = new String((byte[]) value, "UTF-8");

                    list.set(i, str);

                } catch (Throwable e) {

                    System.err.println(e);
                }
            } else if (value instanceof Map) {

                decodeStrings((Map<String, Object>) value);

            } else if (value instanceof List) {

                decodeStrings((List<Object>) value);
            }
        }

        return (list);
    }

    private class BDecoderInputStreamArray extends InputStream {
        final private byte[] bytes;
        private int pos = 0;
        private int markPos;
        private int overPos;

        public BDecoderInputStreamArray(ByteBuffer buffer) {
            bytes = buffer.array();
            pos = buffer.arrayOffset() + buffer.position();
            overPos = pos + buffer.remaining();
        }

        private BDecoderInputStreamArray(byte[] _buffer) {
            bytes = _buffer;
            overPos = bytes.length;
        }

        private BDecoderInputStreamArray(byte[] _buffer, int _offset, int _length) {
            if (_offset == 0) {
                bytes = _buffer;
                overPos = _length;
            } else {
                bytes = _buffer;
                pos = _offset;
                overPos = Math.min(_offset + _length, bytes.length);
            }
        }

        public int read()

        throws IOException {
            if (pos < overPos) {
                return bytes[pos++] & 0xFF;
            }
            return -1;
        }

        public int read(byte[] buffer)

        throws IOException {
            return (read(buffer, 0, buffer.length));
        }

        public int read(byte[] b, int offset, int length)

        throws IOException {

            if (pos < overPos) {
                int toRead = Math.min(length, overPos - pos);
                System.arraycopy(bytes, pos, b, offset, toRead);
                pos += toRead;
                return toRead;
            }
            return -1;

        }

        public int available()

        throws IOException {
            return overPos - pos;
        }

        public boolean markSupported() {
            return (true);
        }

        public void mark(int limit) {
            markPos = pos;
        }

        public void reset()

        throws IOException {
            pos = markPos;
        }
    }
}
