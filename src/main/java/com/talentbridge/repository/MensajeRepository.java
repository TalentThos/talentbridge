package com.talentbridge.repository;

import com.talentbridge.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByComunicacionDestinatarioIdOrderByEnviadoEnDesc(Long destinatarioId);
    List<Mensaje> findByComunicacionIdOrderByEnviadoEnAsc(Long comunicacionId);
}
