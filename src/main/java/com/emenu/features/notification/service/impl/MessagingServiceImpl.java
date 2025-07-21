package com.emenu.features.notification.service.impl;

import com.emenu.features.notification.dto.filter.MessageThreadFilterRequest;
import com.emenu.features.notification.dto.request.MessageCreateRequest;
import com.emenu.features.notification.dto.request.MessageThreadCreateRequest;
import com.emenu.features.notification.dto.response.MessageResponse;
import com.emenu.features.notification.dto.response.MessageThreadResponse;
import com.emenu.features.notification.dto.response.MessagingStatsResponse;
import com.emenu.features.notification.mapper.MessageMapper;
import com.emenu.features.notification.mapper.MessageThreadMapper;
import com.emenu.features.notification.models.Message;
import com.emenu.features.notification.models.MessageThread;
import com.emenu.features.notification.repository.MessageRepository;
import com.emenu.features.notification.repository.MessageThreadRepository;
import com.emenu.features.notification.service.MessagingService;
import com.emenu.features.notification.specification.MessageThreadSpecification;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessagingServiceImpl implements MessagingService {

    private final MessageThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final MessageThreadMapper threadMapper;
    private final MessageMapper messageMapper;
    private final SecurityUtils securityUtils;

    @Override
    public MessageThreadResponse createThread(MessageThreadCreateRequest request) {
        log.info("Creating message thread: {}", request.getSubject());

        // Create thread
        MessageThread thread = threadMapper.toEntity(request);
        MessageThread savedThread = threadRepository.save(thread);

        // Create initial message
        Message initialMessage = new Message();
        initialMessage.setThreadId(savedThread.getId());
        initialMessage.setSenderId(securityUtils.getCurrentUserId());
        initialMessage.setSenderName(securityUtils.getCurrentUser().getFullName());
        initialMessage.setSenderEmail(securityUtils.getCurrentUser().getEmail());
        initialMessage.setContent(request.getContent());
        initialMessage.setHtmlContent(request.getHtmlContent());
        initialMessage.setIsSystemMessage(request.getIsSystemGenerated());

        messageRepository.save(initialMessage);

        // Update thread with last message time
        savedThread.updateLastMessageTime();
        threadRepository.save(savedThread);

        log.info("Message thread created successfully: {}", savedThread.getId());
        return threadMapper.toResponse(savedThread);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageThreadResponse> getThreads(MessageThreadFilterRequest filter) {
        Specification<MessageThread> spec = MessageThreadSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<MessageThread> threadPage = threadRepository.findAll(spec, pageable);
        return threadMapper.toPaginationResponse(threadPage);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageThreadResponse getThreadById(UUID threadId) {
        MessageThread thread = threadRepository.findByIdAndIsDeletedFalse(threadId)
                .orElseThrow(() -> new RuntimeException("Message thread not found"));

        return threadMapper.toResponse(thread);
    }

    @Override
    public void closeThread(UUID threadId) {
        MessageThread thread = threadRepository.findByIdAndIsDeletedFalse(threadId)
                .orElseThrow(() -> new RuntimeException("Message thread not found"));

        thread.closeThread(securityUtils.getCurrentUserId());
        threadRepository.save(thread);
        log.info("Message thread closed: {}", threadId);
    }

    @Override
    public void reopenThread(UUID threadId) {
        MessageThread thread = threadRepository.findByIdAndIsDeletedFalse(threadId)
                .orElseThrow(() -> new RuntimeException("Message thread not found"));

        thread.reopenThread();
        threadRepository.save(thread);
        log.info("Message thread reopened: {}", threadId);
    }

    @Override
    public MessageResponse sendMessage(MessageCreateRequest request) {
        log.info("Sending message to thread: {}", request.getThreadId());

        // Validate thread exists
        MessageThread thread = threadRepository.findByIdAndIsDeletedFalse(request.getThreadId())
                .orElseThrow(() -> new RuntimeException("Message thread not found"));

        // Create message
        Message message = messageMapper.toEntity(request);
        message.setSenderId(securityUtils.getCurrentUserId());
        message.setSenderName(securityUtils.getCurrentUser().getFullName());
        message.setSenderEmail(securityUtils.getCurrentUser().getEmail());

        Message savedMessage = messageRepository.save(message);

        // Update thread
        thread.updateLastMessageTime();
        thread.incrementUnreadCount();
        threadRepository.save(thread);

        log.info("Message sent successfully: {}", savedMessage.getId());
        return messageMapper.toResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getThreadMessages(UUID threadId) {
        List<Message> messages = messageRepository.findByThreadIdAndIsDeletedFalseOrderByCreatedAtAsc(threadId);
        return messageMapper.toResponseList(messages);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(UUID messageId) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        return messageMapper.toResponse(message);
    }

    @Override
    public void markMessageAsRead(UUID messageId) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.markAsRead();
        messageRepository.save(message);
        log.info("Message marked as read: {}", messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageThreadResponse> getUserThreads(UUID userId) {
        List<MessageThread> threads = threadRepository.findByParticipantId(userId);
        return threadMapper.toResponseList(threads);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageThreadResponse> getOpenThreadsForUser(UUID userId) {
        List<MessageThread> threads = threadRepository.findOpenThreadsByParticipantId(userId);
        return threadMapper.toResponseList(threads);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(UUID userId) {
        return messageRepository.countUnreadBySender(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageThreadResponse> getBusinessThreads(UUID businessId) {
        List<MessageThread> threads = threadRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return threadMapper.toResponseList(threads);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBusinessThreadCount(UUID businessId) {
        return threadRepository.countByBusinessId(businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public MessagingStatsResponse getMessagingStats() {
        MessagingStatsResponse stats = new MessagingStatsResponse();

        // Thread statistics
        stats.setTotalThreads(threadRepository.count());
        stats.setOpenThreads(threadRepository.countOpenThreads());
        stats.setClosedThreads(stats.getTotalThreads() - stats.getOpenThreads());
        stats.setSystemThreads(threadRepository.countSystemThreads());

        // Message statistics
        stats.setTotalMessages(messageRepository.count());
        
        // Get today's messages
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        stats.setTodayMessages((long) messageRepository.findByCreatedAtBetween(startOfDay, endOfDay).size());

        // Get weekly messages
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        stats.setWeeklyMessages((long) messageRepository.findByCreatedAtBetween(startOfWeek, LocalDateTime.now()).size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public MessagingStatsResponse getBusinessMessagingStats(UUID businessId) {
        MessagingStatsResponse stats = new MessagingStatsResponse();

        // Business-specific statistics
        stats.setTotalThreads(threadRepository.countByBusinessId(businessId));
        
        return stats;
    }
}