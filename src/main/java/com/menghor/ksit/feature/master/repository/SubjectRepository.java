package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Long>, JpaSpecificationExecutor<SubjectEntity> {
}