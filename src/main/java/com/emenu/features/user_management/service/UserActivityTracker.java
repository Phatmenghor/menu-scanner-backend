package com.emenu.features.user_management.service;

import com.emenu.features.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityTracker {

    private final UserRepository userRepository;

    @Async
    public void trackUserActivity(String userEmail) {
        try {
            userRepository.findByEmailAndIsDeletedFalse(userEmail)
                    .ifPresent(user -> {
                        user.setLastActive(LocalDateTime.now());
                        userRepository.save(user);
                    });
        } catch (Exception e) {
            log.error("Failed to track user activity for {}", userEmail, e);
        }
    }

    @Async
    public void trackLoginTime(String userEmail, long loginDurationMinutes) {
        try {
            userRepository.findByEmailAndIsDeletedFalse(userEmail)
                    .ifPresent(user -> {
                        user.setTotalLoginTime(
                            (user.getTotalLoginTime() != null ? user.getTotalLoginTime() : 0L) + loginDurationMinutes
                        );
                        userRepository.save(user);
                    });
        } catch (Exception e) {
            log.error("Failed to track login time for {}", userEmail, e);
        }
    }
}