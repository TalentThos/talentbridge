package com.talentbridge.service;

import com.talentbridge.dto.PublicacionPendienteDTO;
import com.talentbridge.dto.VerificacionPublicacionDTO;
import com.talentbridge.dto.CrearPasswordPublicacionDTO;

public interface PublicacionPendienteService {
    void crearPublicacionPendiente(PublicacionPendienteDTO dto);
    void verificarYPublicar(VerificacionPublicacionDTO dto);
    void crearPassword(CrearPasswordPublicacionDTO dto);
}
