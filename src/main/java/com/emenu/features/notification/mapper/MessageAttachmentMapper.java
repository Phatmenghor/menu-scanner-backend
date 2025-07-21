package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.response.MessageAttachmentResponse;
import com.emenu.features.notification.models.MessageAttachment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageAttachmentMapper {

    public abstract MessageAttachmentResponse toResponse(MessageAttachment messageAttachment);

    public abstract List<MessageAttachmentResponse> toResponseList(List<MessageAttachment> messageAttachments);

    @AfterMapping
    protected void setDownloadUrl(@MappingTarget MessageAttachmentResponse response, MessageAttachment attachment) {
        // Set download URL (would be generated based on file storage system)
        response.setDownloadUrl("/api/v1/messaging/attachments/" + attachment.getId() + "/download");
    }
}
