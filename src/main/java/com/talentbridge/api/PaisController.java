package com.talentbridge.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/paises")
public class PaisController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerPaises() {
        log.debug("Obteniendo lista de paises");

        List<Map<String, Object>> paises = Arrays.stream(Locale.getISOCountries())
                .map(this::crearPais)
                .sorted((a, b) -> obtenerNombre(a).compareToIgnoreCase(obtenerNombre(b)))
                .toList();

        return ResponseEntity.ok(paises);
    }

    private Map<String, Object> crearPais(String codigoPais) {
        Locale localePais = Locale.of("", codigoPais);

        Map<String, Object> nombre = new LinkedHashMap<>();
        nombre.put("common", localePais.getDisplayCountry(Locale.forLanguageTag("es")));

        Map<String, Object> pais = new LinkedHashMap<>();
        pais.put("name", nombre);
        pais.put("cca2", codigoPais);
        return pais;
    }

    @SuppressWarnings("unchecked")
    private String obtenerNombre(Map<String, Object> pais) {
        Map<String, Object> nombre = (Map<String, Object>) pais.get("name");
        return nombre.get("common").toString();
    }
}
