package com.lodging.Restarurant.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            String target = saved.getRedirectUrl();
            if (target != null && !target.contains("/auth/login")) {
                requestCache.removeRequest(request, response);
                redirectStrategy.sendRedirect(request, response, target);
                return;
            }
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        String redirect = switch (role) {
            case "ROLE_ADMIN"    -> "/admin/dashboard";
            case "ROLE_STAFF"    -> "/staff/dashboard";
            case "ROLE_CUSTOMER" -> "/customer/dashboard";
            default              -> "/";
        };
        redirectStrategy.sendRedirect(request, response, redirect);
    }
}
