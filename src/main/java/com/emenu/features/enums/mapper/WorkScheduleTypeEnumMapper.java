package com.emenu.features.enums.mapper;

import com.emenu.features.enums.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.features.enums.models.WorkScheduleTypeEnum;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleTypeEnumMapper {
    
    WorkScheduleTypeEnumResponse toResponse(WorkScheduleTypeEnum entity);
    
    List<WorkScheduleTypeEnumResponse> toResponseList(List<WorkScheduleTypeEnum> entities);
    
    WorkScheduleTypeEnum toEntity(WorkScheduleTypeEnumCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(WorkScheduleTypeEnumUpdateRequest request, @MappingTarget WorkScheduleTypeEnum entity);
}