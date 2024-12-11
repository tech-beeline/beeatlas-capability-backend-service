package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryTechCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryTechCapabilityRepository extends JpaRepository<HistoryTechCapability, Long> {
    Optional<HistoryTechCapability> findTopByIdRefOrderByVersionDesc(Long idRef);

    List<HistoryTechCapability> findByIdRef(Long idRef);
}
