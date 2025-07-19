package com.emenu.features.services.mapper;

import com.emenu.features.services.domain.PaymentRecord;
import com.emenu.features.services.dto.response.PaymentResponse;
import com.emenu.features.services.dto.update.UpdatePaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {
    
    @Mapping(target = "userEmail", ignore = true)
    PaymentResponse toResponse(PaymentRecord payment);
    
    void updateEntity(UpdatePaymentRequest request, @MappingTarget PaymentRecord payment);
}
