package com.emenu.feature.auth.dto.update;

import com.emenu.enumations.Status;
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
