package com.talentbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCrearUsuarioDTO {
    private String nombre;
    private String email;
    private String ciudad;
    private String numeroMovil;
    private String password;
}
