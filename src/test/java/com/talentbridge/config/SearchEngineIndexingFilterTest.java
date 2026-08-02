package com.talentbridge.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SearchEngineIndexingFilterTest {

    private final SearchEngineIndexingFilter filter = new SearchEngineIndexingFilter();

    @Test
    void addsNoIndexHeaderToPrivatePages() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/perfil");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader("X-Robots-Tag")).isEqualTo("noindex, nofollow, nosnippet");
    }

    @Test
    void leavesCanonicalPublicPageIndexable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/buscar");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader("X-Robots-Tag")).isNull();
    }

    @Test
    void preventsFilteredSearchPageFromBeingIndexed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/buscar");
        request.setQueryString("q=gasfiter");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader("X-Robots-Tag")).isEqualTo("noindex, nofollow, nosnippet");
    }

    @Test
    void leavesPublishedServicePageIndexable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/servicios/42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader("X-Robots-Tag")).isNull();
    }
}
