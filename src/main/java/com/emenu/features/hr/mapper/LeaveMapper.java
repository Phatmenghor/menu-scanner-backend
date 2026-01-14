package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.models.Leave;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveMapper {
    
    @Mapping(target = "statusEnumName", ignore = true)
    LeaveResponse toResponse(Leave leave);
    
    List<LeaveResponse> toResponseList(List<Leave> leaves);
    
    Leave toEntity(LeaveCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "statusEnumId", ignore = true)
    void updateEntity(LeaveUpdateRequest request, @MappingTarget Leave leave);
}