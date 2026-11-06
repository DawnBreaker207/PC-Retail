package com.dawn.common.message;

import java.text.MessageFormat;


public class Message {
    private Message() {
    }

    public static String format(String template, Object... args) {
        return MessageFormat.format(template, args);
    }

    // Auth
    public static final class Auth {
        private Auth() {
        }

        public static final String INVALID_PASSWORD = "Invalid password";
        public static final String INVALID_TOKEN = "Invalid token";
        public static final String TOKEN_EXPIRED = "Token has expired";
        public static final String TOKEN_INVALID_OR_EXPIRED = "Token is invalid or expired";
        public static final String TOKEN_ALREADY_USED = "Token has already been used";
        public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
        public static final String UNAUTHORIZED = "Authentication required";
        public static final String FORBIDDEN = "You do not have permission to perform this action";

        // Login
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String ACCOUNT_NOT_VERIFIED = "Account email has not been verified";
        public static final String ACCOUNT_NOT_ACTIVE = "Account is not active";
        public static final String REQUIRED_LOGIN = "Required login";

        // OTP / Resend
        public static final String OTP_RESEND_COOLDOWN = "Please wait 60 seconds before requesting a new code";
        public static final String OTP_RATE_LIMITED = "Too many OTP requests, please try again later";
        public static final String CAPTCHA_INVALID = "Invalid or expired captcha, please try again";

        // Password reset
        public static final String RESET_TOKEN_INVALID = "Invalid or expired reset token";

        // Refresh token
        public static final String REFRESH_TOKEN_INVALID = "Invalid refresh token";
        public static final String REFRESH_TOKEN_REUSE_DETECTED = "Refresh token reuse detected";
        public static final String REFRESH_TOKEN_EXPIRED = "Refresh token has expired, please log in again";
    }

    // Common
    public static final class Common {
        private Common() {
        }

        public static final String VALIDATION_FAILED = "Request validation failed";
        public static final String ENDPOINT_NOT_FOUND = "API endpoint not found";
        public static final String INTERNAL_ERROR = "An unexpected error occurred";
        public static final String IMAGE_UPLOAD_FAILED = "Image upload failed";
        public static final String EMAIL_SEND_FAILED = "Failed to send email, please contact support";
        public static final String METHOD_NOT_IMPLEMENTED = "This feature is not yet implemented";
        public static final String SHA256_NOT_AVAILABLE = "SHA-256 not available";
        public static final String NULL_PARAM_FOR_HMAC = "Key and data must not be null for HmacSHA512";
    }

    // User
    public static final class User {
        private User() {
        }

        public static final String NOT_FOUND = "User not found: {0}";
        public static final String EMAIL_NOT_FOUND = "No account found with this email";
        public static final String EMAIL_ALREADY_USED = "Email is already in use";
        public static final String EMAIL_INVALID_FORMAT = "Invalid email format";
        public static final String EMAIL_NOT_EMPTY = "Email cannot be empty";
        public static final String USERNAME_EXISTED = "Username already exists";
        public static final String USERNAME_NOT_FOUND = "Username not found: {0}";
        public static final String PASSWORD_NOT_MATCH = "Passwords do not match";
        public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters";
        public static final String USER_INACTIVE = "Account is locked or not yet activated";
        public static final String USER_ALREADY_VERIFICATION = "Account has already been verified";
        public static final String CANNOT_UPDATE_SELF = "You cannot update your own account this way";
        public static final String CANNOT_CHANGE_OWN_ROLE = "You cannot change your own role";
        public static final String CANNOT_ASSIGN_ADMIN_ROLE = "Admin role cannot be assigned via this API";
        // Address
        public static final String ADDRESS_NOT_FOUND = "Address not found: {0}";
    }

    // Catalog
    public static final class Catalog {
        private Catalog() {
        }

        // SKU
        public static final String SKU_NOT_FOUND = "SKU not found: {0}";
        public static final String SKU_CODE_ALREADY_EXISTS = "SKU code already exists";
        public static final String SKU_CATEGORY_MISMATCH = "Cannot compare SKUs from different categories";

        // Brand
        public static final String BRAND_NOT_FOUND = "Brand not found: {0}";
        public static final String BRAND_ALREADY_EXISTS = "Brand already exists";
        public static final String BRAND_HAS_SKU = "Cannot delete brand with existing SKUs";

