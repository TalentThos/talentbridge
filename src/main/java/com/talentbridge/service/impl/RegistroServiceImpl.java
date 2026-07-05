package com.talentbridge.service.impl;

import com.talentbridge.dto.CodigoVerificacionDTO;
import com.talentbridge.dto.RegistroPaso1DTO;
import com.talentbridge.model.CodigoVerificacion;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CodigoVerificacionRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.CorreoService;
import com.talentbridge.service.RegistroService;
import com.talentbridge.tipos.EstadoRegistro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final CorreoService correoService;

    @Override
    @Transactional
    public void registrarPaso1(RegistroPaso1DTO dto) {
        if (usuarioRepository.existsByEmailAndActivoTrue(dto.getEmail())) {
            throw new IllegalArgumentException("Correo ya registrado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseGet(Usuario::new);

        completarDatosUsuario(usuario, dto);
        usuarioRepository.save(usuario);
        invalidarCodigosPendientes(usuario);
        crearCodigoVerificacion(usuario, generarCodigo());
    }

    private void completarDatosUsuario(Usuario usuario, RegistroPaso1DTO dto) {
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setNumeroMovil(dto.getNumeroMovil());
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setPais(dto.getPais());
        usuario.setCiudad(dto.getCiudad());
        usuario.setCalle(dto.getCalle());
        usuario.setNumeroDireccion(dto.getNumeroDireccion());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstadoRegistro(EstadoRegistro.PENDIENTE_VALIDACION);
        usuario.setActivo(false);
        usuario.setVerificado(false);
    }

    private void invalidarCodigosPendientes(Usuario usuario) {
        codigoVerificacionRepository.findByUsuarioAndUsadoFalse(usuario).forEach(codigoPendiente -> {
            codigoPendiente.setUsado(true);
            codigoVerificacionRepository.save(codigoPendiente);
        });
    }

    private void crearCodigoVerificacion(Usuario usuario, String codigo) {
        CodigoVerificacion verificacion = new CodigoVerificacion();
        verificacion.setCodigo(codigo);
        verificacion.setCreadoEn(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        verificacion.setExpiraEn(java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        verificacion.setUsado(false);
        verificacion.setUsuario(usuario);
        codigoVerificacionRepository.save(verificacion);

        programarEnvioCodigo(usuario, codigo);
    }

    private void programarEnvioCodigo(Usuario usuario, String codigo) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarCodigoVerificacion(usuario, codigo);
                }
            });
            return;
        }

        enviarCodigoVerificacion(usuario, codigo);
    }

    private void enviarCodigoVerificacion(Usuario usuario, String codigo) {
        String linkVerificacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/registro/paso2")
                .toUriString();

        try {
            correoService.enviarCorreo(usuario.getEmail(),
                    "Codigo de verificacion TalentBridge",
                    "<p>Hola " + usuario.getNombre() + ",</p>" +
                            "<p>Tu codigo de verificacion es: <strong>" + codigo + "</strong></p>" +
                            "<p>Este codigo expirara en 10 minutos.</p>" +
                            "<p>Continua en el paso de verificacion desde este enlace:</p>" +
                            "<p><a href=\"" + linkVerificacion + "\">Paso 2: Verificacion</a></p>" +
                            "<p>Si el enlace no se abre, copia y pega esta URL en tu navegador:<br>" +
                            linkVerificacion + "</p>");
        } catch (RuntimeException e) {
            log.error("No se pudo enviar el codigo de verificacion a {}", usuario.getEmail(), e);
        }
    }

    private String generarCodigo() {
        return String.valueOf((int) (Math.random() * 900_000) + 100_000);
    }

    @Override
    @Transactional
    public void verificarCodigo(CodigoVerificacionDTO dto) {
        log.info("Verificando codigo:");
        log.info(dto.toString());
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Correo no encontrado."));

        CodigoVerificacion verificacion = codigoVerificacionRepository
                .findByUsuarioAndCodigoAndUsadoFalse(usuario, dto.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Codigo invalido o ya utilizado."));

        if (verificacion.getExpiraEn().toLocalDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Codigo expirado.");
        }

        verificacion.setUsado(true);
        codigoVerificacionRepository.save(verificacion);

        usuario.setActivo(true);
        usuario.setVerificado(true);
        usuario.setEstadoRegistro(EstadoRegistro.CORREO_VALIDADO);
        usuarioRepository.save(usuario);
    }
}
