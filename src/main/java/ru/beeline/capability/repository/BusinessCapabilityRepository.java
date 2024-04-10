package ru.beeline.capability.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessCapabilityRepository extends JpaRepository<BusinessCapability, Long> {

    List<BusinessCapability> findAllByParentIdAndDeletedDateIsNull(Long parentId);

    Boolean existsByParentId(Long parentId);

    @Query("SELECT c FROM BusinessCapability c WHERE c.deletedDate is NULL ORDER BY c.name")
    Page<BusinessCapability> findCapabilities(Pageable pageable);

    @Query("SELECT c FROM BusinessCapability c WHERE c.deletedDate is NULL and c.parentId is null and c.isDomain is true ORDER BY c.name")
    Page<BusinessCapability> findCapabilitiesWithoutParent(Pageable pageable);

    Optional<BusinessCapability> findByCode(String code);

    List<BusinessCapability> findAllByIdIn(List<Long> ids);

}
