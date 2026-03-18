/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.TechCapability;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechCapabilityRepository extends JpaRepository<TechCapability, Long> {

    @Query("SELECT c FROM TechCapability c WHERE c.deletedDate is NULL ORDER BY c.name")
    Page<TechCapability> findCapabilities(Pageable pageable);

    Optional<TechCapability> findByCode(String code);

    List<TechCapability> findAllByIdInAndDeletedDateIsNull(List<Long> ids);

    TechCapability findAllByIdAndDeletedDateIsNull(Long id);

    List<TechCapability> findByDeletedDateIsNull();

    List<TechCapability> findAllByIdIn(List<Long> ids);

    @Query("SELECT tc FROM TechCapability tc WHERE LOWER(tc.code) IN :lowerCodes")
    List<TechCapability> findAllByCodeInIgnoreCase(@Param("lowerCodes") List<String> lowerCodes);

    List<TechCapability> findAllByResponsibilityProductIdAndDeletedDateIsNull(Integer id);
}
