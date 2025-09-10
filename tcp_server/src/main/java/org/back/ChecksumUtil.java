package org.back;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

    /**
     * Calcula el checksum MD5 para un arreglo de bytes.
     * @param fileBytes Los bytes del archivo.
     * @return El checksum MD5 como un string hexadecimal de 32 caracteres.
     * @throws NoSuchAlgorithmException Si el algoritmo MD5 no está disponible.
     */
    public static String calculateChecksumMD5(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(fileBytes);
        BigInteger no = new BigInteger(1, digest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}