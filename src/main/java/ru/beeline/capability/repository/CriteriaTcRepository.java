package ru.beeline.capability.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CriteriasTc;

import java.util.List;

@Repository
public interface CriteriaTcRepository extends JpaRepository<CriteriasTc, Long> {
    List<CriteriasTc> findAllByTcId(Long tcId);

    CriteriasTc findByCriterionIdAndTcId(Long criterionId, Long tcId);

    List<CriteriasTc> findByCriterionIdAndTcIdInAndValue(Long criterionId, List<Long> tcIds, Integer value);
}