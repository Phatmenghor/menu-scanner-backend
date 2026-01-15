package com.emenu.features.enums.mapper;

import com.emenu.features.enums.dto.request.AttendanceStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.AttendanceStatusEnumResponse;
import com.emenu.features.enums.dto.update.AttendanceStatusEnumUpdateRequest;
import com.emenu.features.enums.models.AttendanceStatusEnum;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendanceStatusEnumMapper {
    
    AttendanceStatusEnumResponse toResponse(AttendanceStatusEnum entity);
    
    List<AttendanceStatusEnumResponse> toResponseList(List<AttendanceStatusEnum> entities);
    
    AttendanceStatusEnum toEntity(AttendanceStatusEnumCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AttendanceStatusEnumUpdateRequest request, @MappingTarget AttendanceStatusEnum entity);
}