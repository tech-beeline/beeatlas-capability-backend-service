package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryBusinessCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryBusinessCapabilityRepository extends JpaRepository<HistoryBusinessCapability, Long> {
    Optional<HistoryBusinessCapability> findTopByIdRefOrderByVersionDesc(Long idRef);

    List<HistoryBusinessCapability> findByIdRef(Long idRef);
}
