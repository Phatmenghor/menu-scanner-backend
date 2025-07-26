package com.emenu.enums.subdomain;

import lombok.Getter;

@Getter
public enum DomainType {
    SUBDOMAIN("Subdomain - shop.menu.com"),
    CUSTOM("Custom Domain - shop.com");

    private final String description;

    DomainType(String description) {
        this.description = description;
    }
}
