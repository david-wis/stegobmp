package org.itba.stegobmp.Embedders;

import java.io.IOException;

public interface StegoAlgorithm {
    void embedInFile(String inputFile, String carrierFile, String outputFile) throws IOException;
    void extract(String bitmapFile, String outputFile) throws IOException;
}
