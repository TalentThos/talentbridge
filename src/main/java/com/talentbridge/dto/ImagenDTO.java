package com.talentbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImagenDTO {
    private Long id;
    private String nombre;
    private String datosBase64;
}

