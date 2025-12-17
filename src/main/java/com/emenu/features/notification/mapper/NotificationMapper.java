package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
import com.emenu.features.notification.models.Notification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NotificationMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    public abstract NotificationResponse toResponse(Notification notification);
    
    public abstract List<NotificationResponse> toResponseList(List<Notification> notifications);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "isSystemCopy", constant = "false")
    public abstract Notification toEntity(NotificationRequest request);

    public PaginationResponse<NotificationResponse> toPaginationResponse(Page<Notification> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
