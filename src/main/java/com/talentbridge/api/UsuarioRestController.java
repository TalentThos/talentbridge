package com.talentbridge.api;

import com.talentbridge.dto.UsuarioDTO;
import com.talentbridge.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioDTO obtenerPerfil(Authentication authentication) {
        return usuarioService.obtenerPorEmail(authentication.getName());
    }

    @PutMapping("/me")
    public ResponseEntity<?> actualizarPerfil(@RequestBody UsuarioDTO dto, Authentication authentication) {
        usuarioService.actualizarPerfil(authentication.getName(), dto);
        return ResponseEntity.ok().build();
    }
}
