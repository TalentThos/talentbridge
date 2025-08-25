package com.talentbridge.api;

import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioPublicRestController {

    private final ServicioService servicioService;

    @GetMapping("/{id}/imagenes")
    public List<String> obtenerImagenes(@PathVariable Long id) {
        return servicioService.obtenerImagenesBase64(id);
    }
}

