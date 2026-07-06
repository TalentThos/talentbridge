package com.talentbridge.repository;

import com.talentbridge.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndActivoTrue(String email);

    @Query("""
        SELECT u FROM Usuario u
        WHERE COALESCE(u.activo, false) = true
        AND COALESCE(u.verificado, false) = true
        AND u.recordatorioPublicacionEnviadoEn IS NULL
        AND NOT EXISTS (
            SELECT 1 FROM Servicio s
            WHERE s.usuario = u
        )
        """)
    List<Usuario> buscarUsuariosParaRecordatorioPublicacion();

}

