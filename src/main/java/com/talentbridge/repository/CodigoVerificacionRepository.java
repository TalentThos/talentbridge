package com.talentbridge.repository;

import com.talentbridge.model.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {
    Optional<CodigoVerificacion> findByCodigoAndUsuarioEmailAndUsadoFalse(String codigo, String email);
}
