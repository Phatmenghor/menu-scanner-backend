package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderPaymentFilterRequest;
import com.emenu.features.order.dto.response.OrderPaymentResponse;
import com.emenu.features.order.mapper.OrderPaymentMapper;
import com.emenu.features.order.models.OrderPayment;
import com.emenu.features.order.repository.OrderPaymentRepository;
import com.emenu.features.order.service.OrderPaymentService;
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

import com.emenu.enums.payment.PaymentStatus;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderPaymentRepository paymentRepository;
    private final OrderPaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final PaginationMapper paginationMapper;

    @Override
    public PaginationResponse<OrderPaymentResponse> getAllPayments(OrderPaymentFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();

        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        List<PaymentStatus> statuses = filter.getStatuses() != null && !filter.getStatuses().isEmpty()
                ? filter.getStatuses() : null;

        Page<OrderPayment> page = paymentRepository.findAllWithFilters(
                filter.getBusinessId(),
                statuses,
                filter.getPaymentMethod(),
                filter.getCustomerPaymentMethod(),
                filter.getCreatedFrom(),
                filter.getCreatedTo(),
                filter.getSearch(),
                pageable
        );
        return paymentMapper.toPaginationResponse(page, paginationMapper);
    }

    @Override
    public OrderPaymentResponse getPaymentById(UUID id) {
        OrderPayment payment = paymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public OrderPaymentResponse getPaymentByOrderId(UUID orderId) {
        OrderPayment payment = paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order"));
        return paymentMapper.toResponse(payment);
    }
}
