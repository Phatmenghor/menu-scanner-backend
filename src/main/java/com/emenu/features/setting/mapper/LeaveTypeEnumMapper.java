package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.features.setting.models.LeaveTypeEnum;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class})
public interface LeaveTypeEnumMapper {

    LeaveTypeEnumResponse toResponse(LeaveTypeEnum entity);

    List<LeaveTypeEnumResponse> toResponseList(List<LeaveTypeEnum> entities);

    LeaveTypeEnum toEntity(LeaveTypeEnumCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "auditorAware", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    void updateEntity(LeaveTypeEnumUpdateRequest request, @MappingTarget LeaveTypeEnum entity);

    /**
     * Convert paginated leave types to pagination response
     */
    default PaginationResponse<LeaveTypeEnumResponse> toPaginationResponse(Page<LeaveTypeEnum> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}