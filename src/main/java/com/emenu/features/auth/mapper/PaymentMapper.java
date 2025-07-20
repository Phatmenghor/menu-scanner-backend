package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.models.Payment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PaymentMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Payment toEntity(PaymentCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "plan.displayName", target = "planDisplayName")
    @Mapping(source = "plan.name", target = "planName")
    public abstract PaymentResponse toResponse(Payment payment);

    public abstract List<PaymentResponse> toResponseList(List<Payment> payments);

    // ✅ UNIVERSAL PAGINATION MAPPER USAGE
    public PaginationResponse<PaymentResponse> toPaginationResponse(Page<Payment> paymentPage) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}