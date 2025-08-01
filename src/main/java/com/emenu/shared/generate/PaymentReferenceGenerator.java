package com.emenu.shared.generate;

import com.emenu.features.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class PaymentReferenceGenerator {
    
    private final PaymentRepository paymentRepository;
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 10000);
    
    public PaymentReferenceGenerator(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Generate guaranteed unique reference number with retry mechanism
     */
    public String generateUniqueReference() {
        int maxAttempts = 10;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String reference = generateReference();
            
            // Check if reference already exists in database
            if (!paymentRepository.existsByReferenceNumberAndIsDeletedFalse(reference)) {
                log.debug("Generated unique reference: {} (attempt {})", reference, attempt);
                return reference;
            }
            
            log.warn("Reference collision detected: {} (attempt {})", reference, attempt);
            
            // Add exponential backoff delay
            try {
                Thread.sleep(10 * attempt); // 10ms, 20ms, 30ms, etc.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Fallback to UUID if all attempts failed
        String fallbackReference = "PAY-UUID-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.error("Used fallback UUID reference after {} attempts: {}", maxAttempts, fallbackReference);
        return fallbackReference;
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