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
                b |= (bmpBytes[i * 8 + j] & mask) << (n * (bitsLen-j-1));
            }
            inputLenBytes[i] = (byte) b;
        }

//        inputLenBytes[0] = (byte) ((bmpBytes[0] & 1) << 7 | (bmpBytes[1] & 1) << 6 | (bmpBytes[2] & 1) << 5 | (bmpBytes[3] & 1) << 4 | (bmpBytes[4] & 1) << 3 | (bmpBytes[5] & 1) << 2 | (bmpBytes[6] & 1) << 1 | (bmpBytes[7] & 1));
//        inputLenBytes[1] = (byte) ((bmpBytes[8] & 1) << 7 | (bmpBytes[9] & 1) << 6 | (bmpBytes[10] & 1) << 5 | (bmpBytes[11] & 1) << 4 | (bmpBytes[12] & 1) << 3 | (bmpBytes[13] & 1) << 2 | (bmpBytes[14] & 1) << 1 | (bmpBytes[15] & 1));
//        inputLenBytes[2] = (byte) ((bmpBytes[16] & 1) << 7 | (bmpBytes[17] & 1) << 6 | (bmpBytes[18] & 1) << 5 | (bmpBytes[19] & 1) << 4 | (bmpBytes[20] & 1) << 3 | (bmpBytes[21] & 1) << 2 | (bmpBytes[22] & 1) << 1 | (bmpBytes[23] & 1));
//        inputLenBytes[3] = (byte) ((bmpBytes[24] & 1) << 7 | (bmpBytes[25] & 1) << 6 | (bmpBytes[26] & 1) << 5 | (bmpBytes[27] & 1) << 4 | (bmpBytes[28] & 1) << 3 | (bmpBytes[29] & 1) << 2 | (bmpBytes[30] & 1) << 1 | (bmpBytes[31] & 1));
        int inputLen = ((inputLenBytes[0] & 0xFF) << 24) | ((inputLenBytes[1] & 0xFF) << 16) | ((inputLenBytes[2] & 0xFF) << 8) | (inputLenBytes[3] & 0xFF);

        byte[] inputMessage = new byte[inputLen];
        for (int i = 0; i < inputLen/n; i++) {
            int b = 0x0;
            for (int j = 0; j < bitsLen; j++){
                System.out.println(LENGTH_LENGTH + i * 8 + j * n);
                b |= (bmpBytes[LENGTH_LENGTH + i * 8 + j] & mask) << (n * (bitsLen-j-1));
            }
            inputMessage[i] = (byte) b;

//            inputMessage[i] = (byte) ((bmpBytes[32 + i * 8] & 1) << 7 | (bmpBytes[33 + i * 8] & 1) << 6 | (bmpBytes[34 + i * 8] & 1) << 5 | (bmpBytes[35 + i * 8] & 1) << 4 | (bmpBytes[36 + i * 8] & 1) << 3 | (bmpBytes[37 + i * 8] & 1) << 2 | (bmpBytes[38 + i * 8] & 1) << 1 | (bmpBytes[39 + i * 8] & 1));
        }

        output.write(inputMessage);


        output.close();
        bmpStream.close();
    }


}
