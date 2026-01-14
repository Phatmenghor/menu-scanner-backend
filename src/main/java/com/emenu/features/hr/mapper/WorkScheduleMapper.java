package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.models.WorkSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {
    
    @Mapping(target = "scheduleTypeEnumName", ignore = true)
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);
    
    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);
    
    @Mapping(target = "scheduleTypeEnumId", ignore = true)
    WorkSchedule toEntity(WorkScheduleCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "scheduleTypeEnumId", ignore = true)
    void updateEntity(WorkScheduleUpdateRequest request, @MappingTarget WorkSchedule workSchedule);
}