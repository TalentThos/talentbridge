package com.talentbridge.service.impl;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.model.Categoria;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.ServicioImagen;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.CategoriaRepository;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public void crearServicio(ServicioDTO dto, List<MultipartFile> imagenes, String email) throws IOException {
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

        if (imagenes != null) {
            for (MultipartFile file : imagenes) {
                if (file.isEmpty()) continue;
                String nombre = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path ruta = Paths.get("uploads").resolve(nombre);
                Files.createDirectories(ruta.getParent());
                Files.copy(file.getInputStream(), ruta);

                ServicioImagen img = new ServicioImagen();
                img.setUrl("/uploads/" + nombre);
                img.setServicio(servicio);
                servicio.getImagenes().add(img);
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
                .categoria(s.getCategoria().getNombre())
                .precio(s.getPrecio())
                .imagenes(s.getImagenes().stream().map(ServicioImagen::getUrl).collect(Collectors.toList()))
                .build())
                .collect(Collectors.toList());
    }
}
