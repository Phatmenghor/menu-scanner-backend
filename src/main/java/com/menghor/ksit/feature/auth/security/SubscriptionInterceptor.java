package com.menghor.ksit.feature.auth.security;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.setting.model.SubscriptionEntity;
import com.menghor.ksit.feature.setting.repository.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the authenticated user from the security context
        UserEntity user = (UserEntity) request.getAttribute("currentUser");

        if (user == null) {
            return true; // No user authenticated, let the security handle it
        }

        // Check if the user has an admin, manager, or developer role
        List<Role> roles = user.getRoles();
        boolean isAdmin = roles.stream()
                .anyMatch(role -> role.getName() == RoleEnum.ADMIN ||
                        role.getName() == RoleEnum.MANAGER ||
                        role.getName() == RoleEnum.DEVELOPER);

        if (isAdmin) {
            return true; // Admin users bypass subscription checks
        }

        // If the user is a shop admin, check if they have an active subscription
        boolean isShopAdmin = roles.stream()
                .anyMatch(role -> role.getName() == RoleEnum.SHOP_ADMIN);

        if (isShopAdmin) {
            // Check if user has an active subscription
            Optional<SubscriptionEntity> activeSubscription =
                    subscriptionRepository.findActiveSubscriptionForUser(user.getId(), LocalDateTime.now());

            if (activeSubscription.isEmpty()) {
                log.warn("User ID: {} attempted to access features without an active subscription", user.getId());
                throw new BadRequestException("Your subscription has expired. Please renew your subscription to continue using the platform.");
            }
        }

        return true;
    }
}