package com.talentbridge.service.impl;

import com.talentbridge.model.Comunicacion;
import com.talentbridge.repository.ComunicacionRepository;
import com.talentbridge.service.ComunicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComunicacionServiceImpl implements ComunicacionService {

    private final ComunicacionRepository comunicacionRepository;

    @Override
    public List<Comunicacion> obtenerComunicaciones(Long usuarioId) {
        return comunicacionRepository.findByRemitenteIdOrDestinatarioId(usuarioId, usuarioId);
    }

    @Override
    public Comunicacion obtenerComunicacion(Long comunicacionId) {
        return comunicacionRepository.findById(comunicacionId)
                .orElseThrow(() -> new IllegalArgumentException("Comunicación no encontrada"));
    }
}
