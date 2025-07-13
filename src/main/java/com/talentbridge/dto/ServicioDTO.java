package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class ServicioDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private Integer precio;
}