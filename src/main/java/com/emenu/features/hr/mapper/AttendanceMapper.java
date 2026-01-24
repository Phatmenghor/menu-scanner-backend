package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.models.Attendance;
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

@Mapper(componentModel = "spring", uses = {AttendanceCheckInMapper.class, PaginationMapper.class})
public interface AttendanceMapper {

    @Mapping(target = "userInfo.id", source = "user.id")
    @Mapping(target = "userInfo.userIdentifier", source = "user.userIdentifier")
    @Mapping(target = "userInfo.fullName", expression = "java(attendance.getUser() != null ? attendance.getUser().getFullName() : null)")
    @Mapping(target = "userInfo.email", source = "user.email")
    @Mapping(target = "userInfo.profileImageUrl", source = "user.profileImageUrl")
    AttendanceResponse toResponse(Attendance attendance);

    List<AttendanceResponse> toResponseList(List<Attendance> attendances);

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
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "workScheduleId", ignore = true)
    @Mapping(target = "workSchedule", ignore = true)
    @Mapping(target = "attendanceDate", ignore = true)
    @Mapping(target = "checkIns", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(AttendanceUpdateRequest request, @MappingTarget Attendance attendance);

    /**
     * Convert paginated attendances to pagination response
     */
    default PaginationResponse<AttendanceResponse> toPaginationResponse(Page<Attendance> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}