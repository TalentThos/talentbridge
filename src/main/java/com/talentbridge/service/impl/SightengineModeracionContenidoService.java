package com.talentbridge.service.impl;

import com.talentbridge.dto.ResultadoModeracionImagen;
import com.talentbridge.service.ModeracionContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "content.moderation.provider", havingValue = "sightengine", matchIfMissing = true)
public class SightengineModeracionContenidoService implements ModeracionContenidoService {

    private final RestClient restClient = RestClient.create();

    @Value("${content.moderation.enabled:false}")
    private boolean moderacionHabilitada;

    @Value("${sightengine.api-url:https://api.sightengine.com/1.0/check.json}")
    private String apiUrl;

    @Value("${sightengine.api-user:}")
    private String apiUser;

    @Value("${sightengine.api-secret:}")
    private String apiSecret;

    @Value("${sightengine.models:nudity-2.1}")
    private String models;

    @Value("${sightengine.threshold.adult:0.75}")
    private double umbralAdulto;

    @Value("${sightengine.threshold.suggestive:0.9}")
    private double umbralSugerente;

    @Override
    public ResultadoModeracionImagen moderarImagen(MultipartFile imagen) {
        if (!moderacionHabilitada || imagen == null || imagen.isEmpty()) {
            return ResultadoModeracionImagen.aprobada();
        }

        if (apiUser.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException("La moderacion de imagenes esta habilitada, pero faltan credenciales de Sightengine");
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(crearRequest(imagen))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new IllegalStateException("Sightengine no retorno respuesta");
            }

            String status = String.valueOf(response.getOrDefault("status", ""));
            if (!"success".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Sightengine rechazo la solicitud de moderacion");
            }

            Map<String, Object> nudity = obtenerMapa(response.get("nudity"));
            Double sexualActivity = obtenerDouble(nudity.get("sexual_activity"));
            Double sexualDisplay = obtenerDouble(nudity.get("sexual_display"));
            Double erotica = obtenerDouble(nudity.get("erotica"));
            Double verySuggestive = obtenerDouble(nudity.get("very_suggestive"));

            double scoreAdulto = max(sexualActivity, sexualDisplay, erotica);
            double scoreSugerente = max(verySuggestive, erotica);

            if (scoreAdulto >= umbralAdulto || scoreSugerente >= umbralSugerente) {
                return ResultadoModeracionImagen.rechazada(
                        "La imagen no cumple con la politica de uso de TalentBridge.",
                        scoreAdulto,
                        scoreSugerente
                );
            }

            return ResultadoModeracionImagen.aprobada();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la imagen para moderacion", e);
        }
    }

    private MultiValueMap<String, Object> crearRequest(MultipartFile imagen) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", new ByteArrayResource(imagen.getBytes()) {
            @Override
            public String getFilename() {
                return imagen.getOriginalFilename() != null ? imagen.getOriginalFilename() : "imagen";
            }
        });
        body.add("models", models);
        body.add("api_user", apiUser);
        body.add("api_secret", apiSecret);
        return body;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerMapa(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private Double obtenerDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            return Double.parseDouble(string);
        }
        return 0.0;
    }

    private double max(Double... valores) {
        double maximo = 0.0;
        for (Double valor : valores) {
            if (valor != null && valor > maximo) {
                maximo = valor;
            }
        }
        return maximo;
    }
}
