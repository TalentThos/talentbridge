package com.talentbridge.api;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/oferente/servicios")
@RequiredArgsConstructor
public class ServicioRestController {

    private final ServicioService servicioService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearServicio(@ModelAttribute ServicioDTO dto,
                                           @RequestParam(value = "imagenes", required = false) List<MultipartFile> imagenes,
                                           Authentication authentication) {
        try {
            dto.setImagenes(imagenes);
            String email = authentication.getName();
            servicioService.crearServicio(dto, email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No pudimos validar las imagenes en este momento. Intenta nuevamente mas tarde.");
        }
    }

    @PutMapping("/{id}/actualizar")
    public ResponseEntity<?> actualizarServicio(@PathVariable Long id,
                                                @ModelAttribute ServicioDTO dto,
                                                @RequestParam(value = "imagenes", required = false) List<MultipartFile> imagenes,
                                                @RequestParam(value = "imagenesEliminar", required = false) List<Long> imagenesEliminar,
                                                Authentication authentication) {
        try {
            dto.setImagenes(imagenes);
            dto.setImagenesEliminar(imagenesEliminar);
            String email = authentication.getName();
            servicioService.actualizarServicio(id, dto, email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No pudimos validar las imagenes en este momento. Intenta nuevamente mas tarde.");
        }
    }
}
