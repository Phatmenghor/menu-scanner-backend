package com.emenu.features.payment.mapper;

import com.emenu.features.payment.dto.response.BusinessOrderPaymentResponse;
import com.emenu.features.payment.models.BusinessOrderPayment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessOrderPaymentMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "customerName", expression = "java(getCustomerName(payment))")
    @Mapping(target = "customerPhone", expression = "java(getCustomerPhone(payment))")
    @Mapping(target = "isGuestOrder", expression = "java(payment.getOrder() != null ? payment.getOrder().getIsGuestOrder() : false)")
    @Mapping(target = "isPosOrder", expression = "java(payment.getOrder() != null ? payment.getOrder().getIsPosOrder() : false)")
    public abstract BusinessOrderPaymentResponse toResponse(BusinessOrderPayment payment);

    public abstract List<BusinessOrderPaymentResponse> toResponseList(List<BusinessOrderPayment> payments);

    protected String getCustomerName(BusinessOrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerIdentifier();
    }

    protected String getCustomerPhone(BusinessOrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerContact();
    }

    public PaginationResponse<BusinessOrderPaymentResponse> toPaginationResponse(Page<BusinessOrderPayment> paymentPage) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}