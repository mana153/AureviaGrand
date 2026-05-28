package com.lodging.Restarurant.config;

import com.lodging.Restarurant.model.ApiKey;
import com.lodging.Restarurant.model.ApiUsageLog;
import com.lodging.Restarurant.repository.ApiKeyRepository;
import com.lodging.Restarurant.repository.ApiUsageLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiUsageLogRepository apiUsageLogRepository;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String apiKeyHeader = request.getHeader("X-API-KEY");

        Optional<ApiKey> keyOpt = (apiKeyHeader != null && !apiKeyHeader.isBlank())
                ? apiKeyRepository.findByKeyValueAndActiveTrue(apiKeyHeader.trim())
                : Optional.empty();

        if (keyOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
            return;
        }

        ApiKey key = keyOpt.get();
        key.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(key);
        request.setAttribute(ApiPartnerContext.PARTNER_NAME_ATTR, key.getPartnerName());
        if (key.getCustomerId() != null) {
            request.setAttribute(ApiPartnerContext.CUSTOMER_ID_ATTR, key.getCustomerId());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            apiUsageLogRepository.save(ApiUsageLog.builder()
                    .partnerName(key.getPartnerName())
                    .endpoint(path)
                    .method(request.getMethod())
                    .statusCode(response.getStatus())
                    .calledAt(LocalDateTime.now())
                    .build());
        }
    }
}
