package com.conel.market.security;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

import static com.conel.market.security.Permission.*;

public enum RoleEnum {
    CUSTOMER(new HashSet<>() {{
        add(PRODUCT_READ);
        add(ORDER_CREATE);
        add(ORDER_READ);
        add(USER_READ);
        add(USER_UPDATE);
    }}),

    VENDOR(new HashSet<>(){{
        add(PRODUCT_CREATE);
        add(PRODUCT_READ);
        add(PRODUCT_UPDATE);
        add(ORDER_READ);
        add(PRODUCT_DELETE);
        add(USER_READ);
        add(USER_UPDATE);
    }}),
    ADMIN(new HashSet<>() {{
        add(PRODUCT_READ);
        add(PRODUCT_DELETE);
        add(ORDER_READ_ALL);
        add(USER_MANAGE);
        add(CATEGORY_CREATE);
        add(CATEGORY_UPDATE);
        add(CATEGORY_DELETE);
        add(VENDOR_APPROVE);
        add(VENDOR_SUSPEND);
        add(ADMIN_ACCESS);
        add(VENDOR_READ);
    }}),
    SUPER_ADMIN(new HashSet<>(){{
        for(Permission p:Permission.values()){
            add(p);
        }
    }});

    @Getter
    private final Set<Permission> permissions;

    RoleEnum(Set<Permission> permissions){
        this.permissions=permissions;
    }
}
