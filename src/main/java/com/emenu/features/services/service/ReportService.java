package com.emenu.features.services.service;

import com.emenu.features.services.dto.response.PaymentStatsResponse;
import com.emenu.features.services.dto.response.PlanStatsResponse;
import com.emenu.features.services.dto.response.SubscriptionStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    SubscriptionStatsResponse getSubscriptionStats();
    PaymentStatsResponse getPaymentStats();
    List<PlanStatsResponse> getPlanStats();
    SubscriptionStatsResponse getSubscriptionStatsForPeriod(LocalDate startDate, LocalDate endDate);
    PaymentStatsResponse getPaymentStatsForPeriod(LocalDate startDate, LocalDate endDate);
}