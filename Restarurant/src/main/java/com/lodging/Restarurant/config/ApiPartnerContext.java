package com.lodging.Restarurant.config;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Request-scoped partner identity set by {@link ApiKeyFilter} after API key validation.
 */
public final class ApiPartnerContext {

    public static final String PARTNER_NAME_ATTR = "apiPartnerName";
    public static final String CUSTOMER_ID_ATTR  = "apiCustomerId";

    private ApiPartnerContext() {}

    public static String getPartnerName(HttpServletRequest request) {
        Object value = request.getAttribute(PARTNER_NAME_ATTR);
        return value instanceof String name ? name : null;
    }

    public static Long getCustomerId(HttpServletRequest request) {
        Object value = request.getAttribute(CUSTOMER_ID_ATTR);
        return value instanceof Long id ? id : null;
    }
}
