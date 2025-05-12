package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * Service responsible for generating student identifiers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentIdentifierGenerator {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private static final int IDENTIFIER_LENGTH = 10;
    private static final int PASSWORD_LENGTH = 6;
    private static final Random RANDOM = new SecureRandom();

    /**
     * Generates a student identifier based on class code
     * Format: ClassCode + Sequential Number (10 digits total)
     */
    public String generateStudentIdentifier(Long classId) {
        // Get class code
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new BadRequestException("Class not found with ID: " + classId));
        
        String classCode = classEntity.getCode();
        if (classCode == null || classCode.isEmpty()) {
            throw new BadRequestException("Class code is not set for class with ID: " + classId);
        }
        
        // Find highest existing identifier for this class
        String prefix = classCode;
        if (prefix.length() > 5) {
            prefix = prefix.substring(0, 5); // Take first 5 digits if class code is longer
        }
        
        // Find the highest existing identifier for this class prefix
        String searchPattern = prefix + "%";
        List<UserEntity> existingUsers = userRepository.findByIdentifyNumberLike(searchPattern);
        
        int highestSequence = 0;
        
        // Calculate the next sequence number
        for (UserEntity user : existingUsers) {
            String identifyNumber = user.getIdentifyNumber();
            if (identifyNumber != null && identifyNumber.startsWith(prefix)) {
                try {
                    // Extract the sequence part and convert to integer
                    String sequencePart = identifyNumber.substring(prefix.length());
                    int sequence = Integer.parseInt(sequencePart);
                    if (sequence > highestSequence) {
                        highestSequence = sequence;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    // Skip invalid format
                    log.warn("Invalid identifier format found: {}", identifyNumber);
                }
            }
        }
        
        // Next sequence number
        int nextSequence = highestSequence + 1;
        
        // Calculate padding needed (10 digits total)
        int paddingLength = IDENTIFIER_LENGTH - prefix.length();
        String sequenceFormatted = String.format("%0" + paddingLength + "d", nextSequence);
        
        // Generate full identifier
        String identifier = prefix + sequenceFormatted;
        
        log.info("Generated student identifier: {} for class ID: {}", identifier, classId);
        return identifier;
    }
    
    /**
     * Generates a random numeric password of specified length
     */
    public String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10)); // 0-9
        }
        return sb.toString();
    }
}