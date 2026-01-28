package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.helper.UserSessionCreateHelper;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.models.UserSession;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSessionMapper {

    UserSession createFromHelper(UserSessionCreateHelper helper);

    @Mapping(target = "deviceDisplayName", expression = "java(session.getDeviceDisplayName())")
    UserSessionResponse toResponse(UserSession session);

    List<UserSessionResponse> toResponseList(List<UserSession> sessions);

    @Mapping(target = "deviceDisplayName", expression = "java(session.getDeviceDisplayName())")
    @Mapping(target = "userIdentifier", source = "session.user", qualifiedByName = "getUserIdentifier")
    @Mapping(target = "userFullName", source = "session.user", qualifiedByName = "getUserFullName")
    @Mapping(target = "userType", source = "session.user", qualifiedByName = "getUserType")
    AdminSessionResponse toAdminResponse(UserSession session);

    List<AdminSessionResponse> toAdminResponseList(List<UserSession> sessions);

    @Named("getUserIdentifier")
    default String getUserIdentifier(User user) {
        return user != null ? user.getUserIdentifier() : null;
    }

    @Named("getUserFullName")
    default String getUserFullName(User user) {
        if (user == null) return null;
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? " " + user.getLastName() : "");
        return fullName.trim().isEmpty() ? null : fullName.trim();
    }

    @Named("getUserType")
    default String getUserType(User user) {
        return user != null && user.getUserType() != null ? user.getUserType().name() : null;
    }

    default PaginationResponse<AdminSessionResponse> toPaginationResponse(Page<UserSession> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toAdminResponseList);
    }
}
