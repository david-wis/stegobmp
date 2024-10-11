package org.itba.stegobmp.Encryption;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class DES {
//    public static byte[] encrypt(byte[] key, byte[] data) throws NoSuchAlgorithmException {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "TripleDES");
//
//        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[8]);
//
//
//
//
//
//    }
//
//    public static byte[] decrypt(byte[] key, byte[] data) {
//        KeyGenerator keygen = KeyGenerator.getInstance("DES");
//        SecretKey desKey = keygen.generateKey();
////Se genera instancia de Cipher
//        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
////Se inicializa el cifrador para poder encriptar con la clave
//        desCipher.init(Cipher.ENCRYPT_MODE,desKey);
////Texto a encriptar.
//        byte[] cleartext = "Contenido de prueba".getBytes();
////Se encripta
//        byte[] ciphertext = desCipher.doFinal(cleartext);
//        System.out.println("El cifrado es:"+ new String(ciphertext, "UTF8"));
//        System.out.println("Ahora descifra...");
////Se desencripta
//        desCipher.init(Cipher.DECRYPT_MODE,desKey);
//        byte[] cleartext_out = desCipher.doFinal(ciphertext);
//        System.out.println("El descifrado es:"+new String(cleartext_out, "UTF8"));
//    }
//    }

}


