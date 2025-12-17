package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SecurityUtils securityUtils;
    
    // CREATE
    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());
        
        Notification notification = notificationMapper.toEntity(request);
        notification = notificationRepository.save(notification);
        
        log.info("Notification created with ID: {}", notification.getId());
        return notificationMapper.toResponse(notification);
    }
    
    // READ - Get single notification
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, currentUser.getId())
                .orElseThrow(() -> new ValidationException("Notification not found"));
        
        return notificationMapper.toResponse(notification);
    }
    
    // READ - Get my notifications
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponse> getMyNotifications(NotificationFilterRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortDirection()
        );
        
        Page<Notification> notificationPage;
        
        if (Boolean.TRUE.equals(request.getUnreadOnly())) {
            notificationPage = notificationRepository.findUnreadByUserId(currentUser.getId(), pageable);
        } else if (request.getMessageType() != null) {
            notificationPage = notificationRepository.findByUserIdAndType(currentUser.getId(), request.getMessageType(), pageable);
        } else if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            notificationPage = notificationRepository.searchUserNotifications(currentUser.getId(), request.getSearch(), pageable);
        } else {
            notificationPage = notificationRepository.findByUserId(currentUser.getId(), pageable);
        }
        
        return notificationMapper.toPaginationResponse(notificationPage);
    }
    
    // READ - Get all notifications (admin)
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
        
        if (Boolean.TRUE.equals(request.getSystemNotificationsOnly())) {
            notificationPage = notificationRepository.findSystemNotifications(pageable);
        } else {
            notificationPage = notificationRepository.findAll(pageable);
        }
        
        return notificationMapper.toPaginationResponse(notificationPage);
    }
    
    // READ - Get unread count
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countUnreadByUserId(currentUser.getId());
    }
    
    // UPDATE - Update notification
    @Override
    public NotificationResponse updateNotification(UUID notificationId, NotificationRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, currentUser.getId())
                .orElseThrow(() -> new ValidationException("Notification not found"));
        
        // Update fields
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setMessageType(request.getMessageType());
        notification.setChannel(request.getChannel());
        notification.setActionUrl(request.getActionUrl());
        
        notification = notificationRepository.save(notification);
        
        log.info("Notification updated: {}", notificationId);
        return notificationMapper.toResponse(notification);
    }
    
    // UPDATE - Mark as read
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
    
    // UPDATE - Mark all as read
    @Override
    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        int updated = notificationRepository.markAllAsReadForUser(
                currentUser.getId(), 
                LocalDateTime.now(), 
                MessageStatus.READ
        );
        log.info("Marked {} notifications as read for user: {}", updated, currentUser.getId());
    }
    
    // DELETE - Delete single notification
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
    
    // DELETE - Delete all read notifications
    @Override
    public void deleteAllReadNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        // Delete read notifications older than 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int deleted = notificationRepository.softDeleteOldReadNotifications(sevenDaysAgo);
        log.info("Deleted {} old read notifications", deleted);
    }
}
