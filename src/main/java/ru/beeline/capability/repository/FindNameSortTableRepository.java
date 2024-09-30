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

    FindNameSortTable findByIdAndType(Long id, EntityType entityType);

    @Query(nativeQuery = true, value = "SELECT * FROM capability.fuzzy_search_capability(:text)")
    List<Object> callFuzzySearchCapability(@Param("text") String text);
}
