package com.emenu.features.user_management.specification;

import com.emenu.enums.*;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecification {

    public static Specification<User> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    public static Specification<User> isDeleted(Boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (deleted == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("isDeleted"), deleted);
        };
    }

    public static Specification<User> hasSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("company")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeId")), searchPattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasUserType(UserType userType) {
        return (root, query, criteriaBuilder) -> {
            if (userType == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("userType"), userType);
        };
    }

    public static Specification<User> hasAccountStatus(AccountStatus accountStatus) {
        return (root, query, criteriaBuilder) -> {
            if (accountStatus == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("accountStatus"), accountStatus);
        };
    }

    public static Specification<User> hasRole(RoleEnum role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) return criteriaBuilder.conjunction();

            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("name"), role);
        };
    }

    public static Specification<User> hasAnyRole(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            if (roles == null || roles.isEmpty()) return criteriaBuilder.conjunction();

            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return roleJoin.get("name").in(roles);
        };
    }

    public static Specification<User> hasAllRoles(List<RoleEnum> roles) {
        return (root, query, criteriaBuilder) -> {
            if (roles == null || roles.isEmpty()) return criteriaBuilder.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            for (RoleEnum role : roles) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<User> subRoot = subquery.from(User.class);
                Join<User, Role> subRoleJoin = subRoot.join("roles");

                subquery.select(criteriaBuilder.count(subRoot))
                        .where(criteriaBuilder.and(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subRoleJoin.get("name"), role)
                        ));

                predicates.add(criteriaBuilder.greaterThan(subquery, 0L));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasCustomerTier(CustomerTier customerTier) {
        return (root, query, criteriaBuilder) -> {
            if (customerTier == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("customerTier"), customerTier);
        };
    }

    public static Specification<User> hasBusinessId(UUID businessId) {
        return (root, query, criteriaBuilder) -> {
            if (businessId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("businessId"), businessId);
        };
    }

    public static Specification<User> hasPrimaryBusinessId(UUID primaryBusinessId) {
        return (root, query, criteriaBuilder) -> {
            if (primaryBusinessId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("primaryBusinessId"), primaryBusinessId);
        };
    }

    public static Specification<User> hasAccessToBusinessId(UUID businessId) {
        return (root, query, criteriaBuilder) -> {
            if (businessId == null) return criteriaBuilder.conjunction();

            // Check if businessId is in accessibleBusinessIds collection
            return criteriaBuilder.isMember(businessId, root.get("accessibleBusinessIds"));
        };
    }

    public static Specification<User> hasSubscriptionPlan(SubscriptionPlan plan) {
        return (root, query, criteriaBuilder) -> {
            if (plan == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("subscriptionPlan"), plan);
        };
    }

    public static Specification<User> hasActiveSubscription(Boolean hasActive) {
        return (root, query, criteriaBuilder) -> {
            if (hasActive == null) return criteriaBuilder.conjunction();

            if (hasActive) {
                return criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("subscriptionEnds")),
                        criteriaBuilder.greaterThan(root.get("subscriptionEnds"), LocalDateTime.now()),
                        criteriaBuilder.notEqual(root.get("subscriptionPlan"), SubscriptionPlan.FREE)
                );
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("subscriptionEnds")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("subscriptionEnds"), LocalDateTime.now()),
                        criteriaBuilder.equal(root.get("subscriptionPlan"), SubscriptionPlan.FREE)
                );
            }
        };
    }

    public static Specification<User> subscriptionExpiringSoon(Boolean expiringSoon) {
        return (root, query, criteriaBuilder) -> {
            if (expiringSoon == null || !expiringSoon) return criteriaBuilder.conjunction();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyDaysFromNow = now.plusDays(30);

            return criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("subscriptionEnds")),
                    criteriaBuilder.between(root.get("subscriptionEnds"), now, thirtyDaysFromNow),
                    criteriaBuilder.notEqual(root.get("subscriptionPlan"), SubscriptionPlan.FREE)
            );
        };
    }

    public static Specification<User> hasDepartment(String department) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(department)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("department"), department);
        };
    }

    public static Specification<User> hasEmployeeId(String employeeId) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(employeeId)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("employeeId"), employeeId);
        };
    }

    public static Specification<User> hiredBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hireDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), endDate));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> isEmailVerified(Boolean emailVerified) {
        return (root, query, criteriaBuilder) -> {
            if (emailVerified == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("emailVerified"), emailVerified);
        };
    }

    public static Specification<User> isPhoneVerified(Boolean phoneVerified) {
        return (root, query, criteriaBuilder) -> {
            if (phoneVerified == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("phoneVerified"), phoneVerified);
        };
    }

    public static Specification<User> isTwoFactorEnabled(Boolean twoFactorEnabled) {
        return (root, query, criteriaBuilder) -> {
            if (twoFactorEnabled == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("twoFactorEnabled"), twoFactorEnabled);
        };
    }

    public static Specification<User> lastLoginBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastLogin"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastLogin"), endDate.atTime(23, 59, 59)));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> lastActiveBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastActive"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastActive"), endDate.atTime(23, 59, 59)));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> loyaltyPointsBetween(Integer minPoints, Integer maxPoints) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPoints != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("loyaltyPoints"), minPoints));
            }

            if (maxPoints != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("loyaltyPoints"), maxPoints));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> totalSpentBetween(Double minSpent, Double maxSpent) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minSpent != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalSpent"), minSpent));
            }

            if (maxSpent != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalSpent"), maxSpent));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> totalOrdersBetween(Integer minOrders, Integer maxOrders) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minOrders != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalOrders"), minOrders));
            }

            if (maxOrders != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalOrders"), maxOrders));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(city)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("city"), city);
        };
    }

    public static Specification<User> hasState(String state) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(state)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("state"), state);
        };
    }

    public static Specification<User> hasCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(country)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("country"), country);
        };
    }

    public static Specification<User> hasUtmSource(String utmSource) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(utmSource)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("utmSource"), utmSource);
        };
    }

    public static Specification<User> hasUtmMedium(String utmMedium) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(utmMedium)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("utmMedium"), utmMedium);
        };
    }

    public static Specification<User> hasUtmCampaign(String utmCampaign) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(utmCampaign)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("utmCampaign"), utmCampaign);
        };
    }

    public static Specification<User> hasReferralCode(String referralCode) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(referralCode)) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("referralCode"), referralCode);
        };
    }

    public static Specification<User> hasTermsAccepted(Boolean termsAccepted) {
        return (root, query, criteriaBuilder) -> {
            if (termsAccepted == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("termsAccepted"), termsAccepted);
        };
    }

    public static Specification<User> hasPrivacyAccepted(Boolean privacyAccepted) {
        return (root, query, criteriaBuilder) -> {
            if (privacyAccepted == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("privacyAccepted"), privacyAccepted);
        };
    }

    public static Specification<User> hasDataProcessingConsent(Boolean consent) {
        return (root, query, criteriaBuilder) -> {
            if (consent == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("dataProcessingConsent"), consent);
        };
    }

    public static Specification<User> hasMarketingConsent(Boolean consent) {
        return (root, query, criteriaBuilder) -> {
            if (consent == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("marketingConsent"), consent);
        };
    }

    public static Specification<User> hasEmailNotifications(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("emailNotifications"), enabled);
        };
    }

    public static Specification<User> hasTelegramNotifications(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("telegramNotifications"), enabled);
        };
    }

    public static Specification<User> hasMarketingEmails(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("marketingEmails"), enabled);
        };
    }

    public static Specification<User> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) return criteriaBuilder.conjunction();

            if (active) {
                return criteriaBuilder.and(
                        criteriaBuilder.in(root.get("accountStatus")).value(List.of(AccountStatus.ACTIVE, AccountStatus.TRIAL)),
                        criteriaBuilder.equal(root.get("isDeleted"), false)
                );
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.not(criteriaBuilder.in(root.get("accountStatus")).value(List.of(AccountStatus.ACTIVE, AccountStatus.TRIAL))),
                        criteriaBuilder.equal(root.get("isDeleted"), true)
                );
            }
        };
    }

    public static Specification<User> isLocked(Boolean locked) {
        return (root, query, criteriaBuilder) -> {
            if (locked == null) return criteriaBuilder.conjunction();

            if (locked) {
                return criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("accountStatus"), AccountStatus.LOCKED),
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("accountLockedUntil")),
                                criteriaBuilder.greaterThan(root.get("accountLockedUntil"), LocalDateTime.now())
                        )
                );
            } else {
                return criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("accountStatus"), AccountStatus.LOCKED),
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("accountLockedUntil")),
                                criteriaBuilder.lessThanOrEqualTo(root.get("accountLockedUntil"), LocalDateTime.now())
                        )
                );
            }
        };
    }

    public static Specification<User> sessionCountBetween(Integer minSessions, Integer maxSessions) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minSessions != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sessionCount"), minSessions));
            }

            if (maxSessions != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sessionCount"), maxSessions));
            }

            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> totalLoginTimeGreaterThan(Long minLoginTime) {
        return (root, query, criteriaBuilder) -> {
            if (minLoginTime == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get("totalLoginTime"), minLoginTime);
        };
    }
}