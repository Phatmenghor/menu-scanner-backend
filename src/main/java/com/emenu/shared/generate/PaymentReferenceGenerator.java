package com.emenu.shared.generate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class PaymentReferenceGenerator {

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 10000);

    /**
     * Generate reference number (no uniqueness check)
     * ✅ FIXED: Removed uniqueness check and database dependency
     */
    public String generateUniqueReference() {
        String reference = generateReference();
        log.debug("Generated reference: {}", reference);
        return reference;
    }

    /**
     * Generate reference with multiple entropy sources
     */
    private String generateReference() {
        // Get current timestamp components
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String millis = String.format("%03d", now.getNano() / 1000000);

        // Thread-safe counter
        long counterValue = counter.incrementAndGet() % 10000;

        // Additional randomness
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);

        // Combine all entropy sources
        return String.format("PAY-%s-%s%s-%04d-%04d",
                date, time, millis, counterValue, random);
    }
}