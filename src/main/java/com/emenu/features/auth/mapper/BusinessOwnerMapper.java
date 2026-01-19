package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessOwnerMapper {

    /**
     * Map to create response (used when creating new business owner)
     */
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUserIdentifier", source = "owner.userIdentifier")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "ownerFullName", expression = "java(owner.getFullName())")
    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "businessName", source = "business.name")
    @Mapping(target = "businessEmail", source = "business.email")
    @Mapping(target = "businessStatus", source = "business.status")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    @Mapping(target = "planName", source = "subscription.plan.name")
    @Mapping(target = "planPrice", source = "subscription.plan.price")
    @Mapping(target = "planDurationDays", source = "subscription.plan.durationDays")
    @Mapping(target = "subscriptionStartDate", source = "subscription.startDate")
    @Mapping(target = "subscriptionEndDate", source = "subscription.endDate")
    @Mapping(target = "daysRemaining", expression = "java(subscription.getDaysRemaining())")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paymentAmount", source = "payment.amount")
    @Mapping(target = "paymentStatus", source = "payment.status")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    @Mapping(target = "createdAt", source = "owner.createdAt")
    BusinessOwnerCreateResponse toCreateResponse(
            User owner,
            Business business,
            Subscription subscription,
            Payment payment
    );

    /**
     * Map to detail response - base mapping
     */
    @Mapping(target = "ownerId", source = "id")
    @Mapping(target = "ownerUserIdentifier", source = "userIdentifier")
    @Mapping(target = "ownerEmail", source = "email")
    @Mapping(target = "ownerProfileImageUrl", source = "profileImageUrl ")
    @Mapping(target = "ownerFullName", expression = "java(owner.getFullName())")
    @Mapping(target = "ownerPhone", source = "phoneNumber")
    @Mapping(target = "ownerAccountStatus", source = "accountStatus")
    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "businessName", source = "business.name")
    @Mapping(target = "businessEmail", source = "business.email")
    @Mapping(target = "businessPhone", source = "business.phone")
    @Mapping(target = "businessAddress", source = "business.address")
    @Mapping(target = "businessStatus", source = "business.status")
    @Mapping(target = "isSubscriptionActive", source = "business.isSubscriptionActive")
    @Mapping(target = "businessCreatedAt", source = "business.createdAt")
    BusinessOwnerDetailResponse toDetailResponse(User owner);

    /**
     * Map list to detail responses
     */
    List<BusinessOwnerDetailResponse> toDetailResponseList(List<User> owners);

    /**
     * Pagination response
     */
    default PaginationResponse<BusinessOwnerDetailResponse> toPaginationResponse(Page<User> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toDetailResponseList);
    }
}