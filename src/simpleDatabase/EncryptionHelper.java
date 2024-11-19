package simpleDatabase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Helper class for Base64 encryption and decryption of strings
 */
public class EncryptionHelper {
    
    /**
     * Encrypts a string to Base64
     * @param plainText The string to encrypt
     * @return The Base64 encoded string
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decrypts a Base64 encoded string back to plaintext
     * @param encryptedText The Base64 encoded string
     * @return The decrypted plaintext string
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
