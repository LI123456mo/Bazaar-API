package com.conel.market.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // EMAIL
    ERR_SENDING_ACTIVATION_EMAIL("EMAIL_001", "Failed to send activation email. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_CANNOT_BE_EMPTY("EMAIL_002", "Email cannot be empty", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("EMAIL_003", "Email already exists", HttpStatus.CONFLICT),

    // USER
    USER_NOT_FOUND("USER_001", "User not found", HttpStatus.NOT_FOUND),
    INVALID_CURRENT_PASSWORD("USER_002", "Current password is invalid", HttpStatus.BAD_REQUEST),
    CHANGE_PASSWORD_MISMATCH("USER_003", "New password confirmation does not match", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_DEACTIVATED("USER_004", "Account is already deactivated", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_ACTIVATED("USER_005", "Account is already activated", HttpStatus.BAD_REQUEST),

    ROLE_NOT_FOUND("ROLE_001", "Role not found", HttpStatus.NOT_FOUND),

    PHONE_ALREADY_EXISTS("PHONE_001", "Phone number already exists", HttpStatus.CONFLICT),

    // PRODUCTS
    PRODUCT_NOT_FOUND("PRODUCT_001", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_ARCHIVED("PRODUCT_002", "Product is archived and cannot be ordered", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("PRODUCT_003", "Insufficient stock for this product", HttpStatus.BAD_REQUEST),
    PRODUCT_OWNER_MISMATCH("PRODUCT_004", "You do not have permission to modify this product", HttpStatus.FORBIDDEN),
    PRODUCT_OUT_OF_STOCK("PRODUCT_005", "Product is out of stock or no longer available", HttpStatus.CONFLICT),
    INVALID_STOCK_QUANTITY("PRODUCT_006", "Stock quantity must be a positive number", HttpStatus.BAD_REQUEST),

    // ORDERS
    ORDER_NOT_FOUND("ORDER_001", "Order not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ORDER_ACCESS("ORDER_002", "You do not have permission to view this order", HttpStatus.FORBIDDEN),
    EMPTY_ORDER("ORDER_003", "Order requires at least one item", HttpStatus.BAD_REQUEST), // CHANGED: was ORDER_002 + FORBIDDEN

    // CART
    CART_ITEM_NOT_FOUND("CART_001", "Cart item not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND("CART_002", "Cart not found", HttpStatus.NOT_FOUND),

    // VENDOR
    VENDOR_NOT_FOUND("VENDOR_001", "Vendor not found", HttpStatus.NOT_FOUND),
    INVALID_VENDOR_STATUS("VENDOR_002", "Invalid vendor status for this operation", HttpStatus.BAD_REQUEST),

    // AUTHENTICATION
    UNAUTHORIZED_ACCESS("AUTH_001", "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_TOKEN("AUTH_002", "Invalid or expired verification token", HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_ALREADY_USED("AUTH_003", "This verification token has already been used", HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_EXPIRED("AUTH_004", "Verification token has expired", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("AUTH_005", "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED("AUTH_006", "Please verify your email before logging in", HttpStatus.FORBIDDEN),
    PASSWORD_RESET_TOKEN_INVALID("AUTH_007", "Invalid reset token", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_EXPIRED("AUTH_008", "Password reset token has expired", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_USED("AUTH_009", "This password reset token has already been used", HttpStatus.BAD_REQUEST),

    // CATEGORIES
    CATEGORY_NOT_FOUND("CATEGORY_001", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS_FOR_USER("CATEGORY_002", "Category already exists", HttpStatus.CONFLICT),

    // PASSWORDS
    INVALID_PASSWORD("PWD_001", "Invalid password", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("PWD_002", "Please provide the password", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("PWD_003", "The passwords do not match", HttpStatus.BAD_REQUEST),

    // PAYMENT
    INVALID_PAYMENT_STATE_TRANSITION("PAYMENT_001", "Invalid payment state transition", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT("PAYMENT_002", "A payment with this idempotency key already exists", HttpStatus.CONFLICT),
    INVALID_WEBHOOK_SIGNATURE("PAYMENT_003", "Webhook signature verification failed", HttpStatus.BAD_REQUEST),
    PAYMENT_CONCURRENCY_CONFLICT("PAYMENT_004", "Payment was modified concurrently, please retry", HttpStatus.CONFLICT),
    PAYMENT_GATEWAY_UNAVAILABLE("PAYMENT_005", "Payment gateway is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_INITIATION_FAILED("PAYMENT_006", "Failed to initiate payment", HttpStatus.BAD_REQUEST),
    PAYMENT_REFUND_FAILED("PAYMENT_007", "Failed to process refund", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_TIMEOUT("PAYMENT_008", "Payment timed out", HttpStatus.REQUEST_TIMEOUT),
    PAYMENT_NOT_FOUND("PAYMENT_009", "Payment not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_PAID("PAYMENT_010", "This order has already been paid", HttpStatus.CONFLICT),

    // GENERIC
    INTERNAL_SERVER_ERROR("ERR_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}