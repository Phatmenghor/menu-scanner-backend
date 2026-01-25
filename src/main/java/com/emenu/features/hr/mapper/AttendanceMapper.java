package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.models.Attendance;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AttendanceCheckInMapper.class, PaginationMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AttendanceMapper {

    @Mapping(target = "userInfo.id", source = "user.id")
    @Mapping(target = "userInfo.firstName", source = "user.firstName")
    @Mapping(target = "userInfo.lastName", source = "user.lastName")
    @Mapping(target = "userInfo.email", source = "user.email")
    @Mapping(target = "userInfo.phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "userInfo.profileImageUrl", source = "user.profileImageUrl")
    AttendanceResponse toResponse(Attendance attendance);

    List<AttendanceResponse> toResponseList(List<Attendance> attendances);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AttendanceUpdateRequest request, @MappingTarget Attendance attendance);

    /**
     * Convert paginated attendances to pagination response
     */
    default PaginationResponse<AttendanceResponse> toPaginationResponse(Page<Attendance> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
