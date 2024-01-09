package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.TechCapability;

@Repository
public interface TechCapabilityRepository extends JpaRepository<TechCapability, String> {
}
