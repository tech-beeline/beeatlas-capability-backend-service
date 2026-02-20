/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.UserMap;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMapRepository extends JpaRepository<UserMap, Long> {
    Optional<UserMap> findByUserIdAndMapIdAndAuthorTrue(Integer userId, Integer mapId);

    Optional<UserMap> findByUserIdAndMapId(Integer userId, Integer mapId);

    List<UserMap> findAllByUserIdAndAuthorTrue(Integer userId);
}
