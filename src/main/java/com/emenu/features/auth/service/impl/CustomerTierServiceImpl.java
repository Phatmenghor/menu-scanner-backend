package com.emenu.features.auth.service.impl;

import com.emenu.enums.CustomerTier;
import com.emenu.enums.MessageType;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.CustomerTierService;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerTierServiceImpl implements CustomerTierService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Override
    public void updateCustomerTier(User customer) {
        CustomerTier oldTier = customer.getCustomerTier();
        CustomerTier newTier = CustomerTier.getTierByPoints(customer.getLoyaltyPoints());
        
        if (oldTier != newTier) {
            customer.setCustomerTier(newTier);
            userRepository.save(customer);
            
            sendTierUpgradeNotification(customer, oldTier, newTier);
            log.info("Customer {} upgraded from {} to {}", customer.getEmail(), oldTier, newTier);
        }
    }

    @Override
    public void addLoyaltyPoints(User customer, int points) {
        CustomerTier oldTier = customer.getCustomerTier();
        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        
        // Apply tier multiplier
        double multiplier = oldTier.getPointMultiplier();
        int bonusPoints = (int) (points * multiplier);
        
        customer.setLoyaltyPoints(currentPoints + bonusPoints);
        updateCustomerTier(customer);
        
        log.info("Added {} loyalty points to customer {} (with {}x multiplier)", 
                bonusPoints, customer.getEmail(), multiplier);
    }

    @Override
    public CustomerTier calculateTierByPoints(int points) {
        return CustomerTier.getTierByPoints(points);
    }

    @Override
    public void sendTierUpgradeNotification(User customer, CustomerTier oldTier, CustomerTier newTier) {
        try {
            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientId(customer.getId());
            message.setRecipientEmail(customer.getEmail());
            message.setRecipientName(customer.getFullName());
            message.setSubject("Congratulations! You've been upgraded to " + newTier.getDisplayName() + " tier!");
            message.setContent(String.format(
                    "Hello %s,\n\n" +
                    "Congratulations! You've been upgraded from %s to %s tier!\n\n" +
                    "Your new benefits:\n" +
                    "- %.1fx points multiplier\n" +
                    "- %.1f%% discount on orders\n" +
                    "- Current loyalty points: %d\n\n" +
                    "Thank you for being a loyal customer!\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    customer.getFullName(),
                    oldTier.getDisplayName(),
                    newTier.getDisplayName(),
                    newTier.getPointMultiplier(),
                    newTier.getDiscountPercentage(),
                    customer.getLoyaltyPoints()
            ));
            message.setMessageType(MessageType.NOTIFICATION);

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send tier upgrade notification to: {}", customer.getEmail(), e);
        }
    }
}