
package com.conel.market.security;

import lombok.Getter;

public enum Permission {
    // Product permissions
    PRODUCT_CREATE("product:create"),
    PRODUCT_READ("product:read"),
    PRODUCT_UPDATE("product:update"),
    PRODUCT_DELETE("product:delete"),

    // Order permissions
    ORDER_CREATE("order:create"),
    ORDER_READ("order:read"),
    ORDER_UPDATE("order:update"),
    ORDER_READ_ALL("order:read_all"),

    // User management
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    USER_MANAGE("user:manage"),

    // Vendor management
    VENDOR_APPROVE("vendor:approve"),
    VENDOR_SUSPEND("vendor:suspend"),
    VENDOR_MANAGE("vendor:manage"),

    // Category management
    CATEGORY_CREATE("category:create"),
    CATEGORY_UPDATE("category:update"),
    CATEGORY_DELETE("category:delete"),

    // Admin
    ADMIN_ACCESS("admin:access"),
    SUPER_ADMIN_ACCESS("super_admin:access");

    @Getter
    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}