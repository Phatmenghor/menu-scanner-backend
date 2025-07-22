package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.response.PaymentSummaryResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.features.auth.models.Payment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PaymentMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "amountKhr", ignore = true) // Will be calculated
    public abstract Payment toEntity(PaymentCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(target = "statusDescription", expression = "java(payment.getStatus().getDescription())")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "formattedAmountKhr", expression = "java(payment.getFormattedAmountKhr())")
    @Mapping(target = "isOverdue", expression = "java(payment.isOverdue())")
    @Mapping(target = "daysUntilDue", expression = "java(payment.getDaysUntilDue())")
    @Mapping(target = "processedByName", ignore = true) // Will be set in @AfterMapping
    public abstract PaymentResponse toResponse(Payment payment);

    public abstract List<PaymentResponse> toResponseList(List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(PaymentUpdateRequest request, @MappingTarget Payment payment);

    @AfterMapping
    protected void calculateKhrAmount(@MappingTarget Payment payment, PaymentCreateRequest request) {
        if (request.getExchangeRate() != null && payment.getAmount() != null) {
            payment.calculateAmountKhr(request.getExchangeRate());
        }
    }

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget PaymentResponse response, Payment payment) {
        // Additional calculated fields if needed
        if (payment.getStatus() != null) {
            response.setStatusDescription(payment.getStatus().getDescription());
        }
    }

    // Custom mapping for payment summary
    public PaymentSummaryResponse toSummaryResponse(
            Long totalPayments,
            Long completedPayments, 
            Long pendingPayments,
            Long failedPayments,
            Long overduePayments,
            BigDecimal totalRevenue,
            BigDecimal monthlyRevenue,
            BigDecimal yearlyRevenue,
            BigDecimal pendingAmount,
            BigDecimal overdueAmount,
            BigDecimal totalRevenueKhr,
            BigDecimal monthlyRevenueKhr,
            BigDecimal yearlyRevenueKhr,
            Double averagePaymentAmount) {
        
        PaymentSummaryResponse summary = new PaymentSummaryResponse();
        summary.setTotalPayments(totalPayments != null ? totalPayments : 0L);
        summary.setCompletedPayments(completedPayments != null ? completedPayments : 0L);
        summary.setPendingPayments(pendingPayments != null ? pendingPayments : 0L);
        summary.setFailedPayments(failedPayments != null ? failedPayments : 0L);
        summary.setOverduePayments(overduePayments != null ? overduePayments : 0L);
        
        summary.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        summary.setYearlyRevenue(yearlyRevenue != null ? yearlyRevenue : BigDecimal.ZERO);
        summary.setPendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO);
        summary.setOverdueAmount(overdueAmount != null ? overdueAmount : BigDecimal.ZERO);
        
        summary.setTotalRevenueKhr(totalRevenueKhr != null ? totalRevenueKhr : BigDecimal.ZERO);
        summary.setMonthlyRevenueKhr(monthlyRevenueKhr != null ? monthlyRevenueKhr : BigDecimal.ZERO);
        summary.setYearlyRevenueKhr(yearlyRevenueKhr != null ? yearlyRevenueKhr : BigDecimal.ZERO);
        
        summary.setAveragePaymentAmount(averagePaymentAmount != null ? averagePaymentAmount : 0.0);
        
        // Calculate completion rate
        if (totalPayments != null && totalPayments > 0) {
            double rate = (completedPayments != null ? completedPayments.doubleValue() : 0.0) / totalPayments.doubleValue() * 100;
            summary.setCompletionRate(Math.round(rate * 100.0) / 100.0); // Round to 2 decimal places
        } else {
            summary.setCompletionRate(0.0);
        }
        
        return summary;
    }

    // Universal pagination mapper usage
    public PaginationResponse<PaymentResponse> toPaginationResponse(Page<Payment> paymentPage) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}