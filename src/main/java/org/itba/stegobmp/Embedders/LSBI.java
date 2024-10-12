package org.itba.stegobmp.Embedders;

import org.itba.stegobmp.BmpStream;
import org.itba.stegobmp.Encryption.Encrypter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Predicate;

public class LSBI extends LSB {
    private int[] flipBits;

    public LSBI(Encrypter encrypter) {
        super(1, 1, encrypter);
    }

    @Override
    public void embedInFile(String inputFile, String carrierFile, String outputFile) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        InputStream input = LSB.class.getClassLoader().getResourceAsStream(inputFile);
        String fileExtension = inputFile.substring(inputFile.lastIndexOf('.'));
        byte[] inputBytes = input.readAllBytes();

        BmpStream carrier = new BmpStream(carrierFile);

        int[] stats = {0, 0, 0, 0};

        byte[] firstBytes = new byte[stats.length];
        carrier.read(firstBytes);

        byte[] lsb1Result = super.embed(inputBytes, fileExtension, carrier, stats);

        OutputStream output = new FileOutputStream(outputFile);
        output.write(carrier.getHeader(), 0, 54);
        for (int i = 0; i < stats.length; i++) {
            output.write((firstBytes[i] & 0xFE) | ((stats[i] > 0) ? 0x1 : 0x0));
        }
        for (int i = 0; i < lsb1Result.length; i++) {
            if (i % 3 == 1 || stats[(lsb1Result[i] & 0b00000110) >> 1] <= 0)
                continue; // Skip red channels, or unchanged bytes
            lsb1Result[i] ^= 0x1;
        }

        output.write(lsb1Result);
        output.close();
        input.close();
        carrier.close();
    }

    @Override
    protected int readBytes(byte[] bmpBytes, Predicate<Integer> loopCondition, int offset, byte[] writeBuffer) {
        int i;
        for (i = 0; loopCondition.test(i); i++) {
            int b = 0x0;
            for (int j = 0; j < 8; j++) {
                if ((offset + i * 8 + j) % 3 == 2) { // This is absolute!!!!
                    j--;
                    offset++;
                    continue;
                }
                int group = (bmpBytes[offset + i * 8 + j] & 0b00000110) >> 1;
                b |= ((bmpBytes[offset + i * 8 + j] & 0x1) ^ flipBits[group]) << (7 - j);
            }
            writeBuffer[i] = (byte) b;
        }
        return i;
    }

    @Override
    protected int toOffset(int writtenLength, int offset) {
        return writtenLength + (writtenLength + offset % 3) / 2;
    }

    @Override
    public void extract(String bitmapFile, String outputFile) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        BmpStream bmpStream = new BmpStream(bitmapFile);

        byte[] bmpBytes = bmpStream.readAllBytes();

        flipBits = new int[4];
        for (int i = 0; i < 4; i++)
            flipBits[i] = (bmpBytes[i] & 0x1) == 0 ? 0x0 : 0x1;

        int offset = 4;
        performExtraction(bmpStream, bmpBytes, offset, outputFile);
    }
}
