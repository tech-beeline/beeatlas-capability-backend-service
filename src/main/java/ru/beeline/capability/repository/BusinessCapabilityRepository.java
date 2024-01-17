package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.List;

@Repository
public interface BusinessCapabilityRepository extends JpaRepository<BusinessCapability, String> {
    @Query(value = "SELECT b FROM capability.business_capability b INNER JOIN capability.tech_capability_relations r " +
            "ON b.id = r.id_parent " +
            "WHERE r.id_child=?1 AND " +
            "b.deleted_date is NULL ORDER BY b.id", nativeQuery = true)
    List<BusinessCapability> findParents(Long childId);
}
