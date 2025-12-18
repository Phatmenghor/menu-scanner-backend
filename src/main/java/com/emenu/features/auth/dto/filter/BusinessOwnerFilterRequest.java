package com.emenu.features.auth.dto.filter;

import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.BusinessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerFilterRequest {
    
    private List<BusinessStatus> businessStatuses;
    private List<AccountStatus> ownerAccountStatuses;
    private List<String> subscriptionStatuses;
    private List<PaymentStatus> paymentStatuses;
    private Boolean autoRenew;
    private Integer expiringSoonDays;
    
    private String search;
    
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    
    private Integer pageNo;
    private Integer pageSize;
    private String sortBy;
    private String sortDir;
    
    public Integer getPageNo() {
        return pageNo != null && pageNo >= 0 ? pageNo : 0;
    }
    
    public Integer getPageSize() {
        return pageSize != null && pageSize > 0 ? pageSize : 10;
    }
    
    public String getSortBy() {
        return sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt";
    }
    
    public String getSortDir() {
        return sortDir != null && sortDir.equalsIgnoreCase("asc") ? "asc" : "desc";
    }
    
    public Integer getExpiringSoonDays() {
        return expiringSoonDays != null && expiringSoonDays > 0 ? expiringSoonDays : 7;
    }
}