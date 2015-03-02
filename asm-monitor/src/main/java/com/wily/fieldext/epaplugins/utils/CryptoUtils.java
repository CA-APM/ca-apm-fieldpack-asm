/*    */ package com.wily.fieldext.epaplugins.utils;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import java.security.spec.AlgorithmParameterSpec;

/*    */ import javax.crypto.BadPaddingException;
/*    */ import javax.crypto.Cipher;
/*    */ import javax.crypto.IllegalBlockSizeException;
/*    */ import javax.crypto.SecretKey;
/*    */ import javax.crypto.SecretKeyFactory;
/*    */ import javax.crypto.spec.PBEKeySpec;
/*    */ import javax.crypto.spec.PBEParameterSpec;

/*    */ import sun.misc.BASE64Decoder;
/*    */ import sun.misc.BASE64Encoder;
/*    */ 
/*    */ 
/*    */ public class CryptoUtils
/*    */ {
/*    */   Cipher ecipher;
/*    */   Cipher dcipher;
/* 15 */   byte[] salt = { 
/* 16 */     -87, -101, -56, 50, 
/* 17 */     86, 53, -29, 3 };
/*    */ 
/* 21 */   int iterationCount = 19;
/*    */ 
/*    */   public CryptoUtils(String passPhrase)
/*    */   {
/*    */     try {
/* 26 */       PBEKeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), this.salt, this.iterationCount);
/* 27 */       SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
/* 28 */       this.ecipher = Cipher.getInstance(key.getAlgorithm());
/* 29 */       this.dcipher = Cipher.getInstance(key.getAlgorithm());
/*    */ 
/* 32 */       AlgorithmParameterSpec paramSpec = new PBEParameterSpec(this.salt, this.iterationCount);
/*    */ 
/* 35 */       this.ecipher.init(1, key, paramSpec);
/* 36 */       this.dcipher.init(2, key, paramSpec);
/*    */     } catch (Exception e) {
/* 38 */       e.printStackTrace();
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void main(String[] args)
/*    */   {
/*    */     try {
/* 45 */       CryptoUtils encrypter = new CryptoUtils("1D0NTF33LT4RDY");
/*    */ 
/* 48 */       String encrypted = encrypter.encrypt(args[0]);
/* 49 */       System.out.println("Put this in your APMCloudMonitor.properties file:");
/* 50 */       System.out.println("apmcm.pass=" + encrypted);
/* 51 */       System.out.println("apmcm.pass.encrypted=true");
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 57 */       e.printStackTrace();
/*    */     }
/*    */   }
/*    */ 
/*    */   public String encrypt(String str)
/*    */   {
/*    */     try {
/* 64 */       byte[] utf8 = str.getBytes("UTF8");
/*    */ 
/* 67 */       byte[] enc = this.ecipher.doFinal(utf8);
/*    */ 
/* 70 */       return new BASE64Encoder().encode(enc);
/*    */     } catch (Exception e) {
/* 72 */       e.printStackTrace();
/*    */     }
/* 74 */     return null;
/*    */   }
/*    */ 
/*    */   public String decrypt(String str)
/*    */   {
/*    */     try {
/* 80 */       byte[] dec = new BASE64Decoder().decodeBuffer(str);
/*    */ 
/* 83 */       byte[] utf8 = this.dcipher.doFinal(dec);
/*    */ 
/* 86 */       return new String(utf8, "UTF8");
/*    */     } catch (BadPaddingException localBadPaddingException) {
/*    */     } catch (IllegalBlockSizeException localIllegalBlockSizeException) {
/*    */     } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
/*    */     } catch (IOException localIOException) {
/*    */     }
/* 92 */     return null;
/*    */   }
/*    */ }

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.CryptoUtils
 * JD-Core Version:    0.6.0
 */