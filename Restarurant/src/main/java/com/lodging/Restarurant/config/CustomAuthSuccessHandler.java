package com.lodging.Restarurant.config;

import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        switch (role) {
            case "ROLE_ADMIN"    -> response.sendRedirect("/admin/dashboard");
            case "ROLE_STAFF"    -> response.sendRedirect("/staff/dashboard");
            case "ROLE_CUSTOMER" -> response.sendRedirect("/customer/dashboard");
            default              -> response.sendRedirect("/");
        }
    }
}