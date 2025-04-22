package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.beeline.capability.domain.OrderBusinessCapability;

public interface OrderBusinessCapabilityRepository extends JpaRepository<OrderBusinessCapability, Integer> {
}
