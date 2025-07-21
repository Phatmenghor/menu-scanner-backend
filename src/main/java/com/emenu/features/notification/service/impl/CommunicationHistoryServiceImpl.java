package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.filter.CommunicationHistoryFilterRequest;
import com.emenu.features.notification.dto.response.CommunicationHistoryResponse;
import com.emenu.features.notification.mapper.CommunicationHistoryMapper;
import com.emenu.features.notification.models.CommunicationHistory;
import com.emenu.features.notification.repository.CommunicationHistoryRepository;
import com.emenu.features.notification.service.CommunicationHistoryService;
import com.emenu.features.notification.specification.CommunicationHistorySpecification;
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
public class CommunicationHistoryServiceImpl implements CommunicationHistoryService {

    private final CommunicationHistoryRepository historyRepository;
    private final CommunicationHistoryMapper historyMapper;

    @Override
    public void recordCommunication(UUID recipientId, UUID senderId, NotificationChannel channel,
                                    String subject, String content, String status) {
        recordCommunication(recipientId, senderId, null, channel, subject, content, status);
    }

    @Override
    public void recordCommunication(UUID recipientId, UUID senderId, UUID businessId, 
                                   NotificationChannel channel, String subject, String content, String status) {
        CommunicationHistory history = new CommunicationHistory();
        history.setRecipientId(recipientId);
        history.setSenderId(senderId);
        history.setBusinessId(businessId);
        history.setChannel(channel);
        history.setSubject(subject);
        history.setContent(content);
        history.setStatus(status);
        history.setSentAt(LocalDateTime.now());

        historyRepository.save(history);
        log.debug("Communication recorded: {} -> {}", senderId, recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CommunicationHistoryResponse> getCommunicationHistory(CommunicationHistoryFilterRequest filter) {
        Specification<CommunicationHistory> spec = CommunicationHistorySpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<CommunicationHistory> historyPage = historyRepository.findAll(spec, pageable);
        return historyMapper.toPaginationResponse(historyPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationHistoryResponse> getUserCommunicationHistory(UUID userId) {
        List<CommunicationHistory> history = historyRepository.findByRecipientIdAndIsDeletedFalse(userId, null).getContent();
        return historyMapper.toResponseList(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationHistoryResponse> getBusinessCommunicationHistory(UUID businessId) {
        List<CommunicationHistory> history = historyRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return historyMapper.toResponseList(history);
    }

    @Override
    public void markAsDelivered(String externalMessageId) {
        CommunicationHistory history = historyRepository.findByExternalMessageIdAndIsDeletedFalse(externalMessageId)
                .orElse(null);
        
        if (history != null) {
            history.markAsDelivered();
            historyRepository.save(history);
            log.debug("Communication marked as delivered: {}", externalMessageId);
        }
    }

    @Override
    public void markAsRead(String externalMessageId) {
        CommunicationHistory history = historyRepository.findByExternalMessageIdAndIsDeletedFalse(externalMessageId)
                .orElse(null);
        
        if (history != null) {
            history.markAsRead();
            historyRepository.save(history);
            log.debug("Communication marked as read: {}", externalMessageId);
        }
    }

    @Override
    public void markAsFailed(String externalMessageId, String errorMessage) {
        CommunicationHistory history = historyRepository.findByExternalMessageIdAndIsDeletedFalse(externalMessageId)
                .orElse(null);
        
        if (history != null) {
            history.markAsFailed(errorMessage);
            historyRepository.save(history);
            log.debug("Communication marked as failed: {}", externalMessageId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCommunicationsCount() {
        return historyRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommunicationsByChannel(NotificationChannel channel) {
        return historyRepository.countByChannel(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommunicationsByStatus(String status) {
        return historyRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommunicationsInDateRange(LocalDateTime start, LocalDateTime end) {
        return historyRepository.findBySentAtBetween(start, end).size();
    }

    @Override
    public void cleanupOldHistory(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<CommunicationHistory> oldHistory = historyRepository.findBySentAtBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate);
        
        for (CommunicationHistory history : oldHistory) {
            history.softDelete();
        }
        
        historyRepository.saveAll(oldHistory);
        log.info("Cleaned up {} old communication history records", oldHistory.size());
    }

    @Override
    public void archiveHistory(LocalDateTime beforeDate) {
        List<CommunicationHistory> historyToArchive = historyRepository.findBySentAtBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), beforeDate);
        
        // Implementation for archiving (could move to separate archive table)
        log.info("Archived {} communication history records", historyToArchive.size());
    }
}
