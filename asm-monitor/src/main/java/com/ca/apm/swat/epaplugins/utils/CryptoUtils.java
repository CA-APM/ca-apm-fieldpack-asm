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


public class CryptoUtils
{
  Cipher ecipher;
  Cipher dcipher;
  byte[] salt = { 
    -87, -101, -56, 50, 
    86, 53, -29, 3 };

  int iterationCount = 19;

  public CryptoUtils(String passPhrase)
  {
    try {
      PBEKeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), this.salt, this.iterationCount);
      SecretKey key = SecretKeyFactory.getInstance(EPAConstants.kAlgorithm).generateSecret(keySpec);
      this.ecipher = Cipher.getInstance(key.getAlgorithm());
      this.dcipher = Cipher.getInstance(key.getAlgorithm());

      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(this.salt, this.iterationCount);

      this.ecipher.init(1, key, paramSpec);
      this.dcipher.init(2, key, paramSpec);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    try {
      CryptoUtils encrypter = new CryptoUtils(EPAConstants.kMsgDigest);

      String encrypted = encrypter.encrypt(args[0]);
      System.out.println("Put this in your APMCloudMonitor.properties file:");
      System.out.println("apmcm.pass=" + encrypted);
      System.out.println("apmcm.pass.encrypted=true");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public String encrypt(String str)
  {
    try {
      byte[] utf8 = str.getBytes("UTF8");

      byte[] enc = this.ecipher.doFinal(utf8);

      return new Base64().encodeToString(enc);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String decrypt(String str)
  {
    try {
      byte[] dec = new Base64().decode(str);

      byte[] utf8 = this.dcipher.doFinal(dec);

      return new String(utf8, "UTF8");
    } catch (BadPaddingException localBadPaddingException) {
    } catch (IllegalBlockSizeException localIllegalBlockSizeException) {
    } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
    }
    return null;
  }
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.CryptoUtils
 * JD-Core Version:    0.6.0
 */