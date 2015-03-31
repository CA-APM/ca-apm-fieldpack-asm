package com.ca.apm.swat.epaplugins.utils;

import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Helper class for encryption.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class CryptoUtils implements AsmProperties {
    private Cipher ecipher;
    private Cipher dcipher;
    private byte[] salt = { -87, -101, -56, 50, 86, 53, -29, 3 };

    private int iterationCount = 19;
  
    public static final String UTF8 = "UTF8";
    /**
     * Create new helper class for encryption.
     * @param passPhrase pass phrase
     */
    public CryptoUtils(String passPhrase) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(),
                this.salt, this.iterationCount);
            SecretKey key = SecretKeyFactory.getInstance(kAlgorithm).generateSecret(keySpec);
            this.ecipher = Cipher.getInstance(key.getAlgorithm());
            this.dcipher = Cipher.getInstance(key.getAlgorithm());

            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(this.salt, this.iterationCount);

            this.ecipher.init(1, key, paramSpec);
            this.dcipher.init(2, key, paramSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method.
     * @param args arguments
     */
    public static void main(String[] args) {
        try {
            CryptoUtils encrypter = new CryptoUtils(MESSAGE_DIGEST);

            String encrypted = encrypter.encrypt(args[0]);
            System.out.println(AsmMessages.getMessage(AsmMessages.PUT_PW_IN_PROPERTIES,
                PROPERTY_FILE_NAME));
            System.out.println(PASSWORD + '=' + encrypted);
            System.out.println(PASSWORD_ENCRYPTED + '=' + TRUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt a string.
     * @param str string to encrypt
     * @return encrypted string
     */
    public String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes(UTF8);

            byte[] enc = this.ecipher.doFinal(utf8);

            return new Base64().encodeToString(enc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt a string.
     * @param str string to decrypt
     * @return decrypted string
     */
    public String decrypt(String str) {
        try {
            byte[] dec = new Base64().decode(str);

            byte[] utf8 = this.dcipher.doFinal(dec);

            return new String(utf8, UTF8);
        } catch (BadPaddingException localBadPaddingException) {
            // ignore
        } catch (IllegalBlockSizeException localIllegalBlockSizeException) {
            // ignore
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
            // ignore
        }
        return null;
    }
}
