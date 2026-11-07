package com.dawn.common.security;

public final class AuthorizationConstants {
    private AuthorizationConstants() {}

    public static final String ADMIN_ONLY = "hasRole('ADMIN')";
    // ADMIN > STAFF > SHIPPER > CUSTOMER
    public static final String STAFF_ONLY = "hasAnyRole('STAFF', 'ADMIN')";
    public static final String SHIPPER_ONLY = "hasAnyRole('SHIPPER', 'STAFF', 'ADMIN')";
    public static final String CUSTOMER_ONLY = "hasAnyRole('CUSTOMER', 'USER', 'STAFF', 'ADMIN')";
    public static final String STAFF_OR_ADMIN = "hasAnyRole('STAFF', 'ADMIN')";
}
