package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;
import java.util.List;


@Repository
public interface TechCapabilityRelationsRepository extends JpaRepository<TechCapabilityRelations, String> {
    Boolean existsByBusinessCapability(BusinessCapability businessCapability);

    List<TechCapabilityRelations> findByTechCapability(TechCapability techCapability);

    void deleteByBusinessCapabilityAndTechCapability(BusinessCapability businessCapability, TechCapability techCapability);

    @Query(value = "SELECT * FROM capability.tech_capability_relations " +
            "WHERE capability.tech_capability_relations.id_child IN (:ids)", nativeQuery = true)
    List<TechCapabilityRelations> findByTechCapabilityIn(@Param("ids") List<Long> ids);

    void deleteAllByTechCapability(TechCapability techCapability);
    
}
