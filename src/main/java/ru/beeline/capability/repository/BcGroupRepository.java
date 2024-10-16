package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.BcGroup;

@Repository
public interface BcGroupRepository extends JpaRepository<BcGroup, Long> {
}
