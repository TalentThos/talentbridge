package com.talentbridge.model;

import com.talentbridge.tipos.EstadoRegistro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
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
    @Enumerated(EnumType.STRING)
    private EstadoRegistro estadoRegistro;
    private Boolean verificado;
    private Boolean activo;
}
