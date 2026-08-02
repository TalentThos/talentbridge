package com.talentbridge.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ControllerAdvice
public class WebMetadataAdvice {

    private static final Pattern GA4_MEASUREMENT_ID = Pattern.compile("^G-[A-Z0-9]{4,20}$");
    private static final String DEFAULT_DESCRIPTION =
            "Encuentra servicios y profesionales en Chile o publica tu talento en TalentBridge.";

    private static final Map<String, PageMetadata> INDEXABLE_PAGES = Map.of(
            "/", new PageMetadata(
                    "TalentBridge | Servicios y profesionales en Chile",
                    "Encuentra oficios, clases, reparaciones, eventos y servicios profesionales cerca de ti en Chile."),
            "/blog", new PageMetadata(
                    "Consejos sobre servicios y oficios en Chile | TalentBridge",
                    "Guías y recomendaciones para contratar servicios, ofrecer tu talento y encontrar profesionales en Chile."),
            "/buscar", new PageMetadata(
                    "Buscar servicios en Chile | TalentBridge",
                    "Busca profesionales, técnicos y personas que ofrecen servicios en Chile por categoría y ubicación."),
            "/publicar-servicio", new PageMetadata(
                    "Publica tu servicio en Chile | TalentBridge",
                    "Publica tu talento, oficio o servicio profesional en TalentBridge y conecta con personas que lo necesitan."),
            "/politicas/privacidad", new PageMetadata(
                    "Política de privacidad | TalentBridge",
                    "Conoce cómo TalentBridge recopila, utiliza y protege tus datos personales y tus preferencias de medición."),
            "/politicas/condiciones", new PageMetadata(
                    "Condiciones de uso | TalentBridge",
                    "Revisa las condiciones que regulan el uso de TalentBridge y la publicación de servicios.")
    );

    private final String publicUrl;
    private final String measurementId;
    private final ObjectMapper objectMapper;

    public WebMetadataAdvice(
            @Value("${app.public-url:https://www.talentbridge.cl}") String publicUrl,
            @Value("${google.analytics.measurement-id:}") String measurementId,
            ObjectMapper objectMapper) {
        this.publicUrl = normalizePublicUrl(publicUrl);
        this.measurementId = normalizeMeasurementId(measurementId);
        this.objectMapper = objectMapper;
    }

    @ModelAttribute
    public void addWebMetadata(HttpServletRequest request, Model model) {
        String path = requestPath(request);
        PageMetadata metadata = INDEXABLE_PAGES.getOrDefault(path,
                new PageMetadata("TalentBridge", DEFAULT_DESCRIPTION));
        boolean publicService = path.matches("^/servicios/\\d+$");
        boolean filteredSearch = "/buscar".equals(path)
                && request.getQueryString() != null
                && !request.getQueryString().isBlank();
        boolean indexable = (INDEXABLE_PAGES.containsKey(path) || publicService) && !filteredSearch;
        String canonicalPath = "/home".equals(path) ? "/" : path;

        model.addAttribute("seoTitle", metadata.title());
        model.addAttribute("seoDescription", metadata.description());
        model.addAttribute("seoRobots", indexable
                ? "index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1"
                : "noindex, nofollow");
        model.addAttribute("seoCanonicalUrl", absoluteUrl(canonicalPath));
        model.addAttribute("seoSocialImageUrl", absoluteUrl("/img/talentbridge-social.png"));
        model.addAttribute("seoStructuredData", structuredData());
        model.addAttribute("googleAnalyticsMeasurementId", measurementId);
        model.addAttribute("googleAnalyticsEnabled", !measurementId.isBlank());
    }

    private String structuredData() {
        Map<String, Object> organization = Map.of(
                "@type", "Organization",
                "@id", absoluteUrl("/#organization"),
                "name", "TalentBridge",
                "url", absoluteUrl("/"),
                "logo", absoluteUrl("/img/logo.png")
        );
        Map<String, Object> searchAction = Map.of(
                "@type", "SearchAction",
                "target", Map.of(
                        "@type", "EntryPoint",
                        "urlTemplate", absoluteUrl("/buscar?q={search_term_string}")),
                "query-input", "required name=search_term_string"
        );
        Map<String, Object> website = Map.of(
                "@type", "WebSite",
                "@id", absoluteUrl("/#website"),
                "url", absoluteUrl("/"),
                "name", "TalentBridge",
                "inLanguage", "es-CL",
                "publisher", Map.of("@id", absoluteUrl("/#organization")),
                "potentialAction", searchAction
        );
        Map<String, Object> document = Map.of(
                "@context", "https://schema.org",
                "@graph", List.of(organization, website)
        );
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String absoluteUrl(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return publicUrl + "/";
        }
        return publicUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private static String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isBlank() ? "/" : uri;
    }

    private static String normalizePublicUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!normalized.matches("^https?://[^\\s/]+(?::\\d+)?(?:/.*)?$")) {
            normalized = "https://www.talentbridge.cl";
        }
        return normalized.replaceAll("/+$", "");
    }

    private static String normalizeMeasurementId(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        return GA4_MEASUREMENT_ID.matcher(normalized).matches() ? normalized : "";
    }

    private record PageMetadata(String title, String description) {
    }
}
