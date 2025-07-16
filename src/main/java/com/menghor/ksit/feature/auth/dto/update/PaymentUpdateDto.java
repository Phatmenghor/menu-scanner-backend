package com.menghor.ksit.feature.auth.dto.update;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentUpdateDto {
    private String item;
    private StudentTypePayment type;
    private String amount;
    private String percentage;
    private LocalDate date;
    private Status status;
    private String commend;
    private Long userId;
}
