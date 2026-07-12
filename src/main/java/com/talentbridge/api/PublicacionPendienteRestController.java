package com.talentbridge.api;

import com.talentbridge.dto.PublicacionPendienteDTO;
import com.talentbridge.dto.VerificacionPublicacionDTO;
import com.talentbridge.dto.CrearPasswordPublicacionDTO;
import com.talentbridge.service.PublicacionPendienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publicaciones")
@RequiredArgsConstructor
public class PublicacionPendienteRestController {

    private final PublicacionPendienteService publicacionPendienteService;

    @PostMapping("/pendiente")
    public ResponseEntity<?> crearPendiente(@RequestBody PublicacionPendienteDTO dto) {
        try {
            publicacionPendienteService.crearPublicacionPendiente(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestBody VerificacionPublicacionDTO dto) {
        try {
            publicacionPendienteService.verificarYPublicar(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/crear-password")
    public ResponseEntity<?> crearPassword(@RequestBody CrearPasswordPublicacionDTO dto) {
        try {
            publicacionPendienteService.crearPassword(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
