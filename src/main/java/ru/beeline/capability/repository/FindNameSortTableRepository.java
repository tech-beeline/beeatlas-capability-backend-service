/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.FindNameSortTable;

import java.util.List;

@Repository
public interface FindNameSortTableRepository extends JpaRepository<FindNameSortTable, Long> {
    FindNameSortTable findByRefIdAndType(Long refId, EntityType entityType);

    @Query(nativeQuery = true, value = "SELECT * FROM capability.fuzzy_search_capability_with(:text, :type)")
    List<Object> callFuzzySearchCapability(@Param("text") String text, @Param("type") String type);

    void deleteByRefIdAndType(Long refId, EntityType entityType);
}