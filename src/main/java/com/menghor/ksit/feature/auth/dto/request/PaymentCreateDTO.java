package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StudentTypePayment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCreateDTO {
    @NotBlank(message = "Item is required")
    private String item;

    @NotNull(message = "Type is required")
    private StudentTypePayment type;

    private String amount;

    private String percentage;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private Status status = Status.ACTIVE;

    private String commend;

    @NotNull(message = "User ID is required")
    private Long userId;
}