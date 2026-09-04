package com.food.userinfo.util;

public class DataMaskingUtils {

    /**
     * Mask email or username to hide sensitive information
     * Shows first 2 chars and last 2 chars
     */
    public static String maskEmailOrUsername(String input) {
        if (input == null || input.length() < 4) {
            return "***";
        }
        return input.substring(0, 2) + "***" + input.substring(input.length() - 2);
    }

    /**
     * Mask email address
     * Example: john.doe@email.com -> j***@e***.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        // Mask username and domain
        String maskedUsername = username.length() > 2
                ? username.substring(0, 2) + "***"
                : "***";

        String[] domainParts = domain.split("\\.");
        String maskedDomain = domainParts[0].length() > 2
                ? domainParts[0].substring(0, 1) + "***"
                : "***";

        return maskedUsername + "@" + maskedDomain + "." + domainParts[domainParts.length - 1];
    }

    /**
     * Basic email validation
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
