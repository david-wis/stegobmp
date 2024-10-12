package org.itba.stegobmp.Encryption;

public class AES extends Encrypter {
    public AES(int keyLenBits, EncryptionModes encryptionMode, String password) {
        super("AES", keyLenBits, 128, encryptionMode, password);
        if (keyLenBits != 128 && keyLenBits != 192 && keyLenBits != 256)
            throw new RuntimeException("Invalid AES bytes");
    }
}
