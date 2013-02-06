package com.frostwire.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class DigestUtils {
    /**
     * Returns true if the MD5 of the file corresponds to the given MD5 string.
     * It works with lowercase or uppercase, you don't need to worry about that.
     * 
     * @param f
     * @param expectedMD5
     * @return
     * @throws Exception
     */
    public final static boolean checkMD5(File f, String expectedMD5) throws Exception {
        String md5 = getMD5(f).trim();
        return compareMD5(md5, expectedMD5);
    }
    
    public final static boolean compareMD5(String md5a, String md5b) throws Exception {
        if (!isValidMD5(md5a)) {
            throw new IllegalArgumentException("Invalid md5a");
        }
        
        if (!isValidMD5(md5b)) {
            throw new IllegalArgumentException("Invalid md5b");
        }

        return md5a.equalsIgnoreCase(md5b);
    }

    private static boolean isValidMD5(String md5)  {
        if (md5 == null) {
            return false;
        }

        return (md5.length() == 32);
    }

    public final static String getMD5(File f) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");

        byte[] buf = new byte[4096];
        int num_read;

        InputStream in = new BufferedInputStream(new FileInputStream(f));

        while ((num_read = in.read(buf)) != -1) {
            m.update(buf, 0, num_read);
        }

        in.close();

        String result = new BigInteger(1, m.digest()).toString(16);

        //pad with zeros if until it's 32 chars long.
        if (result.length() < 32) {
            int paddingSize = 32 - result.length();
            for (int i = 0; i < paddingSize; i++) {
                result = "0" + result;
            }
        }

        return result;
    }
}
