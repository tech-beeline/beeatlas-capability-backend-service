package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.beeline.capability.domain.OrderBusinessCapability;

import java.util.List;

public interface OrderBusinessCapabilityRepository extends JpaRepository<OrderBusinessCapability, Integer> {
    @Query("SELECT COALESCE(MAX(o.id), 0) FROM OrderBusinessCapability o")
    Integer findMaxId();

    List<OrderBusinessCapability> findByOrderOwnerIdAndBusinessKeyIsNull(Integer orderOwnerId);
}
