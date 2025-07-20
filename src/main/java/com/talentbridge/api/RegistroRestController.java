package com.talentbridge.api;

import com.talentbridge.dto.CodigoVerificacionDTO;
import com.talentbridge.dto.RegistroPaso1DTO;
import com.talentbridge.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroRestController {

    private final RegistroService registroService;

    @PostMapping("/paso1")
    public ResponseEntity<?> registrarPaso1(@RequestBody RegistroPaso1DTO dto) {
        registroService.registrarPaso1(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody CodigoVerificacionDTO dto) {
        try {
            registroService.verificarCodigo(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
