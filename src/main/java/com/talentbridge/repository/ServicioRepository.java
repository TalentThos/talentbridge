package com.talentbridge.repository;

import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
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
            "AND (:subcategoriaId IS NULL OR s.subcategoria.id = :subcategoriaId)")
    List<Servicio> buscarPorFiltros(@Param("termino") String termino,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("subcategoriaId") Long subcategoriaId);
}
