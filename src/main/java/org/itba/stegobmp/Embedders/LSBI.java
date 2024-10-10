package org.itba.stegobmp.Embedders;

import org.itba.stegobmp.BmpStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LSBI implements StegoAlgorithm {
    @Override
    public void embedInFile(String inputFile, String carrierFile, String outputFile) throws IOException {
        InputStream input = LSB.class.getClassLoader().getResourceAsStream(inputFile);
        String fileExtension = inputFile.substring(inputFile.lastIndexOf('.'));
        byte[] inputBytes = input.readAllBytes();

        BmpStream carrier = new BmpStream(carrierFile);

        LSB lsb = new LSB(1, 1); // Red channels get out of phase
        int[] stats = {0, 0, 0, 0};

        byte[] firstBytes = new byte[stats.length];
        carrier.read(firstBytes);

        byte[] lsb1Result = lsb.embed(inputBytes, fileExtension, carrier, stats);


        OutputStream output = new FileOutputStream(outputFile);
        output.write(carrier.getHeader(), 0, 54);
        for (int i = 0; i < stats.length; i++) {
             output.write((firstBytes[i] & 0xFE) | ((stats[i] > 0) ? 0x1 : 0x0));
        }
        for (int i = 0; i < lsb1Result.length; i++) {
            if (i == 538799+58)
                System.out.println("Here");
            if (i % 3 == 1 || stats[(lsb1Result[i] & 0b00000110) >> 1] <= 0) continue; // Skip red channels, or unchanged bytes
            lsb1Result[i] ^= 0x1;
        }

        output.write(lsb1Result);
        output.close();
        input.close();
        carrier.close();
    }

    @Override
    public void extract(String bitmapFile, String outputFile) throws IOException {
        BmpStream bmpStream = new BmpStream(bitmapFile);

        byte[] bmpBytes = bmpStream.readAllBytes();
        byte[] inputLenBytes = new byte[4];

    }
}
