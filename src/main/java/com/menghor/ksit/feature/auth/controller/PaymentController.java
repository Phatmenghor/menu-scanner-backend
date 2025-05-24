package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.filter.PaymentFilterDTO;
import com.menghor.ksit.feature.auth.dto.request.PaymentCreateDTO;
import com.menghor.ksit.feature.auth.dto.resposne.PaymentResponseDTO;
import com.menghor.ksit.feature.auth.dto.update.PaymentUpdateDTO;
import com.menghor.ksit.feature.auth.service.PaymentService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateDTO createDTO) {
        log.info("Creating payment for user: {}", createDTO.getUserId());
        PaymentResponseDTO payment = paymentService.createPayment(createDTO);
        log.info("Payment created successfully with ID: {}", payment.getId());
        return ApiResponse.success("Payment created successfully", payment);
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentResponseDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO updateDTO) {
        log.info("Updating payment with ID: {}", id);
        PaymentResponseDTO payment = paymentService.updatePayment(id, updateDTO);
        log.info("Payment updated successfully with ID: {}", id);
        return ApiResponse.success("Payment updated successfully", payment);
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        log.info("Retrieving payment with ID: {}", id);
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        log.info("Payment retrieved successfully with ID: {}", id);
        return ApiResponse.success("Payment retrieved successfully", payment);
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<PaymentResponseDTO>> getAllPayments(
            @ModelAttribute PaymentFilterDTO filterDto) {
        CustomPaginationResponseDto<PaymentResponseDTO> payments = paymentService.getAllPayments(filterDto);
        return ApiResponse.success("Payments retrieved successfully", payments);
    }


    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ApiResponse.success("Payment deleted successfully", "Payment with ID " + id + " has been deleted");
    }
}