package com.dawn.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHierarchyTest {

    @Test
    @DisplayName("CUSTOMER_ONLY: cả 3 role vào được (ADMIN > STAFF > CUSTOMER)")
    void customerEndpoints_allRoles() {
        assertThat(AuthorizationConstants.CUSTOMER_ONLY).contains("CUSTOMER", "STAFF", "ADMIN");
    }

    @Test
    @DisplayName("STAFF_ONLY: STAFF + ADMIN, CUSTOMER bị chặn")
    void staffEndpoints_noCustomer() {
        assertThat(AuthorizationConstants.STAFF_ONLY).contains("STAFF", "ADMIN");
        assertThat(AuthorizationConstants.STAFF_ONLY).doesNotContain("CUSTOMER");
    }
}
