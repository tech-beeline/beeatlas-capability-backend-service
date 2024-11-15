package ru.beeline.capability.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.CriteriasTc;

@Repository
public interface CriteriaTcRepository extends JpaRepository<CriteriasTc, Long> {
    CriteriasTc findByTcId(Long tcId);
}