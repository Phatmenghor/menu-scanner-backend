package com.menghor.ksit.feature.menu.mapper;

import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    // Menu item mappings
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentTitle", source = "parent.title")
    @Mapping(target = "children", ignore = true) // Will be set manually in service
    MenuItemResponseDto toMenuItemResponse(MenuItemEntity entity);

    List<MenuItemResponseDto> toMenuItemResponseList(List<MenuItemEntity> entities);

    // User menu mappings
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentTitle", source = "parent.title")
    @Mapping(target = "canView", constant = "false") // Default value
    @Mapping(target = "isCustomized", constant = "false") // Default value
    @Mapping(target = "children", ignore = true) // Will be set manually in service
    UserMenuResponseDto toUserMenuResponse(MenuItemEntity entity);

    List<UserMenuResponseDto> toUserMenuResponseList(List<MenuItemEntity> entities);
}


