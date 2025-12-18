package com.emenu.features.auth.service.impl;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessOwnerFilterRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerChangePlanRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerSubscriptionCancelRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerSubscriptionRenewRequest;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.mapper.BusinessOwnerResponseMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessOwnerService;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.payment.repository.PaymentRepository;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.subscription.dto.request.SubscriptionCancelRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.request.SubscriptionRenewRequest;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessOwnerServiceImpl implements BusinessOwnerService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final BusinessOwnerResponseMapper businessOwnerResponseMapper;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;

    @Override
    public BusinessOwnerCreateResponse createBusinessOwner(BusinessOwnerCreateRequest request) {
        log.info("Creating business owner: {}", request.getBusinessName());

        validateBusinessOwnerCreation(request);

        try {
            Business business = createBusiness(request);
            log.info("Business created: {}", business.getName());

            User owner = createOwnerUser(request, business.getId());
            log.info("Owner created: {}", owner.getUserIdentifier());

            business.setOwnerId(owner.getId());
            business = businessRepository.save(business);

            Subscription subscription = createSubscription(business.getId(), request);
            log.info("Subscription created: {} days", subscription.getPlan().getDurationDays());

            Payment payment = null;
            if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
                payment = createPayment(subscription, request);
                log.info("Payment created: ${}", payment.getAmount());
            }

            business.activateSubscription();
            businessRepository.save(business);

            BusinessOwnerCreateResponse response = businessOwnerResponseMapper.toCreateResponse(
                owner, business, subscription, payment);
            
            log.info("Business owner created successfully: {}", owner.getFullName());
            return response;

        } catch (Exception e) {
            log.error("Failed to create business owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create business owner: " + e.getMessage());
        }
    }

    @Override
    public PaginationResponse<BusinessOwnerDetailResponse> getAllBusinessOwners(BusinessOwnerFilterRequest request) {
        log.info("Getting all business owners with filters");

        List<User> allOwners = userRepository.findByUserTypeAndIsDeletedFalse(UserType.BUSINESS_OWNER);
        
        List<User> filteredOwners = applyFilters(allOwners, request);
        
        List<User> searchedOwners = applySearch(filteredOwners, request.getSearch());
        
        List<User> sortedOwners = applySorting(searchedOwners, request);
        
        Pageable pageable = PaginationUtils.createPageable(
            request.getPageNo(), 
            request.getPageSize(), 
            request.getSortBy(), 
            request.getSortDir()
        );
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedOwners.size());
        List<User> pagedOwners = sortedOwners.subList(start, end);
        
        List<BusinessOwnerDetailResponse> responses = pagedOwners.stream()
                .map(this::buildDetailResponse)
                .collect(Collectors.toList());
        
        Page<BusinessOwnerDetailResponse> page = new PageImpl<>(
            responses, 
            pageable, 
            sortedOwners.size()
        );
        
        return PaginationUtils.toPaginationResponse(page);
    }

    @Override
    public BusinessOwnerDetailResponse getBusinessOwnerDetail(UUID ownerId) {
        log.info("Getting business owner detail: {}", ownerId);
        
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Business owner not found with ID: " + ownerId));
        
        if (owner.getUserType() != UserType.BUSINESS_OWNER) {
            throw new ValidationException("User is not a business owner");
        }
        
        return buildDetailResponse(owner);
    }

    @Override
    public BusinessOwnerDetailResponse renewSubscription(UUID ownerId, BusinessOwnerSubscriptionRenewRequest request) {
        log.info("Renewing subscription for business owner: {}", ownerId);
        
        User owner = getOwnerOrThrow(ownerId);
        Business business = getBusinessOrThrow(owner.getId());
        Subscription currentSubscription = getCurrentSubscription(business.getId());
        
        SubscriptionPlan planToUse = request.getNewPlanId() != null
                ? getPlanOrThrow(request.getNewPlanId())
                : currentSubscription.getPlan();
        
        SubscriptionRenewRequest renewRequest = SubscriptionRenewRequest.builder()
                .newPlanId(request.getNewPlanId())
                .customDurationDays(request.getCustomDurationDays())
                .build();
        
        Subscription renewedSubscription = subscriptionService.renewSubscription(
            currentSubscription.getId(), renewRequest);
        
        if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
            createPaymentForSubscription(renewedSubscription, request.getPaymentAmount(), 
                request.getPaymentMethod(), request.getPaymentReference(), request.getPaymentNotes());
        }
        
        log.info("Subscription renewed for business owner: {}", ownerId);
        return buildDetailResponse(owner);
    }

    @Override
    public BusinessOwnerDetailResponse changePlan(UUID ownerId, BusinessOwnerChangePlanRequest request) {
        log.info("Changing plan for business owner: {}", ownerId);
        
        User owner = getOwnerOrThrow(ownerId);
        Business business = getBusinessOrThrow(owner.getId());
        Subscription currentSubscription = getCurrentSubscription(business.getId());
        SubscriptionPlan newPlan = getPlanOrThrow(request.getNewPlanId());
        
        currentSubscription.setPlan(newPlan);
        
        if (!request.shouldKeepCurrentEndDate()) {
            LocalDateTime newEndDate = currentSubscription.getStartDate()
                    .plusDays(newPlan.getDurationDays());
            currentSubscription.setEndDate(newEndDate);
        }
        
        subscriptionRepository.save(currentSubscription);
        
        if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
            createPaymentForSubscription(currentSubscription, request.getPaymentAmount(), 
                request.getPaymentMethod(), request.getPaymentReference(), request.getPaymentNotes());
        }
        
        log.info("Plan changed for business owner: {} to {}", ownerId, newPlan.getName());
        return buildDetailResponse(owner);
    }

    @Override
    public BusinessOwnerDetailResponse cancelSubscription(UUID ownerId, BusinessOwnerSubscriptionCancelRequest request) {
        log.info("Cancelling subscription for business owner: {}", ownerId);
        
        User owner = getOwnerOrThrow(ownerId);
        Business business = getBusinessOrThrow(owner.getId());
        Subscription currentSubscription = getCurrentSubscription(business.getId());
        
        SubscriptionCancelRequest cancelRequest = SubscriptionCancelRequest.builder()
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();
        
        subscriptionService.cancelSubscription(currentSubscription.getId(), cancelRequest);
        
        List<Payment> pendingPayments = paymentRepository
                .findBySubscriptionIdAndStatusAndIsDeletedFalse(
                    currentSubscription.getId(), PaymentStatus.PENDING);
        
        for (Payment payment : pendingPayments) {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Cancelled due to subscription cancellation: " + request.getReason());
            paymentRepository.save(payment);
        }
        
        business.deactivateSubscription();
        businessRepository.save(business);
        
        if (request.hasRefundAmount()) {
            createRefundPayment(currentSubscription, request);
        }
        
        log.info("Subscription cancelled for business owner: {}", ownerId);
        return buildDetailResponse(owner);
    }

    @Override
    public BusinessOwnerDetailResponse deleteBusinessOwner(UUID ownerId) {
        log.info("Deleting business owner: {}", ownerId);
        
        User owner = getOwnerOrThrow(ownerId);
        Business business = getBusinessOrThrow(owner.getId());
        
        List<Subscription> subscriptions = subscriptionRepository
                .findByBusinessIdAndIsDeletedFalse(business.getId());
        
        for (Subscription subscription : subscriptions) {
            subscription.softDelete();
            subscriptionRepository.save(subscription);
        }
        
        business.softDelete();
        businessRepository.save(business);
        
        owner.softDelete();
        userRepository.save(owner);
        
        log.info("Business owner deleted: {}", ownerId);
        return buildDetailResponse(owner);
    }

    private void validateBusinessOwnerCreation(BusinessOwnerCreateRequest request) {
        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(request.getOwnerUserIdentifier())) {
            throw new ValidationException("User identifier already exists: " + request.getOwnerUserIdentifier());
        }
        
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getOwnerEmail())) {
            throw new ValidationException("Email already exists: " + request.getOwnerEmail());
        }
        
        if (businessRepository.existsByEmailAndIsDeletedFalse(request.getBusinessEmail())) {
            throw new ValidationException("Business email already exists: " + request.getBusinessEmail());
        }
        
        if (!planRepository.existsById(request.getPlanId())) {
            throw new NotFoundException("Subscription plan not found with ID: " + request.getPlanId());
        }
    }

    private Business createBusiness(BusinessOwnerCreateRequest request) {
        Business business = Business.builder()
                .name(request.getBusinessName())
                .email(request.getBusinessEmail())
                .phone(request.getBusinessPhone())
                .address(request.getBusinessAddress())
                .build();
        
        return businessRepository.save(business);
    }

    private User createOwnerUser(BusinessOwnerCreateRequest request, UUID businessId) {
        Role ownerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                .orElseThrow(() -> new NotFoundException("Business owner role not found"));
        
        User owner = org.springframework.security.core.userdetails.User.builder()
                .userIdentifier(request.getOwnerUserIdentifier())
                .email(request.getOwnerEmail())
                .password(passwordEncoder.encode(request.getOwnerPassword()))
                .fullName(request.getOwnerFullName())
                .phoneNumber(request.getOwnerPhone())
                .userType(UserType.BUSINESS_OWNER)
                .accountStatus(AccountStatus.ACTIVE)
                .businessId(businessId)
                .role(ownerRole)
                .build();
        
        return userRepository.save(owner);
    }

    private Subscription createSubscription(UUID businessId, BusinessOwnerCreateRequest request) {
        SubscriptionCreateRequest subscriptionRequest = SubscriptionCreateRequest.builder()
                .businessId(businessId)
                .planId(request.getPlanId())
                .customDurationDays(request.getCustomDurationDays())
                .build();
        
        return subscriptionService.createSubscription(subscriptionRequest);
    }

    private Payment createPayment(Subscription subscription, BusinessOwnerCreateRequest request) {
        PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                .subscriptionId(subscription.getId())
                .amount(request.getPaymentAmount())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentType(PaymentType.SUBSCRIPTION)
                .reference(request.getPaymentReference())
                .notes(request.getPaymentNotes())
                .build();
        
        return paymentService.createPayment(paymentRequest);
    }

    private void createPaymentForSubscription(Subscription subscription, BigDecimal amount,
            String method, String reference, String notes) {
        PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                .subscriptionId(subscription.getId())
                .amount(amount)
                .paymentMethod(PaymentMethod.valueOf(method))
                .paymentType(PaymentType.SUBSCRIPTION)
                .reference(reference)
                .notes(notes)
                .build();
        
        paymentService.createPayment(paymentRequest);
    }

    private void createRefundPayment(Subscription subscription, BusinessOwnerSubscriptionCancelRequest request) {
        PaymentCreateRequest refundRequest = PaymentCreateRequest.builder()
                .subscriptionId(subscription.getId())
                .amount(request.getRefundAmount().negate())
                .paymentMethod(PaymentMethod.valueOf(request.getRefundMethod()))
                .paymentType(PaymentType.REFUND)
                .reference(request.getRefundReference())
                .notes("Refund: " + request.getReason())
                .build();
        
        paymentService.createPayment(refundRequest);
    }

    private BusinessOwnerDetailResponse buildDetailResponse(User owner) {
        BusinessOwnerDetailResponse response = businessOwnerResponseMapper.toDetailResponse(owner, null);
        
        Business business = businessRepository.findByOwnerIdAndIsDeletedFalse(owner.getId()).orElse(null);
        
        if (business != null) {
            response.setBusinessId(business.getId());
            response.setBusinessName(business.getName());
            response.setBusinessEmail(business.getEmail());
            response.setBusinessPhone(business.getPhone());
            response.setBusinessAddress(business.getAddress());
            response.setBusinessStatus(business.getStatus());
            response.setIsSubscriptionActive(business.getIsSubscriptionActive());
            response.setBusinessCreatedAt(business.getCreatedAt());
            
            Subscription subscription = subscriptionRepository
                    .findFirstByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(business.getId())
                    .orElse(null);
            
            if (subscription != null) {
                populateSubscriptionInfo(response, subscription);
                populatePaymentInfo(response, subscription);
            } else {
                response.setSubscriptionStatus("EXPIRED");
            }
        }
        
        response.setCreatedAt(owner.getCreatedAt());
        response.setLastModifiedAt(owner.getUpdatedAt());
        
        return response;
    }

    private void populateSubscriptionInfo(BusinessOwnerDetailResponse response, Subscription subscription) {
        response.setCurrentSubscriptionId(subscription.getId());
        response.setCurrentPlanName(subscription.getPlan().getName());
        response.setCurrentPlanPrice(subscription.getPlan().getPrice());
        response.setCurrentPlanDurationDays(subscription.getPlan().getDurationDays());
        response.setSubscriptionStartDate(subscription.getStartDate());
        response.setSubscriptionEndDate(subscription.getEndDate());
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setDaysActive(subscription.getDaysActive());
        response.setSubscriptionStatus(subscription.getStatus());
        response.setAutoRenew(subscription.getAutoRenew());
        response.setIsExpiringSoon(subscription.isExpiringSoon(7));
    }

    private void populatePaymentInfo(BusinessOwnerDetailResponse response, Subscription subscription) {
        List<Payment> payments = paymentRepository
                .findBySubscriptionIdAndIsDeletedFalse(subscription.getId());
        
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long completedCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
        
        long pendingCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
        
        LocalDateTime lastPaymentDate = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getPaymentDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        BigDecimal planPrice = subscription.getPlan().getPrice();
        String paymentStatus;
        if (totalPaid.compareTo(planPrice) >= 0) {
            paymentStatus = "PAID";
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = "PARTIALLY_PAID";
        } else if (pendingCount > 0) {
            paymentStatus = "PENDING";
        } else {
            paymentStatus = "UNPAID";
        }
        
        response.setTotalPaid(totalPaid);
        response.setTotalPending(totalPending);
        response.setTotalPayments(payments.size());
        response.setCompletedPayments((int) completedCount);
        response.setPendingPayments((int) pendingCount);
        response.setPaymentStatus(paymentStatus);
        response.setLastPaymentDate(lastPaymentDate);
    }

    private List<User> applyFilters(List<User> owners, BusinessOwnerFilterRequest request) {
        return owners.stream()
                .filter(owner -> matchesFilters(owner, request))
                .collect(Collectors.toList());
    }

    private boolean matchesFilters(User owner, BusinessOwnerFilterRequest request) {
        if (request.getOwnerAccountStatuses() != null && !request.getOwnerAccountStatuses().isEmpty()) {
            if (!request.getOwnerAccountStatuses().contains(owner.getAccountStatus())) {
                return false;
            }
        }
        
        Business business = businessRepository.findByOwnerIdAndIsDeletedFalse(owner.getId()).orElse(null);
        if (business == null) return false;
        
        if (request.getBusinessStatuses() != null && !request.getBusinessStatuses().isEmpty()) {
            if (!request.getBusinessStatuses().contains(business.getStatus())) {
                return false;
            }
        }
        
        Subscription subscription = subscriptionRepository
                .findFirstByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(business.getId())
                .orElse(null);
        
        if (subscription != null) {
            if (request.getSubscriptionStatuses() != null && !request.getSubscriptionStatuses().isEmpty()) {
                if (!request.getSubscriptionStatuses().contains(subscription.getStatus())) {
                    return false;
                }
            }
            
            if (request.getAutoRenew() != null) {
                if (!request.getAutoRenew().equals(subscription.getAutoRenew())) {
                    return false;
                }
            }
            
            List<Payment> payments = paymentRepository
                    .findBySubscriptionIdAndIsDeletedFalse(subscription.getId());
            
            if (request.getPaymentStatuses() != null && !request.getPaymentStatuses().isEmpty()) {
                boolean hasMatchingPayment = payments.stream()
                        .anyMatch(p -> request.getPaymentStatuses().contains(p.getStatus()));
                if (!hasMatchingPayment) {
                    return false;
                }
            }
        }
        
        if (request.getCreatedFrom() != null) {
            if (owner.getCreatedAt().isBefore(request.getCreatedFrom())) {
                return false;
            }
        }
        
        if (request.getCreatedTo() != null) {
            if (owner.getCreatedAt().isAfter(request.getCreatedTo())) {
                return false;
            }
        }
        
        return true;
    }

    private List<User> applySearch(List<User> owners, String search) {
        if (search == null || search.isBlank()) {
            return owners;
        }
        
        String searchLower = search.toLowerCase();
        
        return owners.stream()
                .filter(owner -> {
                    if (owner.getUserIdentifier().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    if (owner.getEmail().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    if (owner.getFullName().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    Business business = businessRepository.findByOwnerIdAndIsDeletedFalse(owner.getId()).orElse(null);
                    if (business != null) {
                        if (business.getName().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        if (business.getEmail() != null && business.getEmail().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
    }

    private List<User> applySorting(List<User> owners, BusinessOwnerFilterRequest request) {
        String sortBy = request.getSortBy();
        String sortDir = request.getSortDir();
        boolean ascending = "asc".equalsIgnoreCase(sortDir);
        
        return owners.stream()
                .sorted((o1, o2) -> {
                    int comparison = 0;
                    
                    switch (sortBy) {
                        case "ownerUserIdentifier":
                            comparison = o1.getUserIdentifier().compareTo(o2.getUserIdentifier());
                            break;
                        case "ownerEmail":
                            comparison = o1.getEmail().compareTo(o2.getEmail());
                            break;
                        case "businessName":
                            Business b1 = businessRepository.findByOwnerIdAndIsDeletedFalse(o1.getId()).orElse(null);
                            Business b2 = businessRepository.findByOwnerIdAndIsDeletedFalse(o2.getId()).orElse(null);
                            if (b1 != null && b2 != null) {
                                comparison = b1.getName().compareTo(b2.getName());
                            }
                            break;
                        case "createdAt":
                        default:
                            comparison = o1.getCreatedAt().compareTo(o2.getCreatedAt());
                            break;
                    }
                    
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    private User getOwnerOrThrow(UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Business owner not found with ID: " + ownerId));
        
        if (owner.getUserType() != UserType.BUSINESS_OWNER) {
            throw new ValidationException("User is not a business owner");
        }
        
        return owner;
    }

    private Business getBusinessOrThrow(UUID ownerId) {
        return businessRepository.findByOwnerIdAndIsDeletedFalse(ownerId)
                .orElseThrow(() -> new NotFoundException("Business not found for owner ID: " + ownerId));
    }

    private Subscription getCurrentSubscription(UUID businessId) {
        return subscriptionRepository.findFirstByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(businessId)
                .orElseThrow(() -> new NotFoundException("No subscription found for business ID: " + businessId));
    }

    private SubscriptionPlan getPlanOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Subscription plan not found with ID: " + planId));
    }
}


