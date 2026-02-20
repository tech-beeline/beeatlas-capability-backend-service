/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryBusinessCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryBusinessCapabilityRepository extends JpaRepository<HistoryBusinessCapability, Long> {
    Optional<HistoryBusinessCapability> findTopByIdRefOrderByVersionDesc(Long idRef);

    List<HistoryBusinessCapability> findByIdRef(Long idRef);

    Optional<HistoryBusinessCapability> findByIdRefAndVersion(Long idRef, Long version);

    @Query("SELECT h FROM HistoryBusinessCapability h WHERE h.idRef = :idRef AND h.version = " +
            "(SELECT MAX(h2.version) FROM HistoryBusinessCapability h2 WHERE h2.idRef = :idRef)")
    Optional<HistoryBusinessCapability> findByIdRefOtherVersion(@Param("idRef") Long idRef);

}
