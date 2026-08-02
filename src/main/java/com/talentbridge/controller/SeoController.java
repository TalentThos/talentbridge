package com.talentbridge.controller;

import com.talentbridge.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeoController {

    private static final List<String> INDEXABLE_PATHS = List.of(
            "/",
            "/blog",
            "/buscar",
            "/publicar-servicio",
            "/politicas/privacidad",
            "/politicas/condiciones"
    );

    private final String publicUrl;
    private final ServicioRepository servicioRepository;

    public SeoController(
            @Value("${app.public-url:https://www.talentbridge.cl}") String publicUrl,
            ServicioRepository servicioRepository) {
        String value = publicUrl == null ? "" : publicUrl.trim();
        this.publicUrl = value.matches("^https?://[^\\s/]+(?::\\d+)?(?:/.*)?$")
                ? value.replaceAll("/+$", "")
                : "https://www.talentbridge.cl";
        this.servicioRepository = servicioRepository;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        return """
                User-agent: *
                Allow: /
                Disallow: /admin
                Disallow: /api/
                Disallow: /login
                Disallow: /logout
                Disallow: /mensajes
                Disallow: /mis-servicios
                Disallow: /ofrecer
                Disallow: /perfil
                Disallow: /publicar/
                Disallow: /registro
                Disallow: /usuarios

                Sitemap: %s/sitemap.xml
                """.formatted(publicUrl);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String path : INDEXABLE_PATHS) {
            xml.append("  <url><loc>")
                    .append(publicUrl)
                    .append("/".equals(path) ? "/" : path)
                    .append("</loc></url>\n");
        }
        for (Long serviceId : servicioRepository.findIdsPublicados()) {
            xml.append("  <url><loc>")
                    .append(publicUrl)
                    .append("/servicios/")
                    .append(serviceId)
                    .append("</loc></url>\n");
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }
}
