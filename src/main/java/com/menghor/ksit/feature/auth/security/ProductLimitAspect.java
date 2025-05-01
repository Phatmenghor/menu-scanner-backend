package com.menghor.ksit.feature.auth.security;

import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.setting.model.SubscriptionEntity;
import com.menghor.ksit.feature.setting.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductLimitAspect {
    
    private final SubscriptionRepository subscriptionRepository;
    
    @Before("execution(* com.menghor.smart_shop.feature.customer.service.*.createProduct(..))")
    public void beforeCreateProduct(JoinPoint joinPoint) {
        log.info("Checking product limit before creating a new product");
        
        // Get user from first argument (assuming it's the user or contains the user)
        UserEntity user = extractUser(joinPoint.getArgs());
        
        if (user == null) {
            log.warn("Could not extract user from method arguments");
            return;
        }
        
        Optional<SubscriptionEntity> subscriptionOpt =
                subscriptionRepository.findActiveSubscriptionForUser(user.getId(), LocalDateTime.now());
        
        if (subscriptionOpt.isEmpty()) {
            log.warn("User does not have an active subscription");
            throw new BadRequestException("You do not have an active subscription. Please subscribe to create products.");
        }
        
        SubscriptionEntity subscription = subscriptionOpt.get();
        
        // Check if user has reached the product limit
        int currentProductCount = user.getShop() != null && user.getShop().getProducts() != null 
                ? user.getShop().getProducts().size() 
                : 0;
        
        int maxProducts = subscription.getPlan().getMaxProducts();
        
        if (currentProductCount >= maxProducts) {
            log.warn("User has reached the product limit: {}/{}", currentProductCount, maxProducts);
            throw new BadRequestException(
                    "You have reached the maximum product limit for your subscription plan. " +
                    "Please upgrade your plan to add more products.");
        }
        
        log.info("Product limit check passed: {}/{}", currentProductCount, maxProducts);
    }
    
    @Before("execution(* com.menghor.smart_shop.feature.customer.service.*.createBanner(..))")
    public void beforeCreateBanner(JoinPoint joinPoint) {
        log.info("Checking if banner creation is allowed");
        
        // Get user from arguments
        UserEntity user = extractUser(joinPoint.getArgs());
        
        if (user == null) {
            log.warn("Could not extract user from method arguments");
            return;
        }
        
        Optional<SubscriptionEntity> subscriptionOpt = 
                subscriptionRepository.findActiveSubscriptionForUser(user.getId(), LocalDateTime.now());
        
        if (subscriptionOpt.isEmpty()) {
            log.warn("User does not have an active subscription");
            throw new BadRequestException("You do not have an active subscription. Please subscribe to create banners.");
        }
        
        SubscriptionEntity subscription = subscriptionOpt.get();
        
        // Check if user's plan allows banners
        if (!subscription.getPlan().getAllowBanners()) {
            log.warn("User's plan does not allow banner creation");
            throw new BadRequestException(
                    "Your current subscription plan does not support banner creation. " +
                    "Please upgrade to a plan that includes banner support.");
        }
        
        log.info("Banner creation check passed");
    }
    
    @Before("execution(* com.menghor.smart_shop.feature.customer.service.*.resetDiscountForProduct(..))")
    public void beforeManagePromotions(JoinPoint joinPoint) {
        log.info("Checking if promotion management is allowed");
        
        // Get user from arguments
        UserEntity user = extractUser(joinPoint.getArgs());
        
        if (user == null) {
            log.warn("Could not extract user from method arguments");
            return;
        }
        
        Optional<SubscriptionEntity> subscriptionOpt = 
                subscriptionRepository.findActiveSubscriptionForUser(user.getId(), LocalDateTime.now());
        
        if (subscriptionOpt.isEmpty()) {
            log.warn("User does not have an active subscription");
            throw new BadRequestException("You do not have an active subscription. Please subscribe to manage promotions.");
        }
        
        SubscriptionEntity subscription = subscriptionOpt.get();
        
        // Check if user's plan allows promotions
        if (!subscription.getPlan().getAllowPromotions()) {
            log.warn("User's plan does not allow promotion management");
            throw new BadRequestException(
                    "Your current subscription plan does not support promotion management. " +
                    "Please upgrade to a plan that includes promotion features.");
        }
        
        log.info("Promotion management check passed");
    }
    
    private UserEntity extractUser(Object[] args) {
        // This method should be adapted based on your actual service method signatures
        for (Object arg : args) {
            if (arg instanceof UserEntity) {
                return (UserEntity) arg;
            }
        }
        return null;
    }
}