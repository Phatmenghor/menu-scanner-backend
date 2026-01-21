package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.features.setting.models.LeaveTypeEnum;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveTypeEnumMapper {
    
    LeaveTypeEnumResponse toResponse(LeaveTypeEnum entity);
    
    List<LeaveTypeEnumResponse> toResponseList(List<LeaveTypeEnum> entities);
    
    LeaveTypeEnum toEntity(LeaveTypeEnumCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveTypeEnumUpdateRequest request, @MappingTarget LeaveTypeEnum entity);
}