package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.hr.dto.response.AttendancePolicyResponse;
import com.emenu.features.hr.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.features.hr.models.AttendancePolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendancePolicyMapper {
    AttendancePolicyResponse toResponse(AttendancePolicy policy);
    List<AttendancePolicyResponse> toResponseList(List<AttendancePolicy> policies);
    AttendancePolicy toEntity(AttendancePolicyCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AttendancePolicyUpdateRequest request, @MappingTarget AttendancePolicy policy);
}