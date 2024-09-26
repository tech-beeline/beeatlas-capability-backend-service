package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.Map;

@Repository
public interface MapRepository extends JpaRepository<Map, Long> {
}
