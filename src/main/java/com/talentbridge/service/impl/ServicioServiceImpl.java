package com.talentbridge.service.impl;

import com.talentbridge.dto.ImagenDTO;
import com.talentbridge.dto.ResultadoModeracionImagen;
import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.model.Imagen;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Subcategoria;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.SubcategoriaRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.ModeracionContenidoService;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ModeracionContenidoService moderacionContenidoService;

    private ServicioDTO mapToDTO(Servicio s) {
        return ServicioDTO.builder()
                .id(s.getId())
                .titulo(s.getTitulo())
                .descripcion(s.getDescripcion())
                .linkedin(s.getLinkedin())
                .instagram(s.getInstagram())
                .numeroMovil(s.getNumeroMovil())
                .tipoValorizacion(s.getTipoValorizacion())
                .valorReferencial(s.getValorReferencial())
                .valorizacionTexto(formatearValorizacion(s.getTipoValorizacion(), s.getValorReferencial()))
                .categoriaId(s.getCategoria() != null ? s.getCategoria().getId() : null)
                .categoriaNombre(s.getCategoria() != null ? s.getCategoria().getNombre() : null)
                .subcategoriaId(s.getSubcategoria() != null ? s.getSubcategoria().getId() : null)
                .subcategoriaNombre(s.getSubcategoria() != null ? s.getSubcategoria().getNombre() : null)
                .subcategoriaIcono(iconoSubcategoria(s.getSubcategoria()))
                .usuarioId(s.getUsuario() != null ? s.getUsuario().getId() : null)
                .usuarioNombre(s.getUsuario() != null ? s.getUsuario().getNombre() : null)
                .usuarioMovil(s.getNumeroMovil())
                .tieneImagenes(s.getImagenes() != null && !s.getImagenes().isEmpty())
                .imagenesExistentes(s.getImagenes() != null ?
                        s.getImagenes().stream()
                                .map(i -> new ImagenDTO(i.getId(), i.getNombre(),
                                        Base64.getEncoder().encodeToString(i.getDatos())))
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    @Override
    public void crearServicio(ServicioDTO dto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Subcategoria subcategoria = null;
        if (dto.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoría no encontrada"));
            if (subcategoria.getCategoria() == null || !subcategoria.getCategoria().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException("La subcategoría no pertenece a la categoría seleccionada");
            }
        }

        Servicio servicio = new Servicio();
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setLinkedin(dto.getLinkedin());
        servicio.setInstagram(dto.getInstagram());
        servicio.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
        servicio.setTipoValorizacion(normalizarTipoValorizacion(dto.getTipoValorizacion()));
        servicio.setValorReferencial(normalizarValorReferencial(dto.getValorReferencial(), servicio.getTipoValorizacion()));
        servicio.setCategoria(categoria);
        servicio.setSubcategoria(subcategoria);
        servicio.setUsuario(usuario);

        if (dto.getImagenes() != null) {
            for (MultipartFile file : dto.getImagenes()) {
                if (file != null && !file.isEmpty()) {
                    validarModeracionImagen(file);
                    servicio.getImagenes().add(crearImagen(file, servicio));
                }
            }
        }

        servicioRepository.save(servicio);
    }

    @Override
    public Page<ServicioDTO> buscarServicios(String termino, Long categoriaId, Long subcategoriaId, String pais, int page) {
        String pattern = (termino == null || termino.isBlank())
                ? null
                : "%" + termino.toLowerCase(java.util.Locale.ROOT) + "%";
        Pageable pageable = PageRequest.of(page, 6);
        Page<Servicio> servicios = servicioRepository.buscarPorFiltros(pattern, categoriaId, subcategoriaId, pais, pageable);
        return servicios.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ServicioDTO> listarPorUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        List<Servicio> servicios = servicioRepository.findByUsuario(usuario);
        return servicios.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> obtenerImagenesBase64(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        return servicio.getImagenes().stream()
                .map(img -> Base64.getEncoder().encodeToString(img.getDatos()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ServicioDTO obtenerPorId(Long id, String email) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        if (servicio.getUsuario() == null || !servicio.getUsuario().getEmail().equals(email)) {
            throw new IllegalArgumentException("No autorizado");
        }
        return mapToDTO(servicio);
    }

    @Transactional
    @Override
    public void actualizarServicio(Long id, ServicioDTO dto, String email) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        if (servicio.getUsuario() == null || !servicio.getUsuario().getEmail().equals(email)) {
            throw new IllegalArgumentException("No autorizado");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Subcategoria subcategoria = null;
        if (dto.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoría no encontrada"));
            if (subcategoria.getCategoria() == null || !subcategoria.getCategoria().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException("La subcategoría no pertenece a la categoría seleccionada");
            }
        }

        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setCategoria(categoria);
        servicio.setSubcategoria(subcategoria);
        servicio.setLinkedin(dto.getLinkedin());
        servicio.setInstagram(dto.getInstagram());
        servicio.setNumeroMovil(normalizarTexto(dto.getNumeroMovil()));
        servicio.setTipoValorizacion(normalizarTipoValorizacion(dto.getTipoValorizacion()));
        servicio.setValorReferencial(normalizarValorReferencial(dto.getValorReferencial(), servicio.getTipoValorizacion()));

        if (dto.getImagenesEliminar() != null && !dto.getImagenesEliminar().isEmpty()) {
            servicio.getImagenes().removeIf(img -> dto.getImagenesEliminar().contains(img.getId()));
        }

        if (dto.getImagenes() != null) {
            for (MultipartFile file : dto.getImagenes()) {
                if (file != null && !file.isEmpty()) {
                    validarModeracionImagen(file);
                    servicio.getImagenes().add(crearImagen(file, servicio));
                }
            }
        }

        servicioRepository.save(servicio);
    }

    private void validarModeracionImagen(MultipartFile file) {
        ResultadoModeracionImagen resultado = moderacionContenidoService.moderarImagen(file);
        if (!resultado.isAprobada()) {
            throw new IllegalArgumentException(resultado.getMotivo());
        }
    }

    private Imagen crearImagen(MultipartFile file, Servicio servicio) {
        try {
            Imagen imagen = new Imagen();
            imagen.setNombre(file.getOriginalFilename());
            imagen.setDatos(file.getBytes());
            imagen.setServicio(servicio);
            return imagen;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarTipoValorizacion(String tipoValorizacion) {
        if (tipoValorizacion == null || tipoValorizacion.isBlank()) {
            return "A_CONVENIR";
        }
        String valor = tipoValorizacion.trim().toUpperCase(Locale.ROOT);
        return switch (valor) {
            case "POR_HORA", "POR_TRABAJO", "A_CONVENIR" -> valor;
            default -> "A_CONVENIR";
        };
    }

    private BigDecimal normalizarValorReferencial(BigDecimal valorReferencial, String tipoValorizacion) {
        if ("A_CONVENIR".equals(tipoValorizacion) || valorReferencial == null || valorReferencial.signum() <= 0) {
            return null;
        }
        return valorReferencial;
    }

    private String formatearValorizacion(String tipoValorizacion, BigDecimal valorReferencial) {
        String tipo = normalizarTipoValorizacion(tipoValorizacion);
        if ("A_CONVENIR".equals(tipo)) {
            return "Valor a convenir";
        }

        String monto = valorReferencial != null && valorReferencial.signum() > 0
                ? NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).format(valorReferencial)
                : "Valor referencial";

        return switch (tipo) {
            case "POR_HORA" -> monto + " por hora";
            case "POR_TRABAJO" -> monto + " por trabajo";
            default -> "Valor a convenir";
        };
    }

    private String iconoSubcategoria(Subcategoria subcategoria) {
        if (subcategoria == null || subcategoria.getIcono() == null || subcategoria.getIcono().isBlank()) {
            return "bi-briefcase";
        }
        return subcategoria.getIcono().trim();
    }
}
