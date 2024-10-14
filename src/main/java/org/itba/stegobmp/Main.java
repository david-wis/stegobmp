package org.itba.stegobmp;

import org.itba.stegobmp.Embedders.LSB;
import org.itba.stegobmp.Embedders.LSBI;
import org.itba.stegobmp.Embedders.StegoAlgorithm;
import org.itba.stegobmp.Encryption.AES;
import org.itba.stegobmp.Encryption.DES;
import org.itba.stegobmp.Encryption.Encrypter;
import org.itba.stegobmp.Encryption.EncryptionModes;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Map<String, String> getArgs(String[] args) {
       Map<String, String> parsedArgs = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) continue;
            if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                parsedArgs.put(args[i].substring(1), null);
            } else {
                parsedArgs.put(args[i].substring(1), args[i + 1]);
                i++;
            }
        }
        return parsedArgs;
    }

    public static void main(String[] args) {
        Map<String, String> parsedArgs = getArgs(args);
        try {
            String inputFile = parsedArgs.get("in");
            String carrierFile = parsedArgs.get("p");
            String outputFile = parsedArgs.get("out");
            String stegAlgo = parsedArgs.get("steg");

            if ((inputFile == null && parsedArgs.containsKey("embed")) || carrierFile == null || outputFile == null || stegAlgo == null) {
                System.err.println("Please provide the required options: -in, -p, -out, -steg");
                return;
            }

            System.out.println("Input file: " + inputFile);
            System.out.println("Carrier file: " + carrierFile);
            System.out.println("Output file: " + outputFile);
            System.out.println("Steganography algorithm: " + stegAlgo);

            String alg = parsedArgs.getOrDefault("a", "aes128");
            String mode = parsedArgs.getOrDefault("m", "cbc");
            String pass = parsedArgs.get("pass");

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

            if (parsedArgs.containsKey("embed")) {
                stegoAlgorithm.embedInFile(inputFile, carrierFile, outputFile);
            } else if (parsedArgs.containsKey("extract")) {
                try {
                    stegoAlgorithm.extract(carrierFile, outputFile);
                } catch (Exception e) {
                    System.err.println("Error extracting information: " + e.getMessage());
                    throw e;
                }
            } else {
                System.err.println("Please provide the required '-embed' or '-extract' option");
            }
        } catch (Exception e) {
            System.err.println("Unable to complete operation");
            throw new RuntimeException(e);
        }
    }


//    private static Options getOptions() {
//        Options options = new Options();
//
//        options.addOption("embed", false, "Indicates that information will be embedded");
//        options.addOption("extract", false, "Indicates that information will be extracted");
//
//
//        options.addOption("in", true, "File to be hidden"); // Only for embed
//        options.addOption("p", true, "Bitmap file that will be the carrier");
//        options.addOption("out", true, "Output bitmap file with embedded information");
//        options.addOption("steg", true, "Steganography algorithm to use: LSB1, LSB4, or LSBI");
//
//        options.addOption("a", true, "Encryption algorithm: aes128, aes192, aes256, or 3des");
//        options.addOption("m", true, "Encryption mode: ecb, cfb, ofb, or cbc");
//        options.addOption("pass", true, "Encryption password");
//        return options;
//    }
}
