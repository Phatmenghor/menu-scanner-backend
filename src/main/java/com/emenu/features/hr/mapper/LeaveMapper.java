package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.models.Leave;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class})
public interface LeaveMapper {

    LeaveResponse toResponse(Leave leave);

    List<LeaveResponse> toResponseList(List<Leave> leaves);

    Leave toEntity(LeaveCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LeaveUpdateRequest request, @MappingTarget Leave leave);

    /**
     * Convert paginated leaves to pagination response
     */
    default PaginationResponse<LeaveResponse> toPaginationResponse(Page<Leave> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}