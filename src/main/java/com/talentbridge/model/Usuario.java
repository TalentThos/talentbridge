package com.talentbridge.model;

import com.talentbridge.tipos.EstadoRegistro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String password;
    private String email;
    private String rol;
    private String pais;
    private String ciudad;
    private String calle;
    private String numeroDireccion;
    private String tipoDocumento;
    private String numeroDocumento;
    private String numeroMovil;
    @Enumerated(EnumType.STRING)
    private EstadoRegistro estadoRegistro;
    private Boolean verificado;
    private Boolean activo;
    private Boolean aceptaPoliticaPrivacidad;
    private Boolean aceptaCondicionesUso;
    private LocalDateTime fechaAceptacionPoliticas;
    private String versionPoliticas;
    private String ipAceptacionPoliticas;
    @Column(length = 1000)
    private String userAgentAceptacionPoliticas;
    private LocalDateTime recordatorioPublicacionEnviadoEn;
}