        // Category
        public static final String CATEGORY_NOT_FOUND = "Category not found: {0}";
        public static final String CATEGORY_SLUG_EXISTS = "Slug already existed";
        public static final String CATEGORY_HAS_CHILDREN = "Cannot delete category with subcategories";
        public static final String CATEGORY_HAS_SKU = "Cannot delete category with existing SKUs";
        public static final String CATEGORY_PARENT_NOT_FOUND = "Parent category not found: {0}";
    }

    // Inventory
    public static final class Inventory {
        private Inventory() {
        }

        public static final String NOT_FOUND_FOR_SKU = "Inventory not found for SKU: {0}";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock for SKU: {0}";
        public static final String INVALID_QUANTITY = "Quantity must be positive for SKU: {0}";
    }

    // Cart
    public static final class Cart {
        private Cart() {
        }

        public static final String ITEM_NOT_FOUND = "Cart item not found: sku={0}";
        public static final String INSUFFICIENT_STOCK = "SKU #{0} only has {1} item(s) available but in cart have {2} item(s)";
        public static final String INVALID_QUANTITY = "Quantity must be positive";
    }

    // Order
    public static final class Order {
        private Order() {
        }

        // Order
        public static final String NOT_FOUND = "Order not found: {0}";
        public static final String CART_EMPTY = "Cart is empty";
        public static final String SHIPPING_ADDRESS_REQUIRED = "Shipping address is required for DELIVERY";
        public static final String INSUFFICIENT_STOCK_SKUS = "Not enough stock for SKU: {0}";
        public static final String NO_WAREHOUSE_CAN_FULFILL = "No warehouse has sufficient stock to fulfill this order";
        public static final String EXPIRED = "Order expired, please checkout again";
        public static final String INVALID_TRANSITION = "Invalid transition from {0} -> {1}";
        // Guest risk-gate (docs 13.5)
        public static final String PHONE_NOT_VERIFIED = "Phone number is not verified, please verify OTP first";
        public static final String ORDER_OVER_THRESHOLD = "Guest order over the limit must pay online, please login";
        public static final String PHONE_ABUSE_BLOCKED = "This phone has too many cancelled orders, please login and pay online";
        // SubOrder
        public static final String SUB_ORDER_NOT_FOUND = "SubOrder not found: {0}";
    }

    // Payment
    public static final class Payment {
        private Payment() {
        }

        public static final String NOT_FOUND = "Payment not found: {0}";
        public static final String INVALID_ORDER_AMOUNT = "Invalid order amount for payment";
        public static final String GATEWAY_NOT_FOUND = "Payment gateway not found for: {0}";
        public static final String WEBHOOK_NOT_SUPPORTED = "Webhook is not supported for this payment method";
        public static final String CALLBACK_NOT_SUPPORTED = "Callback is not supported for this payment method";
        public static final String VNPAY_WEBHOOK_INVALID_SIGNATURE = "Invalid VNPay webhook signature";
        public static final String VNPAY_CALLBACK_INVALID_SIGNATURE = "Invalid VNPay callback signature";
        public static final String CALLBACK_PROVIDER_UNSUPPORTED = "Unsupported callback provider: {0}";
        public static final String CASH_NO_EXTERNAL_QUERY = "CASH has no external to query";
    }

    // Promotion
    public static final class Promotion {
        private Promotion() {
        }

        // Voucher - validation
        public static final String VOUCHER_NOT_FOUND = "Voucher code not existed: {0}";
        public static final String VOUCHER_EXPIRED = "Voucher was expired or was not affected";
        public static final String VOUCHER_OUT_OF_STOCK = "Voucher was out of turns";
        public static final String VOUCHER_MIN_ORDER_NOT_MET = "Minimum order amount must be at least {0} to use this voucher";

        // Voucher - management
        public static final String VOUCHER_CODE_EXISTS = "Voucher code already existed";
        public static final String VOUCHER_DATE_INVALID = "validFrom must be before validTo";
        public static final String VOUCHER_ALREADY_USED = "User has used voucher {0} before";

        // Flash Sale
        public static final String FLASH_SALE_VOUCHER_STACK = "Cannot apply voucher for product in Flash Sale";

        // Redis
        public static final String PROMOTION_OUT_OF_STOCK = "Promotion out of stock for keys: {0}";
    }

    // Engagement
    public static final class Engagement {
        private Engagement() {
        }

        public static final String RATING_OUT_OF_RANGE = "Rating must be 1-5";
        public static final String REVIEW_ALREADY_EXISTS = "Review already exists for this item";
        public static final String REVIEW_ONLY_AFTER_DELIVERED = "Review is allowed only after delivery";
    }
}
