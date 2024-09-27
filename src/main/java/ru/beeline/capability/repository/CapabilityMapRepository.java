package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CapabilityMap;

@Repository
public interface CapabilityMapRepository extends JpaRepository<CapabilityMap, Long> {
}
