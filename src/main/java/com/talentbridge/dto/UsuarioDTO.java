package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private String password;
    private String tipoDocumento;
    private String numeroDocumento;
    private String pais;
    private String ciudad;
    private String calle;
    private String numeroDireccion;
}

