package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.request.MessageCreateRequest;
import com.emenu.features.notification.dto.response.MessageResponse;
import com.emenu.features.notification.models.Message;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Autowired
    protected MessageAttachmentMapper attachmentMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "messageThread", ignore = true)
    @Mapping(target = "senderId", ignore = true) // Set by service
    @Mapping(target = "senderName", ignore = true) // Set by service
    @Mapping(target = "senderEmail", ignore = true) // Set by service
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "parentMessage", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Message toEntity(MessageCreateRequest request);

    @Mapping(source = "attachments", target = "attachments")
    @Mapping(source = "replies", target = "replies")
    public abstract MessageResponse toResponse(Message message);

    public abstract List<MessageResponse> toResponseList(List<Message> messages);

    // Universal pagination mapper usage
    public PaginationResponse<MessageResponse> toPaginationResponse(Page<Message> messagePage) {
        return paginationMapper.toPaginationResponse(messagePage, this::toResponseList);
    }
}