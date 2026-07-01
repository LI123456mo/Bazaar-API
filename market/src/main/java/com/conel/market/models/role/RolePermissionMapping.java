package com.conel.market.models.role;

import java.util.Set;

import static com.conel.market.models.role.Permission.*;

public class RolePermissionMapping {
    public static Set<Permission> getPermissionForRole(Role role){
        return switch (role){
            case CUSTOMER -> Set.of(
                    PRODUCT_VIEW,
                    ORDER_CREATE,
                    ORDER_VIEW_OWN
            );
            case VENDOR -> Set.of(
                    PRODUCT_VIEW,
                    PRODUCT_CREATE,
                    PRODUCT_UPDATE,
                    PRODUCT_DELETE,
                    ORDER_VIEW_OWN,
                    ORDER_UPDATE_STATUS
            );
            case ADMIN -> Set.of(
                    PRODUCT_VIEW,
                    PRODUCT_CREATE,
                    PRODUCT_UPDATE,
                    PRODUCT_DELETE,
                    ORDER_VIEW_ALL,
                    ORDER_UPDATE_STATUS,
                    USER_VIEW_ALL
            );
            case SUPER_ADMIN -> Set.of(Permission.values());
        };
    }
}
