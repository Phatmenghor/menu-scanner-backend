package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.NotificationRecipientType;
import com.emenu.enums.user.RoleEnum;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
import com.emenu.features.notification.factory.NotificationFactory;
import com.emenu.features.notification.mapper.NotificationMapper;
import com.emenu.features.notification.models.Notification;
import com.emenu.features.notification.repository.NotificationRepository;
import com.emenu.features.notification.service.NotificationService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationFactory notificationFactory;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    
    // ===== CREATE =====
    @Override
    public List<NotificationResponse> sendNotification(NotificationRequest request) {
        log.info("Sending notification - Type: {}, Recipient: {}", 
            request.getMessageType(), request.getRecipientType());
        
        List<Notification> notifications = new ArrayList<>();
        UUID groupId = UUID.randomUUID();
        
        // Route based on recipient type
        switch (request.getRecipientType()) {
            case INDIVIDUAL_USER:
                validateIndividualRequest(request);
                notifications.add(createIndividualNotification(request, groupId));
                break;
                
            case BUSINESS_TEAM_GROUP:
                validateBusinessRequest(request);
                notifications.addAll(createBusinessTeamNotifications(request, groupId));
                break;
                
            case SYSTEM_OWNER_GROUP:
                notifications.addAll(createSystemOwnerNotifications(request, groupId));
                break;
                
            case ALL_USERS:
                notifications.addAll(createAllUsersNotifications(request, groupId));
                break;
                
            default:
                throw new ValidationException("Unsupported recipient type");
        }
        
        // Save all notifications
        List<Notification> saved = notificationRepository.saveAll(notifications);
        
        // Send system copy if requested
        if (Boolean.TRUE.equals(request.getSendSystemCopy()) && 
            request.getRecipientType() != NotificationRecipientType.SYSTEM_OWNER_GROUP) {
            sendSystemCopies(notifications.get(0), groupId);
        }
        
        log.info("Notification sent to {} recipients", saved.size());
        return saved.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    // ===== VALIDATION =====
    private void validateIndividualRequest(NotificationRequest request) {
        if (request.getUserId() == null) {
            throw new ValidationException("User ID is required for individual notifications");
        }
    }
    
    private void validateBusinessRequest(NotificationRequest request) {
        if (request.getBusinessId() == null) {
            throw new ValidationException("Business ID is required for business team notifications");
        }
    }
    
    // ===== CREATE INDIVIDUAL =====
    private Notification createIndividualNotification(NotificationRequest request, UUID groupId) {
        Notification notification = notificationFactory.createUserNotification(
            request.getTitle(),
            request.getMessage(),
            request.getMessageType(),
            request.getUserId(),
            request.getUserName(),
            request.getPriority()
        );
        notification.setGroupId(groupId);
        notification.setBusinessId(request.getBusinessId());
        return notification;
    }
    
    // ===== CREATE BUSINESS TEAM =====
    private List<Notification> createBusinessTeamNotifications(NotificationRequest request, UUID groupId) {
        List<User> businessUsers = userRepository.findAllByBusinessIdAndIsDeletedFalse(request.getBusinessId());
        
        if (businessUsers.isEmpty()) {
            log.warn("No users found for business: {}", request.getBusinessId());
            return List.of();
        }
        
        log.info("Creating business team notifications for {} users", businessUsers.size());
        return businessUsers.stream()
                .map(user -> notificationFactory.createBusinessTeamNotification(
                    request.getTitle(),
                    request.getMessage(),
                    request.getMessageType(),
                    request.getBusinessId(),
                    user.getId(),
                    user.getFullName(),
                    request.getPriority(),
                    groupId
                ))
                .collect(Collectors.toList());
    }
    
    // ===== CREATE SYSTEM OWNER GROUP =====
    private List<Notification> createSystemOwnerNotifications(NotificationRequest request, UUID groupId) {
        List<User> systemOwners = userRepository.findByRoleAndIsDeletedFalse(RoleEnum.PLATFORM_OWNER);
        
        if (systemOwners.isEmpty()) {
            log.warn("No system owners found");
            return List.of();
        }
        
        log.info("Creating system owner notifications for {} owners", systemOwners.size());
        return systemOwners.stream()
                .map(user -> notificationFactory.createSystemOwnerNotification(
                    request.getTitle(),
                    request.getMessage(),
                    request.getMessageType(),
                    user.getId(),
                    user.getFullName(),
                    request.getPriority(),
                    groupId
                ))
                .collect(Collectors.toList());
    }
    
    // ===== CREATE ALL USERS =====
    private List<Notification> createAllUsersNotifications(NotificationRequest request, UUID groupId) {
        List<User> allUsers = userRepository.findAllActiveUsers();
        
        if (allUsers.isEmpty()) {
            log.warn("No active users found");
            return List.of();
        }
        
        log.info("Creating notifications for ALL {} users", allUsers.size());
        return allUsers.stream()
                .map(user -> notificationFactory.createAllUsersNotification(
                    request.getTitle(),
                    request.getMessage(),
                    request.getMessageType(),
                    user.getId(),
                    user.getFullName(),
                    request.getPriority(),
                    groupId
                ))
                .collect(Collectors.toList());
    }
    
    // ===== SYSTEM COPY =====
    private void sendSystemCopies(Notification original, UUID groupId) {
        List<User> systemOwners = userRepository.findByRoleAndIsDeletedFalse(RoleEnum.PLATFORM_OWNER);
        
        List<Notification> systemCopies = systemOwners.stream()
                .map(owner -> notificationFactory.createSystemCopy(
                    original,
                    owner.getId(),
                    owner.getFullName(),
                    groupId
                ))
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(systemCopies);
        log.info("System copy sent to {} owners", systemCopies.size());
    }
    
    // ===== READ =====
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, currentUser.getId())
                .orElseThrow(() -> new ValidationException("Notification not found"));
        
        return notificationMapper.toResponse(notification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponse> getMyNotifications(NotificationFilterRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Force current user's ID for "my notifications"
        request.setUserId(currentUser.getId());
        
        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortDirection()
        );
        
        // Use comprehensive search with current user filter
        Page<Notification> notificationPage = notificationRepository.searchNotifications(
            currentUser.getId(),
            request.getBusinessId(),
            request.getMessageType(),
            request.getPriority(),
            request.getIsRead(),
            request.getRecipientType(),
            request.getSearch(),
            pageable
        );
        
        return notificationMapper.toPaginationResponse(notificationPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponse> getAllNotifications(NotificationFilterRequest request) {
        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortDirection()
        );
        
        Page<Notification> notificationPage;
        
        // If system notifications only is requested
        if (Boolean.TRUE.equals(request.getSystemNotificationsOnly())) {
            request.setRecipientType(NotificationRecipientType.SYSTEM_OWNER_GROUP);
        }
        
        // Use comprehensive search query with all filters
        notificationPage = notificationRepository.searchNotifications(
            request.getUserId(),
            request.getBusinessId(),
            request.getMessageType(),
            request.getPriority(),
            request.getIsRead(),
            request.getRecipientType(),
            request.getSearch(),
            pageable
        );
        
        return notificationMapper.toPaginationResponse(notificationPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countUnreadByUserId(currentUser.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnseenCount() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countUnseenByUserId(currentUser.getId());
    }
    
    // ===== UPDATE (Self notifications only) =====
    @Override
    public int markAllAsSeen() {
        User currentUser = securityUtils.getCurrentUser();
        int updated = notificationRepository.markAllAsSeenForUser(
                currentUser.getId(),
                LocalDateTime.now()
        );
        log.info("Marked {} notifications as SEEN (badge cleared) for user: {}", updated, currentUser.getId());
        return updated;
    }
    @Override
    public NotificationResponse markAsRead(UUID notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, currentUser.getId())
                .orElseThrow(() -> new ValidationException("Notification not found"));
        
        if (!notification.getIsRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        }
        
        return notificationMapper.toResponse(notification);
    }
    
    @Override
    public int markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        int updated = notificationRepository.markAllAsReadForUser(
                currentUser.getId(),
                LocalDateTime.now(),
                MessageStatus.READ
        );
        log.info("Marked {} notifications as read for user: {}", updated, currentUser.getId());
        return updated;
    }
    
    // ===== DELETE (Self notifications only) =====
    @Override
    public void deleteNotification(UUID notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, currentUser.getId())
                .orElseThrow(() -> new ValidationException("Notification not found"));
        
        notification.softDelete();
        notificationRepository.save(notification);
        log.info("Notification deleted: {}", notificationId);
    }
    
    @Override
    public int deleteAllNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        
        // Use efficient bulk update query
        int deleted = notificationRepository.softDeleteAllUserNotifications(currentUser.getId());
        
        log.info("Deleted {} notifications for user: {}", deleted, currentUser.getId());
        return deleted;
    }
}