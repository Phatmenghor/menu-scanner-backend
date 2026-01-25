package com.emenu.shared.generate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Utility class for generating unique order numbers.
 * Pattern: ORD-YYYYMMDD-XXXX where XXXX is a counter with random component.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final AtomicLong orderCounter = new AtomicLong(System.currentTimeMillis() % 10000);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ORDER_PREFIX = "ORD";

    /**
     * Generate a unique order number with uniqueness check.
     *
     * @param existsChecker Predicate to check if order number already exists
     * @return Unique order number in format ORD-YYYYMMDD-XXXX
     */
    public String generateUniqueOrderNumber(Predicate<String> existsChecker) {
        String orderNumber;
        long counter = orderCounter.incrementAndGet() % 10000;
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            orderNumber = generateOrderNumber(counter);
            counter = (counter + 1) % 10000;
            attempts++;

            if (attempts >= maxAttempts) {
                // Add random component to avoid infinite loop
                counter = ThreadLocalRandom.current().nextLong(1000, 9999);
            }
        } while (existsChecker.test(orderNumber) && attempts < maxAttempts * 2);

        log.debug("Generated order number: {} after {} attempts", orderNumber, attempts);
        return orderNumber;
    }

    /**
     * Generate order number without uniqueness check.
     * Use when uniqueness is guaranteed by database constraints.
     *
     * @return Order number in format ORD-YYYYMMDD-XXXX
     */
    public String generateOrderNumber() {
        long counter = orderCounter.incrementAndGet() % 10000;
        return generateOrderNumber(counter);
    }

    private String generateOrderNumber(long counter) {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        return String.format("%s-%s-%04d", ORDER_PREFIX, date, counter);
    }

    /**
     * Generate order number with custom prefix.
     *
     * @param prefix Custom prefix (e.g., "POS", "WEB", "APP")
     * @return Order number with custom prefix
     */
    public String generateOrderNumber(String prefix) {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        long counter = orderCounter.incrementAndGet() % 10000;
        return String.format("%s-%s-%04d", prefix, date, counter);
    }
}
