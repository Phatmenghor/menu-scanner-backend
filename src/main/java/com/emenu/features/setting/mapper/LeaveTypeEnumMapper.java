package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.features.setting.models.LeaveTypeEnum;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface LeaveTypeEnumMapper {

    LeaveTypeEnumResponse toResponse(LeaveTypeEnum entity);

    List<LeaveTypeEnumResponse> toResponseList(List<LeaveTypeEnum> entities);

    LeaveTypeEnum toEntity(LeaveTypeEnumCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveTypeEnumUpdateRequest request, @MappingTarget LeaveTypeEnum entity);

    /**
     * Convert paginated leave types to pagination response
     */
    default PaginationResponse<LeaveTypeEnumResponse> toPaginationResponse(Page<LeaveTypeEnum> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
