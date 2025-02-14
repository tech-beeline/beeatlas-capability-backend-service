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

    List<BusinessCapability> findAllByParentIdIsNullAndDeletedDateIsNullAndIsDomainIsTrue();

    @Query("SELECT c FROM BusinessCapability c WHERE c.deletedDate is NULL ORDER BY c.name")
    Page<BusinessCapability> findCapabilities(Pageable pageable);

    @Query("SELECT c FROM BusinessCapability c WHERE c.deletedDate is NULL and c.parentId is null and c.isDomain is true ORDER BY c.name")
    Page<BusinessCapability> findCapabilitiesWithoutParent(Pageable pageable);

    List<BusinessCapability> findAllByCodeIn(List<String> codes);

    Optional<BusinessCapability> findByCode(String code);

    List<BusinessCapability> findAllByParentId(Long id);

    BusinessCapability findFirstByOrderByIdDesc();

    List<BusinessCapability> findAllByIdInAndDeletedDateIsNull(List<Long> ids);

    List<BusinessCapability> findByDeletedDateIsNull();
}
