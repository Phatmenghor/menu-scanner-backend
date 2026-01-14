package com.emenu.features.attendance.specification;

import com.emenu.features.attendance.dto.filter.AttendanceFilterRequest;
import com.emenu.features.attendance.models.Attendance;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AttendanceSpecification {

    public static Specification<Attendance> withFilters(AttendanceFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getWorkScheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("workSchedule").get("id"), filter.getWorkScheduleId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("attendanceDate"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("attendanceDate"), filter.getToDate()));
            }

            if (filter.getBusinessId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("workSchedule").get("attendancePolicy").get("business").get("id"),
                        filter.getBusinessId()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
