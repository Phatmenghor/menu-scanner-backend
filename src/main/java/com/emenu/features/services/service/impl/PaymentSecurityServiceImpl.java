package com.emenu.features.services.service.impl;

import com.emenu.features.services.repository.PaymentRecordRepository;
import com.emenu.features.services.service.PaymentSecurityService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentSecurityServiceImpl implements PaymentSecurityService {

    private final PaymentRecordRepository paymentRepository;
    private final SecurityUtils securityUtils;

    @Override
    public boolean canAccessPayment(UUID paymentId) {
        if (securityUtils.isPlatformAdmin()) {
            return true;
        }

        return paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .map(payment -> securityUtils.isCurrentUser(payment.getUserId()))
                .orElse(false);
    }

    @Override
    public boolean isPaymentOwner(UUID paymentId) {
        return paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .map(payment -> securityUtils.isCurrentUser(payment.getUserId()))
                .orElse(false);
    }
}