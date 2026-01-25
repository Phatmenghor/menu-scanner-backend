package com.emenu.features.subscription.mapper;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping    @Mapping    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    Subscription toEntity(SubscriptionCreateRequest request);

    @Mapping    @Mapping    @Mapping    @Mapping    @Mapping    SubscriptionResponse toResponse(Subscription subscription);

    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping    @Mapping    @Mapping    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(SubscriptionUpdateRequest request, @MappingTarget Subscription subscription);

    @AfterMapping
    default void setCalculatedFields(@MappingTarget SubscriptionResponse response, Subscription subscription) {
response.setDaysRemaining(subscription.getDaysRemaining());
response.setStatus(subscription.getStatus());
if (subscription.getBusiness() != null) {
    response.setBusinessName(subscription.getBusiness().getName());
} else {
    response.setBusinessName("Unknown Business");
}
if (subscription.getPlan() != null) {
    response.setPlanName(subscription.getPlan().getName());
    response.setPlanPrice(subscription.getPlan().getPrice().doubleValue());
    response.setPlanDurationDays(subscription.getPlan().getDurationDays());
} else {
    response.setPlanName("Unknown Plan");
    response.setPlanPrice(0.0);
    response.setPlanDurationDays(0);
}
response.setPaymentStatus(subscription.getPaymentStatus());
response.setPaymentAmount(subscription.getPaymentAmount().doubleValue());
    }

    default PaginationResponse<SubscriptionResponse> toPaginationResponse(Page<Subscription> subscriptionPage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(subscriptionPage, this::toResponseList);
    }
}