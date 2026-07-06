package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class RegistroPaso1DTO {
    private String nombre;
    private String email;
    private String numeroMovil;
    private String tipoDocumento;
    private String numeroDocumento;
    private String pais;
    private String ciudad;
    private String calle;
    private String numeroDireccion;
    private String password;
    private Boolean aceptaPoliticaPrivacidad;
    private Boolean aceptaCondicionesUso;
    private String ipAceptacionPoliticas;
    private String userAgentAceptacionPoliticas;
}
