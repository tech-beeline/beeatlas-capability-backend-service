package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryBusinessCapability;
import ru.beeline.capability.domain.HistoryTechCapability;

import java.util.Optional;

@Repository
public interface HistoryBusinessCapabilityRepository extends JpaRepository<HistoryBusinessCapability, Long> {
    Optional<HistoryTechCapability> findTopByIdRefOrderByVersionDesc(Long idRef);

}
