package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.filter.PaymentFilterDTO;
import com.menghor.ksit.feature.auth.dto.request.PaymentCreateDTO;
import com.menghor.ksit.feature.auth.dto.resposne.PaymentResponseDTO;
import com.menghor.ksit.feature.auth.dto.update.PaymentUpdateDTO;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    
    PaymentResponseDTO createPayment(PaymentCreateDTO createDTO);
    
    PaymentResponseDTO updatePayment(Long id, PaymentUpdateDTO updateDTO);
    
    PaymentResponseDTO getPaymentById(Long id);
    
    CustomPaginationResponseDto<PaymentResponseDTO> getAllPayments(PaymentFilterDTO filterDto);

    void deletePayment(Long id);
}