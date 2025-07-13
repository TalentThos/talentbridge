package com.talentbridge.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/paises")
public class PaisController {

    @GetMapping
    public ResponseEntity<?> obtenerPaises() {
        log.debug("Obteniendo lista de países desde la API externa");
        String url = "https://restcountries.com/v3.1/all?fields=name,cca2";
        RestTemplate restTemplate = new RestTemplate();
        try {
            Object[] respuesta = restTemplate.getForObject(url, Object[].class);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener países: {}", e.getMessage());
            log.error("Stack trace: ", e);
            return ResponseEntity.badRequest().body("Error al obtener países");
        }
    }
}
