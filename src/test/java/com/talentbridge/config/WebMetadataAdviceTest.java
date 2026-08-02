package com.talentbridge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;

class WebMetadataAdviceTest {

    @Test
    void exposesIndexableMetadataAndValidAnalyticsConfiguration() {
        WebMetadataAdvice advice = new WebMetadataAdvice(
                "https://www.talentbridge.cl/",
                "g-abcd1234",
                new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        ExtendedModelMap model = new ExtendedModelMap();

        advice.addWebMetadata(request, model);

        assertThat(model.get("seoCanonicalUrl")).isEqualTo("https://www.talentbridge.cl/");
        assertThat(model.get("seoRobots")).asString().startsWith("index, follow");
        assertThat(model.get("googleAnalyticsEnabled")).isEqualTo(true);
        assertThat(model.get("googleAnalyticsMeasurementId")).isEqualTo("G-ABCD1234");
        assertThat(model.get("seoStructuredData")).asString().contains("SearchAction", "TalentBridge");
    }

    @Test
    void preventsFilteredSearchPagesFromBeingIndexed() {
        WebMetadataAdvice advice = new WebMetadataAdvice(
                "https://www.talentbridge.cl",
                "not-a-measurement-id",
                new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/buscar");
        request.setQueryString("q=electricista&page=2");
        ExtendedModelMap model = new ExtendedModelMap();

        advice.addWebMetadata(request, model);

        assertThat(model.get("seoCanonicalUrl")).isEqualTo("https://www.talentbridge.cl/buscar");
        assertThat(model.get("seoRobots")).isEqualTo("noindex, nofollow");
        assertThat(model.get("googleAnalyticsEnabled")).isEqualTo(false);
        assertThat(model.get("googleAnalyticsMeasurementId")).isEqualTo("");
    }
}
