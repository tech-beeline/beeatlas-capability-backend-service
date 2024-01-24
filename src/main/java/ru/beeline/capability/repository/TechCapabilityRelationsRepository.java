package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;


@Repository
public interface TechCapabilityRelationsRepository extends JpaRepository<TechCapabilityRelations, String> {
    Boolean existsByBusinessCapability(BusinessCapability businessCapability);
}
