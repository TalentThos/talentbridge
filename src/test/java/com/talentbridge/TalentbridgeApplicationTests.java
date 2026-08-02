package com.talentbridge;

import com.talentbridge.model.Categoria;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TalentbridgeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ServicioRepository servicioRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void homeRendersSearchMetadata() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"<meta name=\"description\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"<link rel=\"canonical\" href=\"https://www.talentbridge.cl/\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"application/ld+json")));
	}

	@Test
	void seoDiscoveryEndpointsArePublic() throws Exception {
		mockMvc.perform(get("/robots.txt"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/plain"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"Sitemap: https://www.talentbridge.cl/sitemap.xml")));

		mockMvc.perform(get("/sitemap.xml"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/xml"))
				.andExpect(header().doesNotExist("X-Robots-Tag"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"<loc>https://www.talentbridge.cl/buscar</loc>")));
	}

	@Test
	void blogIsPublicIndexableAndLoadsSoroWidget() throws Exception {
		mockMvc.perform(get("/blog"))
				.andExpect(status().isOk())
				.andExpect(header().doesNotExist("X-Robots-Tag"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"<link rel=\"canonical\" href=\"https://www.talentbridge.cl/blog\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"id=\"soro-blog\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"https://app.trysoro.com/api/embed/d17c391e-f2e6-4445-84ae-77a70b6bb10b")));
	}

	@Test
	@Transactional
	void publishedServiceHasAnIndexablePublicPage() throws Exception {
		Usuario usuario = new Usuario();
		usuario.setNombre("Profesional de prueba");
		usuario.setEmail("seo-service@example.test");
		usuario.setActivo(true);
		usuario = usuarioRepository.save(usuario);

		Categoria categoria = new Categoria();
		categoria.setNombre("Servicios técnicos");
		categoria = categoriaRepository.save(categoria);

		Servicio servicio = new Servicio();
		servicio.setTitulo("Electricista a domicilio");
		servicio.setDescripcion("Instalaciones y reparaciones eléctricas en Santiago.");
		servicio.setEstadoPublicacion("PUBLICADO");
		servicio.setUsuario(usuario);
		servicio.setCategoria(categoria);
		servicio = servicioRepository.save(servicio);

		mockMvc.perform(get("/servicios/{id}", servicio.getId()))
				.andExpect(status().isOk())
				.andExpect(header().doesNotExist("X-Robots-Tag"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"Electricista a domicilio | TalentBridge")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"https://www.talentbridge.cl/servicios/" + servicio.getId())));
	}

}
