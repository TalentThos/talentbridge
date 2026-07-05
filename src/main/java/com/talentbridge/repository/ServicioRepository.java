package com.talentbridge.repository;

import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String titulo, String descripcion);
    List<Servicio> findByUsuario(Usuario usuario);

    @Query("SELECT s FROM Servicio s WHERE " +
            "(:termino IS NULL OR LOWER(s.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "AND (:categoriaId IS NULL OR s.categoria.id = :categoriaId) " +
            "AND (:subcategoriaId IS NULL OR s.subcategoria.id = :subcategoriaId) " +
            "AND s.usuario IS NOT NULL " +
            "AND COALESCE(s.usuario.activo, false) = true")
    List<Servicio> buscarPorFiltros2(@Param("termino") String termino,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("subcategoriaId") Long subcategoriaId);

    @Query("""
        SELECT s FROM Servicio s
        WHERE (
            :terminoLower IS NULL
            OR LOWER(s.titulo)      LIKE CONCAT('%', :terminoLower, '%')
            OR LOWER(s.descripcion) LIKE CONCAT('%', :terminoLower, '%')
        )
        AND (:categoriaId   IS NULL OR s.categoria.id   = :categoriaId)
        AND (:subcategoriaId IS NULL OR s.subcategoria.id = :subcategoriaId)
        AND s.usuario IS NOT NULL
        AND COALESCE(s.usuario.activo, false) = true
        """)
    List<Servicio> buscarPorFiltros3(@Param("terminoLower") String terminoLower,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("subcategoriaId") Long subcategoriaId);

    @Query("""
        SELECT s FROM Servicio s
        WHERE (
            :pattern IS NULL
            OR LOWER(s.titulo)      LIKE :pattern
            OR LOWER(s.descripcion) LIKE :pattern
        )
        AND (:categoriaId    IS NULL OR s.categoria.id    = :categoriaId)
        AND (:subcategoriaId IS NULL OR s.subcategoria.id = :subcategoriaId)
        AND (:pais          IS NULL OR s.usuario.pais     = :pais)
        AND s.usuario IS NOT NULL
        AND COALESCE(s.usuario.activo, false) = true
        """)
    Page<Servicio> buscarPorFiltros(
            @Param("pattern") String pattern,
            @Param("categoriaId") Long categoriaId,
            @Param("subcategoriaId") Long subcategoriaId,
            @Param("pais") String pais,
            Pageable pageable);

}

