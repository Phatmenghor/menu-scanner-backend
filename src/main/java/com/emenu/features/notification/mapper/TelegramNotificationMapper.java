
package com.emenu.features.notification.mapper;

import com.emenu.features.auth.models.User;
import com.emenu.features.notification.constants.TelegramMessages;
import com.emenu.features.notification.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class TelegramNotificationMapper {

    public PlatformUserCreationNotificationDto toPlatformUserCreationDto(User newUser, User createdBy) {
        String rolesString = newUser.getRoles().stream()
                .map(role -> role.getName().getDisplayName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Unknown");

        return PlatformUserCreationNotificationDto.builder()
                .userIdentifier(newUser.getUserIdentifier())
                .fullName(newUser.getFullName())
                .email(newUser.getEmail())
                .phoneNumber(newUser.getPhoneNumber())
                .userType(newUser.getUserType().getDescription())
                .accountStatus(newUser.getAccountStatus().getDescription())
                .roles(rolesString)
                .position(newUser.getPosition())
                .createdByUserIdentifier(createdBy.getUserIdentifier())
                .createdByFullName(createdBy.getFullName())
                .createdAt(newUser.getCreatedAt())
                .build();
    }

    public CustomerRegistrationNotificationDto toCustomerRegistrationDto(User customer) {
        return CustomerRegistrationNotificationDto.builder()
                .userIdentifier(customer.getUserIdentifier())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .socialProvider(customer.getSocialProvider().getDisplayName())
                .hasTelegram(customer.hasTelegramLinked())
                .registeredAt(customer.getCreatedAt())
                .telegramUserId(customer.getTelegramUserId())
                .build();
    }

    public BusinessRegistrationNotificationDto toBusinessRegistrationDto(User businessOwner, String businessName, String subdomain) {
        return BusinessRegistrationNotificationDto.builder()
                .businessName(businessName)
                .ownerName(businessOwner.getFullName())
                .ownerUserIdentifier(businessOwner.getUserIdentifier())
                .subdomain(subdomain)
                .registeredAt(businessOwner.getCreatedAt())
                .ownerTelegramUserId(businessOwner.getTelegramUserId())
                .build();
    }

    public TelegramLinkNotificationDto toTelegramLinkDto(User user) {
        return TelegramLinkNotificationDto.builder()
                .userIdentifier(user.getUserIdentifier())
                .fullName(user.getFullName())
                .userType(user.getUserType().getDescription())
                .telegramUsername(user.getTelegramUsername())
                .telegramUserId(user.getTelegramUserId())
                .linkedAt(user.getTelegramLinkedAt())
                .build();
    }

    // Message builders using TelegramMessages constants
    public String buildCustomerWelcomeMessage(CustomerRegistrationNotificationDto dto) {
        return TelegramMessages.buildCustomerWelcomeMessage(
                dto.getFullName() != null ? dto.getFullName() : dto.getUserIdentifier(),
                dto.getUserIdentifier(),
                dto.getRegisteredAt()
        );
    }

    public String buildCustomerRegistrationGroupMessage(CustomerRegistrationNotificationDto dto) {
        return TelegramMessages.buildCustomerRegistrationAdminMessage(
                dto.getUserIdentifier(),
                dto.getFullName(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getSocialProvider(),
                dto.isHasTelegram(),
                dto.getRegisteredAt()
        );
    }

    public String buildBusinessRegistrationMessage(BusinessRegistrationNotificationDto dto) {
        return TelegramMessages.buildBusinessRegistrationMessage(
                dto.getBusinessName(),
                dto.getOwnerName(),
                dto.getOwnerUserIdentifier(),
                dto.getSubdomain(),
                dto.getRegisteredAt()
        );
    }

    public String buildTelegramLinkSuccessMessage(TelegramLinkNotificationDto dto) {
        return TelegramMessages.buildTelegramLinkSuccessMessage(
                dto.getFullName() != null ? dto.getFullName() : dto.getUserIdentifier(),
                dto.getUserIdentifier(),
                dto.getUserType(),
                dto.getLinkedAt()
        );
    }

    public String buildTelegramLinkGroupMessage(TelegramLinkNotificationDto dto) {
        return TelegramMessages.buildTelegramLinkAdminMessage(
                dto.getUserIdentifier(),
                dto.getFullName(),
                dto.getUserType(),
                dto.getTelegramUsername(),
                dto.getTelegramUserId(),
                dto.getLinkedAt()
        );
    }

    public String buildPlatformUserCreationMessage(PlatformUserCreationNotificationDto dto) {
        return TelegramMessages.buildPlatformUserCreationMessage(dto);
    }

    public String buildOrderNotificationMessage(OrderNotificationDto dto) {
        return TelegramMessages.buildOrderNotification(
                dto.getCustomerName(),
                dto.getBusinessName(),
                dto.getOrderDetails(),
                dto.getTotal()
        );
    }

    public String buildOrderStatusUpdateMessage(OrderNotificationDto dto) {
        return TelegramMessages.buildOrderStatusUpdate(
                dto.getOrderStatus(),
                dto.getBusinessName(),
                dto.getOrderDetails()
        );
    }

    public String buildSubscriptionAlertMessage(SubscriptionAlertDto dto) {
        return TelegramMessages.buildSubscriptionExpiredNotification(
                dto.getBusinessName(),
                dto.getDaysUntilExpiry()
        );
    }

    public String buildSystemEventMessage(SystemEventDto dto) {
        return TelegramMessages.buildInfoMessage(dto.getTitle(), dto.getMessage());
    }
}
