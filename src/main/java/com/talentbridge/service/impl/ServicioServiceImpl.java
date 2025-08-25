package com.talentbridge.service.impl;

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
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    private ServicioDTO mapToDTO(Servicio s) {
        return ServicioDTO.builder()
                .id(s.getId())
                .titulo(s.getTitulo())
                .descripcion(s.getDescripcion())
                .categoriaId(s.getCategoria() != null ? s.getCategoria().getId() : null)
                .categoriaNombre(s.getCategoria() != null ? s.getCategoria().getNombre() : null)
                .subcategoriaId(s.getSubcategoria() != null ? s.getSubcategoria().getId() : null)
                .subcategoriaNombre(s.getSubcategoria() != null ? s.getSubcategoria().getNombre() : null)
                .usuarioId(s.getUsuario() != null ? s.getUsuario().getId() : null)
                .usuarioNombre(s.getUsuario() != null ? s.getUsuario().getNombre() : null)
                .usuarioMovil(s.getUsuario() != null ? s.getUsuario().getNumeroMovil() : null)
                .tieneImagenes(s.getImagenes() != null && !s.getImagenes().isEmpty())
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
        servicio.setCategoria(categoria);
        servicio.setSubcategoria(subcategoria);
        servicio.setUsuario(usuario);

        if (dto.getImagenes() != null) {
            for (MultipartFile file : dto.getImagenes()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Imagen imagen = new Imagen();
                        imagen.setNombre(file.getOriginalFilename());
                        imagen.setDatos(file.getBytes());
                        imagen.setServicio(servicio);
                        servicio.getImagenes().add(imagen);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al guardar la imagen", e);
                    }
                }
            }
        }

        servicioRepository.save(servicio);
    }

    @Override
    public List<ServicioDTO> buscarServicios(String termino, Long categoriaId, Long subcategoriaId) {
        String pattern = (termino == null || termino.isBlank())
                ? null
                : "%" + termino.toLowerCase(java.util.Locale.ROOT) + "%";
        List<Servicio> servicios = servicioRepository.buscarPorFiltros(pattern, categoriaId, subcategoriaId);
        return servicios.stream().map(this::mapToDTO).collect(Collectors.toList());
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

        if (dto.getImagenes() != null) {
            servicio.getImagenes().clear();
            for (MultipartFile file : dto.getImagenes()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Imagen imagen = new Imagen();
                        imagen.setNombre(file.getOriginalFilename());
                        imagen.setDatos(file.getBytes());
                        imagen.setServicio(servicio);
                        servicio.getImagenes().add(imagen);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al guardar la imagen", e);
                    }
                }
            }
        }

        servicioRepository.save(servicio);
    }
}
