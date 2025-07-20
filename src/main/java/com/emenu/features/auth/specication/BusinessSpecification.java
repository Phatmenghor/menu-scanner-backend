package com.emenu.features.auth.specication;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.models.Business;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BusinessSpecification {

    public static Specification<Business> buildSpecification(BusinessFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Name filter
            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            // Email filter
            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + filter.getEmail().toLowerCase() + "%"
                ));
            }

            // Phone filter
            if (StringUtils.hasText(filter.getPhone())) {
                predicates.add(criteriaBuilder.like(
                        root.get("phone"),
                        "%" + filter.getPhone() + "%"
                ));
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = criteriaBuilder.like(
                        root.get("phone"), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        namePredicate, emailPredicate, phonePredicate, descriptionPredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}