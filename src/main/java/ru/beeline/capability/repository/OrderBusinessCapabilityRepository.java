package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.beeline.capability.domain.OrderBusinessCapability;

import java.util.List;

public interface OrderBusinessCapabilityRepository extends JpaRepository<OrderBusinessCapability, Integer> {
    @Query(value = "SELECT last_value FROM capability.order_business_capability_seq", nativeQuery = true)
    Integer getLastSequenceValue();

    List<OrderBusinessCapability> findByOrderOwnerIdAndBusinessKeyIsNull(Integer orderOwnerId);
}
