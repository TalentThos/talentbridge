package com.talentbridge.service.impl;

import com.talentbridge.model.Mensaje;
import com.talentbridge.model.Usuario;
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

    @Override
    public void enviarMensaje(Long remitenteId, Long destinatarioId, String contenido) {
        Usuario remitente = usuarioRepository.findById(remitenteId)
                .orElseThrow(() -> new IllegalArgumentException("Remitente no encontrado"));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new IllegalArgumentException("Destinatario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setContenido(contenido);
        mensaje.setEnviadoEn(LocalDateTime.now());
        mensajeRepository.save(mensaje);
    }

    @Override
    public List<Mensaje> obtenerMensajes(Long usuarioId) {
        return mensajeRepository.findByDestinatarioIdOrderByEnviadoEnDesc(usuarioId);
    }
}
