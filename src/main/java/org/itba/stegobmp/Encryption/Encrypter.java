package org.itba.stegobmp.Encryption;

import org.itba.stegobmp.StegoUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

public abstract class Encrypter {
    private String name;
    protected final int keyLen;
    protected int ivLen;
    protected final EncryptionModes encMode;
    protected final String password;
    protected static final int ITERATIONS = 10000;
    protected static final int BLOCK_SIZE = 16;

    public Encrypter(String name, int keyLenBits, int ivLen, EncryptionModes encryptionMode, String password) {
        this.name = name;
        this.keyLen = keyLenBits;
        this.ivLen = ivLen;
        this.password = password;
        this.encMode = encryptionMode;
    }

    public byte[] encrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);

        // Se cifra
        byte[] cipheredText = cipher.doFinal(input);
        int cipherLen = cipheredText.length;
        int paddedLen = cipherLen + ((encMode.needsPadding() && cipherLen % BLOCK_SIZE != 0)? (BLOCK_SIZE - (cipherLen % BLOCK_SIZE)) : 0);
        byte[] lenBytes = StegoUtils.toByteArray(paddedLen);

        byte[] paddedCipheredText = new byte[4 + paddedLen];
        System.arraycopy(lenBytes, 0, paddedCipheredText, 0, 4);
        System.arraycopy(cipheredText, 0, paddedCipheredText, 4, cipherLen);
        return paddedCipheredText;
    }

    public byte[] decrypt(byte[] ciphertext) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        return getCipher(Cipher.DECRYPT_MODE).doFinal(ciphertext);
    }

    protected Cipher getCipher(int mode) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        AES.KeyIV ki = generateKeyIV(password);
        final String transformation = name + "/" + encMode.getName() + "/" + (encMode.needsPadding() ? "PKCS5Padding" : "NoPadding");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, ki.key(), ki.iv());
        return cipher;
    }

    protected int getIVLen() {
        return this.encMode.needsIV()? ivLen : 0;
    }

    protected KeyIV generateKeyIV(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[]{0,0,0,0,0,0,0,0};

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, keyLen + getIVLen());
        byte[] randBytes = skf.generateSecret(spec).getEncoded();

        byte[] keyBytes = copyOf(randBytes, keyLen / 8);

        Key key = new SecretKeySpec(keyBytes, name);
        IvParameterSpec iv = null;
        if (getIVLen() != 0) {
            byte[] ivBytes = copyOfRange(randBytes, keyLen / 8, randBytes.length);
            iv = new IvParameterSpec(ivBytes);
        }

        return new KeyIV(key, iv);
    }

    protected record KeyIV(Key key, IvParameterSpec iv) {}
}
