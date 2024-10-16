package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.Group;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {
    List<Group> findAllByMapId (Integer mapId);
}
