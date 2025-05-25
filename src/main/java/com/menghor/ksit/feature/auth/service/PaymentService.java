package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.filter.PaymentFilterDto;
import com.menghor.ksit.feature.auth.dto.request.PaymentCreateDTO;
import com.menghor.ksit.feature.auth.dto.resposne.PaymentResponseDTO;
import com.menghor.ksit.feature.auth.dto.update.PaymentUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface PaymentService {
    
    PaymentResponseDTO createPayment(PaymentCreateDTO createDTO);
    
    PaymentResponseDTO updatePayment(Long id, PaymentUpdateDto updateDTO);
    
    PaymentResponseDTO getPaymentById(Long id);
    
    CustomPaginationResponseDto<PaymentResponseDTO> getAllPayments(PaymentFilterDto filterDto);

    PaymentResponseDTO deletePayment(Long id);
}