package com.talentbridge.service.impl;

import com.talentbridge.dto.RegistroPaso1DTO;
import com.talentbridge.dto.UsuarioDTO;
import com.talentbridge.model.CodigoVerificacion;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CodigoVerificacionRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.CorreoService;
import com.talentbridge.service.UsuarioService;
import com.talentbridge.tipos.EstadoRegistro;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacionRepository codigoRepo;
    private final CorreoService correoService;

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(usuario -> {
            return UsuarioDTO.builder()
                    .rol(usuario.getRol())
                    .email(usuario.getEmail())
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

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
        codigoRepo.save(verificacion);
        correoService.enviarCorreo(dto.getEmail(),
                "Código de verificación TalentBridge",
                "<p>Hola " + dto.getNombre() + ",</p>" +
                        "<p>Tu código de verificación es: <strong>" + codigo + "</strong></p>" +
                        "<p>Este código expirará en 10 minutos.</p>");

    }

    private String generarCodigo() {
        return String.valueOf((int)(Math.random() * 900_000) + 100_000);
    }

}
