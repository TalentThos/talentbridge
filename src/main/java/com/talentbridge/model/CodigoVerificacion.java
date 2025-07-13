package com.talentbridge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "codigos_verificacion")
@Getter
@Setter
public class CodigoVerificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private Timestamp creadoEn;
    private Timestamp expiraEn;
    private Boolean usado = false;
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
}

