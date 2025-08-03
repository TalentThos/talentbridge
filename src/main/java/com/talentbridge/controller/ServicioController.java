package com.talentbridge.controller;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.service.CategoriaService;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;
    private final CategoriaService categoriaService;

    @GetMapping("/ofrecer")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicio", ServicioDTO.builder().build());
        return "oferente/crear_servicio";
    }

    @ModelAttribute("categorias")
    public List<Categoria> categorias() {
        return categoriaService.listar();
    }

    @PostMapping("/oferente/servicios/crear")
    public String crearServicio(@ModelAttribute("servicio") ServicioDTO dto,
                                @RequestParam("imagenes") List<MultipartFile> imagenes,
                                Authentication authentication) throws java.io.IOException {
        String email = authentication.getName();
        servicioService.crearServicio(dto, imagenes, email);
        return "redirect:/home";
    }

    @GetMapping("/buscar")
    public String buscarServicios(@RequestParam(value = "q", required = false) String q, Model model) {
        List<ServicioDTO> servicios = servicioService.buscarServicios(q);
        model.addAttribute("servicios", servicios);
        return "buscar_servicios";
    }
}
