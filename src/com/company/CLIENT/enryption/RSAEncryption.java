package com.company.CLIENT.enryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Class for RSA encryption
 * Is used to encrypt session keys
 */
public class RSAEncryption {

    /**
     * Generates a key pair
     *
     * @return private-public key pair
     */
    public static KeyPair getKeyPair() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    /**
     * Encrypts string using public key
     *
     * @param msg message to ncrypt
     * @param key public key that is used to encrypt the message
     * @return encrypted message
     */
    public static String encryptText(String msg, PublicKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, key);

        return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
    }

    /**
     * Decrypts a messge using private key
     *
     * @param msg messge to decrypt
     * @param key private key that is used to decrypt the message
     * @return decrypted  message
     */
    public static String decryptText(String msg, PrivateKey key)
            throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(msg)), "UTF-8");
    }

    /**
     * Retrieves a public key from string
     *
     * @param publicKeyString string containing a public key
     * @return public key
     */
    public static PublicKey getPublicKeyFromString(String publicKeyString) {
        //converting string to Bytes
        byte[] bytePublicKey = Base64.getDecoder().decode(publicKeyString);

        //converting it back to public key
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        PublicKey publicKey = null;
        try {
            publicKey = factory.generatePublic(new X509EncodedKeySpec(bytePublicKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }
}
