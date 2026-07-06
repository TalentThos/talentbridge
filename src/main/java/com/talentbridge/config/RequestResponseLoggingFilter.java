package com.talentbridge.config;

import com.talentbridge.model.AuditoriaHttp;
import com.talentbridge.repository.AuditoriaHttpRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile(
            "(?i)(password|pass|pwd|token|secret|api[_-]?key|authorization)=([^&]*)"
    );

    private final AuditoriaHttpRepository auditoriaHttpRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String requestPath = buildRequestPath(request);
        String method = request.getMethod();
        Exception error = null;

        log.info("REQUEST method={} path={} ip={} user={} contentType={} multipart={}",
                method,
                requestPath,
                getClientIp(request),
                getUser(request),
                valueOrDash(request.getContentType()),
                isMultipart(request));

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            error = ex;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info("RESPONSE method={} path={} status={} durationMs={}",
                    method,
                    requestPath,
                    response.getStatus(),
                    durationMs);
            guardarAuditoria(request, response, durationMs, error);
        }
    }

    private String buildRequestPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return path;
        }
        return path + "?" + maskSensitiveParams(query);
    }

    private String maskSensitiveParams(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return SENSITIVE_QUERY_PARAM.matcher(query).replaceAll("$1=***");
    }

    private void guardarAuditoria(HttpServletRequest request,
                                  HttpServletResponse response,
                                  long durationMs,
                                  Exception error) {
        try {
            AuditoriaHttp auditoria = new AuditoriaHttp();
            auditoria.setFecha(LocalDateTime.now());
            auditoria.setMetodo(truncate(request.getMethod(), 10));
            auditoria.setPath(truncate(request.getRequestURI(), 500));
            auditoria.setQueryString(truncate(maskSensitiveParams(request.getQueryString()), 1000));
            auditoria.setIpCliente(truncate(getClientIp(request), 80));
            auditoria.setUsuario(truncate(getCurrentUser(request), 180));
            auditoria.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
            auditoria.setRequestContentType(truncate(request.getContentType(), 200));
            auditoria.setResponseContentType(truncate(response.getContentType(), 200));
            auditoria.setMultipart(isMultipart(request));
            auditoria.setStatus(response.getStatus());
            auditoria.setDuracionMs(durationMs);
            auditoria.setError(error != null ? truncate(error.getClass().getSimpleName(), 300) : null);
            auditoriaHttpRepository.save(auditoria);
        } catch (RuntimeException ex) {
            log.warn("No se pudo guardar auditoria HTTP para {} {}", request.getMethod(), request.getRequestURI(), ex);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    private String getUser(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null ? principal.getName() : "anonymous";
    }

    private String getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            if (name != null && !"anonymousUser".equals(name)) {
                return name;
            }
        }
        return getUser(request);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private String valueOrDash(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
