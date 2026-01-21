package com.emenu.features.location.specification;

import com.emenu.features.location.dto.filter.CustomerAddressFilterRequest;
import com.emenu.features.location.models.CustomerAddress;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerAddressSpecification {

    public static Specification<CustomerAddress> buildSpecification(CustomerAddressFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: not deleted
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // User ID filter
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));
            }

            // Status filter (if you have a status field)
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Global search filter
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                
                Predicate villageePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("village")), searchPattern);
                Predicate communePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("commune")), searchPattern);
                Predicate districtPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("district")), searchPattern);
                Predicate provincePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("province")), searchPattern);
                Predicate streetNumberPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("streetNumber")), searchPattern);
                Predicate houseNumberPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("houseNumber")), searchPattern);
                Predicate notePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("note")), searchPattern);
                Predicate userNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("firstName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        villageePredicate, communePredicate, districtPredicate, provincePredicate,
                        streetNumberPredicate, houseNumberPredicate, notePredicate, userNamePredicate
                ));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}