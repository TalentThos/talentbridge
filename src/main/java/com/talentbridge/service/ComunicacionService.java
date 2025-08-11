package com.talentbridge.service;

import com.talentbridge.model.Comunicacion;

import java.util.List;

public interface ComunicacionService {
    List<Comunicacion> obtenerComunicaciones(Long usuarioId);
    Comunicacion obtenerComunicacion(Long comunicacionId);
}
