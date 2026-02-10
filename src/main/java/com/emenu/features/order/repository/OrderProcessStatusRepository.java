package com.emenu.features.order.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.order.models.OrderProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderProcessStatusRepository extends JpaRepository<OrderProcessStatus, UUID> {

    @Query("SELECT ops FROM OrderProcessStatus ops " +
           "LEFT JOIN FETCH ops.business " +
           "WHERE ops.id = :id AND ops.isDeleted = false")
    Optional<OrderProcessStatus> findByIdWithBusiness(@Param("id") UUID id);

    Optional<OrderProcessStatus> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    Optional<OrderProcessStatus> findByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    @Query("SELECT ops FROM OrderProcessStatus ops " +
           "WHERE ops.businessId = :businessId AND ops.isDeleted = false " +
           "ORDER BY ops.createdAt ASC")
    List<OrderProcessStatus> findByBusinessIdOrderByCreatedAtAsc(@Param("businessId") UUID businessId);

    @Query("SELECT ops FROM OrderProcessStatus ops " +
           "WHERE ops.businessId = :businessId AND ops.status = :status AND ops.isDeleted = false " +
           "ORDER BY ops.createdAt ASC")
    List<OrderProcessStatus> findByBusinessIdAndStatusOrderByCreatedAtAsc(
            @Param("businessId") UUID businessId, @Param("status") Status status);

    @Query("SELECT DISTINCT ops FROM OrderProcessStatus ops " +
           "LEFT JOIN ops.business b " +
           "WHERE ops.isDeleted = false " +
           "AND (:businessId IS NULL OR ops.businessId = :businessId) " +
           "AND (:statuses IS NULL OR ops.status IN :statuses) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(ops.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ops.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OrderProcessStatus> findAllWithFilters(
            @Param("businessId") UUID businessId,
            @Param("statuses") List<Status> statuses,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT DISTINCT ops FROM OrderProcessStatus ops " +
           "LEFT JOIN ops.business b " +
           "WHERE ops.isDeleted = false " +
           "AND (:businessId IS NULL OR ops.businessId = :businessId) " +
           "AND (:statuses IS NULL OR ops.status IN :statuses) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(ops.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ops.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<OrderProcessStatus> findAllWithFilters(
            @Param("businessId") UUID businessId,
            @Param("statuses") List<Status> statuses,
            @Param("search") String search,
            Sort sort);
}
