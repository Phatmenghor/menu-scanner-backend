package com.menghor.ksit.feature.master.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.RoomEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class RoomSpecification {

    /**
     * Search rooms by name (partial match, case-insensitive)
     */
    public static Specification<RoomEntity> searchByName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    "%" + name.toLowerCase() + "%"
                );
            }
            return null; // Return null to indicate no filtering if name is empty
        };
    }

    /**
     * Filter rooms by status
     */
    public static Specification<RoomEntity> filterByStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("status"), status);
            }
            return null; // Return null to indicate no filtering if status is null
        };
    }
    
    /**
     * Combine multiple specifications with AND operator
     */
    public static Specification<RoomEntity> combine(String search, Status status) {
        Specification<RoomEntity> result = Specification.where(null);

        if (StringUtils.hasText(search)) {
            result = result.and(searchByName(search));
        }

        if (status != null) {
            result = result.and(filterByStatus(status));
        }

        return result;
    }
}