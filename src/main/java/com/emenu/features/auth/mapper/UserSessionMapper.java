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

/**
 * MapStruct mapper for UserSession entity
 */
@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSessionMapper {

    /**
     * Create UserSession from helper DTO - pure MapStruct mapping
     */
    UserSession createFromHelper(UserSessionCreateHelper helper);

    /**
     * Map UserSession to UserSessionResponse
     */
    @Mapping(target = "deviceDisplayName", expression = "java(session.getDeviceDisplayName())")
    @Mapping(target = "sessionDurationMinutes", expression = "java(session.getSessionDurationMinutes())")
    @Mapping(target = "inactiveDurationMinutes", expression = "java(session.getInactiveDurationMinutes())")
    UserSessionResponse toResponse(UserSession session);

    /**
     * Map list of UserSession to list of UserSessionResponse
     */
    List<UserSessionResponse> toResponseList(List<UserSession> sessions);

    /**
     * Map UserSession to AdminSessionResponse with user info
     */
    @Mapping(target = "deviceDisplayName", expression = "java(session.getDeviceDisplayName())")
    @Mapping(target = "sessionDurationMinutes", expression = "java(session.getSessionDurationMinutes())")
    @Mapping(target = "inactiveDurationMinutes", expression = "java(session.getInactiveDurationMinutes())")
    @Mapping(target = "userIdentifier", source = "session.user", qualifiedByName = "getUserIdentifier")
    @Mapping(target = "userFullName", source = "session.user", qualifiedByName = "getUserFullName")
    @Mapping(target = "userType", source = "session.user", qualifiedByName = "getUserType")
    AdminSessionResponse toAdminResponse(UserSession session);

    /**
     * Map list of UserSession to list of AdminSessionResponse
     */
    List<AdminSessionResponse> toAdminResponseList(List<UserSession> sessions);

    /**
     * Extract user identifier from User entity
     */
    @Named("getUserIdentifier")
    default String getUserIdentifier(User user) {
        return user != null ? user.getUserIdentifier() : null;
    }

    /**
     * Extract user full name from User entity
     */
    @Named("getUserFullName")
    default String getUserFullName(User user) {
        if (user == null) return null;
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? " " + user.getLastName() : "");
        fullName = fullName.trim();
        return fullName.isEmpty() ? null : fullName;
    }

    /**
     * Extract user type from User entity
     */
    @Named("getUserType")
    default String getUserType(User user) {
        return user != null && user.getUserType() != null ? user.getUserType().name() : null;
    }

    /**
     * Map Page of UserSession to PaginationResponse of AdminSessionResponse
     */
    default PaginationResponse<AdminSessionResponse> toPaginationResponse(Page<UserSession> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toAdminResponseList);
    }
}
