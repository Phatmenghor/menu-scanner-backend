package com.emenu.features.payment.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.payment.dto.filter.BusinessOrderPaymentFilterRequest;
import com.emenu.features.payment.dto.response.BusinessOrderPaymentResponse;
import com.emenu.features.payment.mapper.BusinessOrderPaymentMapper;
import com.emenu.features.payment.models.BusinessOrderPayment;
import com.emenu.features.payment.repository.BusinessOrderPaymentRepository;
import com.emenu.features.payment.service.BusinessOrderPaymentService;
import com.emenu.features.payment.specification.BusinessOrderPaymentSpecification;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessOrderPaymentServiceImpl implements BusinessOrderPaymentService {

    private final BusinessOrderPaymentRepository paymentRepository;
    private final BusinessOrderPaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;

    @Override
    public PaginationResponse<BusinessOrderPaymentResponse> getAllPayments(BusinessOrderPaymentFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();

        // Business users can only see their own payments
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Specification<BusinessOrderPayment> spec = BusinessOrderPaymentSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<BusinessOrderPayment> paymentPage = paymentRepository.findAll(spec, pageable);
        return paymentMapper.toPaginationResponse(paymentPage);
    }

    @Override
    public BusinessOrderPaymentResponse getPaymentById(UUID id) {
        BusinessOrderPayment payment = paymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public BusinessOrderPaymentResponse getPaymentByOrderId(UUID orderId) {
        BusinessOrderPayment payment = paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order"));
        return paymentMapper.toResponse(payment);
    }
}