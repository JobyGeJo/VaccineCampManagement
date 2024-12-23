package org.myapplication.tools;

import org.myapplication.exceptions.InvalidRequestException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Hashing {
    // The pepper is a static secret value known only to the server/application
    private static final String PEPPER = "secretPepperValue"; // Keep this value secure

    public static void main(String[] args) {
        String password = "12345678"; // User's password

        String hashed = hashWithSaltAndPepper(password);
        System.out.println(hashed);
        System.out.println(hashed.length());
        System.out.println(hashed.split(":")[1].length());
        System.out.println(verifyPassword("Hello, World!", hashed));
    }

    // Generate a random salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // You can choose a different length for the salt
        random.nextBytes(salt);
        return salt;
    }

    public static String hashWithSaltAndPepper(String input) {
        byte[] salt = generateSalt();  // Generate random salt if not provided
        return hashWithSaltAndPepper(input, salt);
    }

    // Hash the input with salt and pepper
    public static String hashWithSaltAndPepper(String input, byte[] salt) {

        if (input == null) {
            throw new IllegalArgumentException("password cannot be null");
        }

        if (!(input.matches(".{8,16}"))) {
            throw new InvalidRequestException("Invalid password");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Add the salt and pepper to the input
            digest.update(salt);
            digest.update(PEPPER.getBytes());

            // Perform hashing on the salted and peppered input
            byte[] hashedBytes = digest.digest(input.getBytes());

            // Convert the resulting hash to a hexadecimal string
            return combineSaltAndHash(salt, bytesToHex(hashedBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 algorithm not found.");
        }
    }

    // Verify if the provided password matches the stored hash
    public static boolean verifyPassword(String input, String storedHash) {
        // Hash the input with the same salt and pepper

        String[] parts = storedHash.split(":");
        String hashedInput = hashWithSaltAndPepper(input, Base64.getDecoder().decode(parts[0]));
        // Compare the newly hashed input with the stored hash
        return storedHash.equals(hashedInput);
    }

    // Helper method to convert byte array to a hexadecimal string (Not Standard)
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex);
            if (hex.length() == 1) {
                hexString.append('0');
            }
        }
        return hexString.toString();
    }

    // Method to concatenate salt and hash for storage
    private static String combineSaltAndHash(byte[] salt, String hash) {
        // Encode the salt as Base64 for easy storage and retrieval
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        // Concatenate the Base64-encoded salt and hash, separated by a delimiter (e.g., ":")
        return saltBase64 + ":" + hash;
    }

}