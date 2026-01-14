package com.emenu.features.leave.specification;

import com.emenu.features.leave.dto.filter.LeaveFilterRequest;
import com.emenu.features.leave.models.Leave;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LeaveSpecification {

    public static Specification<Leave> withFilters(LeaveFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getLeavePolicyId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leavePolicy").get("id"), filter.getLeavePolicyId()));
            }

            if (filter.getLeaveType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leavePolicy").get("leaveType"), filter.getLeaveType()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), filter.getToDate()));
            }

            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("leavePolicy").get("business").get("id"),
                        filter.getBusinessId()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
