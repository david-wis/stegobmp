package org.itba.stegobmp.Encryption;

public enum EncryptionModes {

    ECB("ecb", true, false, "ECB"),
    CBC("cbc", true, true, "CBC"),
    CFB("cfb", false, true, "CFB8"),
    OFB("ofb", false, true, "OFB");

    private final String mode;
    private final boolean needsPadding;
    private final boolean needsIV;
    private final String name;

    public boolean needsPadding() {
        return needsPadding;
    }

    public boolean needsIV() {
        return needsIV;
    }

    EncryptionModes(String mode, boolean needsPadding, boolean needsIV, String name) {
        this.mode = mode;
        this.needsPadding = needsPadding;
        this.needsIV = needsIV;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EncryptionModes fromString(String mode) {
        for (EncryptionModes encryptionMode : EncryptionModes.values()) {
            if (encryptionMode.mode.equals(mode)) {
                return encryptionMode;
            }
        }
        throw new IllegalArgumentException("Invalid encryption mode: " + mode);
    }
}
