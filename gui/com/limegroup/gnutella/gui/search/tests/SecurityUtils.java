package com.limegroup.gnutella.gui.search.tests;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {

    public static final String DHT_PUBLIC_KEY = "GCBADOBQQIASYBQHFKDERTRYAQATBAQBD4BIDAIA7V7VHAI5OUJCSUW7JKOC53HE473BDN2SHTXUIAGDDY7YBNSREZUUKXKAEJI7WWJ5RVMPVP6F6W5DB5WLTNKWZV4BHOAB2NDP6JTGBN3LTFIKLJE7T7UAI6YQELBE7O5J277LPRQ37A5VPZ6GVCTBKDYE7OB7NU6FD3BQENKUCNNBNEJS6Z27HLRLMHLSV37SEIBRTHORJAA4OAQVACLWAUEPCURQXTFSSK4YFIXLQQF7AWA46UBIDAIA67Q2BBOWTM655S54VNODNOCXXF4ZJL537I5OVAXZK5GAWPIHQJTVCWKXR25NIWKP4ZYQOEEBQC2ESFTREPUEYKAWCO346CJSRTEKNYJ4CZ5IWVD4RUUOBI5ODYV3HJTVSFXKG7YL7IQTKYXR7NRHUAJEHPGKJ4N6VBIZBCNIQPP6CWXFT4DJFC3GL2AHWVJFMQAUYO76Z5ESUA4BQUAAFAMBADY564OTTNZX2KTYE4D3FK7BYVK5WH2OAOP3XG5W5NY3KHIAJYE5L2JL4QJS3EN2FU6L6NE56Z2S5C4KWM2YSJNFMRKJ7SIPPTIOGK4PLKRI26EGH62CVSP266NBCF4GOMYRZDUZHVDALYE2QR7QZ4Z2ODG6LF45PQS5YRISNHA4L7UIRC76OKGLBXT45HGWC5MZCKJTC2QHPPQ";

    private static final String ENCRYPTION_ALGORITHM = "DSA";
    private static String testBase32PKCS8PrivateKey = "GCBACSYCAEADBAQBFQDAOKUGJDHDQBABGCBACHYCQGAQB7L7KOAR25ISFFJN6SU4F3WOJZ7WCG3VEPHPIQAMGHR7QC3FCJTJIVOUAISR7NMT3DKY7K74L5N2GD3MXG2VNTLYCO4ADU2G74TGMC3WXGKQUWSJ7H7IAR5RAIWCJ652TV76W7DBX6B3K7T4NKFGCUHQJ64D63J4KHWDAI2VIE22C2ITF5TV6OXCWYOXFLX7EIQDDGO5CSABY4BBKAEXMBII6FJDBPGLFEVZQKROXBAL6BMBZ5ICQGAQB57BUCC5NGZ533F3ZK24G24FPOLZSSX3X6R25KBPSV2MBM6QPATHKFMVPDV22RMU7ZTRA4IIDAFUJELHCI7IJQUBME5XZ4ETFDGIU3QTYFT2RNKHZDJI4CR24HRLWOTHLELOUN7QX6RBGVRPD63CPIASIO6MUTY35KCRSCE2RA674FNOLHYGSKFWMXUAPNKSKZABJQ575T2JFICBMAQUFLOU5ZQ7SIHYASFZQVSXWDHKASBXQELR";
    private static String testBase32X509PublicKey = "GCBADNZQQIASYBQHFKDERTRYAQATBAQBD4BIDAIA7V7VHAI5OUJCSUW7JKOC53HE473BDN2SHTXUIAGDDY7YBNSREZUUKXKAEJI7WWJ5RVMPVP6F6W5DB5WLTNKWZV4BHOAB2NDP6JTGBN3LTFIKLJE7T7UAI6YQELBE7O5J277LPRQ37A5VPZ6GVCTBKDYE7OB7NU6FD3BQENKUCNNBNEJS6Z27HLRLMHLSV37SEIBRTHORJAA4OAQVACLWAUEPCURQXTFSSK4YFIXLQQF7AWA46UBIDAIA67Q2BBOWTM655S54VNODNOCXXF4ZJL537I5OVAXZK5GAWPIHQJTVCWKXR25NIWKP4ZYQOEEBQC2ESFTREPUEYKAWCO346CJSRTEKNYJ4CZ5IWVD4RUUOBI5ODYV3HJTVSFXKG7YL7IQTKYXR7NRHUAJEHPGKJ4N6VBIZBCNIQPP6CWXFT4DJFC3GL2AHWVJFMQAUYO76Z5ESUA4BQQAAFAMAPOSFUHKE7LZUBIODLBCGDKQ7MDNEANEPIEIDOKUTIJZHXVAALXH52CEFBTO56O7AGRF75BNV7HEULWRWL46NTLBCBZAILDXV2DLTNRUCCJFCTT4UGUII42WIFSH3JGQ6M6EWTLWA4ZEJ7C46LSFY2GBQCNSWP5LUG423DFZRS3SO5XZMOD274ANXANJP5T2FPCCQJOYXF74US";

    public static PrivateKey getPrivateKey(String dsaBase32PKCS8Key) {
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
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
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            X509EncodedKeySpec x509EncodeKeySpec = new X509EncodedKeySpec(Base32.decode(dsaBase32X509Key));
            publicKey = keyFactory.generatePublic(x509EncodeKeySpec);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static SignedMessage sign(byte[] data, PrivateKey privateKey) {
        return sign(data, 0, data.length, privateKey);
    }

    public static SignedMessage sign(byte[] unsignedData, int offset, int len, final PrivateKey privateKey) {
        SignedMessage signedMessage = null;
        try {
            Signature signature = Signature.getInstance(ENCRYPTION_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(unsignedData, offset, len);
            signedMessage = new SignedMessage(unsignedData, signature.sign());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return signedMessage;
    }

    public static boolean verify(SignedMessage signedMessage, final PublicKey publicKey) {
        return verify(signedMessage.unsignedData, 0, signedMessage.unsignedData.length, signedMessage.signature, publicKey);
    }

    public static boolean verify(byte[] data, int offset, int len, byte[] signature, final PublicKey publicKey) {
        boolean verified = false;
        try {
            Signature verifier = Signature.getInstance(ENCRYPTION_ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(data, offset, len);
            verified = verifier.verify(signature, 0, signature.length);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return verified;
    }

    /**
     * Generates and prints a DSA keypair and encodes them in base32.
     * The Private Key comes in PKCS#8 format.
     * The Public Key comes in X.509 format.
     */
    @SuppressWarnings("unused")
    private static void generateKeyPairInBase32() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM);
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

    @SuppressWarnings("unused")
    private static boolean testKeyInstantiationFromBase32Strings(String privKey, String pubKey) {
        PrivateKey privateKey = getPrivateKey(privKey);
        PublicKey publicKey = getPublicKey(pubKey);

        return Base32.encode(privateKey.getEncoded()).equals(testBase32PKCS8PrivateKey) && Base32.encode(publicKey.getEncoded()).equals(testBase32X509PublicKey);
    }

    @SuppressWarnings("unused")
    private static boolean testSignatureAndVerification(String signThis, String privKey, String pubKey) {
        PrivateKey privateKey = getPrivateKey(privKey);
        PublicKey publicKey = getPublicKey(pubKey);

        SignedMessage signedMessage = null;
        byte[] bytesToSign = null;
        try {
            bytesToSign = signThis.getBytes("utf8");
            signedMessage = sign(bytesToSign, privateKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //System.out.println("Signed String in base32:\n" + signedMessage.base32DataString);
        //System.out.println("Base 32 decoded String: " + new String(Base32.decode(signedMessage.base32DataString)));
        //System.out.println("Signature:\n" + Base32.encode(signedMessage.signature));

        boolean verified = verify(signedMessage, publicKey);
        //System.out.println("Verify? " + verified);

        //hack the message, verification must fail
        signedMessage.signature[10] = 0x23;
        signedMessage.signature[11] = 1;
        signedMessage.signature[12] = 2;
        signedMessage.signature[18] = 3;
        boolean integrityFail = verify(signedMessage, publicKey);
        //System.out.println("Verify tainted message? " + integrityFail);

        return verified && !integrityFail;
    }

    public static void main(String[] args) {
        //tests
        //System.out.println("testKeyInstantiationFromBase32Strings -> " + testKeyInstantiationFromBase32Strings(testBase32PKCS8PrivateKey, testBase32X509PublicKey));
        //System.out.println("testSignatureAndVerification -> " + testSignatureAndVerification("The quick brown fox jump over the lazy dog.", testBase32PKCS8PrivateKey, testBase32X509PublicKey));
        //generateKeyPairInBase32();
    }
}