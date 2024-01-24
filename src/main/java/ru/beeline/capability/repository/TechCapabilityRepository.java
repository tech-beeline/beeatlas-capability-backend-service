package ru.beeline.capability.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.TechCapability;

@Repository
public interface TechCapabilityRepository extends JpaRepository<TechCapability, Long> {

    @Query("SELECT c FROM TechCapability c WHERE c.deletedDate is NULL ORDER BY c.name")
    Page<TechCapability> findCapabilities(Pageable pageable);
}
