package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.request.MessageThreadCreateRequest;
import com.emenu.features.notification.dto.response.MessageThreadResponse;
import com.emenu.features.notification.models.MessageThread;
import com.emenu.shared.domain.UUIDConversionHelper;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageThreadMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Autowired
    protected MessageMapper messageMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "isClosed", constant = "false")
    @Mapping(target = "unreadCount", constant = "0")
    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "participantIds", ignore = true) // Handle in @AfterMapping
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "closedBy", ignore = true)
    public abstract MessageThread toEntity(MessageThreadCreateRequest request);

    @Mapping(source = "messages", target = "messages")
    @Mapping(source = "participantIds", target = "participantIds", ignore = true) // Handle in @AfterMapping
    public abstract MessageThreadResponse toResponse(MessageThread messageThread);

    public abstract List<MessageThreadResponse> toResponseList(List<MessageThread> messageThreads);

    @AfterMapping
    protected void setParticipantIds(MessageThreadCreateRequest request, @MappingTarget MessageThread messageThread) {
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            messageThread.setParticipantIds(UUIDConversionHelper.uuidListToString(request.getParticipantIds()));
        }
    }

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget MessageThreadResponse response, MessageThread messageThread) {
        // Set priority display
        response.setPriorityDisplay(getPriorityDisplay(messageThread.getPriority()));

        // Set participant IDs
        response.setParticipantIds(UUIDConversionHelper.stringToUuidList(messageThread.getParticipantIds()));

        // Set message count
        if (messageThread.getMessages() != null) {
            response.setMessageCount(messageThread.getMessages().size());

            // Set last message
            messageThread.getMessages().stream()
                    .max((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                    .ifPresent(lastMessage -> response.setLastMessage(messageMapper.toResponse(lastMessage)));
        } else {
            response.setMessageCount(0);
        }

        // Set participant names (would need user repository lookups in real implementation)
        // This is simplified - in real implementation you'd fetch user details
        response.setBusinessName(getBusinessName(messageThread.getBusinessId()));
        response.setCustomerName(getCustomerName(messageThread.getCustomerId()));
        response.setPlatformUserName(getPlatformUserName(messageThread.getPlatformUserId()));
    }

    private String getPriorityDisplay(Integer priority) {
        if (priority == null) return "Normal";
        return switch (priority) {
            case 1 -> "Low";
            case 2 -> "Normal";
            case 3 -> "High";
            case 4 -> "Critical";
            default -> "Normal";
        };
    }

    // These methods would typically fetch from repositories
    private String getBusinessName(java.util.UUID businessId) {
        return businessId != null ? "Business " + businessId.toString().substring(0, 8) : null;
    }

    private String getCustomerName(java.util.UUID customerId) {
        return customerId != null ? "Customer " + customerId.toString().substring(0, 8) : null;
    }

    private String getPlatformUserName(java.util.UUID platformUserId) {
        return platformUserId != null ? "Platform User " + platformUserId.toString().substring(0, 8) : null;
    }

    // Universal pagination mapper usage
    public PaginationResponse<MessageThreadResponse> toPaginationResponse(Page<MessageThread> messageThreadPage) {
        return paginationMapper.toPaginationResponse(messageThreadPage, this::toResponseList);
    }
}