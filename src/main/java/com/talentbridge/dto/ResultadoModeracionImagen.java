package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultadoModeracionImagen {

    private boolean aprobada;
    private String motivo;
    private Double scoreAdulto;
    private Double scoreSugerente;

    public static ResultadoModeracionImagen aprobada() {
        return ResultadoModeracionImagen.builder()
                .aprobada(true)
                .build();
    }

    public static ResultadoModeracionImagen rechazada(String motivo, Double scoreAdulto, Double scoreSugerente) {
        return ResultadoModeracionImagen.builder()
                .aprobada(false)
                .motivo(motivo)
                .scoreAdulto(scoreAdulto)
                .scoreSugerente(scoreSugerente)
                .build();
    }
}
