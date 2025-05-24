package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StudentTypePayment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private String item;
    private StudentTypePayment type;
    private String amount;
    private String percentage;
    private LocalDate date;
    private Status status;
    private String commend;
    private Long userId;
    private LocalDateTime createdAt;
}
