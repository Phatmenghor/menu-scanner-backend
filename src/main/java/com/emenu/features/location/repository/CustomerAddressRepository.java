package com.emenu.features.location.repository;

import com.emenu.features.location.models.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {

    /**
     * Finds all non-deleted addresses for a user, ordered by default status and creation date
     */
    List<CustomerAddress> findByUserIdAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    /**
     * Finds the default non-deleted address for a user
     */
    Optional<CustomerAddress> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);

    /**
     * Clears the default flag for all non-deleted addresses belonging to a user
     */
    @Modifying
    @Query("UPDATE CustomerAddress ca SET ca.isDefault = false WHERE ca.userId = :userId AND ca.isDeleted = false")
    void clearDefaultForUser(@Param("userId") UUID userId);

    /**
     * Finds a non-deleted customer address by ID
     */
    Optional<CustomerAddress> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted customer address by ID with user details eagerly fetched
     */
    @Query("SELECT ca FROM CustomerAddress ca " +
           "LEFT JOIN FETCH ca.user " +
           "WHERE ca.id = :id AND ca.isDeleted = false")
    Optional<CustomerAddress> findByIdWithUser(@Param("id") UUID id);

    /**
     * Find all customer addresses with dynamic filtering
     */
    @Query("SELECT ca FROM CustomerAddress ca " +
           "WHERE ca.isDeleted = false " +
           "AND (:userId IS NULL OR ca.userId = :userId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(ca.province) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ca.district) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ca.commune) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ca.village) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ca.streetNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(ca.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CustomerAddress> findAllWithFilters(
        @Param("userId") UUID userId,
        @Param("search") String search,
        Pageable pageable
    );
}