package com.menghor.ksit.feature.setting.mapper;

import com.menghor.ksit.feature.setting.dto.response.MenuItemDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuAccessDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuDto;
import com.menghor.ksit.feature.setting.models.MenuItem;
import com.menghor.ksit.feature.setting.models.UserMenuAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MapStruct mapper for menu-related entities and DTOs
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Component
public interface MenuMapper {

    /**
     * Convert a MenuItem entity to a MenuItemDto
     * @param menuItem The entity to convert
     * @return The converted DTO
     */
    @Mapping(source = "menuKey", target = "key")
    @Mapping(source = "menuName", target = "name")
    @Mapping(source = "parent", target = "isParent")
    @Mapping(source = "enabled", target = "isEnabled")
    @Mapping(target = "children", ignore = true)
    MenuItemDto toDto(MenuItem menuItem);

    /**
     * Convert UserMenuAccess entity to DTO
     * @param userMenuAccess The entity to convert
     * @return The converted DTO
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "menuItem.menuKey", target = "menuKey")
    UserMenuAccessDto toDto(UserMenuAccess userMenuAccess);

    /**
     * Convert list of UserMenuAccess entities to list of DTOs
     * @param userMenuAccesses List of entities to convert
     * @return List of converted DTOs
     */
    List<UserMenuAccessDto> toUserMenuAccessDtoList(List<UserMenuAccess> userMenuAccesses);

    /**
     * Create a UserMenuDto from parent menu items and their children
     * Helper method implementation to build the hierarchical structure
     */
    default UserMenuDto toUserMenuDto(List<MenuItem> parentMenus, Map<String, List<MenuItem>> childrenMap) {
        UserMenuDto userMenu = new UserMenuDto();
        List<MenuItemDto> menuItems = new ArrayList<>();

        for (MenuItem parentMenu : parentMenus) {
            MenuItemDto parentDto = toDto(parentMenu);
            List<MenuItemDto> childrenDtos = new ArrayList<>();

            // Get children for this parent
            List<MenuItem> children = childrenMap.getOrDefault(parentMenu.getMenuKey(), Collections.emptyList());

            // Map children to DTOs and add to parent
            for (MenuItem child : children) {
                childrenDtos.add(toDto(child));
            }

            // Set children list
            parentDto.setChildren(childrenDtos);

            // Only add parent if it has children or is a leaf node
            if (!parentDto.isParent() || !childrenDtos.isEmpty()) {
                menuItems.add(parentDto);
            }
        }

        // Sort menu items by display order
        menuItems.sort(Comparator.comparing(MenuItemDto::getDisplayOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Sort children for each parent by display order
        for (MenuItemDto parent : menuItems) {
            if (parent.getChildren() != null && !parent.getChildren().isEmpty()) {
                parent.getChildren().sort(Comparator.comparing(MenuItemDto::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())));
            }
        }

        userMenu.setMenuItems(menuItems);
        return userMenu;
    }

    /**
     * Convert a list of MenuItem entities to a hierarchical structure of DTOs
     * Helper method implementation to build the hierarchy
     */
    default List<MenuItemDto> toDtoHierarchy(List<MenuItem> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Convert to DTOs and organize in hierarchy
        Map<String, MenuItemDto> menuMap = new HashMap<>();
        List<MenuItemDto> rootMenus = new ArrayList<>();

        // First pass: Create all DTOs
        for (MenuItem menu : menuItems) {
            MenuItemDto dto = toDto(menu);
            dto.setChildren(new ArrayList<>());
            menuMap.put(menu.getMenuKey(), dto);

            // Add root menus to the result list
            if (menu.getParentKey() == null || menu.getParentKey().isEmpty()) {
                rootMenus.add(dto);
            }
        }

        // Second pass: Build hierarchy
        for (MenuItem menu : menuItems) {
            if (menu.getParentKey() != null && !menu.getParentKey().isEmpty()) {
                MenuItemDto parentDto = menuMap.get(menu.getParentKey());
                if (parentDto != null) {
                    MenuItemDto childDto = menuMap.get(menu.getMenuKey());
                    if (childDto != null && parentDto.getChildren() != null) {
                        parentDto.getChildren().add(childDto);
                    }
                }
            }
        }

        // Sort by display order
        rootMenus.sort(Comparator.comparing(MenuItemDto::getDisplayOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Sort children by display order
        for (MenuItemDto parent : rootMenus) {
            if (parent.getChildren() != null) {
                parent.getChildren().sort(Comparator.comparing(MenuItemDto::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())));
            }
        }

        return rootMenus;
    }
}