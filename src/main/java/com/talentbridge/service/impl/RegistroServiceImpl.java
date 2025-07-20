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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Correo ya registrado.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setPais(dto.getPais());
        usuario.setCiudad(dto.getCiudad());
        usuario.setCalle(dto.getCalle());
        usuario.setNumeroDireccion(dto.getNumeroDireccion());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstadoRegistro(EstadoRegistro.PENDIENTE_VALIDACION);
        usuarioRepository.save(usuario);
        String codigo = generarCodigo();
        CodigoVerificacion verificacion = new CodigoVerificacion();
        verificacion.setCodigo(codigo);
        verificacion.setCreadoEn(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        verificacion.setExpiraEn(java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        verificacion.setUsuario(usuario);
        codigoVerificacionRepository.save(verificacion);
        correoService.enviarCorreo(dto.getEmail(),
                "Código de verificación TalentBridge",
                "<p>Hola " + dto.getNombre() + ",</p>" +
                        "<p>Tu código de verificación es: <strong>" + codigo + "</strong></p>" +
                        "<p>Este código expirará en 10 minutos.</p>");

    }

    private String generarCodigo() {
        return String.valueOf((int)(Math.random() * 900_000) + 100_000);
    }

    @Override
    @Transactional
    public void verificarCodigo(CodigoVerificacionDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Correo no encontrado."));

        CodigoVerificacion verificacion = codigoVerificacionRepository
                .findByUsuarioAndCodigoAndUsadoFalse(usuario, dto.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o ya utilizado."));

        if (verificacion.getExpiraEn().toLocalDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado.");
        }

        verificacion.setUsado(true);
        codigoVerificacionRepository.save(verificacion);

        usuario.setEstadoRegistro(EstadoRegistro.CORREO_VALIDADO);
        usuarioRepository.save(usuario);

    }

}
