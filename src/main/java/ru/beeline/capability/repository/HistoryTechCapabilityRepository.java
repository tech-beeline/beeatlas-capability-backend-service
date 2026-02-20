/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryBusinessCapability;
import ru.beeline.capability.domain.HistoryTechCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryTechCapabilityRepository extends JpaRepository<HistoryTechCapability, Long> {
    Optional<HistoryTechCapability> findTopByIdRefOrderByVersionDesc(Long idRef);

    List<HistoryTechCapability> findByIdRef(Long idRef);

    Optional<HistoryTechCapability> findByIdRefAndVersion(Long idRef, Integer version);

    @Query("SELECT h FROM HistoryTechCapability h WHERE h.idRef = :idRef AND h.version = " +
            "(SELECT MAX(h2.version) FROM HistoryTechCapability h2 WHERE h2.idRef = :idRef)")
    Optional<HistoryTechCapability> findByIdRefOtherVersion(@Param("idRef") Long idRef);
}
