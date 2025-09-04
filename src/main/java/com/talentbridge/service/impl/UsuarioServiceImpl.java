package com.talentbridge.service.impl;

import com.talentbridge.dto.UsuarioDTO;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(usuario -> {
            return UsuarioDTO.builder()
                    .rol(usuario.getRol())
                    .email(usuario.getEmail())
                    .numeroMovil(usuario.getNumeroMovil())
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .numeroMovil(usuario.getNumeroMovil())
                .rol(usuario.getRol())
                .tipoDocumento(usuario.getTipoDocumento())
                .numeroDocumento(usuario.getNumeroDocumento())
                .pais(usuario.getPais())
                .ciudad(usuario.getCiudad())
                .calle(usuario.getCalle())
                .numeroDireccion(usuario.getNumeroDireccion())
                .build();
    }

    @Override
    @Transactional
    public void actualizarPerfil(String emailActual, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(emailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Correo ya registrado.");
        }
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setNumeroMovil(dto.getNumeroMovil());
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setPais(dto.getPais());
        usuario.setCiudad(dto.getCiudad());
        usuario.setCalle(dto.getCalle());
        usuario.setNumeroDireccion(dto.getNumeroDireccion());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        usuarioRepository.save(usuario);
    }
}
