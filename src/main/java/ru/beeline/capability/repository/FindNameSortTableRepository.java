package ru.beeline.capability.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.FindNameSortTable;

@Repository
public interface FindNameSortTableRepository extends JpaRepository<FindNameSortTable, Long> {
    FindNameSortTable findByRefIdAndType(Long refId, EntityType entityType);
}
