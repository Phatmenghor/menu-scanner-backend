package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.helper.BusinessOrderPaymentCreateHelper;
import com.emenu.features.order.dto.response.BusinessOrderPaymentResponse;
import com.emenu.features.order.models.BusinessOrderPayment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessOrderPaymentMapper {

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "customerName", expression = "java(getCustomerName(payment))")
    @Mapping(target = "customerPhone", expression = "java(getCustomerPhone(payment))")
    @Mapping(target = "isGuestOrder", expression = "java(payment.getOrder() != null ? payment.getOrder().getIsGuestOrder() : false)")
    @Mapping(target = "isPosOrder", expression = "java(payment.getOrder() != null ? payment.getOrder().getIsPosOrder() : false)")
    BusinessOrderPaymentResponse toResponse(BusinessOrderPayment payment);

    List<BusinessOrderPaymentResponse> toResponseList(List<BusinessOrderPayment> payments);

    /**
     * Create BusinessOrderPayment from helper DTO - pure MapStruct mapping
     */
    BusinessOrderPayment createFromHelper(BusinessOrderPaymentCreateHelper helper);

    default String getCustomerName(BusinessOrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerIdentifier();
    }

    default String getCustomerPhone(BusinessOrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerContact();
    }

    default PaginationResponse<BusinessOrderPaymentResponse> toPaginationResponse(Page<BusinessOrderPayment> paymentPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}