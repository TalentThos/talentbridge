package com.talentbridge.service.impl;

import com.talentbridge.model.Comunicacion;
import com.talentbridge.model.Mensaje;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.ComunicacionRepository;
import com.talentbridge.repository.MensajeRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunicacionRepository comunicacionRepository;

    @Override
    public void enviarMensaje(Long remitenteId, Long destinatarioId, String contenido) {
        Usuario remitente = usuarioRepository.findById(remitenteId)
                .orElseThrow(() -> new IllegalArgumentException("Remitente no encontrado"));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new IllegalArgumentException("Destinatario no encontrado"));

        Comunicacion comunicacion = comunicacionRepository
                .findByRemitenteIdAndDestinatarioIdOrRemitenteIdAndDestinatarioId(remitenteId, destinatarioId, destinatarioId, remitenteId)
                .orElseGet(() -> {
                    Comunicacion nueva = new Comunicacion();
                    nueva.setRemitente(remitente);
                    nueva.setDestinatario(destinatario);
                    nueva.setCreadaEn(LocalDateTime.now());
                    return comunicacionRepository.save(nueva);
                });

        Mensaje mensaje = new Mensaje();
        mensaje.setComunicacion(comunicacion);
        mensaje.setContenido(contenido);
        mensaje.setEnviadoEn(LocalDateTime.now());
        mensajeRepository.save(mensaje);
    }

    @Override
    public List<Mensaje> obtenerMensajes(Long usuarioId) {
        return mensajeRepository.findByComunicacionDestinatarioIdOrderByEnviadoEnDesc(usuarioId);
    }

    @Override
    public List<Mensaje> obtenerMensajesPorComunicacion(Long comunicacionId) {
        return mensajeRepository.findByComunicacionIdOrderByEnviadoEnAsc(comunicacionId);
    }
}
