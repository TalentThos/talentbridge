package com.talentbridge.controller;

import com.talentbridge.model.Comunicacion;
import com.talentbridge.model.Usuario;
import com.talentbridge.service.ComunicacionService;
import com.talentbridge.service.MensajeService;
import com.talentbridge.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;
    private final UsuarioRepository usuarioRepository;
    private final ComunicacionService comunicacionService;

    @GetMapping("/mensajes")
    public String verComunicaciones(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("comunicaciones", comunicacionService.obtenerComunicaciones(usuario.getId()));
        model.addAttribute("usuarioId", usuario.getId());
        return "mensajes";
    }

    @GetMapping("/mensajes/{id}")
    public String verMensajes(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Comunicacion comunicacion = comunicacionService.obtenerComunicacion(id);
        model.addAttribute("mensajes", mensajeService.obtenerMensajesPorComunicacion(id));
        Usuario otro = comunicacion.getRemitente().getId().equals(usuario.getId()) ?
                comunicacion.getDestinatario() : comunicacion.getRemitente();
        model.addAttribute("otro", otro);
        model.addAttribute("usuarioId", usuario.getId());
        return "comunicacion";
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
