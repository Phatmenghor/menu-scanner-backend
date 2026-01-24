package com.emenu.features.auth.service.impl;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import com.emenu.enums.sub_scription.SubscriptionStatus;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.BusinessStatus;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessOwnerFilterRequest;
import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.mapper.BusinessOwnerMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessOwnerRepository;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.service.BusinessOwnerService;
import com.emenu.features.auth.service.UserValidationService;
import com.emenu.features.order.models.Payment;
import com.emenu.features.order.repository.PaymentRepository;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessOwnerServiceImpl implements BusinessOwnerService {

    private final BusinessOwnerRepository businessOwnerRepository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessOwnerMapper mapper;
    private final UserValidationService userValidationService;

    /**
     * Creates a new business owner with associated business, owner user, subscription, and optional payment
     */
    @Override
    public BusinessOwnerCreateResponse createBusinessOwner(BusinessOwnerCreateRequest request) {
        log.info("Creating business owner: {}", request.getBusinessName());

        validateBusinessOwnerCreation(request);

        Business business = createBusiness(request);
        log.info("✅ Business created: {}", business.getName());

        User owner = createOwnerUser(request, business.getId());
        log.info("✅ Owner created: {}", owner.getUserIdentifier());

        business.setOwnerId(owner.getId());
        businessRepository.save(business);

        Subscription subscription = createSubscription(business.getId(), request);
        log.info("✅ Subscription created");

        Payment payment = request.hasPaymentInfo() && request.isPaymentInfoComplete()
                ? createPayment(subscription, request)
                : null;

        if (payment != null) {
            log.info("✅ Payment created: ${}", payment.getAmount());
        }

        business.activateSubscription();
        businessRepository.save(business);

        BusinessOwnerCreateResponse response = mapper.toCreateResponse(owner, business, subscription, payment);
        response.setCreatedComponents(buildCreatedComponentsList(payment != null));

        log.info("✅ Business owner created successfully: {}", owner.getFullName());
        return response;
    }

    /**
     * Retrieves all business owners with filtering and pagination support
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessOwnerDetailResponse> getAllBusinessOwners(BusinessOwnerFilterRequest filter) {
        log.info("Getting all business owners with filters");

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        // Convert empty lists to null for PostgreSQL compatibility
        List<AccountStatus> ownerStatuses = (filter.getOwnerAccountStatuses() != null && !filter.getOwnerAccountStatuses().isEmpty())
                ? filter.getOwnerAccountStatuses() : null;
        List<BusinessStatus> businessStatuses = (filter.getBusinessStatuses() != null && !filter.getBusinessStatuses().isEmpty())
                ? filter.getBusinessStatuses() : null;
        List<SubscriptionStatus> subscriptionStatuses = (filter.getSubscriptionStatuses() != null && !filter.getSubscriptionStatuses().isEmpty())
                ? filter.getSubscriptionStatuses() : null;
        List<PaymentStatus> paymentStatuses = (filter.getPaymentStatuses() != null && !filter.getPaymentStatuses().isEmpty())
                ? filter.getPaymentStatuses() : null;

        // Parse subscription statuses
        boolean hasActive = subscriptionStatuses != null && subscriptionStatuses.contains(SubscriptionStatus.ACTIVE);
        boolean hasExpired = subscriptionStatuses != null && subscriptionStatuses.contains(SubscriptionStatus.EXPIRED);
        boolean hasExpiringSoon = subscriptionStatuses != null && subscriptionStatuses.contains(SubscriptionStatus.EXPIRING_SOON);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryThreshold = now.plusDays(filter.getExpiringSoonDays());

        Page<User> ownerPage = businessOwnerRepository.findAllBusinessOwnersWithFilters(
                ownerStatuses,
                businessStatuses,
                subscriptionStatuses,
                hasActive,
                hasExpired,
                hasExpiringSoon,
                now,
                expiryThreshold,
                filter.getAutoRenew(),
                paymentStatuses,
                filter.getSearch(),
                pageable
        );

        // Map and enrich responses
        List<BusinessOwnerDetailResponse> enrichedResponses = ownerPage.getContent().stream()
                .map(this::buildEnrichedDetailResponse)
                .toList();

        return PaginationResponse.<BusinessOwnerDetailResponse>builder()
                .content(enrichedResponses)
                .pageNo(ownerPage.getNumber() + 1)
                .pageSize(ownerPage.getSize())
                .totalElements(ownerPage.getTotalElements())
                .totalPages(ownerPage.getTotalPages())
                .first(ownerPage.isFirst())
                .last(ownerPage.isLast())
                .hasNext(ownerPage.hasNext())
                .hasPrevious(ownerPage.hasPrevious())
                .build();
    }

    /**
     * Retrieves detailed information for a specific business owner
     */
    @Override
    @Transactional(readOnly = true)
    public BusinessOwnerDetailResponse getBusinessOwnerDetail(UUID ownerId) {
        log.info("Getting business owner detail: {}", ownerId);

        User owner = businessOwnerRepository.findBusinessOwnerById(ownerId)
                .orElseThrow(() -> new NotFoundException("Business owner not found: " + ownerId));

        return buildEnrichedDetailResponse(owner);
    }

    /**
     * Renews the subscription for a business owner with optional plan change and payment
     */
    @Override
    public BusinessOwnerDetailResponse renewSubscription(UUID ownerId, BusinessOwnerSubscriptionRenewRequest request) {
        log.info("Renewing subscription for business owner: {}", ownerId);

        User owner = getOwnerOrThrow(ownerId);
        Business business = owner.getBusiness();
        Subscription currentSubscription = getCurrentSubscription(business.getId());

        SubscriptionPlan planToUse = request.getNewPlanId() != null
                ? getPlanOrThrow(request.getNewPlanId())
                : currentSubscription.getPlan();

        // Update subscription
        currentSubscription.setPlan(planToUse);
        currentSubscription.renew();
        subscriptionRepository.save(currentSubscription);

        // Create payment if requested
        if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
            createPaymentForSubscription(currentSubscription, request.getPaymentAmount(),
                    request.getPaymentMethod(), request.getPaymentReference(), request.getPaymentNotes());
        }

        log.info("✅ Subscription renewed for business owner: {}", ownerId);
        return buildEnrichedDetailResponse(owner);
    }

    /**
     * Changes the subscription plan for a business owner
     */
    @Override
    public BusinessOwnerDetailResponse changePlan(UUID ownerId, BusinessOwnerChangePlanRequest request) {
        log.info("Changing plan for business owner: {}", ownerId);

        User owner = getOwnerOrThrow(ownerId);
        Business business = owner.getBusiness();
        Subscription currentSubscription = getCurrentSubscription(business.getId());
        SubscriptionPlan newPlan = getPlanOrThrow(request.getNewPlanId());

        currentSubscription.setPlan(newPlan);

        if (!request.shouldKeepCurrentEndDate()) {
            LocalDateTime newEndDate = currentSubscription.getStartDate().plusDays(newPlan.getDurationDays());
            currentSubscription.setEndDate(newEndDate);
        }

        subscriptionRepository.save(currentSubscription);

        if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
            createPaymentForSubscription(currentSubscription, request.getPaymentAmount(),
                    request.getPaymentMethod(), request.getPaymentReference(), request.getPaymentNotes());
        }

        log.info("✅ Plan changed for business owner: {}", ownerId);
        return buildEnrichedDetailResponse(owner);
    }

    /**
     * Cancels the subscription for a business owner with optional refund
     */
    @Override
    public BusinessOwnerDetailResponse cancelSubscription(UUID ownerId, BusinessOwnerSubscriptionCancelRequest request) {
        log.info("Cancelling subscription for business owner: {}", ownerId);

        User owner = getOwnerOrThrow(ownerId);
        Business business = owner.getBusiness();
        Subscription currentSubscription = getCurrentSubscription(business.getId());

        // Cancel subscription
        currentSubscription.cancel();
        subscriptionRepository.save(currentSubscription);

        // Cancel pending payments
        List<Payment> pendingPayments = paymentRepository.findBySubscriptionIdAndStatusAndIsDeletedFalse(
                currentSubscription.getId(), PaymentStatus.PENDING);

        pendingPayments.forEach(payment -> {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Cancelled: " + request.getReason());
            paymentRepository.save(payment);
        });

        // Deactivate business subscription
        business.deactivateSubscription();
        businessRepository.save(business);

        // Create refund if requested
        if (request.hasRefundAmount()) {
            createRefundPayment(currentSubscription, request);
        }

        log.info("✅ Subscription cancelled for business owner: {}", ownerId);
        return buildEnrichedDetailResponse(owner);
    }

    /**
     * Soft deletes a business owner and all associated data
     */
    @Override
    public BusinessOwnerDetailResponse deleteBusinessOwner(UUID ownerId) {
        log.info("Deleting business owner: {}", ownerId);

        User owner = getOwnerOrThrow(ownerId);
        Business business = owner.getBusiness();

        // Soft delete subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(business.getId());
        subscriptions.forEach(subscription -> {
            subscription.softDelete();
            subscriptionRepository.save(subscription);
        });

        // Soft delete business
        business.softDelete();
        businessRepository.save(business);

        // Soft delete owner
        owner.softDelete();
        businessOwnerRepository.save(owner);

        log.info("✅ Business owner deleted: {}", ownerId);
        return buildEnrichedDetailResponse(owner);
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private void validateBusinessOwnerCreation(BusinessOwnerCreateRequest request) {
        if (businessOwnerRepository.existsBusinessOwnerByEmail(request.getOwnerEmail())) {
            throw new ValidationException("Email already exists: " + request.getOwnerEmail());
        }

        if (businessRepository.existsByEmailAndIsDeletedFalse(request.getBusinessEmail())) {
            throw new ValidationException("Business email already exists: " + request.getBusinessEmail());
        }

        if (!planRepository.existsById(request.getPlanId())) {
            throw new NotFoundException("Plan not found: " + request.getPlanId());
        }

        // Note: Username validation for business users cannot be done here because the business
        // doesn't exist yet. The username must be unique within the business, but we don't have
        // the businessId at this point. This validation will be handled at the database level
        // through unique constraints and will result in a database exception if violated.
    }

    private Business createBusiness(BusinessOwnerCreateRequest request) {
        Business business = new Business();
        business.setName(request.getBusinessName());
        business.setEmail(request.getBusinessEmail());
        business.setPhone(request.getBusinessPhone());
        business.setAddress(request.getBusinessAddress());
        business.setStatus(BusinessStatus.PENDING);
        return businessRepository.save(business);
    }

    private User createOwnerUser(BusinessOwnerCreateRequest request, UUID businessId) {
        Role ownerRole = roleRepository.findByNameAndIsDeletedFalse("BUSINESS_OWNER")
                .orElseThrow(() -> new NotFoundException("Business owner role not found"));

        User owner = new User();
        owner.setUserIdentifier(request.getOwnerUserIdentifier());
        owner.setEmail(request.getOwnerEmail());
        owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));

        // Split full name
        String[] nameParts = request.getOwnerFullName().split(" ", 2);
        owner.setFirstName(nameParts[0]);
        owner.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        owner.setPhoneNumber(request.getOwnerPhone());
        owner.setUserType(UserType.BUSINESS_USER);
        owner.setAccountStatus(AccountStatus.ACTIVE);
        owner.setBusinessId(businessId);
        owner.setPosition("Owner");
        owner.setRoles(List.of(ownerRole));

        return businessOwnerRepository.save(owner);
    }

    private Subscription createSubscription(UUID businessId, BusinessOwnerCreateRequest request) {
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        Subscription subscription = new Subscription();
        subscription.setBusinessId(businessId);
        subscription.setPlanId(request.getPlanId());

        LocalDateTime startDate = LocalDateTime.now();
        subscription.setStartDate(startDate);

        Integer duration = request.getCustomDurationDays() != null
                ? request.getCustomDurationDays()
                : plan.getDurationDays();
        subscription.setEndDate(startDate.plusDays(duration));

        subscription.setAutoRenew(false);

        return subscriptionRepository.save(subscription);
    }

    private Payment createPayment(Subscription subscription, BusinessOwnerCreateRequest request) {
        Payment payment = new Payment();
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(request.getPaymentAmount());
        payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setPaymentType(PaymentType.SUBSCRIPTION);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setReferenceNumber(request.getPaymentReference());
        payment.setNotes(request.getPaymentNotes());
        return paymentRepository.save(payment);
    }

    private void createPaymentForSubscription(Subscription subscription, java.math.BigDecimal amount,
                                             String method, String reference, String notes) {
        Payment payment = new Payment();
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.valueOf(method));
        payment.setPaymentType(PaymentType.SUBSCRIPTION);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setReferenceNumber(reference);
        payment.setNotes(notes);
        paymentRepository.save(payment);
    }

    private void createRefundPayment(Subscription subscription, BusinessOwnerSubscriptionCancelRequest request) {
        Payment refund = new Payment();
        refund.setBusinessId(subscription.getBusinessId());
        refund.setPlanId(subscription.getPlanId());
        refund.setSubscriptionId(subscription.getId());
        refund.setAmount(request.getRefundAmount().negate());
        refund.setPaymentMethod(PaymentMethod.valueOf(request.getRefundMethod()));
        refund.setPaymentType(PaymentType.REFUND);
        refund.setStatus(PaymentStatus.COMPLETED);
        refund.setReferenceNumber(request.getRefundReference());
        refund.setNotes("Refund: " + request.getReason());
        paymentRepository.save(refund);
    }

    private BusinessOwnerDetailResponse buildEnrichedDetailResponse(User owner) {
        BusinessOwnerDetailResponse response = mapper.toDetailResponse(owner);

        if (owner.getBusiness() != null) {
            enrichDetailResponse(response, owner.getBusiness());
        }

        return response;
    }

    private void enrichDetailResponse(BusinessOwnerDetailResponse response, Business business) {
        if (business == null) return;

        enrichSubscriptionData(response, business.getId());

        if (response.getCurrentSubscriptionId() != null) {
            enrichPaymentData(response, response.getCurrentSubscriptionId());
        }
    }

    private void enrichSubscriptionData(BusinessOwnerDetailResponse response, UUID businessId) {
        subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now())
                .ifPresentOrElse(
                        subscription -> populateSubscriptionInfo(response, subscription),
                        () -> response.setSubscriptionStatus(SubscriptionStatus.EXPIRED)
                );
    }

    private void populateSubscriptionInfo(BusinessOwnerDetailResponse response, Subscription subscription) {
        response.setCurrentSubscriptionId(subscription.getId());
        response.setCurrentPlanName(subscription.getPlan().getName());
        response.setCurrentPlanPrice(subscription.getPlan().getPrice());
        response.setCurrentPlanDurationDays(subscription.getPlan().getDurationDays());
        response.setSubscriptionStartDate(subscription.getStartDate());
        response.setSubscriptionEndDate(subscription.getEndDate());
        response.setDaysRemaining(calculateDaysRemaining(subscription.getEndDate()));
        response.setDaysActive(calculateDaysActive(subscription.getStartDate()));
        response.setSubscriptionStatus(determineSubscriptionStatus(subscription));
        response.setAutoRenew(subscription.getAutoRenew());
        response.setIsExpiringSoon(subscription.isExpiringSoon(7));
    }

    private void enrichPaymentData(BusinessOwnerDetailResponse response, UUID subscriptionId) {
        List<Payment> payments = paymentRepository.findBySubscriptionIdAndIsDeletedFalse(subscriptionId);

        if (payments.isEmpty()) {
            setDefaultPaymentData(response);
            return;
        }

        java.math.BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalPending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        long completedCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();

        long pendingCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        LocalDateTime lastPaymentDate = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        response.setTotalPaid(totalPaid);
        response.setTotalPending(totalPending);
        response.setTotalPayments(payments.size());
        response.setCompletedPayments((int) completedCount);
        response.setPendingPayments((int) pendingCount);
        response.setPaymentStatus(determinePaymentStatus(totalPaid, totalPending, response.getCurrentPlanPrice()));
        response.setLastPaymentDate(lastPaymentDate);
    }

    private void setDefaultPaymentData(BusinessOwnerDetailResponse response) {
        response.setTotalPaid(java.math.BigDecimal.ZERO);
        response.setTotalPending(java.math.BigDecimal.ZERO);
        response.setTotalPayments(0);
        response.setCompletedPayments(0);
        response.setPendingPayments(0);
        response.setPaymentStatus("UNPAID");
        response.setLastPaymentDate(null);
    }

    private Long calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null) return 0L;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) return 0L;
        return java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
    }

    private Long calculateDaysActive(LocalDateTime startDate) {
        if (startDate == null) return 0L;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) return 0L;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, now);
    }

    private SubscriptionStatus determineSubscriptionStatus(Subscription subscription) {
        if (subscription.isExpired()) {
            return SubscriptionStatus.EXPIRED;
        }
        if (subscription.isExpiringSoon(7)) {
            return SubscriptionStatus.EXPIRING_SOON;
        }
        return SubscriptionStatus.ACTIVE;
    }

    private String determinePaymentStatus(java.math.BigDecimal totalPaid, java.math.BigDecimal totalPending, java.math.BigDecimal planPrice) {
        if (planPrice == null) {
            return "UNKNOWN";
        }

        if (totalPaid.compareTo(planPrice) >= 0) {
            return "PAID";
        } else if (totalPaid.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return "PARTIALLY_PAID";
        } else if (totalPending.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return "PENDING";
        }

        return "UNPAID";
    }

    private List<String> buildCreatedComponentsList(boolean hasPayment) {
        List<String> components = new ArrayList<>();
        components.add("Owner User");
        components.add("Business Profile");
        components.add("Subscription");
        if (hasPayment) {
            components.add("Payment");
        }
        return components;
    }

    private User getOwnerOrThrow(UUID ownerId) {
        return businessOwnerRepository.findBusinessOwnerById(ownerId)
                .orElseThrow(() -> new NotFoundException("Business owner not found: " + ownerId));
    }

    private Subscription getCurrentSubscription(UUID businessId) {
        return subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now())
                .orElseThrow(() -> new NotFoundException("No active subscription found"));
    }

    private SubscriptionPlan getPlanOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found: " + planId));
    }
}