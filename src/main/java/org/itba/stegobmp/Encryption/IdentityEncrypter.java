package org.itba.stegobmp.Encryption;

public class IdentityEncrypter implements Encrypter {

    @Override
    public byte[] encrypt(byte[] input) {
        return input;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        return ciphertext;
    }
}
