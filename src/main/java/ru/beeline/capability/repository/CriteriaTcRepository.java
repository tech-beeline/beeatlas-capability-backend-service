/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CriteriasTc;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaTcRepository extends JpaRepository<CriteriasTc, Long> {

    void deleteAllByCriterionId(Long criterionId);

    List<CriteriasTc> findAllByTcId(Long tcId);

    Optional<CriteriasTc> findByTcIdAndCriterionId(Long tcId, Long criterionId);

    CriteriasTc findByCriterionIdAndTcId(Long criterionId, Long tcId);

    List<CriteriasTc> findByCriterionIdAndTcIdInAndValue(Long criterionId, List<Long> tcIds, Integer value);

    List<CriteriasTc> findAllByTcIdIn(List<Long> ids);
}