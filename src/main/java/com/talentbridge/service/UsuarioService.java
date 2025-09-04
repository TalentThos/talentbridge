package com.talentbridge.service;

import com.talentbridge.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> listarTodos();
    UsuarioDTO obtenerPorEmail(String email);
    void actualizarPerfil(String emailActual, UsuarioDTO dto);
}
