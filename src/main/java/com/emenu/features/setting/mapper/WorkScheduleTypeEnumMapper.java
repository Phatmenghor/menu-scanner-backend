package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.setting.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.features.setting.models.WorkScheduleTypeEnum;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkScheduleTypeEnumMapper {
    
    WorkScheduleTypeEnumResponse toResponse(WorkScheduleTypeEnum entity);
    
    List<WorkScheduleTypeEnumResponse> toResponseList(List<WorkScheduleTypeEnum> entities);
    
    WorkScheduleTypeEnum toEntity(WorkScheduleTypeEnumCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(WorkScheduleTypeEnumUpdateRequest request, @MappingTarget WorkScheduleTypeEnum entity);
}