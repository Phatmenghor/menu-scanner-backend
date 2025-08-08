package com.emenu.features.auth.specification;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.models.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecification {

    public static Specification<User> buildSearchSpecification(UserFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Business ID filter
            if (request.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), request.getBusinessId()));
            }

            // Account status filter
            if (request.getAccountStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountStatus"), request.getAccountStatus()));
            }

            // User type filter
            if (request.getUserType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), request.getUserType()));
            }

            // Roles filter
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
                predicates.add(rolesJoin.get("name").in(request.getRoles()));
                query.distinct(true);
            }

            // ✅ NEW: Social provider filter
            if (request.getSocialProvider() != null) {
                predicates.add(criteriaBuilder.equal(root.get("socialProvider"), request.getSocialProvider()));
            }

            // ✅ NEW: Telegram filters
            if (request.getHasTelegram() != null) {
                if (request.getHasTelegram()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("telegramUserId")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("telegramUserId")));
                }
            }

            if (request.getTelegramNotificationsEnabled() != null) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("telegramUserId")),
                    criteriaBuilder.equal(root.get("telegramNotificationsEnabled"), request.getTelegramNotificationsEnabled())
                ));
            }

            // Global search filter (now includes Telegram fields)
            if (StringUtils.hasText(request.getSearch())) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";
                
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate firstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), searchPattern);
                Predicate lastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), searchPattern);
                Predicate userIdentifierPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("userIdentifier")), searchPattern);
                
                // ✅ NEW: Telegram search fields
                Predicate telegramUsernamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("telegramUsername")), searchPattern);
                Predicate telegramFirstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("telegramFirstName")), searchPattern);
                Predicate telegramLastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("telegramLastName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                    emailPredicate, firstNamePredicate, lastNamePredicate, userIdentifierPredicate,
                    telegramUsernamePredicate, telegramFirstNamePredicate, telegramLastNamePredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ===== TELEGRAM-SPECIFIC SPECIFICATIONS =====
    
    public static Specification<User> hasTelegramLinked() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.isNotNull(root.get("telegramUserId"))
            );
    }

    public static Specification<User> canReceiveTelegramNotifications() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.isNotNull(root.get("telegramUserId")),
                criteriaBuilder.equal(root.get("telegramNotificationsEnabled"), true)
            );
    }

    public static Specification<User> bySocialProvider(SocialProvider provider) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.equal(root.get("socialProvider"), provider)
            );
    }

    public static Specification<User> telegramActiveUsers(LocalDateTime since) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("isDeleted"), false),
                criteriaBuilder.isNotNull(root.get("telegramUserId")),
                criteriaBuilder.greaterThan(root.get("lastTelegramActivity"), since)
            );
    }

    // ===== NOTIFICATION RECIPIENT SPECIFICATIONS =====
    
    public static Specification<User> notificationRecipients(
            boolean includePlatformUsers,
            boolean includeBusinessOwners, 
            boolean includeCustomers,
            List<UUID> specificUserIds,
            List<RoleEnum> specificRoles) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> conditions = new ArrayList<>();
            
            // Base condition
            conditions.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            
            List<Predicate> recipientConditions = new ArrayList<>();
            
            // User type conditions
            if (includePlatformUsers) {
                recipientConditions.add(criteriaBuilder.equal(root.get("userType"), UserType.PLATFORM_USER));
            }
            if (includeBusinessOwners) {
                recipientConditions.add(criteriaBuilder.equal(root.get("userType"), UserType.BUSINESS_USER));
            }
            if (includeCustomers) {
                recipientConditions.add(criteriaBuilder.equal(root.get("userType"), UserType.CUSTOMER));
            }
            
            // Specific user IDs
            if (specificUserIds != null && !specificUserIds.isEmpty()) {
                recipientConditions.add(root.get("id").in(specificUserIds));
            }
            
            // Specific roles
            if (specificRoles != null && !specificRoles.isEmpty()) {
                Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
                recipientConditions.add(rolesJoin.get("name").in(specificRoles));
                query.distinct(true);
            }
            
            if (!recipientConditions.isEmpty()) {
                conditions.add(criteriaBuilder.or(recipientConditions.toArray(new Predicate[0])));
            }
            
            return criteriaBuilder.and(conditions.toArray(new Predicate[0]));
        };
    }

    // ===== EXISTING SPECIFICATIONS (KEPT) =====
    
    public static Specification<User> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("accountStatus"), AccountStatus.ACTIVE)
                );
    }

    public static Specification<User> byUserType(UserType userType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("userType"), userType)
                );
    }

    public static Specification<User> byBusiness(UUID businessId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("businessId"), businessId)
                );
    }

    public static Specification<User> byAccountStatus(AccountStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.equal(root.get("accountStatus"), status)
                );
    }

    public static Specification<User> byRole(RoleEnum role) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.equal(rolesJoin.get("name"), role)
            );
        };
    }

    public static Specification<User> byRoles(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
            query.distinct(true);
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    rolesJoin.get("name").in(roles)
            );
        };
    }

    public static Specification<User> byEmailContaining(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("isDeleted"), false),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                "%" + email.toLowerCase() + "%")
                );
    }

    public static Specification<User> byNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("isDeleted"), false),
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern)
                    )
            );
        };
    }
}