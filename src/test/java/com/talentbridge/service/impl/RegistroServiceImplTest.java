package com.talentbridge.service.impl;

import com.talentbridge.dto.RegistroPaso1DTO;
import com.talentbridge.model.CodigoVerificacion;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CodigoVerificacionRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.CorreoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistroServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final CodigoVerificacionRepository codigoVerificacionRepository = mock(CodigoVerificacionRepository.class);
    private final CorreoService correoService = mock(CorreoService.class);
    private final RegistroServiceImpl service = new RegistroServiceImpl(
            usuarioRepository,
            passwordEncoder,
            codigoVerificacionRepository,
            correoService
    );

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void registrarPaso1RejectsEmailOnlyWhenThereIsAnActiveUser() {
        RegistroPaso1DTO dto = registroDto();
        when(usuarioRepository.existsByEmailAndActivoTrue(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.registrarPaso1(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correo ya registrado.");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarPaso1RejectsWhenPoliciesAreNotAccepted() {
        RegistroPaso1DTO dto = registroDto();
        dto.setAceptaPoliticaPrivacidad(false);

        assertThatThrownBy(() -> service.registrarPaso1(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debes aceptar la politica de privacidad y las condiciones de uso para registrarte.");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarPaso1AllowsReusingEmailWhenExistingUserIsInactive() {
        RegistroPaso1DTO dto = registroDto();
        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setEmail(dto.getEmail());
        usuarioInactivo.setActivo(false);

        when(usuarioRepository.existsByEmailAndActivoTrue(dto.getEmail())).thenReturn(false);
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(usuarioInactivo));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-password");
        when(codigoVerificacionRepository.findByUsuarioAndUsadoFalse(usuarioInactivo)).thenReturn(List.of());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        service.registrarPaso1(dto);

        verify(usuarioRepository).save(usuarioInactivo);
        ArgumentCaptor<CodigoVerificacion> codigoCaptor = forClass(CodigoVerificacion.class);
        verify(codigoVerificacionRepository).save(codigoCaptor.capture());
        verify(correoService).enviarCorreo(eq(dto.getEmail()), any(), any());
        assertThat(usuarioInactivo.getActivo()).isFalse();
        assertThat(usuarioInactivo.getVerificado()).isFalse();
        assertThat(usuarioInactivo.getPassword()).isEqualTo("encoded-password");
        assertThat(usuarioInactivo.getAceptaPoliticaPrivacidad()).isTrue();
        assertThat(usuarioInactivo.getAceptaCondicionesUso()).isTrue();
        assertThat(usuarioInactivo.getFechaAceptacionPoliticas()).isNotNull();
        assertThat(usuarioInactivo.getVersionPoliticas()).isEqualTo("2026-07-05");
        assertThat(codigoCaptor.getValue().getUsado()).isFalse();
    }

    private RegistroPaso1DTO registroDto() {
        return RegistroPaso1DTO.builder()
                .nombre("Test User")
                .email("test@example.com")
                .numeroMovil("123456789")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .pais("Chile")
                .ciudad("Santiago")
                .calle("Calle Uno")
                .numeroDireccion("123")
                .password("password")
                .aceptaPoliticaPrivacidad(true)
                .aceptaCondicionesUso(true)
                .ipAceptacionPoliticas("127.0.0.1")
                .userAgentAceptacionPoliticas("JUnit")
                .build();
    }
}
