package org.itba.stegobmp;

public class StegoUtils {
    public static byte[] toByteArray(int value) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (value >> ((3-i) * 8));
        }
        return result;
    }
    public static int byteArrayToInt(byte[] bytes, int offset) {
        if (bytes.length < offset + 4) {
            throw new IllegalArgumentException("Invalid byte array");
        }
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset+1] & 0xFF) << 16) | ((bytes[offset+2] & 0xFF) << 8) | (bytes[offset+3] & 0xFF);
    }
}
