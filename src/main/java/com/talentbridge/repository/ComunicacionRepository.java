package com.talentbridge.repository;

import com.talentbridge.model.Comunicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComunicacionRepository extends JpaRepository<Comunicacion, Long> {
    Optional<Comunicacion> findByRemitenteIdAndDestinatarioId(Long remitenteId, Long destinatarioId);
    Optional<Comunicacion> findByRemitenteIdAndDestinatarioIdOrRemitenteIdAndDestinatarioId(Long remitenteId, Long destinatarioId, Long remitenteId2, Long destinatarioId2);
    List<Comunicacion> findByRemitenteIdOrDestinatarioId(Long remitenteId, Long destinatarioId);
}
