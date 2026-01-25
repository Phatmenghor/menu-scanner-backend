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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

    @Mapping    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "isSeen", constant = "false")
    Notification toEntity(NotificationRequest request);

    default PaginationResponse<NotificationResponse> toPaginationResponse(Page<Notification> page, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
