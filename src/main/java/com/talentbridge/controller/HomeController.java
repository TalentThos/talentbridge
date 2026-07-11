package com.talentbridge.controller;

import com.talentbridge.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoriaService categoriaService;

    @GetMapping("/home")
    public String mostrarHome() {
        return "home";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/publicar-servicio")
    public String publicarServicio(Model model) {
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "publicar_servicio";
    }

}
