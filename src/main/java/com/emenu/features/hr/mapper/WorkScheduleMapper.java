package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.models.WorkSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {
    
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);
    
    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);
    
    WorkSchedule toEntity(WorkScheduleCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(WorkScheduleUpdateRequest request, @MappingTarget WorkSchedule workSchedule);
}