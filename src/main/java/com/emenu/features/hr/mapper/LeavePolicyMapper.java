package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.hr.dto.response.LeavePolicyResponse;
import com.emenu.features.hr.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.hr.models.LeavePolicy;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeavePolicyMapper {
    
    @Mapping(target = "typeEnumName", ignore = true)
    LeavePolicyResponse toResponse(LeavePolicy policy);
    
    List<LeavePolicyResponse> toResponseList(List<LeavePolicy> policies);
    
    @Mapping(target = "typeEnumId", ignore = true)
    LeavePolicy toEntity(LeavePolicyCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "typeEnumId", ignore = true)
    void updateEntity(LeavePolicyUpdateRequest request, @MappingTarget LeavePolicy policy);
}