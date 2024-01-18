package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.List;

@Repository
public interface BusinessCapabilityRepository extends JpaRepository<BusinessCapability, String> {
    @Query(value = "SELECT b FROM BusinessCapability b INNER JOIN TechCapabilityRelations r " +
            "ON b.id = r.idParent " +
            "WHERE r.idChild=?1 AND " +
            "b.deletedDate is NULL ORDER BY b.id")
    List<BusinessCapability> findParents(Long id);
}
