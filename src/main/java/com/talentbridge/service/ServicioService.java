package com.talentbridge.service;

import com.talentbridge.dto.ServicioDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServicioService {
    void crearServicio(ServicioDTO dto, List<MultipartFile> imagenes, String email) throws IOException;
    List<ServicioDTO> buscarServicios(String termino);
}
