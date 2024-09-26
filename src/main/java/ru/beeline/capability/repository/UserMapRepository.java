package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.UserMap;

@Repository
public interface UserMapRepository extends JpaRepository<UserMap,Long> {
}
