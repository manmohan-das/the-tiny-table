package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordUtil {

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hash = md.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Password hashing error",
                    e
            );
        }
    }

    public static boolean verify(
            String password,
            String hashedPassword
    ) {
        if (password == null || hashedPassword == null) {
            return false;
        }

        return hash(password).equals(hashedPassword);
    }

    

    public static boolean isHashed(String password) {
        if (password == null) {
            return false;
        }

        return password.matches("[a-fA-F0-9]{64}");
    }
}