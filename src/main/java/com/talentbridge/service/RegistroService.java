package com.talentbridge.service;

import com.talentbridge.dto.CodigoVerificacionDTO;
import com.talentbridge.dto.RegistroPaso1DTO;

public interface RegistroService {
    void registrarPaso1(RegistroPaso1DTO dto);
    void verificarCodigo(CodigoVerificacionDTO dto);
}
