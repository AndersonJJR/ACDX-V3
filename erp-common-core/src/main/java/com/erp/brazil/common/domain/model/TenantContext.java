package com.erp.brazil.common.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Thread-local storage for the current tenant context.
 * Provides access to the current tenant ID throughout the request lifecycle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> COMPANY_ID = new ThreadLocal<>();

    /**
     * Sets the current tenant ID.
     *
     * @param tenantId the tenant identifier
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * Gets the current tenant ID.
     *
     * @return the tenant identifier, or null if not set
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Sets the current user ID.
     *
     * @param userId the user identifier
     */
    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    /**
     * Gets the current user ID.
     *
     * @return the user identifier, or null if not set
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * Sets the current company ID.
     *
     * @param companyId the company identifier
     */
    public static void setCompanyId(String companyId) {
        COMPANY_ID.set(companyId);
    }

    /**
     * Gets the current company ID.
     *
     * @return the company identifier, or null if not set
     */
    public static String getCompanyId() {
        return COMPANY_ID.get();
    }

    /**
     * Clears all context values.
     */
    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        COMPANY_ID.remove();
    }

    /**
     * Checks if a tenant context is set.
     *
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean hasTenant() {
        return TENANT_ID.get() != null;
    }
}