package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.response.CommunicationHistoryResponse;
import com.emenu.features.notification.models.CommunicationHistory;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CommunicationHistoryMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    public abstract CommunicationHistoryResponse toResponse(CommunicationHistory communicationHistory);

    public abstract List<CommunicationHistoryResponse> toResponseList(List<CommunicationHistory> communicationHistories);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget CommunicationHistoryResponse response, CommunicationHistory history) {
        // Set participant names (simplified - would fetch from repositories)
        response.setRecipientName(getUserName(history.getRecipientId()));
        response.setSenderName(getUserName(history.getSenderId()));
        response.setBusinessName(getBusinessName(history.getBusinessId()));
    }

    // These methods would typically fetch from repositories
    private String getUserName(java.util.UUID userId) {
        return userId != null ? "User " + userId.toString().substring(0, 8) : null;
    }

    private String getBusinessName(java.util.UUID businessId) {
        return businessId != null ? "Business " + businessId.toString().substring(0, 8) : null;
    }

    // Universal pagination mapper usage
    public PaginationResponse<CommunicationHistoryResponse> toPaginationResponse(Page<CommunicationHistory> historyPage) {
        return paginationMapper.toPaginationResponse(historyPage, this::toResponseList);
    }
}