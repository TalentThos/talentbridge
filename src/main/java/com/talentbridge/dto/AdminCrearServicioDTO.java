package com.talentbridge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminCrearServicioDTO {
    private Long usuarioId;
    private String titulo;
    private String descripcion;
    private String numeroMovil;
    private String tipoValorizacion;
    private BigDecimal valorReferencial;
    private Long categoriaId;
    private Long subcategoriaId;
}
