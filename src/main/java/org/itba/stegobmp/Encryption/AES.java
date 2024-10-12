package org.itba.stegobmp.Encryption;
import org.itba.stegobmp.StegoUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

public class AES implements Encrypter {

    private final int keyLen;
    private static final int IV_LEN = 128;
    private final EncryptionModes encMode;
    private final String password;
    private static final int ITERATIONS = 10000;
    private static final int BLOCK_SIZE = 16;

    public AES(int keyLenBits, EncryptionModes encryptionMode, String password) {
        if (keyLenBits != 128 && keyLenBits != 192 && keyLenBits != 256)
            throw new RuntimeException("Invalid AES bytes");
        this.keyLen = keyLenBits;
        this.password = password;
        this.encMode = encryptionMode;
    }

    @Override
    public byte[] encrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        KeyIV ki = generateKeyIV(password);

        // Se genera instancia de Cipher
        final String transformation = "AES" + "/" + encMode.getName() + "/" + (encMode.needsPadding() ? "PKCS5Padding" : "NoPadding");

        Cipher aesCipher = Cipher.getInstance(transformation);

        // Se inicializa el cifrador para poder cifrar con la clave
        aesCipher.init(Cipher.ENCRYPT_MODE, ki.key, ki.iv);

        // Se cifra
        byte[] cipheredText = aesCipher.doFinal(input);
        int cipherLen = cipheredText.length;
        int paddedLen = cipherLen + (cipherLen % BLOCK_SIZE != 0? (BLOCK_SIZE - (cipherLen % BLOCK_SIZE)) : 0);
        byte[] lenBytes = StegoUtils.toByteArray(paddedLen);

        byte[] paddedCipheredText = new byte[4 + paddedLen];
        System.arraycopy(lenBytes, 0, paddedCipheredText, 0, 4);
        System.arraycopy(cipheredText, 0, paddedCipheredText, 4, cipherLen);
        return paddedCipheredText;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        KeyIV ki = generateKeyIV(password);

        final String transformation = "AES" + "/" + encMode.getName() + "/" + (encMode.needsPadding() ? "PKCS5Padding" : "NoPadding");

        Cipher aesCipher = Cipher.getInstance(transformation);

        aesCipher.init(Cipher.DECRYPT_MODE, ki.key, ki.iv);

        return aesCipher.doFinal(ciphertext);
    }

    public int getIVLen() {
        return this.encMode.needsIV()? IV_LEN : 0;
    }


    public KeyIV generateKeyIV(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[]{0,0,0,0,0,0,0,0};

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, keyLen + getIVLen());
        byte[] randBytes = skf.generateSecret(spec).getEncoded();

        byte[] keyBytes = copyOf(randBytes, keyLen / 8);

        Key key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = null;
        if (getIVLen() != 0) {
            byte[] ivBytes = copyOfRange(randBytes, keyLen / 8, randBytes.length);
            iv = new IvParameterSpec(ivBytes);
        }

        return new KeyIV(key, iv);
    }

    public record KeyIV(Key key, IvParameterSpec iv) {}


}
