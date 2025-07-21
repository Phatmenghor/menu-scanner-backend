package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.dto.response.NotificationResponse;
import com.emenu.features.notification.models.Notification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NotificationMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true) // Set by service
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "isSent", constant = "false")
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "deliveryStatus", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Notification toEntity(NotificationCreateRequest request);

    public abstract NotificationResponse toResponse(Notification notification);

    public abstract List<NotificationResponse> toResponseList(List<Notification> notifications);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget NotificationResponse response, Notification notification) {
        // Set participant names (simplified - would fetch from repositories)
        response.setRecipientName(getUserName(notification.getRecipientId()));
        response.setSenderName(getUserName(notification.getSenderId()));
        response.setBusinessName(getBusinessName(notification.getBusinessId()));
    }

    // These methods would typically fetch from repositories
    private String getUserName(java.util.UUID userId) {
        return userId != null ? "User " + userId.toString().substring(0, 8) : null;
    }

    private String getBusinessName(java.util.UUID businessId) {
        return businessId != null ? "Business " + businessId.toString().substring(0, 8) : null;
    }

    // Universal pagination mapper usage
    public PaginationResponse<NotificationResponse> toPaginationResponse(Page<Notification> notificationPage) {
        return paginationMapper.toPaginationResponse(notificationPage, this::toResponseList);
    }
}