package com.talentbridge.controller;

import com.talentbridge.repository.ServicioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeoControllerTest {

    private final ServicioRepository servicioRepository = mock(ServicioRepository.class);
    private final SeoController controller = new SeoController("https://www.talentbridge.cl/", servicioRepository);

    @Test
    void robotsReferencesSitemapAndProtectsPrivateAreas() {
        assertThat(controller.robots())
                .contains("Sitemap: https://www.talentbridge.cl/sitemap.xml")
                .contains("Disallow: /admin")
                .contains("Disallow: /api/")
                .contains("Disallow: /perfil");
    }

    @Test
    void sitemapContainsOnlyCanonicalPublicPages() {
        when(servicioRepository.findIdsPublicados()).thenReturn(List.of(12L, 25L));

        assertThat(controller.sitemap())
                .contains("<loc>https://www.talentbridge.cl/</loc>")
                .contains("<loc>https://www.talentbridge.cl/buscar</loc>")
                .contains("<loc>https://www.talentbridge.cl/publicar-servicio</loc>")
                .contains("<loc>https://www.talentbridge.cl/servicios/12</loc>")
                .contains("<loc>https://www.talentbridge.cl/servicios/25</loc>")
                .doesNotContain("/login")
                .doesNotContain("/registro");
    }
}
