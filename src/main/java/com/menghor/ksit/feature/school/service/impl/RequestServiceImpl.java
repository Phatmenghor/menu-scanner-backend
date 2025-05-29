package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.dto.filter.RequestFilterDto;
import com.menghor.ksit.feature.school.dto.filter.RequestHistoryFilterDto;
import com.menghor.ksit.feature.school.dto.request.RequestCreateDto;
import com.menghor.ksit.feature.school.dto.response.RequestHistoryDto;
import com.menghor.ksit.feature.school.dto.response.RequestResponseDto;
import com.menghor.ksit.feature.school.dto.update.RequestUpdateDto;
import com.menghor.ksit.feature.school.mapper.RequestMapper;
import com.menghor.ksit.feature.school.model.RequestEntity;
import com.menghor.ksit.feature.school.model.RequestHistoryEntity;
import com.menghor.ksit.feature.school.repository.RequestHistoryRepository;
import com.menghor.ksit.feature.school.repository.RequestRepository;
import com.menghor.ksit.feature.school.service.RequestService;
import com.menghor.ksit.feature.school.specification.RequestHistorySpecification;
import com.menghor.ksit.feature.school.specification.RequestSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    
    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;
    private final RequestMapper requestMapper;
    private final SecurityUtils securityUtils;
    
    @Override
    @Transactional
    public RequestResponseDto createRequest(RequestCreateDto createDto) {
        log.info("Creating new request with title: {}", createDto.getTitle());
        
        UserEntity currentUser = securityUtils.getCurrentUser();
        
        // Use MapStruct to convert DTO to entity
        RequestEntity request = requestMapper.toEntity(createDto);
        request.setUser(currentUser);
        
        RequestEntity savedRequest = requestRepository.save(request);
        
        // Create history entry
        createHistoryEntry(savedRequest, RequestStatus.PENDING, RequestStatus.PENDING,
            "Request created by user", currentUser);
        
        log.info("Request created successfully with ID: {}", savedRequest.getId());
        return requestMapper.toResponseDto(savedRequest);
    }
    
    @Override
    @Transactional
    public RequestResponseDto updateRequest(Long id, RequestUpdateDto updateDto) {
        log.info("Updating request with ID: {}", id);
        
        RequestEntity request = findRequestById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Use MapStruct to update entity from DTO (only non-null values)
        requestMapper.updateEntityFromDto(updateDto, request);
        
        RequestEntity updatedRequest = requestRepository.save(request);
        
        // Create history entry
        String action = currentUser.isOther() ? "Request updated by staff" : "Request updated by user";
        createHistoryEntry(updatedRequest, request.getStatus(), updateDto.getStatus(), action, currentUser);
        
        log.info("Request with ID {} updated successfully", id);
        return requestMapper.toResponseDto(updatedRequest);
    }
    
    @Override
    public RequestResponseDto getRequestById(Long id) {
        log.info("Fetching request with ID: {}", id);
        
        RequestEntity request = findRequestById(id);
        return requestMapper.toResponseDto(request);
    }
    
    @Override
    public CustomPaginationResponseDto<RequestResponseDto> getAllRequests(RequestFilterDto filterDto) {
        log.info("Fetching all requests with filter: {}", filterDto);
        
        Pageable pageable = PaginationUtils.createPageable(
            filterDto.getPageNo(),
            filterDto.getPageSize(),
            "createdAt",
            "DESC"
        );
        
        Specification<RequestEntity> spec = RequestSpecification.createSpecification(filterDto);
        Page<RequestEntity> requestPage = requestRepository.findAll(spec, pageable);
        
        // Use list version for better performance in list view
        return requestMapper.toListPaginationResponse(requestPage);
    }

    @Override
    @Transactional()
    public CustomPaginationResponseDto<RequestHistoryDto> getRequestHistory(RequestHistoryFilterDto filterDto) {
        log.info("Fetching history with filter: {}", filterDto);

        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        Specification<RequestHistoryEntity> spec = RequestHistorySpecification.createSpecification(filterDto);
        Page<RequestHistoryEntity> historyPage = historyRepository.findAll(spec, pageable);

        return requestMapper.toHistoryPaginationResponse(historyPage);
    }


    @Override
    @Transactional
    public RequestResponseDto deleteRequest(Long id) {
        log.info("Deleting request with ID: {}", id);

        RequestEntity request = findRequestById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Store the old status before updating
        RequestStatus oldStatus = request.getStatus();

        // Soft delete: Set status to DELETED instead of physically removing the record
        request.setStatus(RequestStatus.DELETED);

        // Create history entry for the status change
        String action = currentUser.isOther() ? "Request deleted by staff" : "Request deleted by user";
        createHistoryEntry(request, oldStatus, RequestStatus.DELETED, action, currentUser);

        // Save the updated request with new status
        RequestEntity updatedRequest = requestRepository.save(request);

        RequestResponseDto responseDto = requestMapper.toResponseDto(updatedRequest);

        log.info("Request with ID {} marked as deleted successfully", id);
        return responseDto;
    }
    // Private helper methods
    
    private RequestEntity findRequestById(Long id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Request not found with ID: " + id));
    }
    
    private void createHistoryEntry(RequestEntity request, RequestStatus fromStatus, 
                                  RequestStatus toStatus, String comment, UserEntity user) {
        RequestHistoryEntity history = new RequestHistoryEntity();
        history.setRequest(request);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setComment(comment);
        history.setActionBy(user.getUsername());
        history.setUser(user);
        
        historyRepository.save(history);
    }
}