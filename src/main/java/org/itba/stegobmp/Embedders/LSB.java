package org.itba.stegobmp.Embedders;

import org.itba.stegobmp.BmpStream;

import java.io.*;
import java.util.Iterator;

public class LSB {
    private final int n, mask, bitsLen;
    private static final int LENGTH_LENGTH = 32;
    private LSB(int n) {
        this.n = n;
        this.bitsLen = 8 / n;
        this.mask = (1<<(n)) - 1;
    }

    public static LSB buildLSB(int n) {
        if (n != 1 && n != 4) return null;
        return new LSB(n);
    }


    public void embed(String inputFile, String carrierFile, String outputFile) throws IOException {
        InputStream input = LSB.class.getClassLoader().getResourceAsStream(inputFile);

        BmpStream carrier = new BmpStream(carrierFile);
        OutputStream output = new FileOutputStream(outputFile);

        // read all bytes and add '\0' at the end
        byte[] inputBytes = input.readAllBytes();

        int inputLen = inputBytes.length;

        byte[] inputLenBytes = new byte[4];

        inputLenBytes[0] = (byte) (inputLen >> 24);
        inputLenBytes[1] = (byte) (inputLen >> 16);
        inputLenBytes[2] = (byte) (inputLen >> 8);
        inputLenBytes[3] = (byte) inputLen;

        String fileExtension = inputFile.substring(inputFile.lastIndexOf('.'));
        byte[] fileExtensionBytes = fileExtension.getBytes(); // TODO: Check that the \0 is included

        byte[] carrierBytes = carrier.readAllBytes();

        if (inputLen + 4 + fileExtensionBytes.length > carrierBytes.length/8) {
            throw new RuntimeException("Input message is too long for the carrier");
        }

        // the input message will be length || message || extension
        byte[] inputMessage = new byte[4 + inputLen + fileExtensionBytes.length];
        System.arraycopy(inputLenBytes, 0, inputMessage, 0, 4);
        System.arraycopy(inputBytes, 0, inputMessage, 4, inputLen);
        System.arraycopy(fileExtensionBytes, 0, inputMessage, 4 + inputLen, fileExtensionBytes.length);

        output.write(carrier.getHeader(), 0, 54);


        Iterator<Integer> bitIterator = new Iterator<>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < inputMessage.length * bitsLen;
            }

            @Override
            public Integer next() {
                Integer bitString;
                if (n == 1) {
                   bitString = ((inputMessage[pos / 8] >> (7 - pos % 8)) & mask);
                } else {
                    bitString = ((inputMessage[pos / 2] >> (4 * ((pos+1) % 2))) & mask);
                }
                pos++;
                return bitString;
            }
        };

        for (int i = 0; i < carrierBytes.length; i++) {
            if (bitIterator.hasNext()) {
                carrierBytes[i] = (byte) ((carrierBytes[i] & ~mask) | bitIterator.next());
            }
        }
        output.write(carrierBytes);

        input.close();
        carrier.close();
        output.close();
    }

    public void extract(String bitmapFile, String outputFile) throws IOException {
        BmpStream bmpStream = new BmpStream(bitmapFile);
        OutputStream output = new FileOutputStream(outputFile);

        byte[] bmpBytes = bmpStream.readAllBytes();
        byte[] inputLenBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            int b = 0x0;
            for (int j = 0; j < bitsLen; j++){
                b |= (bmpBytes[i * bitsLen + j] & mask) << (n * (bitsLen-j-1));
            }
            inputLenBytes[i] = (byte) b;
        }

        int inputLen = ((inputLenBytes[0] & 0xFF) << 24) | ((inputLenBytes[1] & 0xFF) << 16) | ((inputLenBytes[2] & 0xFF) << 8) | (inputLenBytes[3] & 0xFF);
        System.out.println("inputLen " + inputLen);
        byte[] inputMessage = new byte[inputLen];
        for (int i = 0; i < inputLen; i++) {
            int b = 0x0;
            for (int j = 0; j < bitsLen; j++){
                b |= (bmpBytes[LENGTH_LENGTH / n + i * bitsLen + j] & mask) << (n * (bitsLen-j-1));
            }
            inputMessage[i] = (byte) b;
        }
        output.write(inputMessage);
        output.close();
        bmpStream.close();
    }


}
