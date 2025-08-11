package com.talentbridge.service;

import com.talentbridge.model.Mensaje;

import java.util.List;

public interface MensajeService {
    void enviarMensaje(Long remitenteId, Long destinatarioId, String contenido);
    List<Mensaje> obtenerMensajes(Long usuarioId);
    List<Mensaje> obtenerMensajesPorComunicacion(Long comunicacionId);
}
