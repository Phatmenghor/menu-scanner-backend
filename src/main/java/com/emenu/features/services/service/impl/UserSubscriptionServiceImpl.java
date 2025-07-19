package com.emenu.features.services.service.impl;

import com.emenu.enums.SubscriptionStatus;
import com.emenu.exception.UserNotFoundException;
import com.emenu.features.services.domain.UserSubscription;
import com.emenu.features.services.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.services.dto.request.CreateSubscriptionRequest;
import com.emenu.features.services.dto.response.SubscriptionResponse;
import com.emenu.features.services.dto.update.UpdateSubscriptionRequest;
import com.emenu.features.services.service.UserSubscriptionService;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionMapper subscriptionMapper;
    private final SecurityUtils securityUtils;

    @Override
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {}", request.getUserId());

        // Validate user exists
        if (!userRepository.existsByIdAndIsDeletedFalse(request.getUserId())) {
            throw new UserNotFoundException("User not found");
        }

        // Check if user already has active subscription
        if (subscriptionRepository.existsByUserIdAndStatusAndIsDeletedFalse(request.getUserId(), SubscriptionStatus.ACTIVE)) {
            throw new ValidationException("User already has an active subscription");
        }

        // Get plan
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new ValidationException("Subscription plan not found"));

        // Create subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(request.getUserId());
        subscription.setPlanId(request.getPlanId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(calculateEndDate(request.getStartDate(), plan.getBillingCycle()));
        subscription.setAmount(plan.getPrice());
        subscription.setCurrency(plan.getCurrency());
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setCurrentUsers(0);
        subscription.setCurrentMenus(0);
        subscription.setCurrentMonthOrders(0);

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: {}", savedSubscription.getId());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse getSubscription(UUID id) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, UpdateSubscriptionRequest request) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));

        subscriptionMapper.updateEntity(request, subscription);
        UserSubscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription updated: {}", savedSubscription.getId());
        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public void cancelSubscription(UUID id) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled: {}", subscription.getId());
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID id) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(subscription.getPlanId())
                .orElseThrow(() -> new ValidationException("Subscription plan not found"));

        LocalDateTime newStartDate = subscription.getEndDate();
        LocalDateTime newEndDate = calculateEndDate(newStartDate, plan.getBillingCycle());

        subscription.setStartDate(newStartDate);
        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCurrentMonthOrders(0); // Reset monthly usage

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription renewed: {}", savedSubscription.getId());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse getUserSubscription(UUID userId) {
        UserSubscription subscription = subscriptionRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException("Active subscription not found for user"));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse getCurrentUserSubscription() {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return getUserSubscription(currentUserId);
    }

    @Override
    public PaginationResponse<SubscriptionResponse> listSubscriptions(SubscriptionFilterRequest filter) {
        Specification<UserSubscription> spec = buildSubscriptionSpec(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<UserSubscription> subscriptionPage = subscriptionRepository.findAll(spec, pageable);
        List<SubscriptionResponse> content = subscriptionPage.getContent().stream()
                .map(subscriptionMapper::toResponse)
                .toList();

        return PaginationResponse.<SubscriptionResponse>builder()
                .content(content)
                .pageNo(subscriptionPage.getNumber())
                .pageSize(subscriptionPage.getSize())
                .totalElements(subscriptionPage.getTotalElements())
                .totalPages(subscriptionPage.getTotalPages())
                .last(subscriptionPage.isLast())
                .first(subscriptionPage.isFirst())
                .hasNext(subscriptionPage.hasNext())
                .hasPrevious(subscriptionPage.hasPrevious())
                .build();
    }

    @Override
    public boolean canCreateUser(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        return subscription.canCreateUser();
    }

    @Override
    public boolean canCreateMenu(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        return subscription.canCreateMenu();
    }

    @Override
    public boolean canProcessOrder(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        return subscription.canProcessOrder();
    }

    @Override
    public void incrementUserCount(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        
        subscription.setCurrentUsers(subscription.getCurrentUsers() + 1);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void incrementMenuCount(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        
        subscription.setCurrentMenus(subscription.getCurrentMenus() + 1);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void incrementOrderCount(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found"));
        
        subscription.setCurrentMonthOrders(subscription.getCurrentMonthOrders() + 1);
        subscriptionRepository.save(subscription);
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, String billingCycle) {
        return switch (billingCycle.toUpperCase()) {
            case "MONTHLY" -> startDate.plusMonths(1);
            case "YEARLY" -> startDate.plusYears(1);
            default -> startDate.plusMonths(1);
        };
    }

    private Specification<UserSubscription> buildSubscriptionSpec(SubscriptionFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.and();
            predicates = cb.and(predicates, cb.equal(root.get("isDeleted"), false));

            if (filter.getPlanId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("planId"), filter.getPlanId()));
            }

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getAutoRenew() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("autoRenew"), filter.getAutoRenew()));
            }

            return predicates;
        };
    }
}