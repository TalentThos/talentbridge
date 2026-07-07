package com.talentbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String mostrarHome() {
        return "home";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/publicar-servicio")
    public String publicarServicio() {
        return "publicar_servicio";
    }

}
