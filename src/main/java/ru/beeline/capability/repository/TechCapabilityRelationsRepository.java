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

    Boolean existsByBusinessCapabilityAndTechCapability(BusinessCapability businessCapability, TechCapability techCapability);

    void deleteAllByTechCapability(TechCapability techCapability);

    List<TechCapabilityRelations> findByTechCapability(TechCapability techCapability);

    List<TechCapabilityRelations> findByTechCapability_IdIn(List<Long> techCapabilityIds);

    List<TechCapabilityRelations> findByBusinessCapability(BusinessCapability businessCapability);

    boolean existsByBusinessCapabilityAndTechCapability_DeletedDateIsNull(BusinessCapability businessCapability);

    @Query("SELECT DISTINCT tcr.businessCapability.id FROM TechCapabilityRelations tcr " +
            "WHERE tcr.techCapability.deletedDate IS NULL " +
            "AND tcr.businessCapability.deletedDate IS NULL " +
            "AND tcr.businessCapability.id IN :businessCapabilityIds")
    List<Long> findActiveTechCapabilities(@Param("businessCapabilityIds") List<Long> businessCapabilityIds);

}
