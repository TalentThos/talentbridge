package com.talentbridge.controller;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping("/ofrecer")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicio", ServicioDTO.builder().build());
        return "oferente/crear_servicio";
    }

    @PostMapping("/oferente/servicios/crear")
    public String crearServicio(@ModelAttribute("servicio") ServicioDTO dto,
                                Authentication authentication) {
        String email = authentication.getName();
        servicioService.crearServicio(dto, email);
        return "redirect:/home";
    }

    @GetMapping("/buscar")
    public String buscarServicios(@RequestParam(value = "q", required = false) String q, Model model) {
        List<ServicioDTO> servicios = servicioService.buscarServicios(q);
        model.addAttribute("servicios", servicios);
        return "buscar_servicios";
    }
}
