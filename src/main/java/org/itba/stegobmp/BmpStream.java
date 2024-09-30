package org.itba.stegobmp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BmpStream extends FileInputStream {
    private byte[] header = new byte[54];
    private int fileSize;

    public BmpStream(String bmpFilePath) throws IOException {
        super(getFileFromResources(bmpFilePath));

        int bytesRead = this.read(header);
        if (bytesRead == -1) {
            throw new RuntimeException("End of file reached: " + bmpFilePath); // TODO: Custom exception?
        }

        if (header[0] != 'B' || header[1] != 'M' || header[28] != 24 || header[30] != 0 || header[31] != 0) {
            throw new RuntimeException("Invalid BMP file: " + bmpFilePath); // TODO: Custom exception?
        }

        this.fileSize =  ((header[5] & 0xFF) << 24) | ((header[4] & 0xFF) << 16) | ((header[3] & 0xFF) << 8) | (header[2] & 0xFF);
    }

    private static String getFileFromResources(String fileName) throws IOException {
        ClassLoader classLoader = BmpStream.class.getClassLoader();

        if (classLoader.getResource(fileName) != null) {
            return classLoader.getResource(fileName).getPath();
        } else {
            throw new IOException("File not found in resources: " + fileName);
        }
    }

    public byte[] getHeader() {
        return header;
    }

    public int getFileSize() {
        return fileSize;
    }
}
