package com.talentbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO para la información de un servicio. Se utiliza tanto para la creación
 * como para la edición de servicios. Incluye las imágenes existentes y las que
 * se desean eliminar.
 */

@Builder
@Getter
@Setter
@ToString
public class ServicioDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private Long subcategoriaId;
    private String subcategoriaNombre;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioMovil;
    private Boolean tieneImagenes;
    private List<ImagenDTO> imagenesExistentes;
    private List<Long> imagenesEliminar;
    private List<MultipartFile> imagenes;
}
