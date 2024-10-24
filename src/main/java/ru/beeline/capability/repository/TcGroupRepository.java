package ru.beeline.capability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.beeline.capability.domain.TcGroup;

import java.util.List;

@Repository
public interface TcGroupRepository extends JpaRepository<TcGroup, Long> {

    List<TcGroup> findAllByGroupIdIn(List<Integer> Ids);
}
