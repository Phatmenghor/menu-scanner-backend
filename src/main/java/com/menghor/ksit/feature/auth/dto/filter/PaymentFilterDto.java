package com.menghor.ksit.feature.auth.dto.filter;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StudentTypePayment;
import lombok.Data;

@Data
public class PaymentFilterDto {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String search;
    private StudentTypePayment type;
    private Status status;
    private Long userId;
}