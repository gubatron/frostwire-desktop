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
package de.http;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.log.Category;


/**
 * Similar to the UNIX sed
 */
public class SED {
    static Category log = Category.getInstance(SED.class);

    /**
     * Command Line Interface
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: org.apache.lenya.util.SED");
            return;
        }
    }

    /**
     * Substitute prefix, e.g. ".*world.*" by "universe"
     *
     * @param file File which sed shall be applied
     * @param prefixSubstitute Prefix which shall be replaced
     * @param substituteReplacement Prefix which is going to replace the original
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void replaceAll(File file, String substitute, String substituteReplacement) throws IOException {
        log.debug("Replace " + substitute + " by " + substituteReplacement);

        Pattern pattern = Pattern.compile(substitute);

        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(file);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int)fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

    // Decode the file into a char buffer
        // Charset and decoder for ISO-8859-15
        Charset charset = Charset.forName("ISO-8859-15");
        CharsetDecoder decoder = charset.newDecoder();
    CharBuffer cb = decoder.decode(bb);

        Matcher matcher = pattern.matcher(cb);
        String outString = matcher.replaceAll(substituteReplacement);
        log.debug(outString);


        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        PrintStream ps =new PrintStream(fos);
        ps.print(outString);
        ps.close();
        fos.close();
    }
}
