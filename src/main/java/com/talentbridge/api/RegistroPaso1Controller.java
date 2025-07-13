package com.talentbridge.api;

import com.talentbridge.dto.RegistroPaso1DTO;
import com.talentbridge.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroPaso1Controller {

    private final UsuarioService usuarioService;

    @PostMapping("/paso1")
    public ResponseEntity<?> registrarPaso1(@RequestBody RegistroPaso1DTO dto) {
        if (usuarioService.existePorEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body("Correo ya registrado.");
        }
        usuarioService.registrarPaso1(dto);
        return ResponseEntity.ok().build();
    }

}
