package com.talentbridge.service;

import com.talentbridge.dto.ResultadoModeracionImagen;
import org.springframework.web.multipart.MultipartFile;

public interface ModeracionContenidoService {

    ResultadoModeracionImagen moderarImagen(MultipartFile imagen);
}
