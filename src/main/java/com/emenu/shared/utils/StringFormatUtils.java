package com.emenu.shared.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for common string formatting operations.
 */
public final class StringFormatUtils {

    private StringFormatUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Format BigDecimal as currency string with 2 decimal places.
     *
     * @param amount The amount to format
     * @return Formatted string like "$123.45"
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }

    /**
     * Format BigDecimal as currency string with custom symbol.
     *
     * @param amount The amount to format
     * @param symbol Currency symbol (e.g., "$", "€", "៛")
     * @return Formatted string
     */
    public static String formatCurrency(BigDecimal amount, String symbol) {
        if (amount == null) {
            return symbol + "0.00";
        }
        return String.format("%s%.2f", symbol, amount);
    }

    /**
     * Format percentage value.
     *
     * @param value The value to format
     * @return Formatted string like "85.50%"
     */
    public static String formatPercentage(double value) {
        return String.format("%.2f%%", value);
    }

    /**
     * Format percentage value with custom precision.
     *
     * @param value     The value to format
     * @param precision Number of decimal places
     * @return Formatted string
     */
    public static String formatPercentage(double value, int precision) {
        return String.format("%." + precision + "f%%", value);
    }

    /**
     * Format a number with thousand separators.
     *
     * @param value The value to format
     * @return Formatted string like "1,234,567"
     */
    public static String formatNumber(long value) {
        return String.format("%,d", value);
    }

    /**
     * Format a decimal number with thousand separators.
     *
     * @param value The value to format
     * @return Formatted string like "1,234,567.89"
     */
    public static String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%,.2f", value);
    }

    /**
     * Truncate string to max length with ellipsis.
     *
     * @param text      The text to truncate
     * @param maxLength Maximum length
     * @return Truncated string with "..." if exceeds max length
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Build a params string from key-value pairs (for audit logging).
     *
     * @param params Variable number of key-value pairs
     * @return Formatted params string like "key1=value1, key2=value2"
     */
    public static String buildParamsString(Object... params) {
        if (params == null || params.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(params[i]).append("=").append(params[i + 1]);
            }
        }
        return sb.toString();
    }

    /**
     * Check if string is null or empty.
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty.
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Capitalize first letter of string.
     */
    public static String capitalize(String text) {
        if (isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Convert camelCase to Title Case.
     *
     * @param camelCase The camelCase string
     * @return Title Case string (e.g., "firstName" -> "First Name")
     */
    public static String camelToTitleCase(String camelCase) {
        if (isEmpty(camelCase)) {
            return camelCase;
        }
        return camelCase
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^.", String.valueOf(camelCase.charAt(0)).toUpperCase());
    }

    /**
     * Mask sensitive data (e.g., phone numbers, emails).
     *
     * @param text       The text to mask
     * @param visibleEnd Number of visible characters at end
     * @return Masked string (e.g., "****1234")
     */
    public static String maskSensitive(String text, int visibleEnd) {
        if (isEmpty(text) || text.length() <= visibleEnd) {
            return text;
        }
        int maskLength = text.length() - visibleEnd;
        return "*".repeat(maskLength) + text.substring(maskLength);
    }

    /**
     * Format phone number for display.
     *
     * @param phone Raw phone number
     * @return Formatted phone or original if invalid
     */
    public static String formatPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }
        // Remove non-digit characters
        String digits = phone.replaceAll("\\D", "");

        // Format based on length
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6));
        } else if (digits.length() == 9) {
            return String.format("%s %s %s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6));
        }
        return phone;
    }

    /**
     * Round BigDecimal to specified scale.
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
