package org.itba.stegobmp;

import org.apache.commons.cli.*;
import org.itba.stegobmp.Embedders.LSB;
import org.itba.stegobmp.Embedders.LSBI;
import org.itba.stegobmp.Embedders.StegoAlgorithm;
import org.itba.stegobmp.Encryption.AES;
import org.itba.stegobmp.Encryption.DES;
import org.itba.stegobmp.Encryption.Encrypter;
import org.itba.stegobmp.Encryption.EncryptionModes;

public class Main {

    public static void main(String[] args) {
        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String inputFile = cmd.getOptionValue("in");
            String carrierFile = cmd.getOptionValue("p");
            String outputFile = cmd.getOptionValue("out");
            String stegAlgo = cmd.getOptionValue("steg");

            if ((inputFile == null && cmd.hasOption("embed")) || carrierFile == null || outputFile == null || stegAlgo == null) {
                System.err.println("Please provide the required options: -in, -p, -out, -steg");
                return;
            }

            System.out.println("Input file: " + inputFile);
            System.out.println("Carrier file: " + carrierFile);
            System.out.println("Output file: " + outputFile);
            System.out.println("Steganography algorithm: " + stegAlgo);

            String alg = cmd.getOptionValue("a", "aes128");
            String mode = cmd.getOptionValue("m", "cbc");
            String pass = cmd.getOptionValue("pass");

            EncryptionModes encMode = null;
            try {
                encMode = EncryptionModes.fromString(mode);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }

            Encrypter encrypter = pass == null? null :
            switch (alg) {
                case "aes128" -> new AES(128, encMode, pass);
                case "aes192" -> new AES(192, encMode, pass);
                case "aes256" -> new AES(256, encMode, pass);
                case "3des" -> new DES(encMode, pass);
                default -> throw new IllegalArgumentException("Invalid encryption algorithm");
            };

            StegoAlgorithm stegoAlgorithm = switch (stegAlgo) {
                case "LSB1" -> new LSB(1, -1, encrypter);
                case "LSB4" -> new LSB(4, -1, encrypter);
                case "LSBI" -> new LSBI(encrypter);
                default -> throw new IllegalArgumentException("Invalid steganography algorithm");
            };

            if (cmd.hasOption("embed")) {
                stegoAlgorithm.embedInFile(inputFile, carrierFile, outputFile);
            } else if (cmd.hasOption("extract")) {
                try {
                    stegoAlgorithm.extract(carrierFile, outputFile);
                } catch (Exception e) {
                    System.err.println("Error extracting information: " + e.getMessage());
                    throw e;
                }
            } else {
                System.err.println("Please provide the required '-embed' or '-extract' option");
            }
        } catch (ParseException e) {
            System.err.println("Failed to parse command line arguments");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static Options getOptions() {
        Options options = new Options();

        options.addOption("embed", false, "Indicates that information will be embedded");
        options.addOption("extract", false, "Indicates that information will be extracted");


        options.addOption("in", true, "File to be hidden"); // Only for embed
        options.addOption("p", true, "Bitmap file that will be the carrier");
        options.addOption("out", true, "Output bitmap file with embedded information");
        options.addOption("steg", true, "Steganography algorithm to use: LSB1, LSB4, or LSBI");

        options.addOption("a", true, "Encryption algorithm: aes128, aes192, aes256, or 3des");
        options.addOption("m", true, "Encryption mode: ecb, cfb, ofb, or cbc");
        options.addOption("pass", true, "Encryption password");
        return options;
    }
}
