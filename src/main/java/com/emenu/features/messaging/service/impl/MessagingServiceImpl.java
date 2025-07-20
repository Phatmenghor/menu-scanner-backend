package com.emenu.features.messaging.service.impl;

import com.emenu.exception.UserNotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.request.BroadcastMessageRequest;
import com.emenu.features.messaging.dto.request.MessageCreateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.dto.response.MessageStatsResponse;
import com.emenu.features.messaging.dto.response.MessageSummaryResponse;
import com.emenu.features.messaging.dto.update.MessageUpdateRequest;
import com.emenu.features.messaging.mapper.MessageMapper;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.features.messaging.service.MessagingService;
import com.emenu.features.messaging.specication.MessageSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessagingServiceImpl implements MessagingService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final SecurityUtils securityUtils;

    @Override
    public MessageResponse createMessage(MessageCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        User recipient = userRepository.findByIdAndIsDeletedFalse(request.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException("Recipient not found"));

        Message message = messageMapper.toEntity(request);
        message.setSenderId(currentUser.getId());
        message.setSenderEmail(currentUser.getEmail());
        message.setSenderName(currentUser.getFullName());
        message.setRecipientEmail(recipient.getEmail());
        message.setRecipientName(recipient.getFullName());
        message.setBusinessId(request.getBusinessId() != null ? request.getBusinessId() : currentUser.getBusinessId());

        Message savedMessage = messageRepository.save(message);
        log.info("Message created from {} to {}", currentUser.getEmail(), recipient.getEmail());

        return messageMapper.toResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getMessages(MessageFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<Message> spec = MessageSpecification.buildSpecification(filter);
        Page<Message> messagePage = messageRepository.findAll(spec, pageable);

        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Auto-mark as read if current user is recipient
        User currentUser = securityUtils.getCurrentUser();
        if (message.getRecipientId().equals(currentUser.getId()) && !message.isRead()) {
            message.markAsRead();
            messageRepository.save(message);
        }

        return messageMapper.toResponse(message);
    }

    @Override
    public MessageResponse updateMessage(UUID id, MessageUpdateRequest request) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User currentUser = securityUtils.getCurrentUser();
        if (!message.getSenderId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own messages");
        }

        messageMapper.updateEntity(request, message);
        Message updatedMessage = messageRepository.save(message);

        log.info("Message updated: {}", id);
        return messageMapper.toResponse(updatedMessage);
    }

    @Override
    public void deleteMessage(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User currentUser = securityUtils.getCurrentUser();
        if (!message.getSenderId().equals(currentUser.getId()) && 
            !message.getRecipientId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own messages");
        }

        message.softDelete();
        messageRepository.save(message);
        log.info("Message deleted: {}", id);
    }

    @Override
    public void markAsRead(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User currentUser = securityUtils.getCurrentUser();
        if (!message.getRecipientId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only mark your own messages as read");
        }

        if (!message.isRead()) {
            message.markAsRead();
            messageRepository.save(message);
            log.info("Message marked as read: {}", id);
        }
    }

    @Override
    public void markAsUnread(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User currentUser = securityUtils.getCurrentUser();
        if (!message.getRecipientId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only mark your own messages as unread");
        }

        message.setReadAt(null);
        message.setStatus(MessageStatus.SENT);
        messageRepository.save(message);
        log.info("Message marked as unread: {}", id);
    }

    @Override
    public void broadcastMessage(BroadcastMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        List<User> recipients = getRecipientsForBroadcast(request);
        
        for (User recipient : recipients) {
            Message message = new Message();
            message.setSenderId(currentUser.getId());
            message.setSenderEmail(currentUser.getEmail());
            message.setSenderName(currentUser.getFullName());
            message.setRecipientId(recipient.getId());
            message.setRecipientEmail(recipient.getEmail());
            message.setRecipientName(recipient.getFullName());
            message.setSubject(request.getSubject());
            message.setContent(request.getContent());
            message.setMessageType(request.getMessageType());
            message.setPriority(request.getPriority());
            message.setBusinessId(currentUser.getBusinessId());

            messageRepository.save(message);
        }

        log.info("Broadcast message sent to {} recipients", recipients.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getInbox(int pageNo, int pageSize, String search) {
        User currentUser = securityUtils.getCurrentUser();
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage;
        if (StringUtils.hasText(search)) {
            messagePage = messageRepository.findByRecipientIdAndSearchAndIsDeletedFalse(
                    currentUser.getId(), search, pageable);
        } else {
            messagePage = messageRepository.findByRecipientIdAndIsDeletedFalse(currentUser.getId(), pageable);
        }

        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getSentMessages(int pageNo, int pageSize, String search) {
        User currentUser = securityUtils.getCurrentUser();
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage;
        if (StringUtils.hasText(search)) {
            messagePage = messageRepository.findBySenderIdAndSearchAndIsDeletedFalse(
                    currentUser.getId(), search, pageable);
        } else {
            messagePage = messageRepository.findBySenderIdAndIsDeletedFalse(currentUser.getId(), pageable);
        }

        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getUnreadMessages(int pageNo, int pageSize) {
        User currentUser = securityUtils.getCurrentUser();
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findUnreadByRecipientIdAndIsDeletedFalse(
                currentUser.getId(), pageable);
        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount() {
        User currentUser = securityUtils.getCurrentUser();
        return messageRepository.countUnreadByRecipientIdAndIsDeletedFalse(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStatsResponse getMessageStats() {
        User currentUser = securityUtils.getCurrentUser();
        MessageStatsResponse stats = new MessageStatsResponse();

        stats.setTotalMessages(messageRepository.countByIsDeletedFalse());
        stats.setSentMessages(messageRepository.countBySenderIdAndIsDeletedFalse(currentUser.getId()));
        stats.setReceivedMessages(messageRepository.countByRecipientIdAndIsDeletedFalse(currentUser.getId()));
        stats.setUnreadMessages(messageRepository.countUnreadByRecipientIdAndIsDeletedFalse(currentUser.getId()));

        // Calculate read messages
        stats.setReadMessages(stats.getReceivedMessages() - stats.getUnreadMessages());

        // Stats by type
        stats.setGeneralMessages(messageRepository.countByMessageTypeAndIsDeletedFalse(MessageType.GENERAL));
        stats.setNotifications(messageRepository.countByMessageTypeAndIsDeletedFalse(MessageType.NOTIFICATION));
        stats.setAnnouncements(messageRepository.countByMessageTypeAndIsDeletedFalse(MessageType.ANNOUNCEMENT));
        stats.setSupportMessages(messageRepository.countByMessageTypeAndIsDeletedFalse(MessageType.SUPPORT));

        // Stats by priority
        stats.setLowPriorityMessages(messageRepository.countByPriorityAndIsDeletedFalse("LOW"));
        stats.setNormalPriorityMessages(messageRepository.countByPriorityAndIsDeletedFalse("NORMAL"));
        stats.setHighPriorityMessages(messageRepository.countByPriorityAndIsDeletedFalse("HIGH"));
        stats.setUrgentMessages(messageRepository.countByPriorityAndIsDeletedFalse("URGENT"));

        // Recent activity
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekAgo = today.minusDays(7);
        LocalDateTime monthAgo = today.minusMonths(1);

        stats.setMessagesToday(messageRepository.countByCreatedAtAfterAndIsDeletedFalse(today));
        stats.setMessagesThisWeek(messageRepository.countByCreatedAtAfterAndIsDeletedFalse(weekAgo));
        stats.setMessagesThisMonth(messageRepository.countByCreatedAtAfterAndIsDeletedFalse(monthAgo));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStatsResponse getBusinessMessageStats(UUID businessId) {
        MessageStatsResponse stats = new MessageStatsResponse();

        // Basic stats for business
        stats.setTotalMessages(messageRepository.countByIsDeletedFalse());
        stats.setUnreadMessages(messageRepository.countUnreadByBusinessIdAndIsDeletedFalse(businessId));

        return stats;
    }

    @Override
    public void markMultipleAsRead(List<UUID> messageIds) {
        User currentUser = securityUtils.getCurrentUser();
        
        List<Message> messages = messageRepository.findByRecipientIdAndIdInAndIsDeletedFalse(
                currentUser.getId(), messageIds);

        for (Message message : messages) {
            if (!message.isRead()) {
                message.markAsRead();
            }
        }

        messageRepository.saveAll(messages);
        log.info("Marked {} messages as read", messages.size());
    }

    @Override
    public void deleteMultipleMessages(List<UUID> messageIds) {
        User currentUser = securityUtils.getCurrentUser();
        
        List<Message> messages = messageRepository.findByIdInAndIsDeletedFalse(messageIds);

        for (Message message : messages) {
            if (message.getSenderId().equals(currentUser.getId()) || 
                message.getRecipientId().equals(currentUser.getId())) {
                message.softDelete();
            }
        }

        messageRepository.saveAll(messages);
        log.info("Deleted {} messages", messages.size());
    }

    @Override
    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        
        List<Message> unreadMessages = messageRepository.findUnreadByRecipientIdAndIsDeletedFalse(
                currentUser.getId(), Pageable.unpaged()).getContent();

        for (Message message : unreadMessages) {
            message.markAsRead();
        }

        messageRepository.saveAll(unreadMessages);
        log.info("Marked all {} messages as read for user {}", unreadMessages.size(), currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> searchMessages(String query, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findBySearchAndIsDeletedFalse(query, pageable);
        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getMessagesByType(String messageType, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        MessageType type = MessageType.valueOf(messageType.toUpperCase());
        Page<Message> messagePage = messageRepository.findByMessageTypeAndIsDeletedFalse(type, pageable);
        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageSummaryResponse> getMessagesByPriority(String priority, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findByPriorityAndIsDeletedFalse(priority.toUpperCase(), pageable);
        List<MessageSummaryResponse> content = messageMapper.toSummaryResponseList(messagePage.getContent());

        return PaginationResponse.<MessageSummaryResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    public MessageResponse replyToMessage(UUID messageId, MessageCreateRequest request) {
        Message originalMessage = messageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new RuntimeException("Original message not found"));

        User currentUser = securityUtils.getCurrentUser();

        // Create reply
        MessageCreateRequest replyRequest = new MessageCreateRequest();
        replyRequest.setRecipientId(originalMessage.getSenderId());
        replyRequest.setSubject("Re: " + originalMessage.getSubject());
        replyRequest.setContent(request.getContent());
        replyRequest.setMessageType(MessageType.GENERAL);
        replyRequest.setPriority(request.getPriority());
        replyRequest.setBusinessId(originalMessage.getBusinessId());

        return createMessage(replyRequest);
    }

    @Override
    public MessageResponse forwardMessage(UUID messageId, UUID recipientId, String additionalNote) {
        Message originalMessage = messageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new RuntimeException("Original message not found"));

        User recipient = userRepository.findByIdAndIsDeletedFalse(recipientId)
                .orElseThrow(() -> new UserNotFoundException("Recipient not found"));

        // Create forward
        MessageCreateRequest forwardRequest = new MessageCreateRequest();
        forwardRequest.setRecipientId(recipientId);
        forwardRequest.setSubject("Fwd: " + originalMessage.getSubject());
        
        String content = "--- Forwarded Message ---\n";
        if (additionalNote != null && !additionalNote.isEmpty()) {
            content += additionalNote + "\n\n";
        }
        content += "From: " + originalMessage.getSenderName() + "\n";
        content += "Subject: " + originalMessage.getSubject() + "\n";
        content += "Date: " + originalMessage.getCreatedAt() + "\n\n";
        content += originalMessage.getContent();
        
        forwardRequest.setContent(content);
        forwardRequest.setMessageType(MessageType.GENERAL);
        forwardRequest.setPriority(originalMessage.getPriority());
        forwardRequest.setBusinessId(originalMessage.getBusinessId());

        return createMessage(forwardRequest);
    }

    // Helper method to get recipients for broadcast
    private List<User> getRecipientsForBroadcast(BroadcastMessageRequest request) {
        if (request.getSendToAll()) {
            return userRepository.findByIsDeletedFalse();
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            return userRepository.findByIdInAndIsDeletedFalse(request.getUserIds());
        } else if (request.getUserTypes() != null && !request.getUserTypes().isEmpty()) {
            return userRepository.findByUserTypeInAndIsDeletedFalse(request.getUserTypes());
        } else if (request.getBusinessIds() != null && !request.getBusinessIds().isEmpty()) {
            return userRepository.findByBusinessIdInAndIsDeletedFalse(request.getBusinessIds());
        }
        
        return List.of();
    }
}