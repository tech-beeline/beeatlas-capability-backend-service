/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BcGroup;

import java.util.List;

@Repository
public interface BcGroupRepository extends JpaRepository<BcGroup, Long> {

    List<BcGroup> findAllByGroupIdIn (List<Integer> Ids);
}
