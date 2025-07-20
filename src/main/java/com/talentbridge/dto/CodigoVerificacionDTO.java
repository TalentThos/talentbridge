package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class CodigoVerificacionDTO {
    private String email;
    private String codigo;
}
