/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.EnumCriteria;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaRepository extends JpaRepository<EnumCriteria, Long> {

    Optional<EnumCriteria> findByNameIgnoreCase(String name);

    @Query("select ec from EnumCriteria ec where ec.id in (select c.criterionId from CriteriasBc c)")
    List<EnumCriteria> findAllByBcCriteria();

    @Query("select ec from EnumCriteria ec where ec.id in (select c.criterionId from CriteriasTc c)")
    List<EnumCriteria> findAllByTcCriteria();
}
