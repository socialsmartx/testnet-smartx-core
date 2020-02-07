package com.smartx.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.smartx.core.consensus.SatException;

/**
 @author Tenpey(Tencent) */
public class TenpaySecurity {
    // Digest Algorithm
    private static final String DigestAlgorithm = "SHA-1";
    // Default read buffer length
    private static final int DefaultBufferLength = 1024;
    private static final String DefaultCipherAlgorithm = "DESede";
    private static final String DESCipherAlgorighm = "DES/CBC/NoPadding";
    /**
     *
     */
    private static TenpaySecurity instance;
    protected static Logger logger = Logger.getLogger(TenpaySecurity.class);
    /**
     *
     */
    public static TenpaySecurity getInstance() {
        if (instance == null) {
            instance = new TenpaySecurity();
        }
        return instance;
    }
    public static String md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) i += 256;
                if (i < 16) buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            String str = buf.toString();
            return str;
            //System.out.println("result: " + buf.toString());//
            //System.out.println("result: " + buf.toString().substring(8, 24));//
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    /**
     @param originalText
     @return MD5ժҪ
     @throws SatException
     */
    public byte[] computeMD5Digest(String originalText) throws SatException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return messageDigest.digest(originalText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            logger.info(String.format("error", originalText), e);
            throw new SatException("1001", "error");
        }
    }
    /**
     @return MD5ժҪ
     @throws SatException
     */
    public byte[] computeMD5Digest(byte[] originalbytes) throws SatException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return messageDigest.digest(originalbytes);
        } catch (NoSuchAlgorithmException e) {
            logger.info(String.format("error"), e);
            throw new SatException(1000, "error");
        }
    }
    /**
     @param originalText
     @return
     @throws SatException
     */
    public String computerMD5DigestString(String originalText) throws SatException {
        byte[] md5Digest = this.computeMD5Digest(originalText);
        return Base64.encodeBase64String(md5Digest).trim();
    }
    /**
     @param filename
     @return �ļ�ժҪ
     @throws SatException
     */
    public String computeFileDigest(String filename) throws SatException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(TenpaySecurity.DigestAlgorithm);
            FileInputStream file = new FileInputStream(filename);
            int bytesRead = 0;
            byte[] bytesBuffer = new byte[TenpaySecurity.DefaultBufferLength];
            while ((bytesRead = file.read(bytesBuffer)) != -1) {
                messageDigest.update(bytesBuffer, 0, bytesRead);
            }
            Base64 encoder = new Base64(0);
            return encoder.encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new SatException(1000, String.format("error", filename));
        } catch (FileNotFoundException e) {
            throw new SatException(1000, String.format("error", filename));
        } catch (IOException e) {
            throw new SatException(1000, String.format("error", filename));
        }
    }
    /**
     @param sourceData
     @return ժҪ
     @throws SatException
     */
    public byte[] computeBytesDigest(byte[] sourceData) throws SatException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(TenpaySecurity.DigestAlgorithm);
            return messageDigest.digest(sourceData);
        } catch (NoSuchAlgorithmException e) {
            throw new SatException(1000, "error");
        }
    }
    public String base64Encode(String originalText) {
        StringBuffer requestTextBuffer = new StringBuffer();
        for (int index = 0; index < originalText.length(); index++) {
            char currentChar = originalText.charAt(index);
            switch (currentChar) {
                case '/':
                    requestTextBuffer.append('_');
                    break;
                case '+':
                    requestTextBuffer.append('-');
                    break;
                case '-':
                    requestTextBuffer.append('+');
                    break;
                default:
                    requestTextBuffer.append(currentChar);
                    break;
            }
        }
        return requestTextBuffer.toString();
    }
    /**
     @return
     @throws SatException
     */
    public String computerSha1DigestString(String originalText) throws SatException {
        byte[] sha1Digest = this.computeBytesDigest(originalText.getBytes());
        return Base64.encodeBase64String(sha1Digest).trim();
    }
    /**
     @throws SatException
     */
    public String encrypt3DES(String clearText, String key) throws SatException {
        Base64 codec = new Base64(0 /*The output will not be divided into lines*/);
        byte[] clearbytes = clearText.getBytes();
        byte[] keybytes = codec.decode(key);
        byte[] cipherbytes = this.encrypt3DES(clearbytes, keybytes);
        return codec.encodeToString(cipherbytes);
    }
    /**
     @throws SatException
     */
    public String decrypt3DES(String cipherText, String key) throws SatException {
        Base64 codec = new Base64(0 /*The output will not be divided into lines*/);
        byte[] cipherbytes = codec.decode(cipherText);
        byte[] keybytes = codec.decode(key);
        byte[] clearbytes = this.decrypt3DES(cipherbytes, keybytes);
        return new String(clearbytes);
    }
    /**
     @param clearbytes
     @param key
     @return
     @throws SatException
     */
    public byte[] encrypt3DES(byte[] clearbytes, byte[] key) throws SatException {
        try {
            DESedeKeySpec skSpec = new DESedeKeySpec(key);
            SecretKeyFactory skFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey sKey = skFactory.generateSecret(skSpec);
            Cipher encCipher = Cipher.getInstance(TenpaySecurity.DefaultCipherAlgorithm);
            encCipher.init(Cipher.ENCRYPT_MODE, sKey);
            return encCipher.doFinal(clearbytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (NoSuchPaddingException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (InvalidKeySpecException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (InvalidKeyException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (IllegalBlockSizeException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (BadPaddingException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        }
    }
    /**
     @param cipherbytes
     @param key
     @throws SatException
     */
    public byte[] decrypt3DES(byte[] cipherbytes, byte[] key) throws SatException {
        try {
            DESedeKeySpec skSpec = new DESedeKeySpec(key);
            SecretKeyFactory skFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey sKey = skFactory.generateSecret(skSpec);
            Cipher encCipher = Cipher.getInstance(TenpaySecurity.DefaultCipherAlgorithm);
            encCipher.init(Cipher.DECRYPT_MODE, sKey);
            return encCipher.doFinal(cipherbytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (NoSuchPaddingException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (InvalidKeySpecException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (InvalidKeyException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (IllegalBlockSizeException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        } catch (BadPaddingException e) {
            logger.error(String.format("error", e.toString()));
            throw new SatException(1000, "error");
        }
    }
    /**
     @param clearbytes
     @param key
     @param iv
     @throws SatException
     */
    public byte[] encryptDES(byte[] clearbytes, byte[] key, byte[] iv) throws SatException {
        try {
            DESKeySpec skSpec = new DESKeySpec(key);
            SecretKeyFactory skFactory = SecretKeyFactory.getInstance("DES");
            SecretKey sKey = skFactory.generateSecret(skSpec);
            Cipher encCipher = Cipher.getInstance(TenpaySecurity.DESCipherAlgorighm);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            encCipher.init(Cipher.ENCRYPT_MODE, sKey, ivSpec);
            // Padding with zeros if necessary.
            byte[] clearbytesWithPadding = null;
            if (clearbytes.length % 8 > 0) {
                int length = clearbytes.length + 8 - clearbytes.length % 8;
                // Copies the specified array, padding with zeros so the copy has the specified length.
                clearbytesWithPadding = Arrays.copyOf(clearbytes, length);
            } else {
                clearbytesWithPadding = clearbytes;
            }
            return encCipher.doFinal(clearbytesWithPadding);
        } catch (NoSuchAlgorithmException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (NoSuchPaddingException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidKeySpecException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidKeyException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (IllegalBlockSizeException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (BadPaddingException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidAlgorithmParameterException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        }
    }
    /**
     @param cipherbytes
     @param key
     @param iv
     @throws SatException
     */
    public byte[] decryptDES(byte[] cipherbytes, byte[] key, byte[] iv) throws SatException {
        try {
            DESKeySpec skSpec = new DESKeySpec(key);
            SecretKeyFactory skFactory = SecretKeyFactory.getInstance("DES");
            SecretKey sKey = skFactory.generateSecret(skSpec);
            Cipher encCipher = Cipher.getInstance(TenpaySecurity.DESCipherAlgorighm);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            encCipher.init(Cipher.DECRYPT_MODE, sKey, ivSpec);
            return encCipher.doFinal(cipherbytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (NoSuchPaddingException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidKeySpecException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidKeyException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (IllegalBlockSizeException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (BadPaddingException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        } catch (InvalidAlgorithmParameterException e) {
            logger.error("error", e);
            throw new SatException(1000, "error");
        }
    }
}

