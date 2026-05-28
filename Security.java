import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security.java
 * Utility class for password hashing using SHA-256
 * Uses Java built-in MessageDigest - no third-party libraries
 */
public class Security {

    /**
     * Hash a plain-text password using SHA-256
     * @param password plain text password
     * @return SHA-256 hashed hex string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("[ERROR] SHA-256 not available: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate input - prevent empty or null inputs
     * @param input string to validate
     * @return true if valid
     */
    public static boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }
}
