package com.emenu.features.notification.mapper;

import com.emenu.features.notification.dto.request.MessageTemplateRequest;
import com.emenu.features.notification.dto.response.MessageTemplateResponse;
import com.emenu.features.notification.models.MessageTemplate;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageTemplateMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variables", ignore = true) // Set in @AfterMapping
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract MessageTemplate toEntity(MessageTemplateRequest request);

    @Mapping(source = "variables", target = "variables")
    public abstract MessageTemplateResponse toResponse(MessageTemplate messageTemplate);

    public abstract List<MessageTemplateResponse> toResponseList(List<MessageTemplate> messageTemplates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateName", ignore = true) // Don't allow changing template name
    @Mapping(target = "variables", ignore = true) // Set in @AfterMapping
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(MessageTemplateRequest request, @MappingTarget MessageTemplate messageTemplate);

    @AfterMapping
    protected void setVariables(MessageTemplateRequest request, @MappingTarget MessageTemplate messageTemplate) {
        if (request.getVariables() != null) {
            messageTemplate.setVariables(String.join(",", request.getVariables()));
        }
    }

    @AfterMapping
    protected void updateVariables(MessageTemplateRequest request, @MappingTarget MessageTemplate messageTemplate) {
        if (request.getVariables() != null) {
            messageTemplate.setVariables(String.join(",", request.getVariables()));
        }
    }

    @AfterMapping
    protected void parseVariables(@MappingTarget MessageTemplateResponse response, MessageTemplate messageTemplate) {
        if (messageTemplate.getVariables() != null && !messageTemplate.getVariables().isEmpty()) {
            response.setVariables(List.of(messageTemplate.getVariables().split(",")));
        }
    }

    // Universal pagination mapper usage
    public PaginationResponse<MessageTemplateResponse> toPaginationResponse(Page<MessageTemplate> templatePage) {
        return paginationMapper.toPaginationResponse(templatePage, this::toResponseList);
    }
}