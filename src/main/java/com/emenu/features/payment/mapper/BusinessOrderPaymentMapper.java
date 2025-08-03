package com.emenu.features.payment.mapper;

import com.emenu.features.payment.dto.request.BusinessOrderPaymentCreateRequest;
import com.emenu.features.payment.dto.response.BusinessOrderPaymentResponse;
import com.emenu.features.payment.dto.update.BusinessOrderPaymentUpdateRequest;
import com.emenu.features.payment.models.BusinessOrderPayment;
import org.mapstruct.*;

import java.text.DecimalFormat;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessOrderPaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract BusinessOrderPayment toEntity(BusinessOrderPaymentCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    @Mapping(target = "formattedAmount", expression = "java(formatAmount(payment.getAmount()))")
    public abstract BusinessOrderPaymentResponse toResponse(BusinessOrderPayment payment);

    public abstract List<BusinessOrderPaymentResponse> toResponseList(List<BusinessOrderPayment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(BusinessOrderPaymentUpdateRequest request, @MappingTarget BusinessOrderPayment payment);

    protected String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        return df.format(amount);
    }
}