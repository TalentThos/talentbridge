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

}
