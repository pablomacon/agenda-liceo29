package com.liceo.agenda.repository;

import com.liceo.agenda.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Busca eventos creados por un usuario específico
    List<Evento> findByCreadorId(Long usuarioId);

    /**
     * Busca cualquier evento que se solape en el tiempo, independientemente del
     * grupo.
     * Corregido para usar los atributos que mapean a las columnas 'inicio' y 'fin'.
     */
    @Query("SELECT e FROM Evento e WHERE NOT (e.fechaFin <= :inicio OR e.fechaInicio >= :fin)")
    List<Evento> findEventosSolapados(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca conflictos específicos por grupo.
     * La lógica (inicio1 < fin2 AND fin1 > inicio2) es la forma estándar de
     * detectar solapamientos.
     */
    @Query("SELECT e FROM Evento e WHERE e.destinatarios LIKE :grupo AND (:inicio < e.fechaFin AND :fin > e.fechaInicio)")
    List<Evento> buscarConflictos(
            @Param("grupo") String grupo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}