package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentSummaryResponse {
    private Long totalPayments;
    private Long completedPayments;
    private Long pendingPayments;
    private Long failedPayments;
    private Long overduePayments;
    
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private BigDecimal pendingAmount;
    private BigDecimal overdueAmount;
    
    private BigDecimal totalRevenueKhr;
    private BigDecimal monthlyRevenueKhr;
    private BigDecimal yearlyRevenueKhr;
    
    private Double averagePaymentAmount;
    private Double completionRate; // percentage of completed payments
}