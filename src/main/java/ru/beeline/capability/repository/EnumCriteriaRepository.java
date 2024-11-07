package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.EnumCriteria;

@Repository
public interface EnumCriteriaRepository extends JpaRepository<EnumCriteria, Long> {
    EnumCriteria findByName(String name);
}
