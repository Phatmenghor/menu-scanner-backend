package com.emenu.features.services.service;

import java.util.UUID;

public interface PaymentSecurityService {
    boolean canAccessPayment(UUID paymentId);
    boolean isPaymentOwner(UUID paymentId);
}