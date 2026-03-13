package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.helper.OrderPaymentCreateHelper;
import com.emenu.features.order.dto.response.OrderPaymentResponse;
import com.emenu.features.order.models.OrderPayment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderPaymentMapper {

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    @Mapping(source = "discountAmount", target = "totalDiscount")
    @Mapping(source = "totalAmount", target = "finalTotal")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "customerName", expression = "java(getCustomerName(payment))")
    @Mapping(target = "customerPhone", expression = "java(getCustomerPhone(payment))")
    OrderPaymentResponse toResponse(OrderPayment payment);

    List<OrderPaymentResponse> toResponseList(List<OrderPayment> payments);

    /**
     * Create OrderPayment from helper DTO - pure MapStruct mapping
     */
    @Mapping(source = "referenceNumber", target = "paymentReference")
    OrderPayment createFromHelper(OrderPaymentCreateHelper helper);

    default String getCustomerName(OrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerIdentifier();
    }

    default String getCustomerPhone(OrderPayment payment) {
        if (payment.getOrder() == null) return null;
        return payment.getOrder().getCustomerContact();
    }

    default PaginationResponse<OrderPaymentResponse> toPaginationResponse(Page<OrderPayment> paymentPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}