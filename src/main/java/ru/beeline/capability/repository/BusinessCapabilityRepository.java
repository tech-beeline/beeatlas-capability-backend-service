package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.List;

@Repository
public interface BusinessCapabilityRepository extends JpaRepository<BusinessCapability, Long> {

    List<BusinessCapability> findAllByParentIdAndDeletedDateIsNull(Long parentId);
}
