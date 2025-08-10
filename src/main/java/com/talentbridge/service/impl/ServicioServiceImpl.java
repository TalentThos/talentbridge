package com.talentbridge.service.impl;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.model.Imagen;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public void crearServicio(ServicioDTO dto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Servicio servicio = new Servicio();
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setCategoria(categoria);
        servicio.setPrecio(dto.getPrecio());
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
    public List<ServicioDTO> buscarServicios(String termino) {
        List<Servicio> servicios;
        if (termino == null || termino.isBlank()) {
            servicios = servicioRepository.findAll();
        } else {
            servicios = servicioRepository
                    .findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(termino, termino);
        }
        return servicios.stream().map(s -> ServicioDTO.builder()
                .id(s.getId())
                .titulo(s.getTitulo())
                .descripcion(s.getDescripcion())
                .categoriaId(s.getCategoria() != null ? s.getCategoria().getId() : null)
                .categoriaNombre(s.getCategoria() != null ? s.getCategoria().getNombre() : null)
                .precio(s.getPrecio())
                .usuarioId(s.getUsuario() != null ? s.getUsuario().getId() : null)
                .usuarioNombre(s.getUsuario() != null ? s.getUsuario().getNombre() : null)
                .build())
                .collect(Collectors.toList());
    }
}
