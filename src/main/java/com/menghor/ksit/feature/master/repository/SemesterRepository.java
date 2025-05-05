package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SemesterRepository extends JpaRepository<SemesterEntity, Long>, JpaSpecificationExecutor<SemesterEntity> {
}