package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Add to SemesterRepository.java
@Repository
public interface SemesterRepository extends JpaRepository<SemesterEntity, Long>, JpaSpecificationExecutor<SemesterEntity> {

    // Check if semester with same academy year and semester type exists
    boolean existsByAcademyYearAndSemesterAndStatus(Integer academyYear, SemesterEnum semester, Status status);

    // Check if semester with same academy year and semester type exists (excluding current ID for updates)
    boolean existsByAcademyYearAndSemesterAndStatusAndIdNot(Integer academyYear, SemesterEnum semester, Status status, Long id);

    // Find overlapping semesters by date range and academy year
    @Query("SELECT COUNT(s) > 0 FROM SemesterEntity s WHERE " +
            "s.academyYear = :academyYear AND s.status = :status AND " +
            "((s.startDate <= :endDate AND s.endDate >= :startDate))")
    boolean existsOverlappingSemester(@Param("academyYear") Integer academyYear,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("status") Status status);

    // Find overlapping semesters excluding current ID (for updates)
    @Query("SELECT COUNT(s) > 0 FROM SemesterEntity s WHERE " +
            "s.academyYear = :academyYear AND s.status = :status AND s.id != :excludeId AND " +
            "((s.startDate <= :endDate AND s.endDate >= :startDate))")
    boolean existsOverlappingSemesterExcludingId(@Param("academyYear") Integer academyYear,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("status") Status status,
                                                 @Param("excludeId") Long excludeId);

    // Find semesters in same academy year for detailed validation
    List<SemesterEntity> findByAcademyYearAndStatus(Integer academyYear, Status status);
}
