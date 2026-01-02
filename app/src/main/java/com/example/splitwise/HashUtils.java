package com.example.splitwise;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for secure password hashing using SHA-256
 */
public class HashUtils {
    /**
     * Hashes a password using SHA-256 algorithm
     * @param password Plain text password
     * @return Hexadecimal string representation of hashed password
     */
    public static String hashPassword(String password) {
        try {
            // Create MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Add password bytes to digest
            md.update(password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // Convert bytes to Hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}