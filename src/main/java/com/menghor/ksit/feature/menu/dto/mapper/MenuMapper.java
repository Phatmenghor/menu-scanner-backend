package com.menghor.ksit.feature.menu.dto.mapper;

import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import com.menghor.ksit.feature.menu.models.UserMenuPermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {

    @Mapping(target = "parentId", source = "parent.id")
    MenuItemResponseDto toMenuItemResponseDto(MenuItemEntity entity);

    List<MenuItemResponseDto> toMenuItemResponseDtoList(List<MenuItemEntity> entities);

    @Mapping(target = "id", source = "menuItem.id")
    @Mapping(target = "code", source = "menuItem.code")
    @Mapping(target = "title", expression = "java(entity.getCustomTitle() != null ? entity.getCustomTitle() : entity.getMenuItem().getTitle())")
    @Mapping(target = "route", source = "menuItem.route")
    @Mapping(target = "icon", expression = "java(entity.getCustomIcon() != null ? entity.getCustomIcon() : entity.getMenuItem().getIcon())")
    @Mapping(target = "isParent", source = "menuItem.isParent")
    @Mapping(target = "isCustomized", constant = "true")
    UserMenuResponseDto toUserMenuResponseDto(UserMenuPermissionEntity entity);

    @Mapping(target = "isCustomized", constant = "false")
    @Mapping(target = "canView", constant = "true")
    UserMenuResponseDto toUserMenuResponseDtoFromMenuItem(MenuItemEntity entity);
}