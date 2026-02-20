/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.Promt;

import java.util.Optional;

@Repository
public interface PromtRepository extends JpaRepository<Promt, Long> {
    Promt findByAlias(String alias);

}
