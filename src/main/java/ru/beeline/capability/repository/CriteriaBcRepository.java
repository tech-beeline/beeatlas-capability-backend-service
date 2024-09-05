package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CriteriasBc;

@Repository
public interface CriteriaBcRepository extends JpaRepository<CriteriasBc, Long> {
    CriteriasBc findByBcIdAndCriterionId(Long bcId, Long criterionId);
}
