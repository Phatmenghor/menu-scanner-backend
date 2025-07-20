package com.emenu.features.setting.tasks;

import com.emenu.features.auth.models.User;
import com.emenu.features.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventService {

    private final EmailService emailService;

    @EventListener
    @Async
    public void handleUserRegistration(UserRegistrationEvent event) {
        User user = event.getUser();
        log.info("Handling user registration event for: {}", user.getEmail());
        
        // Send welcome email
        emailService.sendWelcomeEmail(user);
        
        // Send email verification if needed
        if (!user.getEmailVerified()) {
            String verificationToken = generateVerificationToken(user);
            emailService.sendEmailVerification(user, verificationToken);
        }
    }

    @EventListener
    @Async
    public void handleTierUpgrade(TierUpgradeEvent event) {
        User user = event.getUser();
        String oldTier = event.getOldTier();
        String newTier = event.getNewTier();
        
        log.info("Handling tier upgrade event for: {} from {} to {}", 
                user.getEmail(), oldTier, newTier);
        
        emailService.sendTierUpgradeEmail(user, oldTier, newTier);
    }

    private String generateVerificationToken(User user) {
        // Simple token generation - in production use proper JWT or UUID
        return java.util.UUID.randomUUID().toString();
    }

    // Event classes
    public static class UserRegistrationEvent {
        private final User user;

        public UserRegistrationEvent(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    public static class TierUpgradeEvent {
        private final User user;
        private final String oldTier;
        private final String newTier;

        public TierUpgradeEvent(User user, String oldTier, String newTier) {
            this.user = user;
            this.oldTier = oldTier;
            this.newTier = newTier;
        }

        public User getUser() {
            return user;
        }

        public String getOldTier() {
            return oldTier;
        }

        public String getNewTier() {
            return newTier;
        }
    }
}