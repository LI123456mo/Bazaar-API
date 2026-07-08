package com.conel.market.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //EMAIL
    ERR_SENDING_ACTIVATION_EMAIL("EMAIL_001",
            "Failed to send activation email. Please try again later.",
            HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_CANNOT_BE_EMPTY("EMAIL_002",
            "Email cannot be empty",
            HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("EMAIL_003",
            "Email already exists",
            HttpStatus.CONFLICT),

    // USER
    USER_NOT_FOUND("USER_001",
            "User not found",
            HttpStatus.NOT_FOUND),
    INVALID_CURRENT_PASSWORD("USER_002",
            "Current password is invalid",
            HttpStatus.BAD_REQUEST),
    CHANGE_PASSWORD_MISMATCH("USER_003",
            "New password confirmation does not match",
            HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_DEACTIVATED("USER_004",
            "Account is already deactivated",
            HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_ACTIVATED("USER_005",
            "Account is already activated",
            HttpStatus.BAD_REQUEST),

    // PHONE
    PHONE_ALREADY_EXISTS("PHONE_001",
            "Phone number already exists",
            HttpStatus.CONFLICT),

    //PRODUCTS
    PRODUCT_NOT_FOUND("PRODUCT_001",
            "Product not found",
            HttpStatus.NOT_FOUND),
    PRODUCT_ARCHIVED("PRODUCT_002",
            "Product is archived and cannot be ordered",
            HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("PRODUCT_003",
            "Insufficient stock for this product",
            HttpStatus.BAD_REQUEST),
    PRODUCT_OWNER_MISMATCH("PRODUCT_004",
            "You do not have permission to modify this product",
            HttpStatus.FORBIDDEN),
    PRODUCT_OUT_OF_STOCK("PRODUCT_005",
            "Product is out of stock or no longer available",
            HttpStatus.CONFLICT),

    // ORDERS
    ORDER_NOT_FOUND("ORDER_001",
            "Order not found",
            HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ORDER_ACCESS("ORDER_002",
            "You do not have permission to view this order",
            HttpStatus.FORBIDDEN),

    //  VENDOR
    VENDOR_NOT_FOUND("VENDOR_001",
            "Vendor not found",
            HttpStatus.NOT_FOUND),
    INVALID_VENDOR_STATUS("VENDOR_002",
            "Invalid vendor status for this operation",
            HttpStatus.BAD_REQUEST),

    // AUTHENTICATION
    UNAUTHORIZED_ACCESS("AUTH_001",
            "You do not have permission to access this resource",
            HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_TOKEN("AUTH_002",
            "Invalid or expired verification token",
            HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_ALREADY_USED("AUTH_003",
            "This verification token has already been used",
            HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_EXPIRED("AUTH_004",
            "Verification token has expired",
            HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("AUTH_005",           // ← fixed: was AUTH_004 (duplicate)
            "You do not have permission to perform this action",
            HttpStatus.FORBIDDEN),

    // CATEGORIES
    CATEGORY_NOT_FOUND("CATEGORY_001",
            "Category not found",
            HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS_FOR_USER("CATEGORY_002",
            "Category already exists",
            HttpStatus.CONFLICT),

    //  PASSWORDS
    INVALID_PASSWORD("PWD_001",
            "Invalid password",
            HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("PWD_002",
            "Please provide the password",
            HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("PWD_003",
            "The passwords do not match",
            HttpStatus.BAD_REQUEST),

    // GENERIC
    INTERNAL_SERVER_ERROR("ERR_001",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}