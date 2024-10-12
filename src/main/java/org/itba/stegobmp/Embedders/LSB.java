package org.itba.stegobmp.Embedders;

import org.itba.stegobmp.BmpStream;
import org.itba.stegobmp.Encryption.Encrypter;
import org.itba.stegobmp.StegoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.function.Predicate;

public class LSB implements StegoAlgorithm {
    private static final int LENGTH_LENGTH = 32;
    private final int n, mask, carrierPerInput, skipN;
    private final Encrypter encrypter;

    public LSB(int n, int skipN, Encrypter encrypter) {
        if (n != 1 && n != 4) throw new IllegalArgumentException("n must be 1 or 4");
        this.n = n;
        this.carrierPerInput = 8 / n;
        this.mask = (1<<(n)) - 1;
        if (skipN < -1 || skipN > 2) throw new IllegalArgumentException("SkipN must be between 0 and 2 or -1");
        this.skipN = skipN;
        this.encrypter = encrypter;
    }

    @Override
    public void embedInFile(String inputFile, String carrierFile, String outputFile) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        InputStream input = LSB.class.getClassLoader().getResourceAsStream(inputFile);
        String fileExtension = inputFile.substring(inputFile.lastIndexOf('.'));
        byte[] inputBytes = input.readAllBytes();

        BmpStream carrier = new BmpStream(carrierFile);

        byte[] result = embed(inputBytes, fileExtension, carrier, null);
        input.close();
        carrier.close();

        OutputStream output = new FileOutputStream(outputFile);
        output.write(carrier.getHeader(), 0, 54);
        output.write(result);
        output.close();
    }

    public byte[] embed(byte[] inputBytes, String fileExtension, BmpStream carrier, int[] stats) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        if (stats != null && stats.length != 4) {
            throw new IllegalArgumentException("Stats must be an array of length 4");
        }

        int inputLen = inputBytes.length;

        byte[] inputLenBytes = StegoUtils.toByteArray(inputLen);
        byte[] fileExtensionBytes = fileExtension.getBytes();
        byte[] carrierBytes = carrier.readAllBytes();

        if (inputLen + 4 + fileExtensionBytes.length > carrierBytes.length/8) {
            throw new RuntimeException("Input message is too long for the carrier");// TODO: Change for LSB4 and LSBI?
        }

        // the input message will be length || message || extension
        byte[] rawInputMessage = new byte[4 + inputLen + fileExtensionBytes.length + 1];
        System.arraycopy(inputLenBytes, 0, rawInputMessage, 0, 4);
        System.arraycopy(inputBytes, 0, rawInputMessage, 4, inputLen);
        System.arraycopy(fileExtensionBytes, 0, rawInputMessage, 4 + inputLen, fileExtensionBytes.length);
        rawInputMessage[rawInputMessage.length - 1] = 0;

        byte[] inputMessage = encrypter == null? rawInputMessage : encrypter.encrypt(rawInputMessage);

        Iterator<Integer> bitIterator = new Iterator<>() {
            int pos = 0;
            @Override
            public boolean hasNext() {
                return pos < inputMessage.length * carrierPerInput;
            }

            @Override
            public Integer next() {
                int bitString;
                if (n == 1)
                   bitString = ((inputMessage[pos / 8] >> (7 - pos % 8)) & mask);
                else
                    bitString = ((inputMessage[pos / 2] >> (4 * ((pos+1) % 2))) & mask);
                pos++;
                return bitString;
            }
        };

        for (int i = 0; i < carrierBytes.length; i++) {
            if (skipN != -1 && i % 3 == skipN) continue;

            if (bitIterator.hasNext()) {
                int bit = bitIterator.next();
                if (stats != null) {
                    int group = (carrierBytes[i] & 0b00000110) >> 1;
                    stats[group] += (carrierBytes[i] & mask) != bit ? 1 : -1;
                }
                carrierBytes[i] = (byte) ((carrierBytes[i] & ~mask) | bit);
            }
        }
        return carrierBytes;
    }



    private void readBytes(byte[] bmpBytes, int limit, int offset, byte[] writeBuffer) {
        readBytes(bmpBytes, i -> i < limit, offset, writeBuffer);
    }

    private int readBytes(byte[] bmpBytes, Predicate<Integer> loopCondition, int offset, byte[] writeBuffer) {
        int i;
        for (i = 0; loopCondition.test(i); i++) {
            int b = 0x0;
            for (int j = 0; j < carrierPerInput; j++){
                b |= (bmpBytes[offset + i * carrierPerInput + j] & mask) << (n * (carrierPerInput -j-1));
            }
            writeBuffer[i] = (byte) b;
        }
        return i;
    }

    // Not used in LSBI, only for LSB1 and LSB4
    @Override
    public void extract(String bitmapFile, String outputFile) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        BmpStream bmpStream = new BmpStream(bitmapFile);

        byte[] bmpBytes = bmpStream.readAllBytes();
        byte[] inputLenBytes = new byte[4];

        readBytes(bmpBytes, 4, 0, inputLenBytes);
        int contentLen = StegoUtils.byteArrayToInt(inputLenBytes, 0);
        System.out.println("contentLen " + contentLen);
        byte[] inputMessage = new byte[contentLen];
        readBytes(bmpBytes, contentLen, LENGTH_LENGTH / n, inputMessage);

        FileData fd = encrypter == null? extractWithoutEncryption(bmpBytes, inputMessage, contentLen)
                                        : extractWithEncryption(bmpBytes, inputMessage, contentLen);
        System.out.println("Writing on: " + outputFile + fd.extension);
        OutputStream output = new FileOutputStream(outputFile + fd.extension);

        output.write(fd.data);
        output.close();
        bmpStream.close();
    }

    private FileData extractWithEncryption(byte[] bmpBytes, byte[] content, int contentLen) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        byte[] decipheredContent = encrypter.decrypt(content);

        int inputLen = StegoUtils.byteArrayToInt(decipheredContent, 0);
        byte[] inputMessage = new byte[inputLen];
        System.out.println("inputLen " + inputLen);
        System.arraycopy(decipheredContent, 4, inputMessage, 0, inputLen);
        byte[] extension = new byte[decipheredContent.length-4-inputLen-1];
        System.arraycopy(decipheredContent, 4 + inputLen, extension, 0, extension.length);
        return new FileData(inputMessage, new String(extension));
    }

    private FileData extractWithoutEncryption(byte[] bmpBytes, byte[] content, int contentLen) {
        byte[] extension = new byte[255];
        int extensionLen = readBytes(bmpBytes, i -> i == 0 || extension[i-1] != 0, LENGTH_LENGTH / n + contentLen * carrierPerInput, extension) - 1;

        String extensionStr = new String(extension, 0, extensionLen);
        return new FileData(content, extensionStr);
    }

    private record FileData(byte[] data, String extension) {}
}
