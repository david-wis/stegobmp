package org.itba.stegobmp.Embedders;

import org.itba.stegobmp.BmpStream;
import org.itba.stegobmp.FileStream;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LSB {
    public LSB() {
    }


    public static void embed(String inputFile, String carrierFile, String outputFile) throws IOException {
        FileStream input = new FileStream(inputFile, 512);
        BmpStream carrier = new BmpStream(carrierFile, 4096);

        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));

        while (carrier.hasNext()) {
            byte[] secretBytes = (input.hasNext())? input.next() : new byte[0];
            byte[] inputBytes = carrier.next();
            byte[] outputBytes = new byte[inputBytes.length];
            for (int i = 0; i < inputBytes.length; i++) {
                if (i < secretBytes.length * 8) {
                    outputBytes[i] = (byte) ((inputBytes[i] & 0xFE) | ((secretBytes[i / 8] >> (7 - i % 8)) & 1));
                } else {
                    outputBytes[i] = inputBytes[i];
                }
            }
            output.write(outputBytes);
        }
    }

    public static void extract(String bitmapFile, String outputFile) throws IOException {
        FileStream bitmap = new FileStream(bitmapFile, 4096);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));

        while (bitmap.hasNext()) {
            byte[] inputBytes = bitmap.next();
            byte[] outputBytes = new byte[inputBytes.length / 8];
            for (int i = 0; i < outputBytes.length; i++) {
                outputBytes[i] = 0;
                for (int j = 0; j < 8; j++) {
                    outputBytes[i] |= (byte) ((inputBytes[i * 8 + j] & 1) << (7 - j));
                }
            }
            output.write(outputBytes);
        }
    }


}
