package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.setting.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.features.setting.models.WorkScheduleTypeEnum;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkScheduleTypeEnumMapper {

    WorkScheduleTypeEnumResponse toResponse(WorkScheduleTypeEnum entity);

    List<WorkScheduleTypeEnumResponse> toResponseList(List<WorkScheduleTypeEnum> entities);

    WorkScheduleTypeEnum toEntity(WorkScheduleTypeEnumCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(WorkScheduleTypeEnumUpdateRequest request, @MappingTarget WorkScheduleTypeEnum entity);

    /**
     * Convert paginated work schedule types to pagination response
     */
    default PaginationResponse<WorkScheduleTypeEnumResponse> toPaginationResponse(Page<WorkScheduleTypeEnum> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}