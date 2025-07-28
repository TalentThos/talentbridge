package com.talentbridge.repository;

import com.talentbridge.model.Servicio;
import com.talentbridge.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String titulo, String descripcion);
    List<Servicio> findByUsuario(Usuario usuario);
}
