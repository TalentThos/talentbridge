package com.talentbridge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.service.CategoriaService;
import com.talentbridge.service.ServicioService;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;
    private final CategoriaService categoriaService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.public-url:https://www.talentbridge.cl}")
    private String publicUrl;

    @GetMapping("/ofrecer")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicio", ServicioDTO.builder().build());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "oferente/crear_servicio";
    }

    @GetMapping("/ofrecer/{id}")
    public String editarServicio(@PathVariable Long id, Authentication authentication, Model model) {
        String email = authentication.getName();
        ServicioDTO servicio = servicioService.obtenerPorId(id, email);
        model.addAttribute("servicio", servicio);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "oferente/crear_servicio";
    }

    @GetMapping("/buscar")
    public String buscarServicios(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "subcategoriaId", required = false) String subcategoriaId,
            @RequestParam(value = "pais", required = false) String pais,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Authentication authentication,
            Model model) {
        Long subcatId = null;
        if (subcategoriaId != null && !subcategoriaId.isBlank()) {
            try {
                subcatId = Long.valueOf(subcategoriaId);
            } catch (NumberFormatException ignored) {
            }
        }
        String filtroPais = pais;
        if ((filtroPais == null || filtroPais.isBlank()) && authentication != null) {
            String email = authentication.getName();
            filtroPais = usuarioRepository.findByEmail(email)
                    .map(Usuario::getPais)
                    .orElse(null);
        }
        Page<ServicioDTO> servicios = servicioService.buscarServicios(q, categoriaId, subcatId, filtroPais, page);
        model.addAttribute("servicios", servicios.getContent());
        model.addAttribute("page", servicios);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("subcategoriaId", subcatId);
        model.addAttribute("termino", q);
        model.addAttribute("pais", pais);
        return "buscar_servicios";
    }

    @GetMapping("/servicios/{id}")
    public String verServicioPublicado(@PathVariable Long id, Model model) {
        final ServicioDTO servicio;
        try {
            servicio = servicioService.obtenerPublicadoPorId(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado");
        }

        String canonicalUrl = publicUrl.replaceAll("/+$", "") + "/servicios/" + id;
        String description = resumenSeo(servicio.getDescripcion());
        model.addAttribute("servicio", servicio);
        model.addAttribute("seoTitle", servicio.getTitulo() + " | TalentBridge");
        model.addAttribute("seoDescription", description);
        model.addAttribute("seoCanonicalUrl", canonicalUrl);
        model.addAttribute("seoRobots", "index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1");
        model.addAttribute("seoStructuredData", serviceStructuredData(servicio, canonicalUrl, description));
        return "detalle_servicio";
    }

    @GetMapping("/mis-servicios")
    public String misServicios(Authentication authentication, Model model) {
        String email = authentication.getName();
        List<ServicioDTO> servicios = servicioService.listarPorUsuario(email);
        model.addAttribute("servicios", servicios);
        return "mis_servicios";
    }

    @GetMapping("/servicios/{id}/imagenes")
    public String verImagenes(@PathVariable Long id, Model model) {
        List<String> imagenes = servicioService.obtenerImagenesBase64(id);
        model.addAttribute("imagenes", imagenes);
        return "imagenes_servicio";
    }

    private String serviceStructuredData(ServicioDTO servicio, String canonicalUrl, String description) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("@context", "https://schema.org");
        data.put("@type", "Service");
        data.put("name", servicio.getTitulo());
        data.put("description", description);
        data.put("url", canonicalUrl);
        data.put("areaServed", Map.of("@type", "Country", "name", "Chile"));
        if (servicio.getCategoriaNombre() != null) {
            data.put("category", servicio.getCategoriaNombre());
        }
        if (servicio.getUsuarioNombre() != null) {
            data.put("provider", Map.of("@type", "Person", "name", servicio.getUsuarioNombre()));
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String resumenSeo(String value) {
        if (value == null || value.isBlank()) {
            return "Conoce este servicio publicado en TalentBridge y contacta directamente a quien lo ofrece.";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 157) + "...";
    }
}
