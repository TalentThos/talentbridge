package com.talentbridge.controller;

import com.talentbridge.model.Usuario;
import com.talentbridge.service.MensajeService;
import com.talentbridge.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/mensajes")
    public String verMensajes(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("mensajes", mensajeService.obtenerMensajes(usuario.getId()));
        return "mensajes";
    }

    @PostMapping("/api/mensajes")
    @ResponseBody
    public void enviarMensaje(@RequestParam Long destinatarioId, @RequestParam String contenido,
                              Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        mensajeService.enviarMensaje(usuario.getId(), destinatarioId, contenido);
    }
}
