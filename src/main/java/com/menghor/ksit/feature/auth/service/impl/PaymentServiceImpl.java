package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.filter.PaymentFilterDTO;
import com.menghor.ksit.feature.auth.dto.request.PaymentCreateDTO;
import com.menghor.ksit.feature.auth.dto.resposne.PaymentResponseDTO;
import com.menghor.ksit.feature.auth.dto.update.PaymentUpdateDTO;
import com.menghor.ksit.feature.auth.mapper.PaymentMapper;
import com.menghor.ksit.feature.auth.models.PaymentEntity;
import com.menghor.ksit.feature.auth.repository.PaymentRepository;
import com.menghor.ksit.feature.auth.service.PaymentService;
import com.menghor.ksit.feature.auth.specification.PaymentSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponseDTO createPayment(PaymentCreateDTO createDTO) {
        log.info("Creating new payment for user: {}", createDTO.getUserId());

        PaymentEntity payment = paymentMapper.toEntity(createDTO);
        PaymentEntity savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully with ID: {}", savedPayment.getId());
        return paymentMapper.toResponseDto(savedPayment);
    }

    @Override
    public PaymentResponseDTO updatePayment(Long id, PaymentUpdateDTO updateDTO) {
        log.info("Updating payment with ID: {}", id);

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));

        paymentMapper.updateEntityFromDto(updateDTO, payment);
        PaymentEntity updatedPayment = paymentRepository.save(payment);

        log.info("Payment updated successfully with ID: {}", id);
        return paymentMapper.toResponseDto(updatedPayment);
    }

    @Override
    @Transactional()
    public PaymentResponseDTO getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional()
    public CustomPaginationResponseDto<PaymentResponseDTO> getAllPayments(PaymentFilterDTO filterDto) {
        log.info("Fetching all payments with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<PaymentEntity> spec = PaymentSpecification.combine(
                filterDto.getSearch(),
                filterDto.getType(),
                filterDto.getStatus(),
                filterDto.getUserId()
        );

        // Execute query with specification and pagination
        Page<PaymentEntity> paymentPage = paymentRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        paymentPage.getContent().forEach(payment -> {
            if (payment.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for payment ID: {}", payment.getId());
                payment.setStatus(Status.ACTIVE);
                paymentRepository.save(payment);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<PaymentResponseDTO> response = paymentMapper.toPaymentAllResponseDto(paymentPage);
        log.info("Retrieved {} payments (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    @Override
    public void deletePayment(Long id) {
        log.info("Soft deleting payment with ID: {}", id);

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));

        payment.setStatus(Status.DELETED);
        paymentRepository.save(payment);

        log.info("Payment soft deleted successfully with ID: {}", id);
    }

}