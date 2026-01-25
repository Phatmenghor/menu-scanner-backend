package com.emenu.shared.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for common date and time operations.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
        // Utility class - prevent instantiation
    }

    // Common date formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter TIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Get current date formatted as string.
     */
    public static String getCurrentDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Get current date time formatted as string.
     */
    public static String getCurrentDateTimeString() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Get current date in compact format (yyyyMMdd).
     */
    public static String getCurrentDateCompact() {
        return LocalDate.now().format(DATE_COMPACT_FORMATTER);
    }

    /**
     * Calculate days remaining from now to end date.
     *
     * @param endDate The end date
     * @return Number of days remaining, 0 if end date is in the past
     */
    public static long calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
        return Math.max(0, days);
    }

    /**
     * Calculate days remaining from now to end date.
     *
     * @param endDate The end date
     * @return Number of days remaining, 0 if end date is in the past
     */
    public static long calculateDaysRemaining(LocalDate endDate) {
        if (endDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return Math.max(0, days);
    }

    /**
     * Calculate days active from start date until now.
     *
     * @param startDate The start date
     * @return Number of days active, 0 if start date is in the future
     */
    public static long calculateDaysActive(LocalDateTime startDate) {
        if (startDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(startDate, LocalDateTime.now());
        return Math.max(0, days);
    }

    /**
     * Calculate days active from start date until now.
     *
     * @param startDate The start date
     * @return Number of days active, 0 if start date is in the future
     */
    public static long calculateDaysActive(LocalDate startDate) {
        if (startDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        return Math.max(0, days);
    }

    /**
     * Check if a date is in the past.
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Check if a date is in the future.
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Check if date is today.
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Check if date is today.
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * Calculate work duration in minutes between two times.
     *
     * @param startTime Start time
     * @param endTime   End time
     * @return Duration in minutes
     */
    public static long calculateDurationMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Format duration in minutes to human-readable string (e.g., "2h 30m").
     */
    public static String formatDuration(long minutes) {
        if (minutes < 0) {
            return "0m";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, mins);
        }
        return String.format("%dm", mins);
    }

    /**
     * Calculate work percentage based on actual and expected minutes.
     */
    public static double calculateWorkPercentage(long actualMinutes, long expectedMinutes) {
        if (expectedMinutes <= 0) {
            return 0.0;
        }
        return (double) actualMinutes / expectedMinutes * 100;
    }

    /**
     * Add days to current date.
     */
    public static LocalDateTime addDays(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Add days to a specific date.
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * Get start of day for a given date time.
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * Get end of day for a given date time.
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }
}
