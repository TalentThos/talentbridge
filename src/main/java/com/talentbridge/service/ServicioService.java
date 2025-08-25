package com.talentbridge.service;

import com.talentbridge.dto.ServicioDTO;
import java.util.List;

public interface ServicioService {
    void crearServicio(ServicioDTO dto, String email);
    List<ServicioDTO> buscarServicios(String termino, Long categoriaId, Long subcategoriaId);
    List<String> obtenerImagenesBase64(Long servicioId);
    List<ServicioDTO> listarPorUsuario(String email);
    ServicioDTO obtenerPorId(Long id, String email);
    void actualizarServicio(Long id, ServicioDTO dto, String email);
}
