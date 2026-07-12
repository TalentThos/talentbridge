package com.talentbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearPasswordPublicacionDTO {
    private String email;
    private String password;
    private String confirmarPassword;
}
