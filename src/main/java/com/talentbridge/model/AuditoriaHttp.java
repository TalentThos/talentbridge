package com.talentbridge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias_http")
@Getter
@Setter
public class AuditoriaHttp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @Column(length = 10)
    private String metodo;

    @Column(length = 500)
    private String path;

    @Column(length = 1000)
    private String queryString;

    @Column(length = 80)
    private String ipCliente;

    @Column(length = 180)
    private String usuario;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 200)
    private String requestContentType;

    @Column(length = 200)
    private String responseContentType;

    private Boolean multipart;

    private Integer status;

    private Long duracionMs;

    @Column(length = 300)
    private String error;
}
