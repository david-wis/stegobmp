package org.itba.stegobmp;

public class StegoUtils {
    public static byte[] toByteArray(int value) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (value >> ((3-i) * 8));
        }
        return result;
    }
}
