package com.emenu.features.messaging.mapper;

import com.emenu.features.messaging.dto.request.MessageCreateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.dto.response.MessageSummaryResponse;
import com.emenu.features.messaging.dto.update.MessageUpdateRequest;
import com.emenu.features.messaging.models.Message;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "senderEmail", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "recipientEmail", ignore = true)
    @Mapping(target = "recipientName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Message toEntity(MessageCreateRequest request);

    MessageResponse toResponse(Message message);
    MessageSummaryResponse toSummaryResponse(Message message);
    
    List<MessageResponse> toResponseList(List<Message> messages);
    List<MessageSummaryResponse> toSummaryResponseList(List<Message> messages);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "senderEmail", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "recipientId", ignore = true)
    @Mapping(target = "recipientEmail", ignore = true)
    @Mapping(target = "recipientName", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(MessageUpdateRequest request, @MappingTarget Message message);

    @AfterMapping
    default void setIsRead(@MappingTarget MessageResponse response, Message message) {
        response.setIsRead(message.isRead());
    }

    @AfterMapping
    default void setIsReadSummary(@MappingTarget MessageSummaryResponse response, Message message) {
        response.setIsRead(message.isRead());
    }
}