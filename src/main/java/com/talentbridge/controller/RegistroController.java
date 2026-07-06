package com.talentbridge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegistroController {

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        return "registro_paso1";
    }

    @GetMapping("/registro/paso2")
    public String mostrarPaso2() {
        return "registro_paso2";
    }

    @GetMapping("/politicas/privacidad")
    public String mostrarPoliticaPrivacidad() {
        return "politicas_privacidad";
    }

    @GetMapping("/politicas/condiciones")
    public String mostrarCondicionesUso() {
        return "politicas_condiciones";
    }

}
