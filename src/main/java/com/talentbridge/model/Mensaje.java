package com.talentbridge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Getter
@Setter
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicacion_id")
    private Comunicacion comunicacion;

    private String contenido;

    private LocalDateTime enviadoEn;
}
