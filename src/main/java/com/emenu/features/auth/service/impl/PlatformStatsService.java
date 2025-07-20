package com.emenu.features.auth.service.impl;

import com.emenu.enums.BusinessStatus;
import com.emenu.enums.MessageStatus;
import com.emenu.enums.SubscriptionPlan;
import com.emenu.enums.UserType;
import com.emenu.features.auth.dto.response.PlatformStatsResponse;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.features.subscription.repository.PaymentRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlatformStatsService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final MessageRepository messageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public PlatformStatsResponse getPlatformStats() {
        PlatformStatsResponse stats = new PlatformStatsResponse();

        // User statistics
        stats.setTotalUsers(userRepository.countByIsDeletedFalse());
        stats.setTotalCustomers(userRepository.countByUserTypeAndIsDeletedFalse(UserType.CUSTOMER));

        // Business statistics
        stats.setTotalBusinesses(businessRepository.countByIsDeletedFalse());
        stats.setActiveBusinesses(businessRepository.countByStatusAndIsDeletedFalse(BusinessStatus.ACTIVE));
        stats.setSuspendedBusinesses(businessRepository.countByStatusAndIsDeletedFalse(BusinessStatus.SUSPENDED));

        // Message statistics
        stats.setTotalMessages(messageRepository.countByIsDeletedFalse());
        stats.setUnreadMessages(messageRepository.countByStatusAndIsDeletedFalse(MessageStatus.SENT));

        // Subscription statistics
        stats.setFreeSubscriptions(subscriptionRepository.countByPlan(SubscriptionPlan.FREE));
        stats.setBasicSubscriptions(subscriptionRepository.countByPlan(SubscriptionPlan.BASIC));
        stats.setProfessionalSubscriptions(subscriptionRepository.countByPlan(SubscriptionPlan.PROFESSIONAL));
        stats.setEnterpriseSubscriptions(subscriptionRepository.countByPlan(SubscriptionPlan.ENTERPRISE));

        // Revenue statistics
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        stats.setMonthlyRevenue(paymentRepository.calculateRevenueForPeriod(startOfMonth, endOfMonth));
        stats.setTotalRevenue(paymentRepository.calculateRevenueForPeriod(
                LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.now()));

        return stats;
    }
}