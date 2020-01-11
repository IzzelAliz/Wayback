package com.ilummc.wayback.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class Crypto {

    public static String encrypt(String content, String password) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
            SecretKey secretKey = keyGen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new String(Base64.getEncoder().encode(cipher.doFinal(byteContent)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public static String decrypt(String str, String password) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
            SecretKey secretKey = keyGen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
