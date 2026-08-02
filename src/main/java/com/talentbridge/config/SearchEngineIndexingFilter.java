package com.talentbridge.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class SearchEngineIndexingFilter extends OncePerRequestFilter {

    private static final Set<String> INDEXABLE_PATHS = Set.of(
            "/",
            "/blog",
            "/buscar",
            "/publicar-servicio",
            "/politicas/privacidad",
            "/politicas/condiciones",
            "/robots.txt",
            "/sitemap.xml"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean staticAsset = path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/");
        boolean publicService = path.matches("^/servicios/\\d+$");
        boolean filteredSearch = "/buscar".equals(path)
                && request.getQueryString() != null
                && !request.getQueryString().isBlank();

        if (!staticAsset && !publicService && (!INDEXABLE_PATHS.contains(path) || filteredSearch)) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow, nosnippet");
        }
        filterChain.doFilter(request, response);
    }
}
