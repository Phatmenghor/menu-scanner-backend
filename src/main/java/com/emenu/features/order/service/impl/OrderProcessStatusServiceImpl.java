package com.emenu.features.order.service.impl;

import com.emenu.enums.common.Status;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderProcessStatusFilterRequest;
import com.emenu.features.order.dto.request.OrderProcessStatusCreateRequest;
import com.emenu.features.order.dto.response.OrderProcessStatusResponse;
import com.emenu.features.order.dto.update.OrderProcessStatusUpdateRequest;
import com.emenu.features.order.mapper.OrderProcessStatusMapper;
import com.emenu.features.order.models.OrderProcessStatus;
import com.emenu.features.order.repository.OrderProcessStatusRepository;
import com.emenu.features.order.service.OrderProcessStatusService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderProcessStatusServiceImpl implements OrderProcessStatusService {

    private final OrderProcessStatusRepository orderProcessStatusRepository;
    private final OrderProcessStatusMapper orderProcessStatusMapper;
    private final SecurityUtils securityUtils;
    private final PaginationMapper paginationMapper;

    @Override
    public OrderProcessStatusResponse createOrderProcessStatus(OrderProcessStatusCreateRequest request) {
        log.info("Creating order process status: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        if (orderProcessStatusRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                request.getName(), currentUser.getBusinessId())) {
            throw new ValidationException("Order process status name already exists in your business");
        }

        OrderProcessStatus orderProcessStatus = orderProcessStatusMapper.toEntity(request);
        orderProcessStatus.setBusinessId(currentUser.getBusinessId());

        OrderProcessStatus saved = orderProcessStatusRepository.save(orderProcessStatus);

        log.info("Order process status created successfully: {} for business: {}",
                saved.getName(), currentUser.getBusinessId());

        return orderProcessStatusMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<OrderProcessStatusResponse> getAllOrderProcessStatuses(OrderProcessStatusFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        List<Status> statuses = filter.getStatuses() != null && !filter.getStatuses().isEmpty()
                ? filter.getStatuses() : null;

        Page<OrderProcessStatus> page = orderProcessStatusRepository.findAllWithFilters(
                filter.getBusinessId(),
                statuses,
                filter.getSearch(),
                pageable
        );

        return orderProcessStatusMapper.toPaginationResponse(page, paginationMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderProcessStatusResponse> getBusinessOrderProcessStatuses(UUID businessId) {
        List<OrderProcessStatus> statuses = orderProcessStatusRepository
                .findByBusinessIdAndStatusOrderByCreatedAtAsc(businessId, Status.ACTIVE);
        return orderProcessStatusMapper.toResponseList(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderProcessStatusResponse getOrderProcessStatusById(UUID id) {
        OrderProcessStatus status = findById(id);
        return orderProcessStatusMapper.toResponse(status);
    }

    @Override
    public OrderProcessStatusResponse updateOrderProcessStatus(UUID id, OrderProcessStatusUpdateRequest request) {
        OrderProcessStatus orderProcessStatus = findById(id);

        if (request.getName() != null && !request.getName().equals(orderProcessStatus.getName())) {
            if (orderProcessStatusRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                    request.getName(), orderProcessStatus.getBusinessId())) {
                throw new ValidationException("Order process status name already exists in your business");
            }
        }

        orderProcessStatusMapper.updateEntity(request, orderProcessStatus);
        OrderProcessStatus updated = orderProcessStatusRepository.save(orderProcessStatus);

        log.info("Order process status updated successfully: {}", id);
        return orderProcessStatusMapper.toResponse(updated);
    }

    @Override
    public OrderProcessStatusResponse deleteOrderProcessStatus(UUID id) {
        OrderProcessStatus orderProcessStatus = findById(id);

        orderProcessStatus.softDelete();
        orderProcessStatus = orderProcessStatusRepository.save(orderProcessStatus);

        log.info("Order process status deleted successfully: {}", id);
        return orderProcessStatusMapper.toResponse(orderProcessStatus);
    }

    private OrderProcessStatus findById(UUID id) {
        return orderProcessStatusRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Order process status not found"));
    }

    private void validateUserBusinessAssociation(User user) {
        if (user.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
    }

    private void validateStatusType(String statusType) {
        if (!"ACTIVE".equals(statusType) && !"COMPLETED".equals(statusType) && !"CANCELLED".equals(statusType)) {
            throw new ValidationException("Status type must be one of: ACTIVE, COMPLETED, CANCELLED");
        }
    }
}
