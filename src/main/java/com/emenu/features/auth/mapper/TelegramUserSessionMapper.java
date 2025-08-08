package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.TelegramUserSessionResponse;
import com.emenu.features.notification.models.TelegramUserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TelegramUserSessionMapper {

    @Mapping(target = "displayName", expression = "java(session.getDisplayName())")
    @Mapping(target = "canReceiveNotifications", expression = "java(session.canReceiveNotifications())")
    @Mapping(target = "isLinkedToUser", expression = "java(session.isLinkedToUser())")
    public abstract TelegramUserSessionResponse toResponse(TelegramUserSession session);

    public abstract List<TelegramUserSessionResponse> toResponseList(List<TelegramUserSession> sessions);
}
