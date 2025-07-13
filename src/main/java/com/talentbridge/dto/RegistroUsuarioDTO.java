package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class RegistroUsuarioDTO {
    String nombre;
    String email;
    String password;
    String pais;
    String ciudad;
    String calle;
    String numero;
    String tipoDocumento;
    String numeroDocumento;
}

