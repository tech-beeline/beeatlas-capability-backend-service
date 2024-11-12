package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.HistoryTechCapabilityRelations;

@Repository
public interface HistoryTechCapabilityRelationsRepository extends JpaRepository<HistoryTechCapabilityRelations, Long> {
}
