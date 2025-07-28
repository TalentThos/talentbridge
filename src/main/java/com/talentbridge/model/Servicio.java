package com.talentbridge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descripcion;

    private String categoria;

    private Integer precio;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
}
