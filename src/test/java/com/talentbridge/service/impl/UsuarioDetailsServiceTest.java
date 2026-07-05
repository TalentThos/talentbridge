package com.talentbridge.service.impl;

import com.talentbridge.model.Usuario;
import com.talentbridge.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioDetailsServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioDetailsService service = new UsuarioDetailsService(usuarioRepository);

    @Test
    void loadUserByUsernameReturnsEnabledUserWhenUsuarioIsActivo() {
        Usuario usuario = usuario(true);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsernameReturnsDisabledUserWhenUsuarioIsNotActivo() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameReturnsDisabledUserWhenActivoIsNull() {
        Usuario usuario = usuario(null);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("test@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameThrowsWhenUsuarioDoesNotExist() {
        when(usuarioRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }

    private Usuario usuario(Boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("encoded-password");
        usuario.setActivo(activo);
        return usuario;
    }
}
