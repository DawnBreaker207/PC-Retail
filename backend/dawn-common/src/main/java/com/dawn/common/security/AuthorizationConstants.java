package com.dawn.common.security;

public final class AuthorizationConstants {
    private AuthorizationConstants() {}

    public static final String ADMIN_ONLY = "hasRole('ADMIN')";
    public static final String STAFF_ONLY = "hasRole('STAFF')";
    public static final String CUSTOMER_ONLY = "hasRole('CUSTOMER')";
    public static final String STAFF_OR_ADMIN = "hasAnyRole('STAFF', 'ADMIN')";
}
