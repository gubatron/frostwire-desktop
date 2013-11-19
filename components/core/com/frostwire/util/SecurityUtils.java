package com.frostwire.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {

    private static String testBase32PKCS8PrivateKey = "GCBACSYCAEADBAQBFQDAOKUGJDHDQBABGCBACHYCQGAQB7L7KOAR25ISFFJN6SU4F3WOJZ7WCG3VEPHPIQAMGHR7QC3FCJTJIVOUAISR7NMT3DKY7K74L5N2GD3MXG2VNTLYCO4ADU2G74TGMC3WXGKQUWSJ7H7IAR5RAIWCJ652TV76W7DBX6B3K7T4NKFGCUHQJ64D63J4KHWDAI2VIE22C2ITF5TV6OXCWYOXFLX7EIQDDGO5CSABY4BBKAEXMBII6FJDBPGLFEVZQKROXBAL6BMBZ5ICQGAQB57BUCC5NGZ533F3ZK24G24FPOLZSSX3X6R25KBPSV2MBM6QPATHKFMVPDV22RMU7ZTRA4IIDAFUJELHCI7IJQUBME5XZ4ETFDGIU3QTYFT2RNKHZDJI4CR24HRLWOTHLELOUN7QX6RBGVRPD63CPIASIO6MUTY35KCRSCE2RA674FNOLHYGSKFWMXUAPNKSKZABJQ575T2JFICBMAQUFLOU5ZQ7SIHYASFZQVSXWDHKASBXQELR";
    private static String testBase32X509PublicKey = "GCBADNZQQIASYBQHFKDERTRYAQATBAQBD4BIDAIA7V7VHAI5OUJCSUW7JKOC53HE473BDN2SHTXUIAGDDY7YBNSREZUUKXKAEJI7WWJ5RVMPVP6F6W5DB5WLTNKWZV4BHOAB2NDP6JTGBN3LTFIKLJE7T7UAI6YQELBE7O5J277LPRQ37A5VPZ6GVCTBKDYE7OB7NU6FD3BQENKUCNNBNEJS6Z27HLRLMHLSV37SEIBRTHORJAA4OAQVACLWAUEPCURQXTFSSK4YFIXLQQF7AWA46UBIDAIA67Q2BBOWTM655S54VNODNOCXXF4ZJL537I5OVAXZK5GAWPIHQJTVCWKXR25NIWKP4ZYQOEEBQC2ESFTREPUEYKAWCO346CJSRTEKNYJ4CZ5IWVD4RUUOBI5ODYV3HJTVSFXKG7YL7IQTKYXR7NRHUAJEHPGKJ4N6VBIZBCNIQPP6CWXFT4DJFC3GL2AHWVJFMQAUYO76Z5ESUA4BQQAAFAMAPOSFUHKE7LZUBIODLBCGDKQ7MDNEANEPIEIDOKUTIJZHXVAALXH52CEFBTO56O7AGRF75BNV7HEULWRWL46NTLBCBZAILDXV2DLTNRUCCJFCTT4UGUII42WIFSH3JGQ6M6EWTLWA4ZEJ7C46LSFY2GBQCNSWP5LUG423DFZRS3SO5XZMOD274ANXANJP5T2FPCCQJOYXF74US";

    static {
        java.security.Security.addProvider(new com.sun.crypto.provider.SunJCE());
    }

    public static PrivateKey getPrivateKey(String dsaBase32PKCS8Key) {
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base32.decode(dsaBase32PKCS8Key));
            privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static PublicKey getPublicKey(String dsaBase32X509Key) {
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            X509EncodedKeySpec x509EncodeKeySpec = new X509EncodedKeySpec(Base32.decode(dsaBase32X509Key));
            publicKey = keyFactory.generatePublic(x509EncodeKeySpec);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static String sign(byte[] data, PrivateKey privateKey) {
        return null;
    }

    /**
     * Generates and prints a DSA keypair and encodes them in base32.
     * The Private Key comes in PKCS#8 format.
     * The Public Key comes in X.509 format.
     */
    public static void generateKeyPairInBase32() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("DSA");
            keyGenerator.initialize(1024);
            KeyPair keyPair = keyGenerator.generateKeyPair();

            //DSA, PKCS#8 format
            PrivateKey privateKey = keyPair.getPrivate();

            //DSA, X.509 format
            PublicKey publicKey = keyPair.getPublic();

            byte[] encodedPrivateKey = privateKey.getEncoded();
            byte[] encodedPublicKey = publicKey.getEncoded();

            String base32PrivateKey = Base32.encode(encodedPrivateKey);
            String base32PublicKey = Base32.encode(encodedPublicKey);

            System.out.println("Private Key (" + encodedPrivateKey.length + " bytes " + privateKey.getFormat() + " " + privateKey.getAlgorithm() + ")");
            System.out.println(base32PrivateKey);

            System.out.println("Public Key (" + encodedPublicKey.length + " bytes " + publicKey.getFormat() + " " + publicKey.getAlgorithm() + ")");
            System.out.println(base32PublicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static boolean testKeyInstantiationFromBase32Strings(String privKey, String pubKey) {
        PrivateKey privateKey = getPrivateKey(privKey);
        PublicKey publicKey = getPublicKey(pubKey);

        return Base32.encode(privateKey.getEncoded()).equals(testBase32PKCS8PrivateKey) && Base32.encode(publicKey.getEncoded()).equals(testBase32X509PublicKey);
    }

    public static void main(String[] args) {
        System.out.println("testKeyInstantiationFromBase32Strings -> " + testKeyInstantiationFromBase32Strings(testBase32PKCS8PrivateKey,testBase32X509PublicKey));
    }
}