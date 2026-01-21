package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.models.WorkSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {
    
    @Mapping(target = "scheduleTypeEnum", ignore = true)
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);
    
    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);
    
    @Mapping(target = "scheduleTypeEnum", ignore = true)
    WorkSchedule toEntity(WorkScheduleCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "scheduleTypeEnum", ignore = true)
    void updateEntity(WorkScheduleUpdateRequest request, @MappingTarget WorkSchedule workSchedule);
}