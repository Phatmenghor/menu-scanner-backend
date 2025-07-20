package com.emenu.features.subscription.mapper;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.resposne.SubscriptionResponse;
import com.emenu.features.subscription.models.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "monthlyPrice", expression = "java(request.getPlan().getMonthlyPrice())")
    @Mapping(target = "customMaxStaff", source = "customMaxStaff")
    @Mapping(target = "customMaxMenuItems", source = "customMaxMenuItems")
    @Mapping(target = "customMaxTables", source = "customMaxTables")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Subscription toEntity(SubscriptionCreateRequest request);

    @Mapping(source = "businessId", target = "businessId")
    @Mapping(target = "businessName", ignore = true) // Will be set manually
    @Mapping(target = "isActive", expression = "java(subscription.isActive())")
    @Mapping(target = "isInTrial", expression = "java(subscription.isInTrial())")
    @Mapping(target = "maxStaff", expression = "java(subscription.getMaxStaff())")
    @Mapping(target = "maxMenuItems", expression = "java(subscription.getMaxMenuItems())")
    @Mapping(target = "maxTables", expression = "java(subscription.getMaxTables())")
    @Mapping(target = "daysRemaining", ignore = true) // Will be calculated
    SubscriptionResponse toResponse(Subscription subscription);

    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);
}