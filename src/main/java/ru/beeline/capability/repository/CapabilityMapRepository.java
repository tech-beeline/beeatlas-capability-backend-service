package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CapabilityMap;

import java.util.Optional;

@Repository
public interface CapabilityMapRepository extends JpaRepository<CapabilityMap, Long> {
    Optional<CapabilityMap> findById(Integer id);
}
