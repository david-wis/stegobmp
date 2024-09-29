package org.itba.stegobmp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class FileStream implements Iterator<byte[]> {
    protected int fileSize;
    protected String filePath;
    protected int bufferSize;
    protected BufferedInputStream inputStream;
    protected byte[] buffer;
    protected int start = 0;

    public FileStream(String bmpFilePath, int bufferSize) throws IOException {
        this.filePath = bmpFilePath;
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        inputStream = new BufferedInputStream(Objects.requireNonNull(classloader.getResourceAsStream(bmpFilePath)));
        fileSize = inputStream.available();
    }

    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing file: " + filePath, e); // TODO: Custom exception?
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e); // TODO: Custom exception?
        }

    }

    @Override
    public byte[] next() {
        try {
            int bytesRead = inputStream.read(buffer, start, bufferSize - start);
            if (bytesRead == -1) {
                throw new RuntimeException("End of file reached: " + filePath); // TODO: Custom exception?
            }

            start = 0;
            if (bytesRead < buffer.length) {
                byte[] trimmedBuffer = new byte[bytesRead];
                System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                return trimmedBuffer;
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e); // TODO: Custom exception?
        }
    }
}
