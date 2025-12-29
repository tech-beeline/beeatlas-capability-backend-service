package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CriteriasBc;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaBcRepository extends JpaRepository<CriteriasBc, Long> {
    void deleteAllByCriterionId(Long criterionId);

    List<CriteriasBc> findAllByBcId(Long bcId);

    Optional <CriteriasBc> findByBcIdAndCriterionId(Long bcId, Long criterionId);
}
