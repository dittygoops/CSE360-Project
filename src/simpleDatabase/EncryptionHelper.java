package simpleDatabase;

import java.util.Base64;

// make an encryption helper class


public class EncryptionHelper {
    public String encrypt(String str) {
        // use base 64 for encryption
        byte[] encodedBytes = Base64.getEncoder().encode(str.getBytes());
        return new String(encodedBytes);
    }

    public String decrypt(String str) {
        // use base 64 for decryption
        byte[] decodedBytes = Base64.getDecoder().decode(str);
        return new String(decodedBytes);
    }
}