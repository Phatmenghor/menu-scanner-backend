package com.emenu.shared.generate;

import com.emenu.features.order.models.OrderCounter;
import com.emenu.features.order.repository.OrderCounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * Utility class for generating unique order numbers.
 * Pattern: ORD-YYYYMMDD-XXXXXX where XXXXXX is a database-backed counter (unlimited, starts from 000001).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderCounterRepository orderCounterRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ORDER_PREFIX = "ORD";

    /**
     * Generate a unique order number with database-backed counter.
     * Counter is per-day and grows dynamically without limits.
     *
     * @return Unique order number in format ORD-YYYYMMDD-XXXXXX
     */
    @Transactional
    public String generateOrderNumber() {
        LocalDate today = LocalDate.now();

        // Get or create counter for today
        OrderCounter counter = orderCounterRepository.findByCounterDate(today)
                .orElseGet(() -> {
                    OrderCounter newCounter = new OrderCounter();
                    newCounter.setCounterDate(today);
                    newCounter.setCounterValue(0L);
                    return orderCounterRepository.save(newCounter);
                });

        // Increment counter
        counter.setCounterValue(counter.getCounterValue() + 1);
        OrderCounter savedCounter = orderCounterRepository.save(counter);

        String date = today.format(DATE_FORMATTER);
        return String.format("%s-%s-%06d", ORDER_PREFIX, date, savedCounter.getCounterValue());
    }

    /**
     * Generate a unique order number with uniqueness check.
     * For legacy compatibility.
     *
     * @param existsChecker Predicate to check if order number already exists
     * @return Unique order number in format ORD-YYYYMMDD-XXXXXX
     */
    @Transactional
    public String generateUniqueOrderNumber(Predicate<String> existsChecker) {
        String orderNumber = generateOrderNumber();

        // Check if order number exists (should rarely happen with database sequence)
        int attempts = 0;
        final int maxAttempts = 5;

        while (existsChecker.test(orderNumber) && attempts < maxAttempts) {
            orderNumber = generateOrderNumber();
            attempts++;
        }

        if (attempts > 0) {
            log.warn("Had to retry order number generation {} times", attempts);
        }

        log.debug("Generated order number: {}", orderNumber);
        return orderNumber;
    }

    /**
     * Generate order number with custom prefix.
     *
     * @param prefix Custom prefix (e.g., "POS", "WEB", "APP")
     * @return Order number with custom prefix
     */
    @Transactional
    public String generateOrderNumber(String prefix) {
        LocalDate today = LocalDate.now();

        OrderCounter counter = orderCounterRepository.findByCounterDate(today)
                .orElseGet(() -> {
                    OrderCounter newCounter = new OrderCounter();
                    newCounter.setCounterDate(today);
                    newCounter.setCounterValue(0L);
                    return orderCounterRepository.save(newCounter);
                });

        counter.setCounterValue(counter.getCounterValue() + 1);
        OrderCounter savedCounter = orderCounterRepository.save(counter);

        String date = today.format(DATE_FORMATTER);
        return String.format("%s-%s-%06d", prefix, date, savedCounter.getCounterValue());
    }
}
