package com.talentbridge.service.impl;

import com.talentbridge.model.Usuario;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.CorreoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioPublicacionService {

    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;

    @Value("${recordatorio-publicacion.enabled:true}")
    private boolean recordatorioPublicacionEnabled;

    @Value("${app.public-url:https://www.talentbridge.cl}")
    private String appPublicUrl;

    @Scheduled(cron = "${recordatorio-publicacion.cron:0 0 * * * *}")
    @Transactional
    public void enviarRecordatoriosPublicacion() {
        if (!recordatorioPublicacionEnabled) {
            log.debug("Recordatorio de publicacion deshabilitado.");
            return;
        }

        List<Usuario> usuarios = usuarioRepository.buscarUsuariosParaRecordatorioPublicacion();
        if (usuarios.isEmpty()) {
            log.info("No hay usuarios pendientes para recordatorio de publicacion.");
            return;
        }

        for (Usuario usuario : usuarios) {
            enviarRecordatorio(usuario);
        }
    }

    private void enviarRecordatorio(Usuario usuario) {
        String asunto = "Tu cuenta TalentBridge ya esta lista";
        String contenidoHtml = construirContenidoHtml(usuario);

        correoService.enviarCorreo(usuario.getEmail(), asunto, contenidoHtml);
        usuario.setRecordatorioPublicacionEnviadoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);
        log.info("Recordatorio de publicacion enviado a usuario id={} email={}", usuario.getId(), usuario.getEmail());
    }

    private String construirContenidoHtml(Usuario usuario) {
        String nombre = usuario.getNombre() != null && !usuario.getNombre().isBlank()
                ? usuario.getNombre()
                : "Hola";
        String urlPublicar = normalizarUrlBase(appPublicUrl) + "/ofrecer";

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937; line-height: 1.5;">
                    <h2 style="color: #246BCE;">Tu cuenta TalentBridge ya esta lista</h2>
                    <p>%s, gracias por registrarte en TalentBridge.</p>
                    <p>
                        Si ofreces algun servicio, ya puedes publicar tu perfil para que otras
                        personas puedan encontrarte.
                    </p>
                    <p>
                        Publicar servicio: <a href="%s">%s</a>
                    </p>
                    <p>
                        Saludos,<br>
                        TalentBridge
                    </p>
                </body>
                </html>
                """.formatted(escaparHtml(nombre), urlPublicar, urlPublicar);
    }

    private String normalizarUrlBase(String url) {
        if (url == null || url.isBlank()) {
            return "https://www.talentbridge.cl";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String escaparHtml(String valor) {
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
