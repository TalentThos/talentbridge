package com.talentbridge.service.impl;

import com.talentbridge.dto.ServicioDTO;
import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
import com.talentbridge.repository.ServicioRepository;
import com.talentbridge.repository.UsuarioRepository;
import com.talentbridge.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void crearServicio(ServicioDTO dto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Servicio servicio = new Servicio();
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setCategoria(dto.getCategoria());
        servicio.setPrecio(dto.getPrecio());
        servicio.setUsuario(usuario);
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
                .categoria(s.getCategoria())
                .precio(s.getPrecio())
                .build())
                .collect(Collectors.toList());
    }
}
