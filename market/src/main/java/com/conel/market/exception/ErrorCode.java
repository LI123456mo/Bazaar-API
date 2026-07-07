package com.conel.market.exception;

public enum ErrorCode {
    // Email
    ERR_SENDING_ACTIVATION_EMAIL("EMAIL_001", "Failed to send activation email. Please try again later."),
    EMAIL_CANNOT_BE_EMPTY("EMAIL_002", "Email cannot be empty"),
    EMAIL_ALREADY_EXISTS("EMAIL_003", "Email already exists"),

    // User
    USER_NOT_FOUND("USER_001", "User not found"),
    INVALID_CURRENT_PASSWORD("USER_002", "Current password is invalid"),
    CHANGE_PASSWORD_MISMATCH("USER_003", "New password confirmation does not match"),
    ACCOUNT_ALREADY_DEACTIVATED("USER_004", "Account is already deactivated"),
    ACCOUNT_ALREADY_ACTIVATED("USER_005", "Account is already activated"),

    // Phone
    PHONE_ALREADY_EXISTS("PHONE_001", "Phone number already exists"),

    // Products
    PRODUCT_NOT_FOUND("PRODUCT_001", "Product not found"),
    PRODUCT_ARCHIVED("PRODUCT_002", "Product is archived and cannot be ordered"),
    INSUFFICIENT_STOCK("PRODUCT_003", "Insufficient stock for this product"),
    PRODUCT_OWNER_MISMATCH("PRODUCT_004", "You do not have permission to modify this product"),
    PRODUCT_OUT_OF_STOCK("PRODUCT_OUT_OF_STOCK", "Product is out of stock or no longer available"),

    // Orders
    ORDER_NOT_FOUND("ORDER_001", "Order not found"),
    UNAUTHORIZED_ORDER_ACCESS("ORDER_002", "You do not have permission to view this order"),

    // Vendor
    VENDOR_NOT_FOUND("VENDOR_001", "Vendor not found"),
    INVALID_VENDOR_STATUS("VENDOR_002", "Invalid vendor status for this operation"),

    // Authentication
    UNAUTHORIZED_ACCESS("AUTH_001", "You do not have permission to access this resource"),
    INVALID_VERIFICATION_TOKEN("AUTH_002", "Invalid or expired verification token"),
    VERIFICATION_TOKEN_ALREADY_USED("AUTH_003", "This verification token has already been used"),
    VERIFICATION_TOKEN_EXPIRED("AUTH_004", "Verification token has expired"),
    ACCESS_DENIED("AUTH_004","Invalid owner"),

    // Categories
    CATEGORY_NOT_FOUND("CATEGORY_001", "Category not found"),

    // Generic
    INTERNAL_SERVER_ERROR("ERR_001", "An unexpected error occurred"),
    INVALID_PASSWORD("ERR_001", "Invalid password"),
    PASSWORD_REQUIRED("ERR_001","Please provide the password"),
    PASSWORD_MISMATCH("ERR_001","The passwords does not match");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
