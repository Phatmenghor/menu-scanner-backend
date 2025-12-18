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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessOwnerMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    /**
     * Map to create responses
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
    @Mapping(target = "daysRemaining", expression = "java(calculateDaysRemaining(subscription))")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paymentAmount", source = "payment.amount")
    @Mapping(target = "paymentStatus", source = "payment.status")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    @Mapping(target = "createdAt", source = "owner.createdAt")
    public abstract BusinessOwnerCreateResponse toCreateResponse(
            User owner,
            Business business,
            Subscription subscription,
            Payment payment
    );

    /**
     * Map to detail response - base mapping only
     */
    @Mapping(target = "ownerId", source = "id")
    @Mapping(target = "ownerUserIdentifier", source = "userIdentifier")
    @Mapping(target = "ownerEmail", source = "email")
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
    public abstract BusinessOwnerDetailResponse toDetailResponse(User owner);

    /**
     * Map list to detail responses
     */
    public abstract List<BusinessOwnerDetailResponse> toDetailResponseList(List<User> owners);

    /**
     * Calculate days remaining
     */
    protected Long calculateDaysRemaining(Subscription subscription) {
        if (subscription == null || subscription.getEndDate() == null) {
            return 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(subscription.getEndDate())) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(now, subscription.getEndDate());
    }

    /**
     * Pagination response
     */
    public PaginationResponse<BusinessOwnerDetailResponse> toPaginationResponse(Page<User> page) {
        return paginationMapper.toPaginationResponse(page, this::toDetailResponseList);
    }
}