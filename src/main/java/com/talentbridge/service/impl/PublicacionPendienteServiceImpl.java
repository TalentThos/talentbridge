package com.talentbridge.service.impl;

import com.talentbridge.dto.PublicacionPendienteDTO;
import com.talentbridge.dto.VerificacionPublicacionDTO;
import com.talentbridge.dto.CrearPasswordPublicacionDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.model.CodigoVerificacion;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Subcategoria;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.CodigoVerificacionRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.SubcategoriaRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.CorreoService;
import com.talentbridge.service.PublicacionPendienteService;
import com.talentbridge.tipos.EstadoRegistro;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicacionPendienteServiceImpl implements PublicacionPendienteService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE_VERIFICACION";
    private static final String ESTADO_PUBLICADO = "PUBLICADO";
    private static final String VERSION_POLITICAS = "2026-07-05";

    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.public-url:https://www.talentbridge.cl}")
    private String appPublicUrl;

    @Override
    @Transactional
    public void crearPublicacionPendiente(PublicacionPendienteDTO dto) {
        validar(dto);

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada."));

        Subcategoria subcategoria = subcategoriaRepository.findById(dto.getSubcategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategoria no encontrada."));
        if (subcategoria.getCategoria() == null || !subcategoria.getCategoria().getId().equals(categoria.getId())) {
            throw new IllegalArgumentException("La subcategoria no pertenece a la categoria seleccionada.");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseGet(Usuario::new);
        completarUsuarioPendiente(usuario, dto);
        usuarioRepository.save(usuario);
        invalidarCodigosPendientes(usuario);

        Servicio servicio = new Servicio();
        servicio.setTitulo(dto.getTitulo().trim());
        servicio.setDescripcion(dto.getDescripcion().trim());
        servicio.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
        servicio.setTipoValorizacion(normalizarTipoValorizacion(dto.getTipoValorizacion()));
        servicio.setValorReferencial(normalizarValor(dto.getValorReferencial(), servicio.getTipoValorizacion()));
        servicio.setCategoria(categoria);
        servicio.setSubcategoria(subcategoria);
        servicio.setUsuario(usuario);
        servicio.setEstadoPublicacion(ESTADO_PENDIENTE);
        servicioRepository.save(servicio);

        crearCodigoYEnviar(usuario, generarCodigo());
    }

    @Override
    @Transactional
    public void verificarYPublicar(VerificacionPublicacionDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
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

        servicioRepository.findByUsuarioAndEstadoPublicacion(usuario, ESTADO_PENDIENTE)
                .forEach(servicio -> {
                    servicio.setEstadoPublicacion(ESTADO_PUBLICADO);
                    servicioRepository.save(servicio);
                });
    }

    @Override
    @Transactional
    public void crearPassword(CrearPasswordPublicacionDTO dto) {
        validarPassword(dto);

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Correo no encontrado."));

        if (!Boolean.TRUE.equals(usuario.getVerificado()) || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Debes verificar tu correo antes de crear la contrasena.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioRepository.save(usuario);
    }

    private void completarUsuarioPendiente(Usuario usuario, PublicacionPendienteDTO dto) {
        boolean usuarioNuevo = usuario.getId() == null;
        boolean usuarioYaActivo = Boolean.TRUE.equals(usuario.getActivo());

        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        usuario.setPais("Chile");
        usuario.setCiudad(dto.getCiudad().trim());
        usuario.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
        if (usuarioNuevo || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        usuario.setRol("OFERENTE");
        usuario.setEstadoRegistro(usuarioYaActivo ? EstadoRegistro.CORREO_VALIDADO : EstadoRegistro.PENDIENTE_VALIDACION);
        usuario.setActivo(usuarioYaActivo);
        usuario.setVerificado(usuarioYaActivo);
        usuario.setAceptaPoliticaPrivacidad(Boolean.TRUE);
        usuario.setAceptaCondicionesUso(Boolean.TRUE);
        usuario.setFechaAceptacionPoliticas(LocalDateTime.now());
        usuario.setVersionPoliticas(VERSION_POLITICAS);
    }

    private void crearCodigoYEnviar(Usuario usuario, String codigo) {
        CodigoVerificacion verificacion = new CodigoVerificacion();
        verificacion.setCodigo(codigo);
        verificacion.setCreadoEn(Timestamp.valueOf(LocalDateTime.now()));
        verificacion.setExpiraEn(Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
        verificacion.setUsado(false);
        verificacion.setUsuario(usuario);
        codigoVerificacionRepository.save(verificacion);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarCodigo(usuario, codigo);
                }
            });
            return;
        }
        enviarCodigo(usuario, codigo);
    }

    private void enviarCodigo(Usuario usuario, String codigo) {
        String url = normalizarUrlBase(appPublicUrl) + "/publicar/verificar?email=" + usuario.getEmail();
        correoService.enviarCorreo(usuario.getEmail(),
                "Verifica tu aviso en TalentBridge",
                "<p>Hola " + escapar(usuario.getNombre()) + ",</p>" +
                        "<p>Recibimos tu aviso en TalentBridge. Para publicarlo, verifica tu correo con este codigo:</p>" +
                        "<p style=\"font-size:24px\"><strong>" + codigo + "</strong></p>" +
                        "<p>Continua aqui: <a href=\"" + url + "\">verificar y publicar mi aviso</a></p>" +
                        "<p>El codigo expira en 15 minutos.</p>");
    }

    private void invalidarCodigosPendientes(Usuario usuario) {
        codigoVerificacionRepository.findByUsuarioAndUsadoFalse(usuario).forEach(codigo -> {
            codigo.setUsado(true);
            codigoVerificacionRepository.save(codigo);
        });
    }

    private void validar(PublicacionPendienteDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) throw new IllegalArgumentException("Ingresa tu nombre.");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new IllegalArgumentException("Ingresa tu correo.");
        if (dto.getCiudad() == null || dto.getCiudad().isBlank()) throw new IllegalArgumentException("Ingresa tu ciudad o comuna.");
        if (dto.getTitulo() == null || dto.getTitulo().isBlank()) throw new IllegalArgumentException("Ingresa el titulo del servicio.");
        if (dto.getDescripcion() == null || dto.getDescripcion().isBlank()) throw new IllegalArgumentException("Ingresa la descripcion del servicio.");
        if (dto.getCategoriaId() == null) throw new IllegalArgumentException("Selecciona una categoria.");
        if (dto.getSubcategoriaId() == null) throw new IllegalArgumentException("Selecciona una subcategoria.");
    }

    private void validarPassword(CrearPasswordPublicacionDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Correo no informado.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres.");
        }
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new IllegalArgumentException("Las contrasenas no coinciden.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarTipoValorizacion(String tipoValorizacion) {
        if (tipoValorizacion == null || tipoValorizacion.isBlank()) return "A_CONVENIR";
        String valor = tipoValorizacion.trim().toUpperCase(Locale.ROOT);
        return switch (valor) {
            case "POR_HORA", "POR_TRABAJO", "A_CONVENIR" -> valor;
            default -> "A_CONVENIR";
        };
    }

    private BigDecimal normalizarValor(BigDecimal valor, String tipo) {
        if ("A_CONVENIR".equals(tipo) || valor == null || valor.signum() <= 0) return null;
        return valor;
    }

    private String generarCodigo() {
        return String.valueOf((int) (Math.random() * 900_000) + 100_000);
    }

    private String normalizarUrlBase(String url) {
        if (url == null || url.isBlank()) return "https://www.talentbridge.cl";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String escapar(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
