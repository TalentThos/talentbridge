package com.talentbridge.controller;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.service.CategoriaService;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;
    private final CategoriaService categoriaService;

    @GetMapping("/ofrecer")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicio", ServicioDTO.builder().build());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "oferente/crear_servicio";
    }

    @GetMapping("/buscar")
    public String buscarServicios(@RequestParam(value = "q", required = false) String q, Model model) {
        List<ServicioDTO> servicios = servicioService.buscarServicios(q);
        model.addAttribute("servicios", servicios);
        return "buscar_servicios";
    }

    @GetMapping("/servicios/{id}/imagenes")
    public String verImagenes(@PathVariable Long id, Model model) {
        List<String> imagenes = servicioService.obtenerImagenesBase64(id);
        model.addAttribute("imagenes", imagenes);
        return "imagenes_servicio";
    }
}
