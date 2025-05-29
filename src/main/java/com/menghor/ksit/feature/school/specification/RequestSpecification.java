package com.menghor.ksit.feature.school.specification;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.dto.filter.RequestFilterDto;
import com.menghor.ksit.feature.school.model.RequestEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestSpecification {
    
    public static Specification<RequestEntity> createSpecification(RequestFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Search in title, description, user name
            if (StringUtils.hasText(filter.getSearch())) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Join<RequestEntity, UserEntity> userJoin = root.join("user", JoinType.LEFT);
                
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchTerm);
                Predicate userUsernamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("username")), searchTerm);
                Predicate userKhmerFirstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("khmerFirstName")), searchTerm);
                Predicate userKhmerLastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("khmerLastName")), searchTerm);
                Predicate userEnglishFirstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("englishFirstName")), searchTerm);
                Predicate userEnglishLastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("englishLastName")), searchTerm);
                Predicate userIdentifyNumberPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("identifyNumber")), searchTerm);
                
                predicates.add(criteriaBuilder.or(
                    titlePredicate, 
                    descriptionPredicate, 
                    userUsernamePredicate,
                    userKhmerFirstNamePredicate,
                    userKhmerLastNamePredicate,
                    userEnglishFirstNamePredicate,
                    userEnglishLastNamePredicate,
                    userIdentifyNumberPredicate
                ));
            }
            
            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }
            
            // Filter by user ID
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<RequestEntity> findByUserId(Long userId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
    
    public static Specification<RequestEntity> findByStatus(RequestStatus status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), status);
    }
}