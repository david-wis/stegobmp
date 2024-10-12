package org.itba.stegobmp.Encryption;

public class DES extends Encrypter {
    public DES(EncryptionModes encryptionMode, String password) {
        super("DESede", 192, 64, encryptionMode, password);
    }
}