package com.emenu.features.enums.mapper;

import com.emenu.features.enums.dto.request.LeaveStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.LeaveStatusEnumResponse;
import com.emenu.features.enums.dto.update.LeaveStatusEnumUpdateRequest;
import com.emenu.features.enums.models.LeaveStatusEnum;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveStatusEnumMapper {
    
    LeaveStatusEnumResponse toResponse(LeaveStatusEnum entity);
    
    List<LeaveStatusEnumResponse> toResponseList(List<LeaveStatusEnum> entities);
    
    LeaveStatusEnum toEntity(LeaveStatusEnumCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveStatusEnumUpdateRequest request, @MappingTarget LeaveStatusEnum entity);
}