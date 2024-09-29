package org.itba.stegobmp;

import java.io.IOException;

public class BmpStream extends FileStream {


    private FileStatus status = FileStatus.STARTED;
    public BmpStream(String bmpFilePath, int bufferSize) throws IOException {
        super(bmpFilePath, bufferSize);

        byte[] buffer = new byte[54];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            throw new RuntimeException("End of file reached: " + filePath); // TODO: Custom exception?
        }

        if (buffer[0] != 'B' || buffer[1] != 'M' || buffer[28] != 24 || buffer[30] != 0 || buffer[31] != 0) {
            throw new RuntimeException("Invalid BMP file: " + filePath); // TODO: Custom exception?
        }

        this.fileSize =  ((buffer[5] & 0xFF) << 24) | ((buffer[4] & 0xFF) << 16) | ((buffer[3] & 0xFF) << 8) | (buffer[2] & 0xFF);
    }


    private enum FileStatus {
        STARTED,
        READING,
        EXTENSION,
        FINISHED
    }

    @Override
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    public byte[] next() {
        if (status == FileStatus.STARTED) {
            status = FileStatus.READING;
            start = 4;
//            buffer
        }
        return super.next();
    }
}
